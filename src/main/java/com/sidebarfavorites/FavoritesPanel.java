/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

final class FavoritesPanel extends PluginPanel
{
    private final Runnable refresh;
    private final Runnable reset;
    private final Consumer<String> add;
    private final FavoritesList list;
    private final DefaultListModel<FavoritesList.Row> availableModel = new DefaultListModel<>();
    private final JList<FavoritesList.Row> available = new JList<>(availableModel);
    private final JTextField search = new JTextField();
    private final JButton toggle = new JButton("Add favorites");
    private final JButton addSelected = new JButton("Add selected");
    private final JButton up = new JButton("Up");
    private final JButton down = new JButton("Down");
    private final JButton remove = new JButton("Remove");
    private final JButton repair = new JButton("Reset saved favorites");
    private final JTextArea status = text("");
    private final JLabel heading = label("Favorites");
    private final JLabel pickerCount = label("");
    private final JTextArea empty = text("Use Add favorites to choose panels. Your favorites will appear here.");
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private List<PanelCatalog.Entry> panels = Collections.emptyList();
    private Favorites saved = Favorites.empty();
    private boolean choosing;
    private boolean editable;
    private long viewGeneration;

    FavoritesPanel(Runnable refresh, Consumer<String> open, Consumer<String> add,
        Consumer<String> removeFavorite, BiConsumer<String, Integer> move, Runnable reset)
    {
        super(false);
        this.refresh = refresh;
        this.add = add;
        this.reset = reset;
        list = new FavoritesList(open, move);
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        toggle.setFocusable(false);
        toggle.addActionListener(event ->
        {
            choosing = !choosing;
            refresh.run();
            showCard();
        });
        add(toggle, BorderLayout.NORTH);

        JPanel favorites = container(new BorderLayout(0, 6));
        JPanel intro = container(new BorderLayout(0, 6));
        intro.add(heading, BorderLayout.NORTH);
        intro.add(empty, BorderLayout.CENTER);
        favorites.add(intro, BorderLayout.NORTH);
        list.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        list.setCellRenderer((items, row, index, selected, focused) -> render(row, selected, true));
        favorites.add(scroll(list), BorderLayout.CENTER);
        JPanel buttons = container(new GridLayout(1, 3, 4, 0));
        buttons.add(up);
        buttons.add(down);
        buttons.add(remove);
        favorites.add(buttons, BorderLayout.SOUTH);
        list.addListSelectionListener(event -> updateControls());
        up.addActionListener(event -> moveSelected(move, -1));
        down.addActionListener(event -> moveSelected(move, 1));
        remove.addActionListener(event ->
        {
            if (editable && list.getSelectedValue() != null)
            {
                removeFavorite.accept(list.getSelectedValue().id);
            }
        });

        JPanel picker = container(new BorderLayout(0, 6));
        JPanel pickerHeader = container(new BorderLayout(0, 6));
        pickerHeader.add(label("Search available panels"), BorderLayout.NORTH);
        search.setToolTipText("Only enabled plugins with an available sidebar panel are listed.");
        search.getAccessibleContext().setAccessibleName("Search available panels");
        pickerHeader.add(search, BorderLayout.CENTER);
        pickerHeader.add(pickerCount, BorderLayout.SOUTH);
        picker.add(pickerHeader, BorderLayout.NORTH);
        available.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        available.setFixedCellHeight(46);
        available.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        available.setCellRenderer((items, row, index, selected, focused) -> render(row, selected, false));
        available.getAccessibleContext().setAccessibleName("Available panels");
        available.addListSelectionListener(event -> updateControls());
        picker.add(scroll(available), BorderLayout.CENTER);
        addSelected.addActionListener(event ->
        {
            if (editable && available.getSelectedValue() != null)
            {
                String id = available.getSelectedValue().id;
                choosing = false;
                add.accept(id);
                showCard();
            }
        });
        picker.add(addSelected, BorderLayout.SOUTH);
        search.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override public void insertUpdate(DocumentEvent event) { filter(); }
            @Override public void removeUpdate(DocumentEvent event) { filter(); }
            @Override public void changedUpdate(DocumentEvent event) { filter(); }
        });
        body.add(favorites, "favorites");
        body.add(picker, "picker");
        add(body, BorderLayout.CENTER);

        JPanel footer = container(new BorderLayout(0, 6));
        status.setForeground(new Color(245, 190, 72));
        footer.add(status, BorderLayout.CENTER);
        repair.addActionListener(event ->
        {
            long requestedView = viewGeneration;
            if (JOptionPane.showConfirmDialog(this,
                "Clear the unreadable favorites saved for this profile?", "Reset favorites",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION && requestedView == viewGeneration)
            {
                this.reset.run();
            }
        });
        footer.add(repair, BorderLayout.SOUTH);
        repair.setVisible(false);
        status.setVisible(false);
        add(footer, BorderLayout.SOUTH);
        updateControls();
    }

    @Override
    public void onActivate()
    {
        refresh.run();
    }

    @Override
    public void onDeactivate()
    {
        list.cancelDrag();
    }

    void update(Favorites favorites, List<PanelCatalog.Entry> current, boolean readable, String message)
    {
        PanelCatalog.requireEdt();
        viewGeneration++;
        saved = favorites;
        panels = new ArrayList<>(current);
        editable = readable;
        Map<String, PanelCatalog.Entry> byId = new HashMap<>();
        for (PanelCatalog.Entry entry : panels)
        {
            byId.put(entry.id, entry);
        }
        List<FavoritesList.Row> rows = new ArrayList<>();
        for (Favorites.Entry favorite : favorites.entries())
        {
            PanelCatalog.Entry entry = byId.get(favorite.id);
            rows.add(new FavoritesList.Row(favorite.id, favorite.title,
                entry == null ? null : entry.icon, entry != null && entry.available));
        }
        list.setRows(rows);
        list.setEnabled(readable);
        heading.setText("Favorites (" + rows.size() + ")");
        empty.setText(rows.isEmpty() ? "Use Add favorites to choose panels. Your favorites will appear here."
            : "Click a name to open. Use the grip to select or drag a favorite.");
        status.setText(message == null ? "" : message);
        status.setVisible(message != null && !message.isEmpty());
        repair.setVisible(!readable);
        filter();
        updateControls();
        revalidate();
        repaint();
    }

    private void showCard()
    {
        cards.show(body, choosing ? "picker" : "favorites");
        toggle.setText(choosing ? "Back to favorites" : "Add favorites");
        if (choosing)
        {
            SwingUtilities.invokeLater(search::requestFocusInWindow);
        }
    }

    private void filter()
    {
        String selected = available.getSelectedValue() == null ? null : available.getSelectedValue().id;
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        availableModel.clear();
        panels.stream().filter(entry -> entry.available && !saved.contains(entry.id)
            && entry.title.toLowerCase(Locale.ROOT).contains(query))
            .sorted(Comparator.comparing(entry -> entry.title, String.CASE_INSENSITIVE_ORDER))
            .forEach(entry ->
            {
                availableModel.addElement(new FavoritesList.Row(entry.id, entry.title, entry.icon, true));
                if (entry.id.equals(selected))
                {
                    available.setSelectedIndex(availableModel.size() - 1);
                }
            });
        pickerCount.setText(availableModel.isEmpty() ? "No available panels match" : availableModel.size() + " available");
        if (available.getSelectedIndex() < 0 && !availableModel.isEmpty())
        {
            available.setSelectedIndex(0);
        }
        updateControls();
    }

    private void moveSelected(BiConsumer<String, Integer> move, int offset)
    {
        FavoritesList.Row row = list.getSelectedValue();
        if (editable && row != null)
        {
            int index = list.getSelectedIndex();
            move.accept(row.id, offset < 0 ? index - 1 : index + 2);
        }
    }

    private void updateControls()
    {
        int selected = list.getSelectedIndex();
        up.setEnabled(editable && selected > 0);
        down.setEnabled(editable && selected >= 0 && selected < list.getModel().getSize() - 1);
        remove.setEnabled(editable && selected >= 0);
        addSelected.setEnabled(editable && available.getSelectedIndex() >= 0);
        toggle.setEnabled(editable || choosing);
    }

    private static JPanel render(FavoritesList.Row row, boolean selected, boolean grip)
    {
        JPanel cell = container(new BorderLayout(6, 0));
        cell.setBackground(selected ? new Color(65, 69, 74) : ColorScheme.DARKER_GRAY_COLOR);
        cell.setBorder(BorderFactory.createEmptyBorder(5, grip ? 0 : 8, 5, 6));
        if (grip)
        {
            JLabel handle = label("\u2261");
            handle.setHorizontalAlignment(JLabel.CENTER);
            handle.setPreferredSize(new Dimension(FavoritesList.GRIP_WIDTH, 20));
            handle.setForeground(Color.GRAY);
            cell.add(handle, BorderLayout.WEST);
        }
        JPanel lines = container(null);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        lines.setOpaque(false);
        JLabel name = label(row.title);
        name.setIcon(row.icon);
        name.setIconTextGap(8);
        name.setForeground(row.available ? Color.WHITE : Color.GRAY);
        lines.add(name);
        if (!row.available)
        {
            JLabel missing = label("Unavailable \u2014 favorite saved");
            missing.setForeground(Color.GRAY);
            lines.add(missing);
        }
        cell.add(lines, BorderLayout.CENTER);
        return cell;
    }

    private static JPanel container(java.awt.LayoutManager layout)
    {
        JPanel panel = new JPanel(layout);
        panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        return panel;
    }

    private static JLabel label(String text)
    {
        JLabel label = new JLabel();
        label.putClientProperty("html.disable", Boolean.TRUE);
        label.setText(text);
        return label;
    }

    private static JTextArea text(String value)
    {
        JTextArea area = new JTextArea(value);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(null);
        area.setFont(new JLabel().getFont());
        return area;
    }

    private static JScrollPane scroll(Component content)
    {
        JScrollPane pane = new JScrollPane(content);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 300));
        pane.setMinimumSize(new Dimension(0, 100));
        return pane;
    }
}
