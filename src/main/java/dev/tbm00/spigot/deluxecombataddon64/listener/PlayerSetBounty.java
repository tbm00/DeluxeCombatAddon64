package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import nl.marido.deluxecombat.events.BountyInitiateEvent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.CooldownManager;
import dev.tbm00.spigot.deluxecombataddon64.data.ProtectionManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class PlayerSetBounty implements Listener {
    private final ConfigHandler configHandler;
    private final CooldownManager cooldownManager;
    private final ProtectionManager protectionManager;
    private final DCHook dcHook;

    public PlayerSetBounty(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, CooldownManager cooldownManager, ProtectionManager protectionManager, DCHook dcHook) {
        this.configHandler = configHandler;
        this.cooldownManager = cooldownManager;
        this.protectionManager = protectionManager;
        this.dcHook = dcHook;
    }

    /**
     * Handles the event when a player sets a bounty on another player.
     * Adds cooldown time for the sender, if configured.
     * Force enabled pvp for the sender, if configured.
     *
     * @param event the BountyInitiateEvent
     */
    @EventHandler
    public void onBountySet(BountyInitiateEvent event) {
        Player sender = event.getInitiator();

        if (configHandler.isBountyProtCommandEnabled() && protectionManager.hasActiveProtection(event.getTarget().getName())) {
            sendMessage(sender, configHandler.getCannotSetBountyMessage().replace("<time_left>", protectionManager.getMapTime(event.getTarget().getName())));
            event.setCancelled(true);
            return;
        }

        if (configHandler.isPreventedAfterSetBounty()) {
            cooldownManager.setMapTime(sender, "SETBOUNTY", configHandler.getPreventedAfterSetBountyTicks());
        }

        if (configHandler.isForceEnabledAfterSetBounty() && !dcHook.hasPvPEnabled(sender)) {
            dcHook.togglePvP(sender, true);
            sendMessage(sender, configHandler.getForceEnabledAfterMessage());
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
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getPVPChatPrefix() + string)));
    }
}