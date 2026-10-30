/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

/**
 * The one chat line said after an update. Every version bump changes VERSION and rewrites NOTE
 * to what that version brings, or empties it to say nothing while still remembering the version.
 */
final class WhatsNew
{
    static final String VERSION = "1.0.5";
    static final String NOTE = "pressing a hotkey again closes its panel, "
        + "and you can turn that off with the new \"Hotkeys also close\" setting";

    private WhatsNew()
    {
    }

    /**
     * The line to say, or null. Players who have no saved favorites hear nothing, so a fresh
     * install stays quiet; versions before 1.0.5 stored no version, so saved favorites are
     * what show the plugin was used before.
     */
    static String message(String lastVersion, boolean hasFavorites)
    {
        if (VERSION.equals(lastVersion) || NOTE.isEmpty() || !hasFavorites)
        {
            return null;
        }
        return "Sidebar Favorites updated to " + VERSION + ": " + NOTE + ".";
    }

    /** Whether the saved favorites document holds at least one favorite. Unreadable data counts as used. */
    static boolean hasFavorites(String saved)
    {
        try
        {
            return !Favorites.decode(saved).entries().isEmpty();
        }
        catch (IllegalArgumentException ex)
        {
            return true;
        }
    }
}
