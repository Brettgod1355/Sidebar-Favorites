/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import net.runelite.client.config.Keybind;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Sidebar Favorites",
    description = "Save and organize shortcuts to your favorite sidebar panels",
    tags = {"sidebar", "favorites", "favourites", "shortcuts", "panels"})
public class SidebarFavoritesPlugin extends Plugin
{
    private static final Logger LOG = LoggerFactory.getLogger(SidebarFavoritesPlugin.class);
    @Inject private ClientToolbar toolbar;
    @Inject private ConfigManager configManager;
    @Inject private SidebarFavoritesConfig config;
    @Inject private KeyManager keyManager;
    private HotkeyListener openHotkey;
    private final List<HotkeyListener> favoriteListeners = new ArrayList<>();
    private Map<Keybind, String> registeredKeys = Collections.emptyMap();
    private long bindingRevision = -1;

    private final AtomicBoolean refreshQueued = new AtomicBoolean();
    private final AtomicLong settingsRevision = new AtomicLong();
    private FavoritesStore store;
    private FavoritesPanel panel;
    private PanelCatalog catalog;
    private NavigationButton navigation;
    private long viewRevision;
    private int generation;
    private boolean active;

    @Provides
    SidebarFavoritesConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(SidebarFavoritesConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        onEdt(() ->
        {
            active = true;
            int session = ++generation;
            store = new FavoritesStore(
                () -> configManager.getConfiguration(SidebarFavoritesConfig.GROUP, SidebarFavoritesConfig.FAVORITES),
                value -> configManager.setConfiguration(SidebarFavoritesConfig.GROUP, SidebarFavoritesConfig.FAVORITES, value));
            panel = new FavoritesPanel(
                () -> withSession(session, () -> refresh(null)),
                id -> withSession(session, () -> open(id)),
                id -> withSession(session, () -> add(id)),
                id -> withSession(session, () -> edit(favorites -> favorites.remove(id))),
                (id, gap) -> withSession(session, () -> edit(favorites -> favorites.move(id, gap))),
                () -> withSession(session, this::reset));
            panel.configure(
                (key, value) -> withSession(session, () -> saveSetting(key, value)),
                (id, key) -> withSession(session, () -> edit(favorites -> favorites.bind(id, key, config.openHotkey()))));
            catalog = new PanelCatalog(panel.getWrappedPanel());
            navigation = NavigationButton.builder().tooltip("Sidebar Favorites")
                .priority(config.sidebarPosition().priority()).icon(icon()).panel(panel).build();
            toolbar.addNavigation(navigation);
            openHotkey = new HotkeyListener(() -> config.openHotkey())
            {
                @Override
                public void hotkeyPressed()
                {
                    SwingUtilities.invokeLater(() -> withSession(session, () ->
                    {
                        if (navigation != null)
                        {
                            toolbar.openPanel(navigation);
                        }
                    }));
                }
            };
            openHotkey.setEnabledOnLoginScreen(true);
            keyManager.registerKeyListener(openHotkey);
            // ClientToolbar queues the insertion even when called on the Swing thread.
            scheduleRefresh();
        });
    }

    private void withSession(int session, Runnable action)
    {
        if (active && generation == session)
        {
            action.run();
        }
    }

    private void refresh(String message)
    {
        PanelCatalog.requireEdt();
        if (!active)
        {
            return;
        }
        int priority = config.sidebarPosition().priority();
        if (navigation.getPriority() != priority)
        {
            // Replace only our own button using the public toolbar API.
            toolbar.removeNavigation(navigation);
            navigation = NavigationButton.builder().tooltip("Sidebar Favorites")
                .priority(priority).icon(navigation.getIcon()).panel(panel).build();
            toolbar.addNavigation(navigation);
            // Read the catalog after the queued remove/add operations finish.
            scheduleRefresh();
            return;
        }
        long revision = settingsRevision.get();
        boolean readable = store.reload();
        List<PanelCatalog.Entry> entries = Collections.emptyList();
        try
        {
            entries = catalog.entries();
        }
        catch (IllegalStateException ex)
        {
            message = "Available panels could not be read. Reopen Favorites after the client finishes loading.";
        }
        if (!readable)
        {
            message = "Saved favorites could not be read. They have been left unchanged. You can reset them below.";
        }
        viewRevision = revision;
        syncHotkeys(readable ? store.favorites() : Favorites.empty(), entries, revision);
        panel.syncSettings(config);
        panel.setShowInstructions(config.showInstructions());
        panel.update(store.favorites(), entries, readable, message);
    }

    private void saveSetting(String key, Object value)
    {
        if (!currentView()) { return; }
        if ("openHotkey".equals(key) && !Keybind.NOT_SET.equals(value))
        {
            for (Favorites.Entry entry : store.favorites().entries())
            {
                if (value.equals(entry.hotkey))
                {
                    refresh("That shortcut is assigned to " + entry.title + ".");
                    return;
                }
            }
        }
        configManager.setConfiguration(SidebarFavoritesConfig.GROUP, key, value);
        refresh(null);
    }

    private void syncHotkeys(Favorites favorites, List<PanelCatalog.Entry> entries, long revision)
    {
        Map<Keybind, Integer> counts = new HashMap<>();
        for (Favorites.Entry favorite : favorites.entries())
        {
            counts.merge(favorite.hotkey, 1, Integer::sum);
        }
        Map<Keybind, String> next = new HashMap<>();
        for (Favorites.Entry favorite : favorites.entries())
        {
            Keybind key = favorite.hotkey;
            boolean available = entries.stream().anyMatch(entry -> entry.id.equals(favorite.id) && entry.available);
            if (available && !Keybind.NOT_SET.equals(key) && !key.equals(config.openHotkey()) && counts.get(key) == 1)
            {
                next.put(key, favorite.id);
            }
        }
        if (next.equals(registeredKeys) && bindingRevision == revision) { return; }
        clearFavoriteHotkeys();
        registeredKeys = next;
        bindingRevision = revision;
        int session = generation;
        for (Map.Entry<Keybind, String> entry : next.entrySet())
        {
            HotkeyListener listener = new HotkeyListener(() ->
                settingsRevision.get() == revision ? entry.getKey() : Keybind.NOT_SET)
            {
                @Override public void hotkeyPressed()
                {
                    SwingUtilities.invokeLater(() -> withSession(session, () ->
                    {
                        if (settingsRevision.get() != revision) { return; }
                        // Reveal the sidebar through our own button before selecting the target.
                        toolbar.openPanel(navigation);
                        open(entry.getValue());
                    }));
                }
            };
            listener.setEnabledOnLoginScreen(true);
            keyManager.registerKeyListener(listener);
            favoriteListeners.add(listener);
        }
    }

    private void clearFavoriteHotkeys()
    {
        for (HotkeyListener listener : favoriteListeners) { keyManager.unregisterKeyListener(listener); }
        favoriteListeners.clear();
        registeredKeys = Collections.emptyMap();
    }

    private boolean currentView()
    {
        if (viewRevision != settingsRevision.get())
        {
            refresh(null);
            return false;
        }
        return true;
    }

    private void add(String id)
    {
        if (!currentView())
        {
            return;
        }
        try
        {
            for (PanelCatalog.Entry entry : catalog.entries())
            {
                if (entry.id.equals(id) && entry.available)
                {
                    edit(favorites -> favorites.add(entry.id, entry.title));
                    return;
                }
            }
            refresh("That panel is no longer available. Enable its plugin and try again.");
        }
        catch (IllegalStateException ex)
        {
            refresh(null);
        }
    }

    private void open(String id)
    {
        if (!currentView())
        {
            return;
        }
        try
        {
            if (!catalog.open(id))
            {
                refresh("That panel is unavailable or cannot be identified uniquely. Your favorite is still saved.");
            }
        }
        catch (IllegalStateException ex)
        {
            refresh(null);
        }
    }

    private void edit(UnaryOperator<Favorites> change)
    {
        if (!currentView())
        {
            return;
        }
        try
        {
            boolean saved = store.edit(change);
            refresh(saved ? null : "Favorites changed in this profile. Please try that action again.");
        }
        catch (IllegalArgumentException ex)
        {
            refresh(ex.getMessage());
        }
        catch (RuntimeException ex)
        {
            LOG.warn("Could not save Sidebar Favorites settings", ex);
            refresh("Could not save favorites. Please try again.");
        }
    }

    private void reset()
    {
        if (currentView())
        {
            try
            {
                store.reset();
                refresh(null);
            }
            catch (RuntimeException ex)
            {
                LOG.warn("Could not reset Sidebar Favorites settings", ex);
                refresh("Could not reset favorites. Please try again.");
            }
        }
    }

    private void scheduleRefresh()
    {
        if (refreshQueued.compareAndSet(false, true))
        {
            SwingUtilities.invokeLater(() ->
            {
                refreshQueued.set(false);
                refresh(null);
            });
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (SidebarFavoritesConfig.GROUP.equals(event.getGroup())
            && (SidebarFavoritesConfig.FAVORITES.equals(event.getKey())
                || SidebarFavoritesConfig.POSITION.equals(event.getKey())
                || SidebarFavoritesConfig.SHOW_INSTRUCTIONS.equals(event.getKey())
                || "openHotkey".equals(event.getKey())))
        {
            settingsRevision.incrementAndGet();
            scheduleRefresh();
        }
    }

    @Subscribe
    public void onProfileChanged(ProfileChanged event)
    {
        settingsRevision.incrementAndGet();
        scheduleRefresh();
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event)
    {
        scheduleRefresh();
    }

    @Override
    protected void shutDown() throws Exception
    {
        onEdt(() ->
        {
            active = false;
            clearFavoriteHotkeys();
            if (openHotkey != null)
            {
                keyManager.unregisterKeyListener(openHotkey);
                openHotkey = null;
            }
            generation++;
            if (panel != null)
            {
                panel.onDeactivate();
            }
            if (navigation != null)
            {
                toolbar.removeNavigation(navigation);
            }
            navigation = null;
            catalog = null;
            panel = null;
            store = null;
        });
    }

    private static void onEdt(Runnable action) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            action.run();
        }
        else
        {
            SwingUtilities.invokeAndWait(action);
        }
    }

    private static BufferedImage icon()
    {
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D star = new Path2D.Double();
            for (int point = 0; point < 10; point++)
            {
                double angle = -Math.PI / 2 + point * Math.PI / 5;
                double radius = point % 2 == 0 ? 10 : 4.5;
                double x = 12 + radius * Math.cos(angle);
                double y = 12 + radius * Math.sin(angle);
                if (point == 0)
                {
                    star.moveTo(x, y);
                }
                else
                {
                    star.lineTo(x, y);
                }
            }
            star.closePath();
            graphics.setColor(new Color(245, 190, 72));
            graphics.fill(star);
        }
        finally
        {
            graphics.dispose();
        }
        return image;
    }
}
