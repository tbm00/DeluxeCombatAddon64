package dev.tbm00.spigot.deluxecombataddon64.command;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class TogglePvpCmd implements TabExecutor {
    private final DeluxeCombatAddon64 javaPlugin;
    private final ConfigHandler configHandler;
    private final EntryManager entryManager;
    private final DCHook dcHook;
    private final String PERMISSION_MANAGE_COOLDOWNS = "deluxecombataddon64.managecooldowns";
    private final String PERMISSION_TOGGLE_OTHERS = "deluxecombataddon64.toggle.others";
    private final String PERMISSION_TOGGLE_SELF = "deluxecombataddon64.toggle.self";
    private final Set<String> COOLDOWN_TYPES = new HashSet<>(Set.of("COMBAT", "MURDER", "DEATH", "COMBATLOG", "JOIN", "TOGGLE", "BONUS"));

    public TogglePvpCmd(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager, DCHook dcHook) {
        this.entryManager = entryManager;
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    /**
     * Handles the "/pvp" command for toggling PvP and giving/clearing cooldowns.
     * 
     * @param sender the command sender
     * @param consoleCommand the command being executed
     * @param label the label used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean onCommand(CommandSender sender, Command consoleCommand, String label, String[] args) {

        // "/pvp <username> giveCooldown <type> <seconds>" - admin command 
        if ((args.length==4) && hasPermission(sender, PERMISSION_MANAGE_COOLDOWNS)) {
            if (args[1].equalsIgnoreCase("giveCooldown"))
                return handleGiveCooldown(sender, getPlayer(args[0]), args[2].toUpperCase(), Integer.valueOf(args[3]));
        } 
        
        // "/pvp <username> [clearCooldown/getCooldown]" - admin command 
        if ((args.length==2) && hasPermission(sender, PERMISSION_MANAGE_COOLDOWNS)) {
            if (args[1].equalsIgnoreCase("getCooldown"))
                return handleGetCooldown(sender, getPlayer(args[0]));
            if (args[1].equalsIgnoreCase("clearCooldown"))
                return handleClearCooldown(sender, getPlayer(args[0]));
        } 

        // "/pvp <username> [on/off]" - admin command
        if ((args.length==1 || args.length == 2) && hasPermission(sender, PERMISSION_TOGGLE_OTHERS)) { 
            String newStatus = null;
            if (args.length==2) {
                if (args[1].equalsIgnoreCase("on")||args[1].equalsIgnoreCase("enable"))
                    newStatus = "enable";
                else if (args[1].equalsIgnoreCase("off")||args[1].equalsIgnoreCase("disable"))
                    newStatus = "disable"; 
                else {
                    sendMessage(sender, "&cSecond argument must be 'on' or 'off'!");
                    return false;
                }
            }
            return handleTogglePvpOthers(sender, getPlayer(args[0]), newStatus);
        } 

        // "/pvp" - player command
        if (args.length==0 && (sender instanceof Player) && hasPermission(sender, PERMISSION_TOGGLE_SELF)) { 
            Player target = (Player) sender;
            return handleTogglePvpSelf(target);
        } 
    
        return false;
    }

    /**
     * Handles the command for giving a cooldown to a player.
     * 
     * @param sender the command sender
     * @param target the target player
     * @param type the type of cooldown (e.g., "COMBAT")
     * @param seconds the duration of the cooldown in seconds
     * @return true if the cooldown was successfully given, false otherwise
     */
    private boolean handleGiveCooldown(CommandSender sender, Player target, String type, Integer seconds) {
        if (target == null) {
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } else if (!COOLDOWN_TYPES.contains(type.toUpperCase())) {
            sendMessage(target, "&cCouldn't find cooldown type!");
            return false;
        } else if (seconds==null) {
            sendMessage(target, "&cCouldn't parse seconds integer!");
            return false;
        }

        entryManager.addMapTime(target, type.toUpperCase(), seconds*20);
        sendMessage(sender, "&fGave " + entryManager.getFormattedTime(seconds) + " " + type + " cooldown to " + target.getName()
                    + ". &7Their current cooldown: " + entryManager.getHighestTypeAndTime(target.getName()));
        return true;
    }

    /**
     * Handles the command for getting a player's cooldown.
     * 
     * @param sender the command sender
     * @param target the target player
     * @return true if the target was found and cooldown was sent, false otherwise
     */
    private boolean handleGetCooldown(CommandSender sender, Player target) {
        if (target == null) {
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } 
        sendMessage(sender, target.getName() + "'s current cooldown: &a" + entryManager.getHighestTypeAndTime(target.getName()));
        return true;
    }

    /**
     * Handles the command for clearing a player's cooldowns.
     * 
     * @param sender the command sender
     * @param target the target player
     * @return true if the cooldowns were successfully cleared, false otherwise
     */
    private boolean handleClearCooldown(CommandSender sender, Player target) {
        if (target == null) {
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } 
        
        if (entryManager.deletePlayerEntry(target.getName())) {
            sendMessage(sender, "&fCleared " + target.getName() + "'s cooldown map. &7Their current cooldown: " 
                        + entryManager.getHighestTypeAndTime(target.getName()));
            return true;
        } else {
            sendMessage(sender, "&cError occured clearing " + target.getName() + "'s cooldown map. &7Their current cooldown: " 
            + entryManager.getHighestTypeAndTime(target.getName()));
            return true;
        } 
    }

    /**
     * Handles the command for toggling PvP for the sender (self).
     * 
     * @param target the player sending the command
     * @return true if PvP was successfully toggled, false otherwise
     */
    private boolean handleTogglePvpSelf(Player target) {
        // Check if command should be prevented because player is in disabled world
        if (preventUsageInWorlds(target)) return true;

        // Disable newbie protection (grace) if applied
        if (disableGraceProtection(target)==1) return true;

        // Check if command should be prevented because player is in combat
        if (preventUsageInCombat(target)) return true;

        // Check if command should be prevented for all other reasons
        if (preventUsageCooldown(target)) return true;

        // Switch pvp status
        boolean currentPvpStatus = dcHook.hasPvPEnabled(target);
        if (currentPvpStatus) { // if enabled, disable it
            dcHook.togglePvP(target, false);
            sendMessage(target, configHandler.getDisabledMessage());
            if (configHandler.isPreventedAfterDisable())
                entryManager.addMapTime(target, "TOGGLE", configHandler.getPreventedAfterDisableTicks());
            return true;
        } else { // if disabled, enable it
            dcHook.togglePvP(target, true);
            sendMessage(target, configHandler.getEnabledMessage());
            if (configHandler.isPreventedAfterEnable())
                entryManager.addMapTime(target, "TOGGLE", configHandler.getPreventedAfterEnableTicks());
            return true;
        }
    }

    /**
     * Handles the command for toggling PvP for another player.
     * 
     * @param sender the command sender
     * @param target the target player
     * @param newStatus the desired PvP status ("enable" or "disable")
     * @return true if PvP was successfully toggled, false otherwise
     */
    private boolean handleTogglePvpOthers(CommandSender sender, Player target, String newStatus) {
        if (newStatus!=null) {
            if (newStatus.equalsIgnoreCase("enable")) {
                dcHook.togglePvP(target, true);
                sendMessage(sender, "You enabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getEnabledByOtherMessage());
                return true;
            } if (newStatus.equalsIgnoreCase("disable")) {
                dcHook.togglePvP(target, false);
                sendMessage(sender, "You disabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getDisabledByOtherMessage());
                return true;
            } else {
                sendMessage(sender, "&cError changing " + target.getName() + "'s PVP!");
                return false;
            }
        } else {
            // Disable newbie protection (grace) if applied
            if (disableGraceProtection(sender, target)==1) return true;

            // Switch pvp status
            boolean currentPvpStatus = dcHook.hasPvPEnabled(target);
            if (currentPvpStatus) { // if enabled, disable it
                dcHook.togglePvP(target, false);
                sendMessage(sender, "You disabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getDisabledByOtherMessage());
                return true;
            } else { // if disabled, enable it
                dcHook.togglePvP(target, true);
                sendMessage(sender, "You enabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getEnabledByOtherMessage());
                return true;
            }
        }
    }

    /**
     * Disables the grace protection (newbie protection) for the given player.
     * 
     * @param target the player to disable grace protection for
     * @return 1 if grace protection was removed, 2 if there was an error, or 0 if no protection was present
     */
    private int disableGraceProtection(Player target) {
        if (dcHook.hasProtection(target)) {
            boolean disabledGrace = sudoCommand(target, "grace disable");
            if (disabledGrace) {
                sendMessage(target, configHandler.getDisabledGraceMessage());
                return 1; // 1 - had newbie prot, it was removed
            } else {
                sendMessage(target, "&cError occured while disabling your newbie protection!");
                return 2; // 2 - had newbie prot, it was not removed due to error
            }
        } else return 0; // 0 - had no newbie prot, did nothing
    }

    /**
     * Disables the grace protection (newbie protection) for another player.
     * 
     * @param sender the command sender
     * @param target the player whose grace protection will be disabled
     * @return 1 if grace protection was removed, 2 if there was an error, or 0 if no protection was present
     */
    private int disableGraceProtection(CommandSender sender, Player target) {
        if (dcHook.hasProtection(target)) {
            boolean disabledGrace = runCommand("grace disable " + target.getName());
            if (disabledGrace) {
                sendMessage(sender, "You disabled " + target.getName() + "'s newbie protection (aka grace period)!");
                sendMessage(target, configHandler.getDisabledGraceByOtherMessage());
                return 1; // 1 - had newbie prot, it was removed
            } else {
                sendMessage(sender, "&cError occoured while disabling " + target.getName() + "'s newbie protection!");
                return 2; // 2 - had newbie prot, it was not removed due to error
            }
        } else return 0; // 0 - had no newbie prot, did nothing
    }

    /**
     * Prevents the usage of the PvP toggle command if the player is in a disabled world.
     * 
     * @param target the player executing the command
     * @return true if the command should be prevented, false otherwise
     */
    private boolean preventUsageInWorlds(Player target) {
        if (configHandler.getPreventedWorlds().contains(target.getWorld().getName())) {
            sendMessage(target, configHandler.getPreventedToggleInWorldsMessage());
            return true;
        } return false;
    }

    /**
     * Prevents the usage of the PvP toggle command if the player is in combat.
     * 
     * @param target the player executing the command
     * @return true if the command should be prevented, false otherwise
     */
    private boolean preventUsageInCombat(Player target) {
        if (dcHook.isInCombat(target) && configHandler.isPreventedInCombat()) {
            sendMessage(target, configHandler.getPreventedToggleInCombatMessage());
            return true;
        } return false;
    }

    /**
     * Prevents the usage of the PvP toggle command if the player has an active cooldown.
     * 
     * @param target the player executing the command
     * @return true if the command should be prevented, false otherwise
     */
    private boolean preventUsageCooldown(Player target) {
        String tickAndType = entryManager.getHighestTickAndType(target.getName());
        String[] pair = tickAndType.split("\\ ");
        Integer highest_map_ticks = Integer.parseInt(pair[0]);
        if (highest_map_ticks<1) return false;

        int current_play_ticks;
        try {
            current_play_ticks = target.getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_ticks = target.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                current_play_ticks = 0;
            }
        }

        if (highest_map_ticks>current_play_ticks+1) {
            int time_difference = (highest_map_ticks-current_play_ticks)/20;
            String msg = configHandler.getPreventedMessage(pair[1]).replace("<time_left>", entryManager.getFormattedTime(time_difference));
            sendMessage(target, msg);
            return true;
        } else {
            entryManager.deletePlayerEntry(target.getName());
            return false;
        }
    }

    /**
     * Retrieves a player by their name.
     * 
     * @param arg the name of the player to retrieve
     * @return the Player object, or null if not found
     */
    private Player getPlayer(String arg) {
        return javaPlugin.getServer().getPlayer(arg);
    }

    /**
     * Checks if the sender has a specific permission.
     * 
     * @param sender the command sender
     * @param perm the permission string
     * @return true if the sender has the permission, false otherwise
     */
    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }

    /**
     * Sends a message to a target CommandSender.
     * 
     * @param target the CommandSender to send the message to
     * @param string the message to send
     */
    private void sendMessage(CommandSender target, String string) {
        if (!string.isBlank())
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getChatPrefix() + string)));
    }

    /**
     * Executes a command as the console.
     * 
     * @param command the command to execute
     * @return true if the command was successfully executed, false otherwise
     */
    private boolean runCommand(String command) {
        ConsoleCommandSender console = javaPlugin.getServer().getConsoleSender();
        try {
            return Bukkit.dispatchCommand(console, command);
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception running command " + command + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes a command as a specific player.
     * 
     * @param target the player to execute the command as
     * @param command the command to execute
     * @return true if the command was successfully executed, false otherwise
     */
    private boolean sudoCommand(Player target, String command) {
        try {
            return Bukkit.dispatchCommand(target, command);
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles tab completion for the "/pvp" command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command consoleCommand, String alias, String[] args) {
        if (!(hasPermission(sender, PERMISSION_TOGGLE_OTHERS)||hasPermission(sender, PERMISSION_MANAGE_COOLDOWNS))) return null;
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        } else if (args.length == 2) {
            list.add("enable");
            list.add("disable");
            list.add("getCooldown");
            list.add("giveCooldown");
            list.add("clearCooldown");
        } else if (args.length == 3) {
            for (String type : COOLDOWN_TYPES) {
                list.add(type);
            }
        } else if (args.length == 4) {
            list.add("30");
            list.add("60");
            list.add("120");
            list.add("180");
            list.add("240");
            list.add("300");
            list.add("600");
        }
        return list;
    }
}