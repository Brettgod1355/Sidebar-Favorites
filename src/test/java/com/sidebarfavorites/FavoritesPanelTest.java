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
            button(panel, "Add").doClick();
            JTextField search = find(panel, JTextField.class);
            search.setText("Alpha");
            assertFalse(button(panel, "Add selected").isEnabled());
            search.setText("Gamma");
            assertFalse(button(panel, "Add selected").isEnabled());
            search.setText("beTA");
            assertTrue(button(panel, "Add selected").isEnabled());
            button(panel, "Add selected").doClick();
            assertEquals("B", added.get());
            assertTrue(button(panel, "Add").isVisible());
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
            assertFalse(button(panel, "Remove").isEnabled());
            button(panel, "Edit").doClick();
            assertNotNull(button(panel, "Done"));
            list.setSelectedIndex(0);
            button(panel, "Remove").doClick();
            assertEquals("A", removed.get());
            panel.update(Favorites.empty(), Arrays.asList(), false, "Unreadable favorites");
            assertFalse(button(panel, "Add").isEnabled());
            assertTrue(button(panel, "Reset saved favorites").isVisible());
        });
    }

    @Test
    public void plusAddsWithoutLeavingPickerAndEmptySpaceDoesNothing() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            AtomicReference<String> added = new AtomicReference<>();
            FavoritesPanel panel = new FavoritesPanel(() -> {}, id -> {}, added::set,
                id -> {}, (id, gap) -> {}, () -> {});
            panel.update(Favorites.empty(), Arrays.asList(
                new PanelCatalog.Entry("A", "Alpha", null, true)), true, null);
            button(panel, "Add").doClick();
            Container picker = find(panel, JTextField.class).getParent().getParent();
            javax.swing.JList<?> choices = find(picker, javax.swing.JList.class);
            choices.setSize(220, 200);
            clickPlus(choices, 210, 10);
            assertEquals("A", added.get());
            assertNotNull(button(panel, "Back"));
            added.set(null);
            clickPlus(choices, 210, 150);
            assertNull(added.get());
            clickPlus(choices, 50, 10);
            assertNull(added.get());
        });
    }

    private static void clickPlus(javax.swing.JList<?> list, int x, int y)
    {
        for (java.awt.event.MouseListener listener : list.getMouseListeners())
        {
            if (listener.getClass().getName().startsWith(FavoritesPanel.class.getName() + "$"))
            {
                listener.mousePressed(new java.awt.event.MouseEvent(list,
                    java.awt.event.MouseEvent.MOUSE_PRESSED, 1, 0, x, y, 1, false,
                    java.awt.event.MouseEvent.BUTTON1));
                listener.mouseReleased(new java.awt.event.MouseEvent(list,
                    java.awt.event.MouseEvent.MOUSE_RELEASED, 2, 0, x, y, 1, false,
                    java.awt.event.MouseEvent.BUTTON1));
            }
        }
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
