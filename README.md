# Incog-Homes

A homes plugin built for **Purpur 26.2** (Minecraft 26.2 "Chaos Cubed", Java 25).

## Building

This project depends on the Purpur API, which lives on PurpurMC's own Maven
repository rather than Maven Central. Build it wherever you have normal
internet access:

```
mvn clean package
```

The resulting jar is `target/Incog-Homes-1.0.0.jar`. Drop it in your
server's `plugins/` folder.

The plugin is compiled targeting Java 21 bytecode, which runs fine on the
Java 25 JVM that Purpur 26.2 requires.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/sethome [name]` | Set a home at your current location. Blocked if the spot is dangerous. | `incoghomes.sethome` |
| `/forcesethome [name]` | Same as above, but ignores danger checks entirely. | `incoghomes.sethome.force` |
| `/home [name]` | Teleport to one of your homes (subject to cooldown/warmup). | `incoghomes.home` |
| `/homes` | List your own homes and their locations. | `incoghomes.home` |
| `/delhome <name>` | Delete one of your homes. | `incoghomes.delhome` |
| `/homeadmin revoke <player> <home>` | Remove a specific home from a player's list. Tells you the home's location before removing it. | `incoghomes.admin` |
| `/homeadmin list <player>` | List every home a player has, with locations and set-times. | `incoghomes.admin` |

If no name is given to `/sethome`, `/forcesethome`, or `/home`, the
configured `home.default-home-name` (default: `home`) is used.

## Danger checks (`/sethome` vs `/forcesethome`)

A plain `/sethome` is blocked if any of these are true (each is
individually toggleable in `config.yml`):

- You're standing in lava or actively on fire.
- There's no solid ground within `fall-check-distance` blocks below you
  (so teleporting back wouldn't drop you into a fall or the void).
- A hostile mob is within `hostile-mob-radius` blocks.
- You took damage from another entity within the last
  `combat-tag-seconds` seconds.

`/forcesethome` (permission `incoghomes.sethome.force`) skips all of this.
Players with `incoghomes.bypass.danger` always pass the checks, even on a
plain `/sethome`.

## Cooldowns, warmup, and limits

- `home.cooldown-seconds` — delay between successful `/home` uses.
  `incoghomes.bypass.cooldown` skips it.
- `home.teleport-warmup-seconds` — countdown before a `/home` teleport
  actually happens; cancellable on move and/or damage via
  `cancel-warmup-on-move` / `cancel-warmup-on-damage`.
- `home.default-limit` plus `home.permission-limits` — how many homes a
  player can have; the highest limit they have permission for wins.
  `incoghomes.bypass.limit` removes the cap entirely.
- `home.confirm-overwrite` — if true, setting a home over an existing
  name requires running the same command again within
  `confirm-overwrite-seconds`.
- `home.disabled-worlds` — worlds where homes can't be set or used.

## Logging

Every action that touches the logs is also printed to console, per spec.

**Actions log** (`plugins/Incog-Homes/logs/actions.log` by default):

```
[Incog-Homes] =|= 'Steve' (a1b2c3d4-...) | Set a home base (~world:120,64,-30~) <|> 2026-08-07 14:02:11
[Incog-Homes] =|= 'Steve' {a1b2c3d4-...} | Teleported to base (~world:120,64,-30~) <|> 2026-08-07 14:05:44
[Incog-Homes] =|= 'Steve' (a1b2c3d4-...) | Deleted a home base (~world:120,64,-30~) <|> 2026-08-07 15:00:02
[Incog-Homes] =|= 'Steve' (a1b2c3d4-...) | Home base (~world:120,64,-30~) was revoked by admin 'Alex' <|> 2026-08-07 15:10:19
```

(`Force-set a home` is used instead of `Set a home` when `/forcesethome`
is used.)

**Homelist log** (`plugins/Incog-Homes/logs/homelist.log`) — a standing
snapshot of every home currently on the server, rewritten whenever a home
is created, overwritten, deleted, or revoked:

```
'Steve' (a1b2c3d4-...) =|= base <|> world:120,64,-30 /|\ 2026-08-07 14:02:11
```

Both file paths and the timestamp format are configurable under
`logging:` in `config.yml`.

## Notes

- Homes are stored in `plugins/Incog-Homes/homes.yml`, keyed by player
  UUID (so name changes don't break anything).
- `/homeadmin` resolves target players by name via Bukkit's offline
  player lookup, so it works for players who are currently offline.
- Credit: Siegeisok67 and the Incog Dev Team.
