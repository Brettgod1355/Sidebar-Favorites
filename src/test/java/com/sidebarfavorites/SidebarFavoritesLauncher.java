/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class SidebarFavoritesLauncher
{
    @SuppressWarnings("unchecked") // RuneLite's loadBuiltin uses generic varargs.
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(SidebarFavoritesPlugin.class);
        RuneLite.main(args);
    }
}
