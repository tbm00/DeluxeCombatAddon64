package dev.tbm00.spigot.deluxecombataddon64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.ProtectionManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class BountyCmd implements TabExecutor {
    private final DeluxeCombatAddon64 javaPlugin;
    private final ConfigHandler configHandler;
    private final ProtectionManager protectionManager;
    private final String PERMISSION_BOUNTYPROT_MANAGE = "deluxecombataddon64.manageprotections";
    private final DCHook dcHook;

    public BountyCmd(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, ProtectionManager protectionManager,  DCHook dcHook) {
        this.protectionManager = protectionManager;
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    /**
     * Handles the "/bounty" command for checking and setting bounty protections.
     * 
     * @param sender the command sender
     * @param consoleCommand the command being executed
     * @param label the label used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean onCommand(CommandSender sender, Command consoleCommand, String label, String[] args) {
        if (args.length==0) {
            return handleBaseCmd(sender);
        } else if (args.length==1) {
            if (!(sender instanceof Player)) return true;
            if (args[0].toLowerCase().equals("create")) {
                return sudoDCCommand((Player) sender, "dcbounty create");
            } else if (args[0].toLowerCase().equals("list")) {
                return sudoDCCommand((Player) sender, "dcbounty list");
            } else {
                return handleBaseCmd(sender);
            }
        } else if ((args.length==2) && hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) {
            // "/bountyprot <username> [clear/get]" - admin command 
            if (args[1].equalsIgnoreCase("get"))
                return handleGetOther(sender, getPlayer(args[0]));
            if (args[1].equalsIgnoreCase("clear"))
                return handleClearOther(sender, getPlayer(args[0]));
        } else if ((args.length==3) && hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) {
            // "/bountyprot <username> set <seconds>" - admin command 
            if (args[1].equalsIgnoreCase("set"))
                return handleSetOther(sender, getPlayer(args[0]), Integer.valueOf(args[2]));
            if (args[1].equalsIgnoreCase("add"))
                return handleAddOther(sender, getPlayer(args[0]), Integer.valueOf(args[2]));
        } 
        
        return false;
    }

    /**
     * Handles the command for adding protection on a player.
     * 
     * @param sender the command sender
     * @param target the target player
     * @param seconds the duration of the protection in seconds
     * @return true if the protection was successfully given, false otherwise
     */
    private boolean handleBaseCmd(CommandSender sender) {
        sendMessage(sender, false, " ");
        sendMessage(sender, false, " &6Bounty Commands");
        sendMessage(sender, false, " &f/bounty create");
        sendMessage(sender, false, " &f/bounty list");
        sendMessage(sender, false, " ");

        boolean activeProtection = protectionManager.hasActiveProtection(sender.getName());
        boolean activeBounty = dcHook.hasBounty((Player) sender);
        if (activeProtection) {
            String time = protectionManager.getMapTime(sender.getName());
            String msg = configHandler.getCurrentProtectionTimeMessage().replace("<time_left>", time);
            sendMessage(sender, false, "&2 "+msg);
        }
        if (activeBounty) {
            sendMessage(sender, false, "&c You currently have a bounty..!");
        }
        if (activeBounty || activeProtection) sendMessage(sender, false, " ");
        return true;
    }

    /**
     * Sends a message to a target CommandSender.
     * 
     * @param target the CommandSender to send the message to
     * @param string the message to send
     */
    private void sendMessage(CommandSender target, boolean prefixed, String string) {
        if (prefixed) target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getProtChatPrefix() + string)));
        else target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', string)));
    }

    public boolean sudoDCCommand(Player target, String command) {
        try {
            String msg = command.startsWith("/") ? command : "/" + command;
            target.chat(msg);  
            return true;
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception sudoing command: " + target.getName() + " : /" + command + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles the command for adding protection on a player.
     * 
     * @param sender the command sender
     * @param target the target player
     * @param seconds the duration of the protection in seconds
     * @return true if the protection was successfully given, false otherwise
     */
    private boolean handleAddOther(CommandSender sender, Player target, Integer seconds) {
        if (target == null) {
            sendMessage(target, true, "&cCouldn't find target player!");
            return false;
        } else if (seconds==null) {
            sendMessage(target, true, "&cCouldn't parse seconds integer!");
            return false;
        }

        protectionManager.addMapTime(target, seconds*20);
        sendMessage(sender, true, "&fAdded " + protectionManager.formatTime(seconds) + " protection to " + target.getName()
                    + ". &7Their current protection: " + protectionManager.getMapTime(target.getName()));
        sendMessage(target, true, configHandler.getProtectionEnabledMessage().replace("<time_left>", protectionManager.getMapTime(target.getName())));
        return true;
    }

    /**
     * Handles the command for setting protection on a player.
     * 
     * @param sender the command sender
     * @param target the target player
     * @param seconds the duration of the protection in seconds
     * @return true if the protection was successfully given, false otherwise
     */
    private boolean handleSetOther(CommandSender sender, Player target, Integer seconds) {
        if (target == null) {
            sendMessage(target, true, "&cCouldn't find target player!");
            return false;
        } else if (seconds==null) {
            sendMessage(target, true, "&cCouldn't parse seconds integer!");
            return false;
        }

        protectionManager.setMapTime(target, seconds*20);
        sendMessage(sender, true, "&fSet " + protectionManager.formatTime(seconds) + " protection on " + target.getName()
                    + ". &7Their current protection: " + protectionManager.getMapTime(target.getName()));
        sendMessage(target, true, configHandler.getProtectionEnabledMessage().replace("<time_left>", protectionManager.getMapTime(target.getName())));
        return true;
    }

    /**
     * Handles the command for getting a player's protection.
     * 
     * @param sender the command sender
     * @param target the target player
     * @return true if the target was found and protection was sent, false otherwise
     */
    private boolean handleGetOther(CommandSender sender, Player target) {
        if (target == null) {
            sendMessage(target, true, "&cCouldn't find target player!");
            return false;
        } 

        if (!protectionManager.hasActiveProtection(target.getName())) {
            sendMessage(sender, true, target.getName() + " has no active bounty protection!");
        } else {
            String time = protectionManager.getMapTime(target.getName());
            String msg = (target.getName() + " has bounty protection for <time_left>!").replace("<time_left>", time);
            sendMessage(sender, true, msg);
        } return true;
    }

    /**
     * Handles the command for clearing a player's protection.
     * 
     * @param sender the command sender
     * @param target the target player
     * @return true if the protection was successfully cleared, false otherwise
     */
    private boolean handleClearOther(CommandSender sender, Player target) {
        if (target == null) {
            sendMessage(target, true, "&cCouldn't find target player!");
            return false;
        } 
        
        if (protectionManager.deleteProtectionEntry(target.getName())) {
            sendMessage(sender, true, "&fCleared " + target.getName() + "'s protection map. &7Their current protection: " 
                        + protectionManager.getMapTime(target.getName()));
            return true;
        } else {
            sendMessage(sender, true, "&cError occured clearing " + target.getName() + "'s protection map. &7Their current protection: " 
            + protectionManager.getMapTime(target.getName()));
            return true;
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
     * Handles tab completion for the "/bounty" command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command consoleCommand, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("create");
            list.add("list");
            if (hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        } else if (args.length == 2 && hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) {
            list.add("get");
            list.add("set");
            list.add("add");
            list.add("clear");
        } return list;
    }
}