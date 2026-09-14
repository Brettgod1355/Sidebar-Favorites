/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SidebarFavoritesConfig.GROUP)
public interface SidebarFavoritesConfig extends Config
{
    String GROUP = "sidebarfavorites";
    String FAVORITES = "favoritesV1";

    @ConfigItem(keyName = FAVORITES, name = "Saved favorites",
        description = "Favorites managed through the Sidebar Favorites panel", hidden = true)
    default String favorites()
    {
        return "";
    }
}
