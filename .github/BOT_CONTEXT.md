# Notes for the helper bot

Helper-Bot (https://github.com/Brettgod1355/Helper-Bot) adds this file to Claude's instructions for the PR audit
and issue triage here. The README is the guide for players.

## What the plugin is

Sidebar Favorites adds a sidebar panel (a gold star) that lists the player's favorite RuneLite sidebar panels.
Clicking a favorite opens that plugin's own panel; each favorite can have a hotkey, and there is one hotkey to open
the Favorites list itself. It only opens existing panels. It never moves, removes, restyles or hides another
plugin's sidebar icon: the Plugin Hub does not allow that.

## Known questions players ask

- **"Can it reorder or hide RuneLite's sidebar icons?"** No, on purpose: the Plugin Hub does not allow a plugin to
  change another plugin's sidebar entry. Favorites gives a separate list in your own order instead. Say it politely;
  never say it is planned.
- **"A plugin is missing from Add."** The Add picker lists only enabled plugins that have a sidebar panel. Plugins
  without a panel, and utility buttons, cannot be added.
- **"My favorite shows as unavailable."** Its plugin is disabled, or has not created its panel yet. It becomes usable
  again when the panel returns. If a plugin changed its panel identity, remove the favorite and add it again.
- **"A hotkey does nothing."** Hotkeys work while the game has keyboard focus (also at the login screen). Duplicate
  shortcuts inside Favorites are rejected; a shortcut that another plugin also uses can conflict; unavailable
  favorites do not activate their shortcuts. Hotkeys are assigned in **Edit**: select a favorite, click its hotkey
  field and press the combination. The **Open Favorites hotkey** is in the settings (the gear).
- **"Where is the star?"** At the bottom of the sidebar icons by default; **Sidebar position** in the settings moves
  it to the top. Top and Bottom are priorities, so another icon with the same priority can affect the exact spot.
- **"My favorites disappeared."** Favorites, their order and hotkeys are saved with the active RuneLite
  configuration profile: a different profile has its own list.
- **"Less help text, please."** Turn off **Show instructions** in the settings.

## Replying

- Use the names players see: **Add**, **Edit**, **Done**, **Back**, **Up / Down**, **Remove**, **Clear**, the
  settings **Sidebar position**, **Show instructions** and **Open Favorites hotkey**. Never Java names.
- Point to the README sections when they answer the question.
