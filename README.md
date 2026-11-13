# Random Gun Drops

A Forge 1.20.1 mod for **TACZ (Timeless and Classics Zero)**. Breaking a block drops a random TACZ gun instead of the
block's normal items. It reads TACZ's own gun list, so guns from the base game and **every installed TACZ addon gunpack**
(like Omega Guns) are included.

## Press M for the menu
- Turn the mod on or off
- Same gun per block type, or a new random gun every break
- Drop chance slider (0 to 100%)
- Also keep the normal drops
- Also drop matching ammo (on by default) and a random attachment (off by default)
- Reroll which gun each block gives

Changing settings needs operator permission on a server. In single player you can always change them. You can rebind
the key under Options > Controls > Random Gun Drops.

## Install
Put `random_gun_drops-1.1.0.jar` in `mods/` with Forge 1.20.1 (47.3.0 or newer) and TACZ.

## Build
Push to GitHub and open Actions > Build mod, then download `random-gun-drops-mod`. Or run `gradle build` with JDK 17.

## Compatibility
The mod talks to TACZ only through its public lookup API, using reflection, so it needs no specific TACZ version to load.
Anything TACZ has registered shows up automatically: base guns, addon mods, gunpacks (including Omega Guns), ammo and attachments.
Blocks from any other mod also work, because it uses Forge's global loot system.
If TACZ renames one of those lookups in a future version, the mod logs a warning and leaves normal drops alone instead of crashing.

## Known limits
- Guns drop with their ID and first fire mode, but empty. Use the matching ammo to load them.
- Chests, mobs and other non-block loot are not changed.
- On a dedicated server the menu shows the client's copy of the settings, not the server's.
- Settings are also in `config/random_gun_drops-common.toml`.
