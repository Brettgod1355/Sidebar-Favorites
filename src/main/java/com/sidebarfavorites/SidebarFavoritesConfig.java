/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup(SidebarFavoritesConfig.GROUP)
public interface SidebarFavoritesConfig extends Config
{
    String GROUP = "sidebarfavorites";
    String SHOW_INSTRUCTIONS = "showInstructions";
    String HOTKEYS_CLOSE = "hotkeysClose";
    String UPDATE_MESSAGE = "updateMessage";

    @ConfigSection(name = "Chat messages", description = "Lines Sidebar Favorites says in game chat", position = 10)
    String CHAT = "chat";

    @ConfigItem(keyName = SHOW_INSTRUCTIONS, name = "Show instructions",
        description = "Show the help text above your favorites", position = 1)
    default boolean showInstructions()
    {
        return true;
    }

    @ConfigItem(keyName = "openHotkey", name = "Open Favorites hotkey",
        description = "Open Sidebar Favorites while the game has keyboard focus. "
            + "Suggestions: Ctrl+F, Ctrl+Shift+F, or Alt+F. "
            + "Choose any key combination that does not conflict with your other bindings.", position = 2)
    default Keybind openHotkey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(keyName = HOTKEYS_CLOSE, name = "Hotkeys also close",
        description = "Press a hotkey again to close the panel it opened. "
            + "Turn off to have hotkeys only open panels.", position = 3)
    default boolean hotkeysClose()
    {
        return true;
    }

    String POSITION = "sidebarPosition";

    enum SidebarPosition
    {
        TOP, BOTTOM;

        int priority()
        {
            return this == TOP ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }

        @Override
        public String toString()
        {
            return this == TOP ? "Top" : "Bottom";
        }
    }

    @ConfigItem(keyName = POSITION, name = "Sidebar position",
        description = "Place the Favorites star at the top or bottom of sidebar panel icons", position = 0)
    default SidebarPosition sidebarPosition()
    {
        return SidebarPosition.BOTTOM;
    }

    @ConfigItem(keyName = UPDATE_MESSAGE, name = "Say when updated",
        description = "One line in chat the first time you log in after an update, saying what's new",
        position = 11, section = CHAT)
    default boolean updateMessage()
    {
        return true;
    }

    String LAST_VERSION = "lastVersion";

    @ConfigItem(keyName = LAST_VERSION, name = "Last version",
        description = "The plugin version that last said what is new in chat", hidden = true)
    default String lastVersion()
    {
        return "";
    }

    String FAVORITES = "favoritesV1";

    @ConfigItem(keyName = FAVORITES, name = "Saved favorites",
        description = "Favorites managed through the Sidebar Favorites panel", hidden = true)
    default String favorites()
    {
        return "";
    }
}
