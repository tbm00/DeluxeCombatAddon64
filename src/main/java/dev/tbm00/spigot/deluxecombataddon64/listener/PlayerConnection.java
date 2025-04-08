package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nl.marido.deluxecombat.events.CombatlogEvent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.CooldownManager;

public class PlayerConnection implements Listener {
    private final CooldownManager cooldownManager;
    private final ConfigHandler configHandler;

    public PlayerConnection(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
        this.configHandler = configHandler;
    }

    /**
     * Handles the player join event.
     * Adds cooldown time for the player after they join, if configured.
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configHandler.getPreventDisableAfterJoin()) return;

        if (configHandler.getColdownNotAppliedInWorlds().contains(event.getPlayer().getWorld().getName())) return;

        cooldownManager.setMapTime(event.getPlayer(), "JOIN", configHandler.getPreventDisableAfterJoinTicks());
    }

    /**
     * Handles the combat log event.
     * Adds cooldown time for the player who logged out during combat, if configured.
     *
     * @param event the CombatlogEvent
     */
    @EventHandler
    public void onCombatLog(CombatlogEvent event) {
        if (!configHandler.getPreventDisableAfterCombatLog()) return;

        //if (configHandler.getColdownNotAppliedInWorlds().contains(event.getCombatlogger().getWorld().getName())) return;

        cooldownManager.setMapTime(event.getCombatlogger(), "COMBATLOG", configHandler.getPreventDisableAfterCombatLogTicks());
    }
}