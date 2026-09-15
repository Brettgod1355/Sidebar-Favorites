# Testing

## Recorded result

On 2026-09-14, [GitHub Actions run 34908177299](https://github.com/Brettgod1355/Sidebar-Favorites/actions/runs/34908177299)
built the plugin JAR and passed **all 20 tests on both Java 11 and Java 17**.
The tested code commit is `e5d8e48b1820c4a01dd4e48d6c5a732c6b3f28e8`.
The build used the current RuneLite release dependency; release metadata at the
time identified version `1.12.38`.

The local workspace could not download the Gradle distribution, so the complete
Gradle build/test results come from GitHub Actions.

On 2026-09-15, Windows development-client testing was reported as working well.
The supplied [screenshot](images/sidebar-favorites.png) shows five favorites, the
gold star near the end of the sidebar, and the corrected full-width Remove button.
This is an overall user report; individual edge-case checks below remain open
until explicitly confirmed. The footer fix also passed Java 11/17 CI in
[run 34917986179](https://github.com/Brettgod1355/Sidebar-Favorites/actions/runs/34917986179).

## Automated coverage

The test suite exercises saved order and JSON round trips, malformed/newer settings,
stale edits across profile/configuration changes, and failed writes. Swing tests
exercise the real RuneLite tabbed UI with wrapped/unwrapped plugin panels, tab
removal/recreation, duplicate identities, disabled tabs, and correct selection
without changing the delegate, tab order, or layout manager.

Additional tests cover the add/search picker, unavailable favorites, and local
click/drag behavior. Headless drag tests invoke the plugin's own mouse listeners;
they do not simulate the operating system's complete mouse event pipeline.

GitHub Actions builds/tests against the current RuneLite release on Java 11 and 17.
See the repository's Actions tab for the result of each commit.

## Manual development-client checklist

These checks still require a live RuneLite client. Results from Sidebar Organizer
do not count as validation of this new plugin.

- [ ] The gold star defaults to the bottom; Sidebar position switches it to Top and back immediately.
- [ ] The position setting survives restart and follows configuration profile changes.
- [ ] Assign/change/clear Open Favorites hotkey; verify opening from the game and login screen, and no handling after disabling the plugin.
- [ ] Add favorites shows enabled plugins that have sidebar panels.
- [ ] Searching and adding several panels saves the intended shortcuts.
- [ ] Clicking each favorite opens the correct original panel.
- [ ] Grabbing/dragging reorders favorites without opening a panel.
- [ ] Grip selection plus Up/Down/Remove works at both ends of the list.
- [ ] Restarting RuneLite preserves favorites and their order.
- [ ] Disabling and re-enabling a target plugin preserves its favorite/position.
- [ ] Disabling and re-enabling Sidebar Favorites preserves its saved list.
- [ ] Switching RuneLite configuration profiles shows each profile's favorites.
- [ ] Native panel activation, navigation history, and keyboard navigation work.
- [ ] Resizing the client and switching sidebar column counts works.
- [ ] Scroll/drag works with more favorites than fit in the panel.

The automated suite does not establish Plugin Hub approval or cover every
third-party panel and operating system.

## Settings view and individual hotkeys

Live checks pending for this update:
- [ ] Gear opens settings; changes in either interface appear in the other.
- [ ] Profile changes update settings and cancel pending key capture.
- [ ] Per-favorite keybind survives restart, move, and disable/re-enable.
- [ ] Duplicate shortcuts are refused, including the main shortcut.
- [ ] Native settings conflicts give the main shortcut precedence.
- [ ] Removed/unavailable favorites do not consume shortcuts.

Saved favorites now use format version 2 with key codes and modifiers. Version 1
is read without bindings and upgraded on the next edit. Older plugin versions
will treat version 2 as unreadable rather than overwriting the saved list.
