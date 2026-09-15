# Development

Install JDK 17, clone this repository, and open its `build.gradle` in IntelliJ IDEA.
Wait for Gradle import to finish. Run:

```sh
./gradlew run
```

On Windows:

```powershell
.\gradlew.bat run
```

Or create an IntelliJ **Application** run configuration:

| Field | Value |
| --- | --- |
| Name | Sidebar Favorites Test |
| JDK | 17 |
| Module classpath | `sidebar-favorites.test` |
| Main class | `com.sidebarfavorites.SidebarFavoritesLauncher` |
| VM options | `-ea` |
| Program arguments | `--developer-mode --debug` |
| Working directory | This repository's checkout |

Enable **Sidebar Favorites** in the development client's plugin list. You can
exercise panel shortcuts without logging into the game. The launcher uses the
standard RuneLite data directory unless you configure a separate one.

Run automated tests and build the plugin JAR with:

```sh
./gradlew test build
```

The default dependency is RuneLite's latest release. To reproduce a version:

```sh
./gradlew test build -PruneLiteVersion=1.12.38
```

## Review and compatibility

See [implementation notes](DESIGN.md) and [testing](TESTING.md) for integration
details, verification results, and the remaining manual checks.
