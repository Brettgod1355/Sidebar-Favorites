/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WhatsNewTest
{
    @Test
    public void versionMatchesTheBuildFiles() throws Exception
    {
        assertTrue("build.gradle", lines("build.gradle").contains("version = '" + WhatsNew.VERSION + "'"));
        assertTrue("runelite-plugin.properties",
            lines("runelite-plugin.properties").contains("version=" + WhatsNew.VERSION));
    }

    @Test
    public void onlyPlayersWithSavedFavoritesHearItOncePerVersion()
    {
        String line = "Sidebar Favorites updated to " + WhatsNew.VERSION + ": " + WhatsNew.NOTE + ".";
        assertEquals(line, WhatsNew.message(null, true));
        assertEquals(line, WhatsNew.message("1.0.4", true));
        assertNull(WhatsNew.message(null, false));
        assertNull(WhatsNew.message("1.0.4", false));
        assertNull(WhatsNew.message(WhatsNew.VERSION, true));
    }

    @Test
    public void onlyAListWithAFavoriteCountsAsSaved()
    {
        assertFalse(WhatsNew.hasFavorites(null));
        assertFalse(WhatsNew.hasFavorites(""));
        assertFalse(WhatsNew.hasFavorites(Favorites.empty().encode()));
        assertTrue(WhatsNew.hasFavorites(Favorites.empty().add("A", "Alpha").encode()));
        assertTrue(WhatsNew.hasFavorites("not a favorites document"));
    }

    private static List<String> lines(String name) throws Exception
    {
        String text = new String(Files.readAllBytes(new File(name).toPath()), StandardCharsets.UTF_8);
        return Arrays.asList(text.split("\\R"));
    }
}
