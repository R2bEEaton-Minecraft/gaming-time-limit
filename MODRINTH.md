# Gaming Time Limit

Gaming Time Limit is a client-side Minecraft mod that gives you a daily play timer.

Set a limit in minutes for how long you want to play each day, and the mod will count it down while you are actually in a world or server. When your time runs out, it can either kick you automatically or just keep showing `00:00` while you continue playing.

## Features

- Daily Minecraft play limit in minutes
- Works in both singleplayer and multiplayer
- Remaining time shown in the TAB list
- Optional automatic disconnect when time reaches zero
- Blocks joining worlds or servers when you are already out of time
- Local daily reset at midnight
- Config screen available from a client command

## Settings

You can configure:

- `Daily Limit (Minutes)`
- `Kick Automatically At Zero`
- `Count While Singleplayer Is Paused`

Default settings:

- `Daily Limit`: `60` minutes
- `Kick Automatically At Zero`: `On`
- `Count While Singleplayer Is Paused`: `On`

## Commands

Open the settings screen with:

```text
/gamingtimelimit
/gtl
```

## Notes

- This is a client-side mod.
- Servers do not need to install it.
- The timer is stored locally on your client.
- On Fabric, Mod Menu integration is included where a compatible Mod Menu version exists.

## Supported Loaders

- Fabric on all supported versions
- Forge on `1.20.1`
- NeoForge on `1.21.11`
