# Gaming Time Limit

![Gaming Time Limit icon](common/src/main/resources/assets/gamingtimelimit/icon.png)

Client-side Minecraft mod that gives you a daily play budget and counts it down while you are actually in-game. When your time runs out, it can either kick you automatically or just keep showing `00:00` while you continue playing.

This project targets Fabric, Forge, and NeoForge from one shared codebase.

## What It Does

- Sets a daily play limit in minutes.
- Tracks time in singleplayer and multiplayer.
- Shows remaining time in the TAB overlay without replacing the existing footer.
- Optionally kicks or disconnects you when the timer reaches zero.
- Blocks world/server entry when you are already out of time and auto-kick is enabled.
- Resets automatically at local system midnight.

## Settings

Open the settings screen with:

```text
/gamingtimelimit
/gtl
```

Available options:

- `Daily Limit (Minutes)`
- `Kick Automatically At Zero`
- `Count While Singleplayer Is Paused`

Default values:

- `Daily Limit`: `60`
- `Kick Automatically At Zero`: `true`
- `Count While Singleplayer Is Paused`: `true`

Settings are stored in:

```text
config/gamingtimelimit-client.json
```

## Loader Notes

- Fabric: supports Mod Menu where that target version has a compatible Mod Menu build. On newer targets where Mod Menu is unavailable, the command still works.
- Forge: the config screen is exposed through the built-in Mods screen.
- NeoForge: the config screen is exposed through the built-in Mods screen.
- Servers do not need this mod installed.

## Supported Versions

Support is driven by the files in [`versionProperties`](versionProperties).

Current targets in this repo:

- `1.20.1`: Fabric, Forge
- `1.20.4`: Fabric, Forge, NeoForge
- `1.21`: Fabric, Forge, NeoForge
- `1.21.1`: Fabric, Forge, NeoForge
- `1.21.4`: Fabric, Forge, NeoForge
- `1.21.11`: Fabric, Forge, NeoForge
- `26.1`: Fabric, Forge
- `26.1.1`: Fabric, Forge
- `26.1.2`: Fabric

## Building

This is a Manifold + Unimined multi-loader project.

Examples:

```bash
./gradlew :fabric:assemble -Pmc_ver=26.1.2
./gradlew :forge:assemble -Pmc_ver=1.21.4
./gradlew :neoforge:assemble -Pmc_ver=1.21.4
```

On Windows:

```powershell
.\gradlew.bat :fabric:assemble "-Pmc_ver=26.1.2"
.\gradlew.bat :forge:assemble "-Pmc_ver=1.21.4"
.\gradlew.bat :neoforge:assemble "-Pmc_ver=1.21.4"
```

Useful dev commands:

```powershell
.\gradlew.bat :fabric:runClient "-Pmc_ver=26.1.2"
.\gradlew.bat :common:test "-Pmc_ver=1.21.4"
```

Notes:

- `mc_ver` must match one of the files in `versionProperties`.
- `assemble` is the safest publish/build target for loader jars in this repo.
- Java requirements depend on the selected Minecraft version and are defined per target in `versionProperties`.

## Release Automation

The repo includes a GitHub Actions workflow for Modrinth publishing:

- [publish-modrinth.yml](.github/workflows/publish-modrinth.yml)

Required GitHub secrets:

- `MODRINTH_PROJECT_ID`
- `MODRINTH_TOKEN`

The workflow discovers supported Minecraft versions automatically from `versionProperties` and uploads the appropriate loader jars for each target.

## Project Layout

- [`common`](common): shared logic, UI, translations, mixins, tests
- [`fabric`](fabric): Fabric entrypoints and Mod Menu integration
- [`forge`](forge): Forge entrypoint and Mods screen config integration
- [`neoforge`](neoforge): NeoForge entrypoint and Mods screen config integration
- [`versionProperties`](versionProperties): per-Minecraft-version loader and dependency matrix

## License

MIT
