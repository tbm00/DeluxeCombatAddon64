package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class PlayerDeath implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;
    private final DCHook dcHook;

    public PlayerDeath(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager, DCHook dcHook) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    /**
     * Handles the player death event.
     * Adds cooldown time for the killer and/or victim, and forces PvP enable on the victim if configured.
     *
     * @param event the PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer!=null) {
            if (configHandler.isPreventedAfterMurder())
                entryManager.addMapTime(killer, "MURDER", configHandler.getPreventedAfterMurderTicks());
            if (configHandler.isPreventedAfterPVPDeath())
                entryManager.addMapTime(victim, "DEATH", configHandler.getPreventedAfterPVPDeathTicks());
        } else {
            if (configHandler.isPreventedAfterPVEDeath())
                entryManager.addMapTime(victim, "DEATH", configHandler.getPreventedAfterPVEDeathTicks());
        }

        if (configHandler.isForceEnabledAfterDeath() && !dcHook.hasPvPEnabled(victim)) {
            dcHook.togglePvP(victim, true);
            sendMessage(victim, configHandler.getForceEnabledAfterMessage());
        }
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
}