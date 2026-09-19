/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.runelite.client.config.Keybind;

/** Local key capture only; does not install global keyboard hooks. */
final class KeybindEditor extends JPanel
{
    private final JButton capture = new JButton();
    private Keybind value = Keybind.NOT_SET;
    private boolean listening;

    KeybindEditor(Consumer<Keybind> save)
    {
        super(new BorderLayout(4, 0));
        setOpaque(false);
        JButton clear = new JButton("Clear");
        add(capture, BorderLayout.CENTER);
        add(clear, BorderLayout.EAST);
        capture.setToolTipText("Click, then press a shortcut. Escape cancels.");
        capture.addActionListener(event ->
        {
            listening = true;
            capture.setText("Press keys...");
            capture.requestFocusInWindow();
        });
        capture.addFocusListener(new FocusAdapter()
        {
            @Override public void focusLost(FocusEvent event) { setValue(value); }
        });
        capture.addKeyListener(new KeyAdapter()
        {
            @Override public void keyPressed(KeyEvent event)
            {
                if (!listening) { return; }
                event.consume();
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { setValue(value); return; }
                if (Keybind.getModifierForKeyCode(event.getKeyCode()) != null) { return; }
                Keybind next = new Keybind(event.getKeyCode(), event.getModifiersEx());
                setValue(value);
                save.accept(next);
            }
        });
        clear.addActionListener(event -> { setValue(value); save.accept(Keybind.NOT_SET); });
        setValue(value);
    }

    void setValue(Keybind next)
    {
        value = next;
        listening = false;
        capture.setText(next.toString());
    }
}
