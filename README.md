# Sidebar Favorites

**Your favorite panels. Your order. Your shortcuts.**

Keep the RuneLite panels you use most in one convenient list. Add favorites,
arrange them your way, and open a panel with a click or a custom hotkey.

Look for the **gold star** on the sidebar. It starts at the bottom so you always
know where to find it, with a setting to move it to the top.

![Main favorites list showing plugin icons, names, saved hotkeys, and a scrollbar](https://raw.githubusercontent.com/Brettgod1355/Sidebar-Favorites/main/docs/images/favorites-main.png)

## Build your list

Click **Add**, search for a panel, and click its green **+**. The picker stays
open so you can add several favorites in one go. Click **Back** when you're done.

Your list shows each plugin's icon, name, and assigned hotkey. Click a favorite
to open its original panel; use the gold star or your Open Favorites hotkey to return.
Long lists scroll automatically.

![Add view with searchable panels and green plus buttons](https://raw.githubusercontent.com/Brettgod1355/Sidebar-Favorites/main/docs/images/favorites-add.png)

## Make it your own

Click **Edit** to organize your favorites without opening their panels.

- **Reorder:** drag and drop, or select a favorite and use **Up / Down**.
- **Remove:** click the red **×**, or select a favorite and click **Remove**.
- **Assign a hotkey:** select a favorite, click its hotkey field below the list,
  and press your preferred combination. **Clear** removes the binding; **Escape** cancels capture.

Click **Done** to return to your list. Hotkeys stay visible on the main screen,
so you can learn them as you go. They can only be changed in Edit mode.

![Edit view showing drag grips, red remove buttons, movement controls, and the selected favorite's hotkey field](https://raw.githubusercontent.com/Brettgod1355/Sidebar-Favorites/main/docs/images/favorites-edit.png)

## Keep Favorites within reach

The **gear** beside Add and Edit opens your settings. These are the same saved
settings shown in RuneLite's plugin configuration, so you can use either interface.

| Setting | What it does | Default |
| --- | --- | --- |
| **Sidebar position** | Place the gold star at the top or bottom of sidebar panel icons. | Bottom |
| **Show instructions** | Hide the help text to leave more room for favorites. | On |
| **Open Favorites hotkey** | Open your favorites list with a custom shortcut. | Not set |

Try **Ctrl+F**, **Ctrl+Shift+F**, or **Alt+F**, or choose your own combination.
The main shortcut opens Favorites; individual shortcuts open their assigned panels.
Hotkeys work while the game has keyboard focus, including at the login screen.
Choose bindings that don't conflict with your other plugins.

![Settings view with sidebar position, instructions toggle, and customizable Open Favorites hotkey](https://raw.githubusercontent.com/Brettgod1355/Sidebar-Favorites/main/docs/images/favorites-settings.png)

## Saved the way you left it

Your favorites, order, and hotkeys are saved with your active **RuneLite configuration
profile** and survive restarts. Disable a plugin and its favorite stays saved as
unavailable; enable it again and the shortcut becomes usable when its panel returns.
Removing a favorite also removes its hotkey.

## Good to know

- Favorites opens existing sidebar panels. It does **not rearrange RuneLite's native sidebar icons**.
- The Add picker lists enabled plugins with available sidebar panels. Plugins without
  panels and utility buttons cannot be added.
- Top and Bottom are priority preferences. Another icon with the same priority can affect the exact position.
- Duplicate shortcut assignments within Favorites are rejected. If a conflicting main
  shortcut is assigned through RuneLite's settings, the main shortcut takes precedence.
  Unavailable favorites do not activate their shortcuts.
- If a plugin changes its panel identity, remove the unavailable favorite and add it again.

## Feedback

Found a bug or have an idea? [Open an issue](https://github.com/Brettgod1355/Sidebar-Favorites/issues).

**Availability:** not yet submitted to or approved for the RuneLite Plugin Hub.

## License

[BSD 2-Clause](https://github.com/Brettgod1355/Sidebar-Favorites/blob/main/LICENSE) — free to use, modify, and redistribute under the license terms.
See [third-party notices](https://github.com/Brettgod1355/Sidebar-Favorites/blob/main/THIRD_PARTY_NOTICES.md) for attribution.
