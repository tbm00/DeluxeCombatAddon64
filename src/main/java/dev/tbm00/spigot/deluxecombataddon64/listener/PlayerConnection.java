package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.EntryManager;

public class PlayerConnection implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerConnection(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int additional_time = configHandler.isPreventedAfterJoin() ? configHandler.getPreventedAfterJoinTicks() : 0;
        entryManager.setMapTime(player, "JOIN", additional_time);
    }
}