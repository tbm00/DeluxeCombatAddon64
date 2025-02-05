# DeluxeCombatAddon64
A spigot plugin that extends DelxueCombat's togglepvp feature.

Created by tbm00 for play.mc64.wtf.

## Features
- Merge DeluxeCombat's `/grace disable` & `/togglepvp` command into `/pvp`.
- Define use murder, combat, and join cooldowns toggling pvp.
- Prevent respawn anchor explosion while nearby players have pvp disabled.

## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **DeluxeCombat**: REQUIRED

## Note for Admins
This plugin leaves the original `/togglepvp` command offered by DeluxeCombat untouched. To disable/reroute it, add the following to your `commands.yml`: 
```
aliases:
  togglepvp:
  - pvp
```

## Commands
#### Player Commands
- `/pvp`
    - 1st Ever Use: disables your newbie protection (grace period)
    - All Subsequent Uses: switches your PVP status (enabled<->disabled)

#### Admin Commands
- `/pvp <player>`
    - 1st Ever Use: disables player's newbie protection (grace period)
    - All Subsequent Uses: switches player's PVP status (enabled<->disabled)
- `/pvp <player> [on/off]` Turn player's PVP status on/enabled or off/disabled

## Permissions
#### Player Permissions
- `deluxecombataddon64.toggle.self` Ability to use /pvp command on yourself *(default: everyone)*.

#### Admin Permissions
- `deluxecombataddon64.toggle.others` Ability to use /pvp command on others *(default: op)*.

## Config
```
# DeluxeCombatAddon64 v0.0.1-beta by @tbm00
# https://github.com/tbm00/DeluxeCombatAddon64

enabled: true

# Prevents respawn anchor usage if player or nearby players have pvp disabled
fixes:
  anchorExplosionPvpCheck: true

togglePvpCommand:
  enabled: true
  chat:
    prefix: "&8[&f-&8] &f"
    enabledMessage: "You &a&nenabled&r your pvp!"
    enabledByOtherMessage: "Your pvp has been &a&nenabled&r!"
    disabledMessage: "You &c&ndisabled&r your pvp!"
    disabledByOtherMessage: "Your pvp has been &c&ndisabled&r!"
    disabledGraceMessage: "" # "You &ndisabled&r your newbie protection, your PVP is &a&nenabled&r!"
    disabledGraceByOtherMessage: "" # "&fYour newbie protection has been &ndisabled&r, your PVP is &a&nenabled&r!"
    preventedToggleInWorldsMessage: "&cYou cannot toggle pvp in this world!"
    preventedToggleInCombatMessage: "&cYou cannot toggle pvp during combat!"
    preventedToggleAfterCombatMessage: "&cYou cannot toggle pvp after recent combat -- please wait &6<time_left>&c!"
    preventedToggleAfterMurderMessage: "&cYou cannot toggle pvp after killing someone -- please wait &6<time_left>&c!"
    preventedToggleAfterJoinMessage: "&cYou cannot toggle pvp after recently connecting -- please wait &6<time_left>&c!"
    preventedToggleAfterToggleMessage: "&cYou cannot toggle pvp after recently toggling -- please wait &6<time_left>&c!"
    preventedToggleAfterBonusMessage: "&cYou cannot toggle pvp -- please wait &6<time_left>&c!"
  preventedInWorlds: []
    #- "world_nether"
  preventedInCombat: true
  preventedAfterCombat:
    enabled: true
    time: 30 # seconds
  preventedAfterMurder:
    enabled: true
    time: 300 # seconds
  preventedAfterJoin:
    enabled: true
    time: 120 # seconds
  preventedAfterEnable:
    enabled: true
    time: 30 # seconds
  preventedAfterDisable:
    enabled: true
    time: 15 # seconds
```