package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nl.marido.deluxecombat.events.CombatlogEvent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;

public class PlayerConnection implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerConnection(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.entryManager = entryManager;
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
        if (configHandler.isPreventedAfterJoin())
            entryManager.addMapTime(event.getPlayer(), "JOIN", configHandler.getPreventedAfterJoinTicks());
    }

    /**
     * Handles the combat log event.
     * Adds cooldown time for the player who logged out during combat, if configured.
     *
     * @param event the CombatlogEvent
     */
    @EventHandler
    public void onCombatLog(CombatlogEvent event) {
        if (configHandler.isPreventedAfterCombatLog())
            entryManager.addMapTime(event.getCombatlogger(), "COMBATLOG", configHandler.getPreventedAfterCombatLogTicks());
    }
}