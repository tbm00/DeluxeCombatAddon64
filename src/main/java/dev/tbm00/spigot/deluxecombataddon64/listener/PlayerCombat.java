package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import nl.marido.deluxecombat.events.CombatStateChangeEvent;
import nl.marido.deluxecombat.events.CombatStateChangeEvent.CombatState;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.CooldownManager;

public class PlayerCombat implements Listener {
    private final CooldownManager cooldownManager;
    private final ConfigHandler configHandler;

    public PlayerCombat(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
        this.configHandler = configHandler;
    }

    /**
     * Handles the event when a player's combat state changes.
     * Adds cooldown time for the player after they finish combat, if configured.
     *
     * @param event the CombatStateChangeEvent
     */
    @EventHandler
    public void onCombatStateChange(CombatStateChangeEvent event) {
        // if the state changed to off/untagged
        if (event.getState() == CombatState.UNTAGGED) {
            cooldownManager.setMapTime(event.getPlayer(), "COMBAT", configHandler.getPreventedAfterCombatTicks());
        }
    }
}
