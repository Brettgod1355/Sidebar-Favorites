/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TabbedPaneUI;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.laf.RuneLiteLAF;
import net.runelite.client.ui.laf.RuneLiteTabbedPaneUI;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PanelCatalogTest
{
    @BeforeClass
    public static void lookAndFeel() throws Exception
    {
        SwingUtilities.invokeAndWait(RuneLiteLAF::setup);
    }

    @Test
    public void discoveryAndOpeningPreserveNativeOrderLayoutAndDelegate() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            Fixture fixture = new Fixture();
            List<Component> original = Arrays.asList(fixture.pane.getComponentAt(0),
                fixture.pane.getComponentAt(1), fixture.pane.getComponentAt(2));
            TabbedPaneUI ui = fixture.pane.getUI();
            LayoutManager layout = fixture.pane.getLayout();
            int listeners = fixture.pane.getMouseListeners().length;
            AtomicInteger selected = new AtomicInteger();
            fixture.pane.addChangeListener(event -> selected.incrementAndGet());
            List<PanelCatalog.Entry> entries = fixture.catalog.entries();
            assertEquals(2, entries.size());
            assertEquals("Alpha", entries.get(0).title);
            assertTrue(fixture.catalog.open(entries.get(1).id));
            assertSame(fixture.beta.getWrappedPanel(), fixture.pane.getSelectedComponent());
            assertEquals(1, selected.get());
            assertSame(ui, fixture.pane.getUI());
            assertSame(layout, fixture.pane.getLayout());
            assertEquals(listeners, fixture.pane.getMouseListeners().length);
            for (int i = 0; i < original.size(); i++)
            {
                assertSame(original.get(i), fixture.pane.getComponentAt(i));
            }
        });
    }

    @Test
    public void removedTabCannotOpenWrongPanelAndRecreatedTabCanOpen() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            Fixture fixture = new Fixture();
            String id = fixture.catalog.entries().get(1).id;
            fixture.pane.remove(fixture.alpha.getWrappedPanel());
            assertTrue(fixture.catalog.open(id));
            assertSame(fixture.beta.getWrappedPanel(), fixture.pane.getSelectedComponent());
            fixture.pane.remove(fixture.beta.getWrappedPanel());
            assertFalse(fixture.catalog.open(id));
            BetaPanel replacement = new BetaPanel();
            fixture.pane.insertTab(null, null, replacement.getWrappedPanel(), "Beta", 0);
            assertTrue(fixture.catalog.open(id));
            assertSame(replacement.getWrappedPanel(), fixture.pane.getSelectedComponent());
        });
    }

    @Test
    public void ambiguousIdentifiersDoNotPickAnArbitraryPanel() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            Fixture fixture = new Fixture();
            String id = fixture.catalog.entries().get(0).id;
            fixture.pane.insertTab(null, null, new AlphaPanel().getWrappedPanel(), "Alpha", 0);
            assertFalse(fixture.catalog.entries().stream().filter(entry -> entry.id.equals(id)).findFirst().get().available);
            Component selected = fixture.pane.getSelectedComponent();
            assertFalse(fixture.catalog.open(id));
            assertSame(selected, fixture.pane.getSelectedComponent());
        });
    }

    @Test
    public void unsupportedAndDisabledTabsAreNotOpened() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            Fixture fixture = new Fixture();
            String id = fixture.catalog.entries().get(0).id;
            fixture.pane.setEnabledAt(0, false);
            assertFalse(fixture.catalog.open(id));
            fixture.pane.insertTab(null, null, new JPanel(), "No plugin panel", 0);
            assertEquals(2, fixture.catalog.entries().size());
            fixture.pane.setTabPlacement(JTabbedPane.TOP);
            assertThrows(IllegalStateException.class, fixture.catalog::entries);
            fixture.pane.setTabPlacement(JTabbedPane.RIGHT);
            fixture.pane.remove(fixture.own.getWrappedPanel());
            assertThrows(IllegalStateException.class, fixture.catalog::entries);
        });
    }

    @Test
    public void accessOffTheSwingThreadIsRejected()
    {
        assertThrows(IllegalStateException.class, PanelCatalog::requireEdt);
    }

    private static final class Fixture
    {
        final JTabbedPane pane = new JTabbedPane(JTabbedPane.RIGHT);
        final AlphaPanel alpha = new AlphaPanel();
        final BetaPanel beta = new BetaPanel();
        final OwnPanel own = new OwnPanel();
        final PanelCatalog catalog;

        Fixture()
        {
            pane.setUI(new RuneLiteTabbedPaneUI());
            pane.insertTab(null, null, alpha.getWrappedPanel(), "Alpha", 0);
            pane.insertTab(null, null, beta.getWrappedPanel(), "Beta", 1);
            pane.insertTab(null, null, own.getWrappedPanel(), "Sidebar Favorites", 2);
            pane.setSelectedIndex(2);
            catalog = new PanelCatalog(own.getWrappedPanel());
        }
    }

    private static final class AlphaPanel extends PluginPanel { }
    private static final class BetaPanel extends PluginPanel
    {
        BetaPanel() { super(false); }
    }
    private static final class OwnPanel extends PluginPanel
    {
        OwnPanel() { super(false); }
    }
}
