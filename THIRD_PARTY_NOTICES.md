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

## Discord and GitHub logos (trademarks, used as link buttons)

The two buttons beside the sidebar's title show the brands' own logos, taken from their official
brand pages on 2026-10-04 and only scaled down, never recoloured or redrawn:

- Discord: `discord_white.png` and `discord_blurple.png` are `Discord-Symbol-White.png` and
  `Discord-Symbol-Blurple.png` from the white and colour "Symbol" downloads at
  <https://discord.com/branding>, which lists white and Blurple among the logo's colours.
- GitHub: `github_white.png` is `GitHub_Invertocat_White.png` from `GitHub_Logos.zip` at
  <https://brand.github.com/foundations/logo>. The pack has no green file, so `github_green.png` is
  the green Invertocat cut from the same page's colour illustration
  (<https://brand.github.com/_next/static/media/logo-04.c5edeefa.png>), with the black around it made
  transparent and its pixels otherwise unchanged; the page allows the mark "in white, black, or in
  few cases grey or green", and its use "as a social button to link to your GitHub profile or project".

Discord and the Discord logo are trademarks of Discord Inc.; GitHub and the Invertocat are
trademarks of GitHub, Inc. They are used only to link to this plugin's own Discord server and GitHub
repository, and imply no endorsement by either company.

## Gradle wrapper

The wrapper scripts, JAR, and properties originate from the RuneLite example
plugin, commit `5370caa0f5f6a5bba4fbb42931722ca535ad3fd5`, via Sidebar Organizer.
Gradle is licensed under Apache 2.0; see `licenses/Apache-2.0.txt` and the notices
retained in the scripts and wrapper. This development tooling is not included
in the plugin JAR.
