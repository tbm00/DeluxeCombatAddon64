package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import nl.marido.deluxecombat.events.CombatStateChangeEvent;
import nl.marido.deluxecombat.events.CombatStateChangeEvent.CombatState;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;

public class PlayerCombat implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerCombat(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
    }

    @EventHandler
    public void onCombatStateChange(CombatStateChangeEvent event) {
        // if the state changed to off/untagged
        if (event.getState() == CombatState.UNTAGGED) {
            entryManager.addMapTime(event.getPlayer(), "COMBAT", configHandler.getPreventedAfterCombatTicks());
        }
    }
}
