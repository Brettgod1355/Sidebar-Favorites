/*
 * SPDX-License-Identifier: BSD-2-Clause
 * Copyright (c) 2026, Brettgod1355 <github.com/Brettgod1355>
 * See LICENSE for redistribution conditions and disclaimer.
 */
package com.sidebarfavorites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.client.config.Keybind;

/** Only ordered identifiers and display names are persisted, never live panels. */
final class Favorites
{
    private static final int MAX_FAVORITES = 256;
    private static final int MAX_TEXT = 2048;
    private static final int MAX_JSON = 1024 * 1024;
    private final List<Entry> entries;

    static final class Entry
    {
        final String id;
        final String title;
        final Keybind hotkey;

        Entry(String id, String title)
        {
            this(id, title, Keybind.NOT_SET);
        }

        Entry(String id, String title, Keybind hotkey)
        {
            this.hotkey = hotkey;
            this.id = validText(id);
            this.title = validText(title);
        }
    }

    private Favorites(List<Entry> entries)
    {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    static Favorites empty()
    {
        return new Favorites(Collections.emptyList());
    }

    List<Entry> entries()
    {
        return entries;
    }

    boolean contains(String id)
    {
        return indexOf(id) >= 0;
    }

    private int indexOf(String id)
    {
        for (int i = 0; i < entries.size(); i++)
        {
            if (entries.get(i).id.equals(id))
            {
                return i;
            }
        }
        return -1;
    }

    Favorites add(String id, String title)
    {
        if (contains(id))
        {
            return this;
        }
        if (entries.size() >= MAX_FAVORITES)
        {
            throw new IllegalArgumentException("The favorites list is full.");
        }
        List<Entry> next = new ArrayList<>(entries);
        next.add(new Entry(id, title));
        return new Favorites(next);
    }

    Favorites remove(String id)
    {
        int index = indexOf(id);
        if (index < 0)
        {
            return this;
        }
        List<Entry> next = new ArrayList<>(entries);
        next.remove(index);
        return new Favorites(next);
    }

    /** Destination is a gap in the original list: 0 is before first, size is after last. */
    Favorites move(String id, int destination)
    {
        int source = indexOf(id);
        if (source < 0 || destination < 0 || destination > entries.size())
        {
            return this;
        }
        int target = destination > source ? destination - 1 : destination;
        if (target == source)
        {
            return this;
        }
        List<Entry> next = new ArrayList<>(entries);
        Entry entry = next.remove(source);
        next.add(target, entry);
        return new Favorites(next);
    }

    Favorites bind(String id, Keybind key, Keybind main)
    {
        int index = indexOf(id);
        if (index < 0) { return this; }
        if (!Keybind.NOT_SET.equals(key))
        {
            if (key.equals(main)) { throw new IllegalArgumentException("That shortcut opens Favorites already."); }
            for (Entry entry : entries)
            {
                if (!entry.id.equals(id) && key.equals(entry.hotkey))
                {
                    throw new IllegalArgumentException("That shortcut is assigned to " + entry.title + ".");
                }
            }
        }
        List<Entry> next = new ArrayList<>(entries);
        Entry old = next.get(index);
        next.set(index, new Entry(old.id, old.title, key));
        return new Favorites(next);
    }

    String encode()
    {
        JsonObject document = new JsonObject();
        document.addProperty("version", 2);
        JsonArray items = new JsonArray();
        for (Entry entry : entries)
        {
            JsonObject item = new JsonObject();
            item.addProperty("id", entry.id);
            item.addProperty("title", entry.title);
            item.addProperty("keyCode", entry.hotkey.getKeyCode());
            item.addProperty("modifiers", entry.hotkey.getModifiers());
            items.add(item);
        }
        document.add("favorites", items);
        return document.toString();
    }

    static Favorites decode(String json)
    {
        if (json == null || json.isEmpty())
        {
            return empty();
        }
        if (json.length() > MAX_JSON)
        {
            throw new IllegalArgumentException("Saved favorites are too large.");
        }
        try
        {
            JsonObject document = new JsonParser().parse(json).getAsJsonObject();
            JsonElement version = document.get("version");
            if (version == null || !version.isJsonPrimitive()
                || !version.getAsJsonPrimitive().isNumber() || !("1".equals(version.getAsString()) || "2".equals(version.getAsString())))
            {
                throw new IllegalArgumentException("Unsupported favorites version.");
            }
            JsonArray items = document.getAsJsonArray("favorites");
            if (items == null || items.size() > MAX_FAVORITES)
            {
                throw new IllegalArgumentException("Invalid favorites list.");
            }
            List<Entry> entries = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (JsonElement element : items)
            {
                JsonObject item = element.getAsJsonObject();
                Keybind key = "1".equals(version.getAsString()) ? Keybind.NOT_SET
                    : new Keybind(readInt(item, "keyCode"), readInt(item, "modifiers"));
                Entry entry = new Entry(readString(item, "id"), readString(item, "title"), key);
                if (!ids.add(entry.id))
                {
                    throw new IllegalArgumentException("Duplicate favorite.");
                }
                entries.add(entry);
            }
            return new Favorites(entries);
        }
        catch (RuntimeException ex)
        {
            // Never overwrite unreadable or newer settings with an empty list.
            throw new IllegalArgumentException("Saved favorites could not be read.", ex);
        }
    }

    private static int readInt(JsonObject object, String key)
    {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
        {
            throw new IllegalArgumentException("Invalid hotkey.");
        }
        int result = new java.math.BigDecimal(value.getAsString()).intValueExact();
        if (result < 0 || result > 65535) { throw new IllegalArgumentException("Invalid hotkey."); }
        return result;
    }

    private static String readString(JsonObject object, String key)
    {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
        {
            throw new IllegalArgumentException("Invalid favorite field.");
        }
        return element.getAsString();
    }

    private static String validText(String value)
    {
        if (value == null || value.trim().isEmpty() || value.length() > MAX_TEXT)
        {
            throw new IllegalArgumentException("Invalid favorite name or identifier.");
        }
        return value;
    }
}
