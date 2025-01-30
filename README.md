# DeluxeCombatAddon64
A spigot plugin that extends DelxueCombat's togglepvp feature.

Created by tbm00 for play.mc64.wtf.

## Features
- Merge DeluxeCombat's `/grace disable` command into `/togglepvp`
- Define limitations for DeluxeCombat's `/togglepvp`
- Prevent respawn anchor explosion while nearby players have pvp disabled

## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **DeluxeCombat**: REQUIRED

## Commands
#### Player Commands
- `/togglepvp`
    - 1st Ever Use: disables your newbie protection (grace period)
    - All Subsequent Uses: switches your PVP status (enabled<->disabled)

#### Admin Commands
- `/togglepvp <player>`
    - 1st Ever Use: disables player's newbie protection (grace period)
    - All Subsequent Uses: switches player's PVP status (enabled<->disabled)
- `/togglepvp <player> [on/off]` Turn player's PVP status on/enabled or off/disabled

## Permissions
#### Player Permissions
- `deluxecombataddon64.toggle.self` Ability to use /togglepvp command on yourself *(default: everyone)*.

#### Admin Permissions
- `deluxecombataddon64.toggle.others` Ability to use /togglepvp command on others *(default: op)*.

## Config
```
```