/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.config.Keybind;
import net.runelite.client.util.HotkeyListener;
import org.junit.Test;
import static org.junit.Assert.*;

public class WindowHotkeysTest
{
    @Test
    public void hotkeyWorksWhileAnotherPanelHasFocusButNotWhileTypingOrRecording() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            AtomicInteger opened = new AtomicInteger();
            HotkeyListener open = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK))
            {
                @Override public void hotkeyPressed() { opened.incrementAndGet(); }
            };
            JPanel game = new JPanel();
            JButton otherPanel = new JButton("Other plugin");
            JTextField typing = new JTextField();
            JButton recorder = new JButton("Not set");
            recorder.addKeyListener(new KeyAdapter() {});
            WindowHotkeys hotkeys = new WindowHotkeys(() -> game, () -> Collections.singletonList(open));

            assertTrue(press(hotkeys, otherPanel));
            assertEquals(1, opened.get());

            assertFalse(press(hotkeys, typing));
            assertFalse(press(hotkeys, recorder));
            // KeyManager already delivers keys pressed in the game.
            assertFalse(press(hotkeys, game));
            assertEquals(1, opened.get());
        });
    }

    private static boolean press(WindowHotkeys hotkeys, Component focused)
    {
        boolean handled = hotkeys.dispatchKeyEvent(ctrlF(focused, KeyEvent.KEY_PRESSED));
        hotkeys.dispatchKeyEvent(ctrlF(focused, KeyEvent.KEY_RELEASED));
        return handled;
    }

    private static KeyEvent ctrlF(Component focused, int id)
    {
        return new KeyEvent(focused, id, 0, InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_F, KeyEvent.CHAR_UNDEFINED)
        {
            // Keybind matches the extended key code, which only real key presses carry.
            @Override public int getExtendedKeyCode() { return getKeyCode(); }
        };
    }
}
