# Implementation notes

## Boundary of the feature

Sidebar Favorites owns one ordinary `PluginPanel` and navigation button. All
favorites sorting and dragging happen inside that panel. `ClientToolbar` is used
to add/remove our own navigation button and open it for hotkey navigation.
Its priority is `Integer.MAX_VALUE` by default (Bottom), or `Integer.MIN_VALUE`
when the user chooses Top.

The catalog finds the `JTabbedPane` containing our wrapped panel through
`SwingUtilities.getAncestorOfClass`. It checks for a right-hand RuneLite tabbed UI
and reads tab components, tooltips, icons, and enabled state. Opening a shortcut
rescans and calls `setSelectedComponent` on the existing component, on the Swing
event thread. RuneLite's own change listener handles normal panel activation.
No replacement navigation buttons are made for other plugins.

The catalog retains no references to other plugins' panels between operations.
Refresh runs when Favorites activates, when the picker opens, and after plugin
or configuration events. We attach no listeners to the shared tabbed pane.
Unknown structures and ambiguous identifiers are not opened.

## Persistence and lifecycle

The ordered favorites are JSON under `sidebarfavorites.favoritesV1`, managed by
RuneLite's `ConfigManager`. Format version 2 stores panel identifiers, display names, key codes, and
modifier masks. Version 1 is read with unassigned hotkeys.
Disabled panels retain their position. Reloading a profile replaces the displayed
list; stale edits are refused if the loaded settings no longer match storage.
Malformed or newer data is preserved until the user explicitly resets it.

Config/profile events invalidate the current view before queuing its refresh.
Callbacks from an earlier plugin session are ignored after shutdown/restart.
Shutdown removes our own navigation button, unregisters the main and favorite
hotkey listeners, and releases the panel/catalog. The settings view uses the same
ConfigManager keys as native configuration; per-favorite bindings stay in the
favorites document.

## Reused work and review context

The public component discovery and build setup adapt Sidebar Organizer. Its
UI-delegate replacement and native-toolbar drag/reorder code are not included.
The previous implementation helped identify the need for stable identifiers,
restart persistence, missing-panel handling, and drag/click separation.

The Plugin Hub maintainer declined shared client UI/toolbar modifications in
[runelite/plugin-hub#16377](https://github.com/runelite/plugin-hub/pull/16377).
This separate favorites panel is a different feature, but its use of shared tab
discovery/selection still needs review. No Plugin Hub acceptance is implied.

Relevant upstream source:

- [ClientToolbar](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/ClientToolbar.java)
- [ClientUI](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/ClientUI.java)
- [NavigationButton](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/NavigationButton.java)
- [PluginPanel](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/ui/PluginPanel.java)
