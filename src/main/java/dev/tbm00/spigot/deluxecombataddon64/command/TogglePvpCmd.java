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
import dev.tbm00.spigot.deluxecombataddon64.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;

public class TogglePvpCmd implements TabExecutor {
    private final DeluxeCombatAddon64 javaPlugin;
    private final ConfigHandler configHandler;
    private final EntryManager entryManager;
    private final DCHook dcHook;
    private final String PERMISSION_GIVE_COOLDOWNS = "deluxecombataddon64.givecooldown";
    private final String PERMISSION_TOGGLE_OTHERS = "deluxecombataddon64.toggle.others";
    private final String PERMISSION_TOGGLE_SELF = "deluxecombataddon64.toggle.self";
    private final Set<String> COOLDOWN_TYPES = new HashSet<>(Set.of("JOIN", "TOGGLE", "COMBAT", "MURDER", "DEATH", "BONUS"));

    public TogglePvpCmd(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager, DCHook dcHook) {
        this.entryManager = entryManager;
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    public boolean onCommand(CommandSender sender, Command consoleCommand, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "&cThis command can only be run by a player!");
            return false;
        }

        // "/togglepvp" - player command
        if (args.length==0 && (sender instanceof Player) && hasPermission(sender, PERMISSION_TOGGLE_SELF)) { 
            Player target = (Player) sender;
            return handleTogglePvpSelf(target);
        } // "/togglepvp <username> [on/off]" - admin command
        else if ((args.length==1 || args.length == 2) && hasPermission(sender, PERMISSION_TOGGLE_OTHERS)) { 
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
        }  // "/togglepvp <username> giveCooldown <type> <seconds>" - admin command 
        else if ((args.length==4) && hasPermission(sender, PERMISSION_GIVE_COOLDOWNS)) {
            if (args[1].equalsIgnoreCase("giveCooldown"))
                return handleCooldownAdjustment(sender, getPlayer(args[0]), args[2].toUpperCase(), Integer.valueOf(args[3]));
        } return false;
    }

    private boolean handleCooldownAdjustment(CommandSender sender, Player target, String type, Integer seconds) {
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
        sendMessage(sender, "&7Added "+seconds+"sec to "+target.getName()+"'s "+type+" cooldown. Their highest cooldown is "
                    + entryManager.getHighestTimeAndType(target.getName()));
        return true;
    }

    private boolean handleTogglePvpSelf(Player target) {
        // Check if command should be prevented because of world
        if (preventUsageWorlds(target)) return true;

        // Disable newbie protection (grace) if applied
        boolean currentGraceStatus = dcHook.hasProtection(target);
        if (currentGraceStatus) {
            boolean disabledGrace = sudoCommand(target, "grace disable");
            if (disabledGrace) {
                sendMessage(target, configHandler.getDisabledGraceMessage());
                return true;
            } else {
                sendMessage(target, "&cError occured while disabling your newbie protection!");
                return false;
            }
        }

        // Check if command should be prevented for all other reasons
        if (preventUsage(target)) return true;

        // Switch pvp status
        boolean currentPvpStatus = dcHook.hasPvPEnabled(target);
        if (currentPvpStatus) { // if enabled, disable it
            dcHook.togglePvP(target, false);
            sendMessage(target, configHandler.getDisabledMessage());
            if (configHandler.isPreventedAfterDisable()) entryManager.addMapTime(target, "TOGGLE", configHandler.getPreventedAfterDisableTicks());
            return true;
        } else { // if disabled, enable it
            dcHook.togglePvP(target, true);
            sendMessage(target, configHandler.getEnabledMessage());
            if (configHandler.isPreventedAfterEnable()) entryManager.addMapTime(target, "TOGGLE", configHandler.getPreventedAfterEnableTicks());
            return true;
        }
    }

    private boolean handleTogglePvpOthers(CommandSender sender, Player target, String newStatus) {
        if (newStatus!=null) {
            if (newStatus.equalsIgnoreCase("enable")) {
                dcHook.togglePvP(target, true);
                sendMessage(sender, "You enabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getEnabledByOtherMessage());
                return true;
            }
            if (newStatus.equalsIgnoreCase("disable")) {
                dcHook.togglePvP(target, false);
                sendMessage(sender, "You disabled " + target.getName() + "'s PVP!");
                sendMessage(target, configHandler.getDisabledByOtherMessage());
                return true;
            }
            else {
                sendMessage(sender, "&cError changing " + target.getName() + "'s PVP!");
                return false;
            }
        } else {
            // Disable newbie protection (grace) if applied
            boolean currentGraceStatus = dcHook.hasProtection(target);
            if (currentGraceStatus) {
                boolean disabledGrace = runCommand("grace disable " + target.getName());
                if (disabledGrace) {
                    sendMessage(sender, "You disabled " + target.getName() + "'s newbie protection (aka grace period)!");
                    sendMessage(target, configHandler.getDisabledGraceByOtherMessage());
                    return true;
                } else {
                    sendMessage(sender, "&cError disabling " + target.getName() + "'s newbie protection!");
                    return false;
                }
            }

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

    private boolean preventUsageWorlds(Player target) {
        // Prevent Usage if in Disable World
        if (configHandler.getPreventedWorlds().contains(target.getWorld().getName())) {
            sendMessage(target, configHandler.getPreventedToggleInWorldsMessage());
            return true;
        } return false;
    }

    private boolean preventUsage(Player target) {
        // Prevent Usage if in Combat
        if (dcHook.isInCombat(target) && configHandler.isPreventedInCombat()) {
            sendMessage(target, configHandler.getPreventedToggleInCombatMessage());
            return true;
        }

        // Prevent Usage if recently in "JOIN", "TOGGLE", "COMBAT", "MURDER", "DEATH", "BONUS"
        int current_play_time;
        try {
            current_play_time = target.getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_time = target.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.logRed("Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                current_play_time = 0;
            }
        }

        String tickAndType = entryManager.getHighestTickAndType(target.getName());
        String[] pair = tickAndType.split("\\ ");
        String preventedMessage = configHandler.getPreventedMessage(pair[1]);
        Integer highest_map_time = Integer.parseInt(pair[0]);

        if (highest_map_time==null || highest_map_time==0 || preventedMessage.equalsIgnoreCase("none")) {
            entryManager.saveEntry(target.getName(), "BONUS", current_play_time);
            return false;
        } else if (highest_map_time>current_play_time) {
            int time_difference = (highest_map_time-current_play_time)/20;
            String string = preventedMessage.replace("<time_left>", getFormattedTime(time_difference));
            sendMessage(target, string);
            return true;
        } else return false;
    }

    private boolean runCommand(String command) {
        ConsoleCommandSender console = javaPlugin.getServer().getConsoleSender();
        try {
            return Bukkit.dispatchCommand(console, command);
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception running command " + command + ": " + e.getMessage());
            return false;
        }
    }

    private boolean sudoCommand(Player target, String command) {
        try {
            return Bukkit.dispatchCommand(target, command);
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

    private String getFormattedTime(int totalSeconds) {
        final int SECONDS_IN_WEEK = 604800;
        final int SECONDS_IN_DAY = 86400;
        final int SECONDS_IN_HOUR = 3600;
        final int SECONDS_IN_MINUTE = 60;

        // Calculate each time unit
        int weeks = totalSeconds / SECONDS_IN_WEEK;
        totalSeconds %= SECONDS_IN_WEEK;

        int days = totalSeconds / SECONDS_IN_DAY;
        totalSeconds %= SECONDS_IN_DAY;

        int hours = totalSeconds / SECONDS_IN_HOUR;
        totalSeconds %= SECONDS_IN_HOUR;

        int minutes = totalSeconds / SECONDS_IN_MINUTE;
        int seconds = totalSeconds % SECONDS_IN_MINUTE;

        // Build the formatted string
        StringBuilder formattedTime = new StringBuilder();
        if (weeks > 0) {
            formattedTime.append(weeks).append(" week");
            if (weeks > 1) formattedTime.append("s");
            formattedTime.append(", ");
        } if (days > 0) {
            formattedTime.append(days).append(" day");
            if (days > 1) formattedTime.append("s");
            formattedTime.append(", ");
        } if (hours > 0) {
            formattedTime.append(hours).append(" hour");
            if (hours > 1) formattedTime.append("s");
            formattedTime.append(", ");
        } if (minutes > 0) {
            formattedTime.append(minutes).append(" minute");
            if (minutes > 1) formattedTime.append("s");
            formattedTime.append(", ");
        } if (seconds > 0) {
            formattedTime.append(seconds).append(" second");
            if (seconds != 1) formattedTime.append("s");
        }
        return formattedTime.toString();
    }

    private void sendMessage(CommandSender target, String string) {
        if (!string.isBlank())
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getChatPrefix() + string)));
    }

    private Player getPlayer(String arg) {
        return javaPlugin.getServer().getPlayer(arg);
    }

    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command consoleCommand, String alias, String[] args) {
        if (!(hasPermission(sender, PERMISSION_TOGGLE_OTHERS)||hasPermission(sender, PERMISSION_GIVE_COOLDOWNS))) return null;
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        } else if (args.length == 2) {
            list.add("enable");
            list.add("disable");
            list.add("giveCooldown");
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