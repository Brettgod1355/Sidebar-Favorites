/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup(SidebarFavoritesConfig.GROUP)
public interface SidebarFavoritesConfig extends Config
{
    String GROUP = "sidebarfavorites";
    String SHOW_INSTRUCTIONS = "showInstructions";

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

    String FAVORITES = "favoritesV1";

    @ConfigItem(keyName = FAVORITES, name = "Saved favorites",
        description = "Favorites managed through the Sidebar Favorites panel", hidden = true)
    default String favorites()
    {
        return "";
    }
}
