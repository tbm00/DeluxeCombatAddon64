# DeluxeCombatAddon64
A spigot plugin that extends DelxueCombat's togglepvp feature.

Created by tbm00 for play.mc64.wtf.

## Features
- Merge DeluxeCombat's `/grace disable` & `/togglepvp` command into `/pvp`.
- Define join, combat, death, murder, combatlog, and more cooldowns for toggling pvp.
- Give players bounty protection timers.
- Prevent respawn anchor explosion while nearby players have pvp disabled.

## Dependencies
- **Java 17+**: REQUIRED
- **Spigot 1.18.1+**: UNTESTED ON OLDER VERSIONS
- **DeluxeCombat**: REQUIRED

## Commands
#### Player Commands
- `/pvp` - Toggle pvp with prevent checks
    - 1st Ever Use: disables your newbie protection (if active)
    - All Subsequent Uses: switches your PVP status (enabled<->disabled)
- `/bountyprot` - Returns your current bounty protection time if active

#### Admin Commands
- `/pvp <player>` - Toggle pvp without prevent checks
    - 1st Ever Use: disables player's newbie protection (if active)
    - All Subsequent Uses: switches player's PVP status (enabled<->disabled)
- `/pvp <player> [on/off]` Turn player's PVP status on/enabled or off/disabled
- `/pvp <player> getCooldown` Get a player's cooldown time
- `/pvp <player> giveCooldown <TYPE> <seconds>` Give a player a cooldown
- `/pvp <player> clearCooldown` Clear a player's cooldown
- `/bountyprot <player> get` Get a player's bounty protection time
- `/bountyprot <player> set <seconds>` Set a player's bounty protection time
- `/bountyprot <player> add <seconds>` Add time to player's bounty protection
- `/bountyprot <player> clear` Clear a player's cooldown

## Permissions
#### Player Permissions
- `deluxecombataddon64.toggle.self` Ability to toggle your pvp and grace protection with /pvp *(default: everyone)*.

#### Admin Permissions
- `deluxecombataddon64.toggle.others` Ability to toggle others' pvp and grace protection with /pvp *(default: op)*.
- `deluxecombataddon64.managecooldowns` Ability to get/give/clear players' toggle pvp cooldown timers with /pvp *(default: op)*.
- `deluxecombataddon64.manageprotections` Ability to get/give/clear players' toggle pvp cooldown timers with /pvp *(default: op)*.

## Note for Server Admins
This plugin leaves the original `/togglepvp` command offered by DeluxeCombat untouched. To disable/reroute it, add the following to your `<server_directory>/commands.yml`: 
```
aliases:
  togglepvp:
  - pvp
```

## Config
```
# DeluxeCombatAddon64 v0.0.8-beta by @tbm00
# https://github.com/tbm00/DeluxeCombatAddon64

enabled: true

# Prevents respawn anchor usage if player or nearby players have pvp disabled
fixes:
  anchorExplosionPvpCheck: true

bountyProtectionCommand:
  enabled: true
  saveMapOnPluginDisable: true
  chat:
    prefix: "&8[&f-&8] &f"
    protectionEnabledMessage: "You are now protected from bounties for &a<time_left>&r!"
    currentProtectionTimeMessage: "You are protected from bounties for &a<time_left>&r!"
    noCurrentProtectionMessage: "&cYou are not protected from bounties!"
    cannotSetBountyMessage: "&cThat player is protected from bounties for &6<time_left>&c!"

togglePvpCommand:
  enabled: true
  saveMapOnPluginDisable: true
  chat:
    prefix: "&8[&f-&8] &f"
    enabledMessage: "You &a&nenabled&r your pvp!"
    enabledByOtherMessage: "Your pvp has been &a&nenabled&r!"
    disabledMessage: "You &c&ndisabled&r your pvp!"
    disabledByOtherMessage: "Your pvp has been &c&ndisabled&r!"
    disabledGraceMessage: "" # "You &ndisabled&r your newbie protection, your PVP is &a&nenabled&r!"
    disabledGraceByOtherMessage: "" # "Your newbie protection has been &ndisabled&r, your PVP is &a&nenabled&r!"
    preventedToggleInWorldsMessage: "&cYou cannot toggle pvp in this world!"
    preventedToggleWithBountyMessage: "&cYou cannot toggle pvp with an active bounty!"
    preventedToggleInCombatMessage: "&cYou cannot toggle pvp during combat!"
    preventedToggleAfterCombatMessage: "&cYou cannot toggle pvp after recent combat -- please wait &6<time_left>&c!"
    preventedToggleAfterMurderMessage: "&cYou cannot toggle pvp after killing someone -- please wait &6<time_left>&c!"
    preventedToggleAfterDeathMessage: "&cYou cannot toggle pvp after recently dying -- please wait &6<time_left>&c!"
    preventedToggleAfterCombatLogMessage: "&cYou cannot toggle pvp after combat-logging -- please wait &6<time_left>&c!"
    preventedToggleAfterJoinMessage: "&cYou cannot toggle pvp after recently connecting -- please wait &6<time_left>&c!"
    preventedToggleAfterToggleMessage: "&cYou cannot toggle pvp after recently toggling -- please wait &6<time_left>&c!"
    preventedToggleAfterSetBountyMessage: "&cYou cannot toggle pvp after recently setting a bounty -- please wait &6<time_left>&c!"
    preventedToggleAfterBonusMessage: "&cYou cannot toggle pvp -- please wait &6<time_left>&c!"
    forceEnabledAfterMessage: "Your pvp was &are-&nenabled&r!"
  forceEnabledAfterDeath: true
  forceEnabledAfterSetBounty: true
  preventedInWorlds: []
    #- "world_nether"
  preventedWithBounty: true
  preventedInCombat: true
  preventedAfterCombat:
    enabled: true
    time: 30 # seconds
  preventedAfterMurder:
    enabled: true
    time: 600 # seconds
  preventedAfterPVPDeath:
    enabled: true
    time: 120 # seconds
  preventedAfterPVEDeath:
    enabled: true
    time: 120 # seconds
  preventedAfterCombatLog:
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
  preventedAfterSetBounty:
    enabled: true
    time: 600 # seconds
```