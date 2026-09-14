/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/** Mouse handling is local to our list; no native sidebar drag handlers are installed. */
final class FavoritesList extends JList<FavoritesList.Row>
{
    static final int GRIP_WIDTH = 22;
    private static final int DRAG_DISTANCE = 6;
    private final DefaultListModel<Row> rows = new DefaultListModel<>();
    private final Consumer<String> open;
    private final BiConsumer<String, Integer> move;
    private Row pressed;
    private Point origin;
    private boolean dragging;
    private int destination = -1;

    static final class Row
    {
        final String id;
        final String title;
        final Icon icon;
        final boolean available;

        Row(String id, String title, Icon icon, boolean available)
        {
            this.id = id;
            this.title = title;
            this.icon = icon;
            this.available = available;
        }
    }

    FavoritesList(Consumer<String> open, BiConsumer<String, Integer> move)
    {
        this.open = open;
        this.move = move;
        setModel(rows);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFixedCellHeight(46);
        getAccessibleContext().setAccessibleName("Favorite panels");
        setToolTipText("Click a panel to open it. Select or drag its grip to change the order.");
        MouseAdapter mouse = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                cancelDrag();
                if (isEnabled() && SwingUtilities.isLeftMouseButton(event))
                {
                    pressed = at(event.getPoint());
                    origin = event.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent event)
            {
                if (pressed == null || !isEnabled())
                {
                    return;
                }
                dragging |= origin.distance(event.getPoint()) >= DRAG_DISTANCE;
                if (dragging)
                {
                    destination = gapAt(event.getPoint());
                    setSelectedValue(pressed, false);
                    scrollRectToVisible(new Rectangle(0, Math.max(0, event.getY() - 12), 1, 24));
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent event)
            {
                Row source = pressed;
                boolean wasDragging = dragging;
                int target = destination;
                boolean onGrip = origin != null && origin.x < GRIP_WIDTH;
                cancelDrag();
                if (!isEnabled() || !SwingUtilities.isLeftMouseButton(event) || source == null)
                {
                    return;
                }
                if (wasDragging)
                {
                    if (contains(event.getPoint()) && target >= 0)
                    {
                        move.accept(source.id, target);
                    }
                }
                else if (!onGrip && at(event.getPoint()) == source && source.available)
                {
                    open.accept(source.id);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "openFavorite");
        getActionMap().put("openFavorite", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                Row row = getSelectedValue();
                if (isEnabled() && row != null && row.available)
                {
                    open.accept(row.id);
                }
            }
        });
    }

    void setRows(List<Row> next)
    {
        String selected = getSelectedValue() == null ? null : getSelectedValue().id;
        cancelDrag();
        rows.clear();
        for (Row row : next)
        {
            rows.addElement(row);
            if (row.id.equals(selected))
            {
                setSelectedIndex(rows.size() - 1);
            }
        }
    }

    void cancelDrag()
    {
        pressed = null;
        origin = null;
        dragging = false;
        destination = -1;
        repaint();
    }

    private Row at(Point point)
    {
        int index = locationToIndex(point);
        Rectangle bounds = index < 0 ? null : getCellBounds(index, index);
        return bounds != null && bounds.contains(point) ? rows.get(index) : null;
    }

    private int gapAt(Point point)
    {
        int index = locationToIndex(point);
        if (index < 0)
        {
            return 0;
        }
        Rectangle bounds = getCellBounds(index, index);
        return point.y < bounds.y + bounds.height / 2 ? index : index + 1;
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        if (dragging && destination >= 0 && !rows.isEmpty())
        {
            int index = Math.min(destination, rows.size() - 1);
            Rectangle bounds = getCellBounds(index, index);
            int y = destination == rows.size() ? bounds.y + bounds.height - 2 : bounds.y;
            graphics.setColor(new Color(245, 190, 72));
            graphics.fillRect(0, y, getWidth(), 2);
        }
    }
}
