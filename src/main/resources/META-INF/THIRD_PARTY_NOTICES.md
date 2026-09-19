# Third-party notices

## Sidebar Organizer

Panel discovery and the development setup adapt work from
[Sidebar Organizer](https://github.com/Brettgod1355/runelite-sidebar-organizer),
commit `91f62803e6f05c0d2cccf67390b327dd7b4ac65d`, under the BSD 2-Clause license.
The original copyright and license are retained in `PanelCatalog.java`.
Saved ordering and drag interactions also build on lessons from that project.
The toolbar UI replacement and toolbar reordering implementation are not used.

## RuneLite

The build and launcher follow the official
[example plugin](https://github.com/runelite/example-plugin). RuneLite is a
build/runtime dependency and is not bundled in the plugin JAR. Existing panel
icons are read from the running client and are not copied into this repository.
Sidebar Favorites is an independent project, not an official RuneLite product.

## Gradle wrapper

The wrapper scripts, JAR, and properties originate from the RuneLite example
plugin, commit `5370caa0f5f6a5bba4fbb42931722ca535ad3fd5`, via Sidebar Organizer.
Gradle is licensed under Apache 2.0; see `licenses/Apache-2.0.txt` and the notices
retained in the scripts and wrapper. This development tooling is not included
in the plugin JAR.
