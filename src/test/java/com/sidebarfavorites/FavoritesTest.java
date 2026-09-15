/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors. See LICENSE.
 */
package com.sidebarfavorites;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class FavoritesTest
{
    @Test
    public void roundTripPreservesOrderAndEscapedIdentifiers()
    {
        Favorites favorites = Favorites.empty().add("example.Panel\nA, \"B\"", "A, \"B\"")
            .add("other.Panel\nTools", "Tools");
        Favorites loaded = Favorites.decode(favorites.encode());
        assertEquals(ids(favorites), ids(loaded));
        assertEquals("A, \"B\"", loaded.entries().get(0).title);
    }

    @Test
    public void noSavedConfigurationStartsEmpty()
    {
        assertTrue(Favorites.decode(null).entries().isEmpty());
        assertTrue(Favorites.decode("").entries().isEmpty());
        assertTrue(Favorites.decode(Favorites.empty().encode()).entries().isEmpty());
    }

    @Test
    public void movingUsesGapsAndSupportsBothEnds()
    {
        Favorites favorites = three();
        assertEquals(Arrays.asList("B", "C", "A"), ids(favorites.move("A", 3)));
        assertEquals(Arrays.asList("C", "A", "B"), ids(favorites.move("C", 0)));
        assertEquals(Arrays.asList("A", "C", "B"), ids(favorites.move("B", 3)));
        assertSame(favorites, favorites.move("A", 1));
        assertSame(favorites, favorites.move("B", 1));
        assertSame(favorites, favorites.move("missing", 0));
        assertSame(favorites, favorites.move("A", 4));
    }

    @Test
    public void disabledFavoritesRemainUntilExplicitlyRemoved()
    {
        // Availability is deliberately not part of the persisted model.
        Favorites favorites = three().move("C", 0).add("D", "New panel");
        assertEquals(Arrays.asList("C", "A", "B", "D"), ids(Favorites.decode(favorites.encode())));
        assertEquals(Arrays.asList("C", "B", "D"), ids(favorites.remove("A")));
        assertSame(favorites, favorites.add("C", "Same favorite"));
    }

    @Test
    public void malformedAndNewerConfigurationsAreRejected()
    {
        String[] invalid = {
            "null", "[]", "{", "{}", "{\"version\":3,\"favorites\":[]}",
            "{\"version\":1.5,\"favorites\":[]}", "{\"version\":\"1\",\"favorites\":[]}",
            "{\"version\":1,\"favorites\":[null]}",
            "{\"version\":1,\"favorites\":[{\"id\":1,\"title\":\"A\"}]}",
            "{\"version\":1,\"favorites\":[{\"id\":\"A\",\"title\":\"\"}]}",
            "{\"version\":1,\"favorites\":[{\"id\":\"A\",\"title\":\"A\"},{\"id\":\"A\",\"title\":\"B\"}]}"
        };
        for (String json : invalid)
        {
            assertThrows(json, IllegalArgumentException.class, () -> Favorites.decode(json));
        }
    }

    @Test
    public void hotkeysRoundTripMoveAndRemoveWithFavorite()
    {
        net.runelite.client.config.Keybind key = new net.runelite.client.config.Keybind(70, 128);
        Favorites bound = three().bind("B", key, net.runelite.client.config.Keybind.NOT_SET);
        Favorites loaded = Favorites.decode(bound.move("B", 0).encode());
        assertEquals(key, loaded.entries().get(0).hotkey);
        assertEquals("B", loaded.entries().get(0).id);
        assertFalse(loaded.remove("B").entries().stream().anyMatch(entry -> key.equals(entry.hotkey)));
        assertThrows(IllegalArgumentException.class,
            () -> bound.bind("A", key, net.runelite.client.config.Keybind.NOT_SET));
        assertThrows(IllegalArgumentException.class, () -> three().bind("A", key, key));
    }

    @Test
    public void oldFavoritesLoadWithoutBindings()
    {
        Favorites old = Favorites.decode("{\"version\":1,\"favorites\":[{\"id\":\"A\",\"title\":\"Alpha\"}]}");
        assertEquals(net.runelite.client.config.Keybind.NOT_SET, old.entries().get(0).hotkey);
        assertEquals("A", Favorites.decode(old.encode()).entries().get(0).id);
    }

    private static Favorites three()
    {
        return Favorites.empty().add("A", "Alpha").add("B", "Beta").add("C", "Gamma");
    }

    private static List<String> ids(Favorites favorites)
    {
        return favorites.entries().stream().map(entry -> entry.id).collect(Collectors.toList());
    }
}
