package dev.tbm00.spigot.deluxecombataddon64.command;

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

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;

public class TogglePvpCmd implements TabExecutor {
    private final DeluxeCombatAddon64 javaPlugin;
    private final ConfigHandler configHandler;
    private final EntryManager entryManager;
    private final DCHook dcHook;
    private final String PERMISSION_TOGGLE_OTHERS = "deluxecombataddon64.toggle.others";
    private final String PERMISSION_TOGGLE_SELF = "deluxecombataddon64.toggle.self";

    public TogglePvpCmd(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager, DCHook dcHook) {
        this.entryManager = entryManager;
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    public boolean onCommand(CommandSender sender, Command consoleCommand, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "This command can only be run by a player!");
            return false;
        }

        if (args.length==0 && (sender instanceof Player) && hasPermission(sender, PERMISSION_TOGGLE_SELF)) { // "/togglepvp" - player command
            Player target = (Player) sender;
            return handleTogglePvpSelf(target);
        } else if ((args.length==1 || args.length == 2) && hasPermission(sender, PERMISSION_TOGGLE_OTHERS)) { // "/togglepvp <username> [on/off]" - admin command
            String newStatus = null;
            if (args.length==2) {
                if (args[1].equalsIgnoreCase("on")||args[1].equalsIgnoreCase("enable"))
                    newStatus = "enable";
                else if (args[1].equalsIgnoreCase("off")||args[1].equalsIgnoreCase("disable"))
                    newStatus = "disable";
                else {
                    sender.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "Second argument must be 'on' or 'off'!");
                    return false;
                }
            }
            return handleTogglePvpOthers(sender, getPlayer(args[0]), newStatus);
        } else return false;
    }

    private boolean handleTogglePvpSelf(Player target) {
        target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "1: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
        target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "1: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));

        // Check if command should be prevented
        if (preventUsage(target)) return true;

        // Disable newbie protection (grace) if applied
        boolean currentGraceStatus = dcHook.hasProtection(target);
        if (currentGraceStatus) {
            boolean disabledGrace = sudoCommand(target, "grace disable");
            if (disabledGrace) {
                target.sendMessage(configHandler.getChatPrefix() + "You &cdisabled &7your newbie protection (aka grace period)!");
                target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                //target.sendMessage(configHandler.getChatPrefix() + "Your newbie protection has been disabled!");
                return true;
            } else {
                target.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "Error disabling your newbie protection!");
                return false;
            }
        }

        // Switch pvp status
        boolean currentPvpStatus = dcHook.hasPvPEnabled(target);
        if (currentPvpStatus) { // if enabled, disable it
            dcHook.togglePvP(target, false);
            target.sendMessage(configHandler.getChatPrefix() + "You &cdisabled &7your PVP!");
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
            return true;
        } else {
            // if disabled, enable it
            dcHook.togglePvP(target, true);
            target.sendMessage(configHandler.getChatPrefix() + "You &aenabled &7your PVP!");
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
            return true;
        }
    }

    private boolean handleTogglePvpOthers(CommandSender sender, Player target, String newStatus) {
        sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "1: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
        sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "1: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));

        if (newStatus!=null) {
            if (newStatus.equalsIgnoreCase("enable")) {
                dcHook.togglePvP(target, true);
                sender.sendMessage(configHandler.getChatPrefix() + "You enabled " + target.getName() + "'s PVP!");
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                return true;
            }
            if (newStatus.equalsIgnoreCase("disable")) {
                dcHook.togglePvP(target, false);
                sender.sendMessage(configHandler.getChatPrefix() + "You disabled " + target.getName() + "'s PVP!");
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                return true;
            }
            else {
                dcHook.togglePvP(target, false);
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "Error changing " + target.getName() + "'s PVP!");
                return false;
            }
        } else {
            // Disable newbie protection (grace) if applied
            boolean currentGraceStatus = dcHook.hasProtection(target);
            if (currentGraceStatus) {
                boolean disabledGrace = sudoCommand(target, "grace disable");
                if (disabledGrace) {
                    sender.sendMessage(configHandler.getChatPrefix() + "You disabled " + target.getName() + "'s newbie protection (aka grace period)!");
                    sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                    sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                    //target.sendMessage(configHandler.getChatPrefix() + "Your newbie protection has been disabled!");
                    return true;
                } else {
                    sender.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "Error disabling " + target.getName() + "'s newbie protection!");
                    return false;
                }
            }

            // Switch pvp status
            boolean currentPvpStatus = dcHook.hasPvPEnabled(target);
            if (currentPvpStatus) { // if enabled, disable it
                dcHook.togglePvP(target, false);
                sender.sendMessage(configHandler.getChatPrefix() + "You disabled " + target.getName() + "'s PVP!");
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                return true;
            } else {
                // if disabled, enable it
                dcHook.togglePvP(target, true);
                sender.sendMessage(configHandler.getChatPrefix() + "You enabled " + target.getName() + "'s PVP!");
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s pvp status is: " + dcHook.hasPvPEnabled(target));
                sender.sendMessage(configHandler.getChatPrefix() + ChatColor.WHITE + "2: " + ChatColor.YELLOW + target.getName() + "'s prot status is: " + dcHook.hasProtection(target));
                return true;
            }
        }
    }

    private boolean preventUsage(Player target) {
        // Prevent Usage if in Disable World
        if (configHandler.getDisabledWorlds().contains(target.getWorld().getName())) {
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "You cannot toggle pvp in this world!");
            return true;
        }

        // Prevent Usage if in Combat
        if (dcHook.isInCombat(target)) {
            target.sendMessage(configHandler.getChatPrefix() + ChatColor.RED + "You cannot toggle pvp during combat!");
            return true;
        }

        // Prevent Usage if recently in Combat, Murderer, or Joined
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
        Integer current_map_time = entryManager.getTickTime(target.getName());
        if (current_map_time>current_play_time) {
            return true;
        } else return false;
    }

    private boolean sudoCommand(Player target, String command) {
        try {
            return Bukkit.dispatchCommand(target, command);
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

    private Player getPlayer(String arg) {
        return javaPlugin.getServer().getPlayer(arg);
    }

    private boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm) || sender instanceof ConsoleCommandSender;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command consoleCommand, String alias, String[] args) {
        if (!hasPermission(sender, PERMISSION_TOGGLE_OTHERS)) return null;
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        } else if (args.length == 2) {
            list.add("enable");
            list.add("disable");
        }
        return list;
    }
}