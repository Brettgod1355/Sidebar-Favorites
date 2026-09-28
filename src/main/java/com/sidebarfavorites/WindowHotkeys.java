/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>. See LICENSE.
 */
package com.sidebarfavorites;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * KeyManager only hears keys pressed while the game has focus, so after a click in another
 * sidebar panel our hotkeys went unheard until the game was clicked. Like RuneLite's own sidebar
 * toggle, this also hears them elsewhere in the client window. Keys pressed in the game are left
 * to KeyManager, and text fields and shortcut recorders keep their keys.
 */
final class WindowHotkeys implements KeyEventDispatcher
{
    private final Supplier<Component> game;
    private final Supplier<List<? extends KeyListener>> listeners;

    WindowHotkeys(Supplier<Component> game, Supplier<List<? extends KeyListener>> listeners)
    {
        this.game = game;
        this.listeners = listeners;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Component source = event.getComponent();
        Component canvas = game.get();
        if (source == null || canvas == null || source == canvas
            || windowOf(source) != windowOf(canvas) || ownsKeys(source))
        {
            return false;
        }
        for (KeyListener listener : listeners.get())
        {
            switch (event.getID())
            {
                case KeyEvent.KEY_PRESSED: listener.keyPressed(event); break;
                case KeyEvent.KEY_RELEASED: listener.keyReleased(event); break;
                case KeyEvent.KEY_TYPED: listener.keyTyped(event); break;
                default: return false;
            }
            if (event.isConsumed()) { return true; }
        }
        return false;
    }

    /** Typing, or recording a shortcut: RuneLite's and ours are buttons that listen for keys. */
    private static boolean ownsKeys(Component component)
    {
        return component instanceof JTextComponent
            || component instanceof AbstractButton && component.getKeyListeners().length > 0;
    }

    private static Window windowOf(Component component)
    {
        return component instanceof Window ? (Window) component : SwingUtilities.getWindowAncestor(component);
    }
}
