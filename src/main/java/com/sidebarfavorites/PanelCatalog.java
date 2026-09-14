/*
 * Copyright (c) 2026, Brettgod1355 (https://github.com/Brettgod1355)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sidebarfavorites;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.laf.RuneLiteTabbedPaneUI;

/** Discovers tabs through public Swing APIs. The only write selects an existing tab. */
final class PanelCatalog
{
    static final class Entry
    {
        final String id;
        final String title;
        final Icon icon;
        final boolean available;

        Entry(String id, String title, Icon icon, boolean available)
        {
            this.id = id;
            this.title = title;
            this.icon = icon;
            this.available = available;
        }
    }

    private final Component ownPanel;

    PanelCatalog(Component ownPanel)
    {
        this.ownPanel = ownPanel;
    }

    List<Entry> entries()
    {
        JTabbedPane pane = pane();
        Map<String, Entry> entries = new LinkedHashMap<>();
        for (int index = 0; index < pane.getTabCount(); index++)
        {
            Entry entry = read(pane, index);
            if (entry == null)
            {
                continue;
            }
            if (entries.containsKey(entry.id))
            {
                // Identical class + tooltip is ambiguous; never guess which panel to open.
                entries.put(entry.id, new Entry(entry.id, entry.title, entry.icon, false));
            }
            else
            {
                entries.put(entry.id, entry);
            }
        }
        return new ArrayList<>(entries.values());
    }

    boolean open(String id)
    {
        JTabbedPane pane = pane();
        Component match = null;
        for (int index = 0; index < pane.getTabCount(); index++)
        {
            Entry entry = read(pane, index);
            if (entry != null && entry.id.equals(id))
            {
                if (match != null || !entry.available)
                {
                    return false;
                }
                match = pane.getComponentAt(index);
            }
        }
        if (match == null || !pane.isVisible())
        {
            return false;
        }
        // Resolve at click time: tab indexes change when plugins are enabled/disabled.
        pane.setSelectedComponent(match);
        return true;
    }

    private JTabbedPane pane()
    {
        requireEdt();
        JTabbedPane pane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, ownPanel);
        if (pane == null || pane.indexOfComponent(ownPanel) < 0
            || pane.getTabPlacement() != JTabbedPane.RIGHT
            || !(pane.getUI() instanceof RuneLiteTabbedPaneUI))
        {
            throw new IllegalStateException("The sidebar layout is not supported.");
        }
        return pane;
    }

    private Entry read(JTabbedPane pane, int index)
    {
        Component component = pane.getComponentAt(index);
        if (component == ownPanel)
        {
            return null;
        }
        String title = pane.getToolTipTextAt(index);
        PluginPanel panel = findPanel(component);
        if (panel == null || title == null || title.trim().isEmpty() || title.length() > 1024)
        {
            return null;
        }
        String id = panel.getClass().getName() + "\n" + title;
        if (id.length() > 2048)
        {
            return null;
        }
        return new Entry(id, title, pane.getIconAt(index), pane.isEnabledAt(index));
    }

    private static PluginPanel findPanel(Component root)
    {
        Deque<Component> pending = new ArrayDeque<>();
        pending.add(root);
        int visited = 0;
        while (!pending.isEmpty() && ++visited <= 256)
        {
            Component component = pending.removeFirst();
            if (component instanceof PluginPanel)
            {
                return (PluginPanel) component;
            }
            if (component instanceof Container)
            {
                for (Component child : ((Container) component).getComponents())
                {
                    pending.addLast(child);
                }
            }
        }
        return null;
    }

    static void requireEdt()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            throw new IllegalStateException("Panel access must be on the Swing event thread.");
        }
    }
}
