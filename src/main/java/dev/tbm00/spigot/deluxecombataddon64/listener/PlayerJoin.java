package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;

public class PlayerJoin implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerJoin(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        entryManager.addMapTime(event.getPlayer(), "JOIN", configHandler.getPreventedAfterJoinTicks());
    }
}