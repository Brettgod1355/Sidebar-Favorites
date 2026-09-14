/* SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors. See LICENSE.
 */
package com.sidebarfavorites;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class FavoritesStoreTest
{
    @Test
    public void restartLoadsSavedFavorites()
    {
        AtomicReference<String> configuration = new AtomicReference<>();
        FavoritesStore first = new FavoritesStore(configuration::get, configuration::set);
        assertTrue(first.reload());
        assertTrue(first.edit(favorites -> favorites.add("A", "Alpha")));
        FavoritesStore restarted = new FavoritesStore(configuration::get, configuration::set);
        assertTrue(restarted.reload());
        assertTrue(restarted.favorites().contains("A"));
    }

    @Test
    public void staleViewCannotOverwriteAnotherProfileOrAnExternalEdit()
    {
        String original = Favorites.empty().add("A", "Alpha").encode();
        String changed = Favorites.empty().add("B", "Beta").encode();
        AtomicReference<String> configuration = new AtomicReference<>(original);
        FavoritesStore store = new FavoritesStore(configuration::get, configuration::set);
        store.reload();
        configuration.set(changed);
        assertFalse(store.edit(favorites -> favorites.add("C", "Gamma")));
        assertEquals(changed, configuration.get());
        assertTrue(store.favorites().contains("B"));
        assertFalse(store.favorites().contains("A"));
        configuration.set(original);
        store.reload();
        assertTrue(store.favorites().contains("A"));
    }

    @Test
    public void unreadableSettingsArePreservedUntilExplicitReset()
    {
        AtomicReference<String> configuration = new AtomicReference<>("{\"version\":2,\"favorites\":[]}");
        FavoritesStore store = new FavoritesStore(configuration::get, configuration::set);
        assertFalse(store.reload());
        String original = configuration.get();
        assertFalse(store.edit(favorites -> favorites.add("A", "Alpha")));
        assertEquals(original, configuration.get());
        assertTrue(store.reset());
        assertTrue(Favorites.decode(configuration.get()).entries().isEmpty());
        assertTrue(store.edit(favorites -> favorites.add("A", "Alpha")));
    }

    @Test
    public void resetDoesNotClearSettingsChangedAfterTheViewWasLoaded()
    {
        AtomicReference<String> configuration = new AtomicReference<>("bad configuration");
        FavoritesStore store = new FavoritesStore(configuration::get, configuration::set);
        store.reload();
        String next = Favorites.empty().add("B", "Beta").encode();
        configuration.set(next);
        assertFalse(store.reset());
        assertEquals(next, configuration.get());
    }

    @Test
    public void noOpDoesNotWriteAndFailedWriteDoesNotClaimSuccess()
    {
        AtomicInteger writes = new AtomicInteger();
        FavoritesStore store = new FavoritesStore(() -> "", value ->
        {
            writes.incrementAndGet();
            throw new IllegalStateException("Simulated write failure");
        });
        store.reload();
        assertTrue(store.edit(favorites -> favorites.remove("missing")));
        assertEquals(0, writes.get());
        assertThrows(IllegalStateException.class, () -> store.edit(favorites -> favorites.add("A", "Alpha")));
        assertTrue(store.favorites().entries().isEmpty());
    }
}
