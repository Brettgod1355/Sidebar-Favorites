/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class FavoritesListTest
{
    @Test
    public void normalClickOpensAndEditClickSelectsWithoutOpening() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            List<String> opened = new ArrayList<>();
            FavoritesList list = list(opened, new ArrayList<>());
            press(list, 50, 10);
            release(list, 50, 10);
            assertEquals(Arrays.asList("A"), opened);
            list.setEditing(true, id -> {});
            press(list, 50, 60);
            release(list, 50, 60);
            assertEquals("B", list.getSelectedValue().id);
            assertEquals(1, opened.size());
        });
    }

    @Test
    public void dragToLastGapMovesWithoutOpeningAndOutsideDropCancels() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            List<String> opened = new ArrayList<>();
            List<String> moved = new ArrayList<>();
            FavoritesList list = list(opened, moved);
            list.setEditing(true, id -> {});
            press(list, 50, 10);
            drag(list, 50, 130);
            release(list, 50, 130);
            assertEquals(Arrays.asList("A:3"), moved);
            assertTrue(opened.isEmpty());
            press(list, 50, 10);
            drag(list, 250, 130);
            release(list, 250, 130);
            assertEquals(1, moved.size());
            assertTrue(opened.isEmpty());
        });
    }

    @Test
    public void unavailableAndEmptySpaceDoNotOpenAndRefreshCancelsDrag() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            List<String> opened = new ArrayList<>();
            List<String> moved = new ArrayList<>();
            FavoritesList list = list(opened, moved);
            list.setEditing(true, id -> {});
            press(list, 50, 100);
            release(list, 50, 100);
            press(list, 50, 180);
            release(list, 50, 180);
            assertTrue(opened.isEmpty());
            press(list, 50, 10);
            drag(list, 50, 80);
            list.setRows(Arrays.asList(new FavoritesList.Row("B", "Beta", null, true)));
            release(list, 50, 80);
            assertTrue(moved.isEmpty());
        });
    }

    @Test
    public void deleteOnlyWorksInEditModeAndLeavingEditCancelsDrag() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            List<String> opened = new ArrayList<>();
            List<String> moved = new ArrayList<>();
            List<String> removed = new ArrayList<>();
            FavoritesList list = list(opened, moved);
            press(list, 210, 10);
            release(list, 210, 10);
            assertEquals(Arrays.asList("A"), opened);
            list.setEditing(true, removed::add);
            press(list, 210, 100);
            release(list, 210, 100);
            assertEquals(Arrays.asList("C"), removed);
            list.getActionMap().get("openFavorite").actionPerformed(null);
            assertEquals(1, opened.size());
            press(list, 50, 10);
            drag(list, 50, 130);
            list.setEditing(false, removed::add);
            release(list, 50, 130);
            assertTrue(moved.isEmpty());
            assertEquals(1, opened.size());
        });
    }

    private static FavoritesList list(List<String> opened, List<String> moved)
    {
        FavoritesList list = new FavoritesList(opened::add, (id, gap) -> moved.add(id + ":" + gap));
        list.setSize(220, 200);
        list.setRows(Arrays.asList(new FavoritesList.Row("A", "Alpha", null, true),
            new FavoritesList.Row("B", "Beta", null, true), new FavoritesList.Row("C", "Gamma", null, false)));
        return list;
    }

    // Exercise our listener directly: the headless toolkit cannot run native mouse selection.
    private static void press(FavoritesList list, int x, int y)
    {
        MouseEvent event = event(list, MouseEvent.MOUSE_PRESSED, x, y);
        for (MouseListener listener : list.getMouseListeners())
        {
            if (own(listener)) { listener.mousePressed(event); }
        }
    }

    private static void release(FavoritesList list, int x, int y)
    {
        MouseEvent event = event(list, MouseEvent.MOUSE_RELEASED, x, y);
        for (MouseListener listener : list.getMouseListeners())
        {
            if (own(listener)) { listener.mouseReleased(event); }
        }
    }

    private static void drag(FavoritesList list, int x, int y)
    {
        MouseEvent event = event(list, MouseEvent.MOUSE_DRAGGED, x, y);
        for (MouseMotionListener listener : list.getMouseMotionListeners())
        {
            if (own(listener)) { listener.mouseDragged(event); }
        }
    }

    private static boolean own(Object listener)
    {
        return listener.getClass().getName().startsWith(FavoritesList.class.getName() + "$");
    }

    private static MouseEvent event(FavoritesList list, int type, int x, int y)
    {
        return new MouseEvent(list, type, 1, MouseEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1);
    }
}
