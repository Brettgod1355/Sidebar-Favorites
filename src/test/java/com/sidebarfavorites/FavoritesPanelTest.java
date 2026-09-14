/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class FavoritesPanelTest
{
    @Test
    public void addPickerFiltersAndAddsTheChosenPanel() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            AtomicReference<String> added = new AtomicReference<>();
            FavoritesPanel panel = new FavoritesPanel(() -> {}, id -> {}, added::set,
                id -> {}, (id, gap) -> {}, () -> {});
            panel.update(Favorites.empty().add("A", "Alpha"), Arrays.asList(
                new PanelCatalog.Entry("A", "Alpha", null, true),
                new PanelCatalog.Entry("B", "Beta", null, true),
                new PanelCatalog.Entry("C", "Gamma", null, false)), true, null);
            button(panel, "Add favorites").doClick();
            JTextField search = find(panel, JTextField.class);
            search.setText("Alpha");
            assertFalse(button(panel, "Add selected").isEnabled());
            search.setText("Gamma");
            assertFalse(button(panel, "Add selected").isEnabled());
            search.setText("beTA");
            assertTrue(button(panel, "Add selected").isEnabled());
            button(panel, "Add selected").doClick();
            assertEquals("B", added.get());
            assertTrue(button(panel, "Add favorites").isVisible());
        });
    }

    @Test
    public void unavailableFavoriteIsKeptAndCanBeRemoved() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            AtomicReference<String> removed = new AtomicReference<>();
            FavoritesPanel panel = new FavoritesPanel(() -> {}, id -> {}, id -> {},
                removed::set, (id, gap) -> {}, () -> {});
            panel.update(Favorites.empty().add("A", "Alpha"), Arrays.asList(), true, null);
            FavoritesList list = find(panel, FavoritesList.class);
            assertEquals(1, list.getModel().getSize());
            assertFalse(list.getModel().getElementAt(0).available);
            list.setSelectedIndex(0);
            button(panel, "Remove").doClick();
            assertEquals("A", removed.get());
            panel.update(Favorites.empty(), Arrays.asList(), false, "Unreadable favorites");
            assertFalse(button(panel, "Add favorites").isEnabled());
            assertTrue(button(panel, "Reset saved favorites").isVisible());
        });
    }

    private static JButton button(Container root, String text)
    {
        for (Component child : root.getComponents())
        {
            if (child instanceof JButton && text.equals(((JButton) child).getText()))
            {
                return (JButton) child;
            }
            if (child instanceof Container)
            {
                JButton found = button((Container) child, text);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    private static <T> T find(Container root, Class<T> type)
    {
        for (Component child : root.getComponents())
        {
            if (type.isInstance(child)) { return type.cast(child); }
            if (child instanceof Container)
            {
                T found = find((Container) child, type);
                if (found != null) { return found; }
            }
        }
        return null;
    }
}
