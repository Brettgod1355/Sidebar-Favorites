# Testing

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

- [ ] The gold star appears near the end of the native sidebar.
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
