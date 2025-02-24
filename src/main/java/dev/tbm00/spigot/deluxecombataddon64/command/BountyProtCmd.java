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

public class BountyProtCmd implements TabExecutor {
    private final DeluxeCombatAddon64 javaPlugin;
    private final ConfigHandler configHandler;
    private final ProtectionManager protectionManager;
    private final String PERMISSION_BOUNTYPROT_MANAGE = "deluxecombataddon64.manageprotections";

    public BountyProtCmd(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, ProtectionManager protectionManager) {
        this.protectionManager = protectionManager;
        this.javaPlugin = javaPlugin;
        this.configHandler = configHandler;
    }

    /**
     * Handles the "/bountyprot" command for checking and setting bounty protections.
     * 
     * @param sender the command sender
     * @param consoleCommand the command being executed
     * @param label the label used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    public boolean onCommand(CommandSender sender, Command consoleCommand, String label, String[] args) {

        // "/bountyprot <username> set <seconds>" - admin command 
        if ((args.length==3) && hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) {
            if (args[1].equalsIgnoreCase("set"))
                return handleSetOther(sender, getPlayer(args[0]), Integer.valueOf(args[2]));
            if (args[1].equalsIgnoreCase("add"))
                return handleAddOther(sender, getPlayer(args[0]), Integer.valueOf(args[2]));
        } 
        
        // "/bountyprot <username> [clear/get]" - admin command 
        if ((args.length==2) && hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) {
            if (args[1].equalsIgnoreCase("get"))
                return handleGetOther(sender, getPlayer(args[0]));
            if (args[1].equalsIgnoreCase("clear"))
                return handleClearOther(sender, getPlayer(args[0]));
        } 

        // "/bountyprot" - player command
        if (args.length==0 && (sender instanceof Player)) { 
            Player target = (Player) sender;
            return handleGetSelf(target);
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
    private boolean handleAddOther(CommandSender sender, Player target, Integer seconds) {
        if (target == null) {
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } else if (seconds==null) {
            sendMessage(target, "&cCouldn't parse seconds integer!");
            return false;
        }

        protectionManager.addMapTime(target, seconds*20);
        sendMessage(sender, "&fAdded " + protectionManager.formatTime(seconds) + " protection to " + target.getName()
                    + ". &7Their current protection: " + protectionManager.getMapTime(target.getName()));
        sendMessage(target, configHandler.getProtectionEnabledMessage().replace("<time_left>", protectionManager.getMapTime(target.getName())));
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
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } else if (seconds==null) {
            sendMessage(target, "&cCouldn't parse seconds integer!");
            return false;
        }

        protectionManager.setMapTime(target, seconds*20);
        sendMessage(sender, "&fSet " + protectionManager.formatTime(seconds) + " protection on " + target.getName()
                    + ". &7Their current protection: " + protectionManager.getMapTime(target.getName()));
        sendMessage(target, configHandler.getProtectionEnabledMessage().replace("<time_left>", protectionManager.getMapTime(target.getName())));
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
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } 

        if (!protectionManager.hasActiveProtection(target.getName())) {
            sendMessage(sender, target.getName() + " has no active bounty protection!");
        } else {
            String time = protectionManager.getMapTime(target.getName());
            String msg = (target.getName() + " has bounty protection for <time_left>!").replace("<time_left>", time);
            sendMessage(sender, msg);
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
            sendMessage(target, "&cCouldn't find target player!");
            return false;
        } 
        
        if (protectionManager.deleteProtectionEntry(target.getName())) {
            sendMessage(sender, "&fCleared " + target.getName() + "'s protection map. &7Their current protection: " 
                        + protectionManager.getMapTime(target.getName()));
            return true;
        } else {
            sendMessage(sender, "&cError occured clearing " + target.getName() + "'s protection map. &7Their current protection: " 
            + protectionManager.getMapTime(target.getName()));
            return true;
        } 
    }

    /**
     * Handles the command for getting sender's protection (self).
     * 
     * @param sender the command sender
     * @return true if the target was found and protection was sent, false otherwise
     */
    private boolean handleGetSelf(CommandSender sender) {
        if (!protectionManager.hasActiveProtection(sender.getName())) {
            sendMessage(sender, configHandler.getNoCurrentProtectionMessage());
        } else {
            String time = protectionManager.getMapTime(sender.getName());
            String msg = configHandler.getCurrentProtectionTimeMessage().replace("<time_left>", time);
            sendMessage(sender, msg);
        } return true;
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
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getProtChatPrefix() + string)));
    }

    /**
     * Handles tab completion for the "/bountyprot" command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command consoleCommand, String alias, String[] args) {
        if (!hasPermission(sender, PERMISSION_BOUNTYPROT_MANAGE)) return null;
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
        } else if (args.length == 2) {
            list.add("get");
            list.add("set");
            list.add("add");
            list.add("clear");
        } return list;
    }
}