/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Sidebar Favorites contributors
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/** Reloads profile configuration and refuses to save over a stale or unreadable view. */
final class FavoritesStore
{
    private final Supplier<String> read;
    private final Consumer<String> write;
    private Favorites favorites = Favorites.empty();
    private String loaded;
    private boolean readable;

    FavoritesStore(Supplier<String> read, Consumer<String> write)
    {
        this.read = read;
        this.write = write;
    }

    boolean reload()
    {
        loaded = read.get();
        try
        {
            favorites = Favorites.decode(loaded);
            readable = true;
        }
        catch (IllegalArgumentException ex)
        {
            favorites = Favorites.empty();
            readable = false;
        }
        return readable;
    }

    Favorites favorites()
    {
        return favorites;
    }

    boolean edit(UnaryOperator<Favorites> edit)
    {
        if (!Objects.equals(loaded, read.get()))
        {
            reload();
            return false;
        }
        if (!readable)
        {
            return false;
        }
        Favorites next = edit.apply(favorites);
        if (next != favorites)
        {
            String encoded = next.encode();
            write.accept(encoded);
            loaded = encoded;
            favorites = next;
        }
        return true;
    }

    boolean reset()
    {
        if (!Objects.equals(loaded, read.get()))
        {
            reload();
            return false;
        }
        String encoded = Favorites.empty().encode();
        write.accept(encoded);
        loaded = encoded;
        favorites = Favorites.empty();
        readable = true;
        return true;
    }
}
