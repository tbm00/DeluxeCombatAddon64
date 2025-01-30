package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.Statistic;

import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.EntryManager;

public class PlayerConnection implements Listener {
    private final DeluxeCombatAddon64 javaPlugin;
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerConnection(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.javaPlugin = javaPlugin;
        this.entryManager = entryManager;
        this.configHandler = configHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        int current_play_time;
        try {
            current_play_time = player.getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_time = player.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.logRed("Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                current_play_time = 0;
            }
        }
        int additional_time = configHandler.isDisabledAfterJoin() ? configHandler.getDisabledAfterJoinTicks() : 0;
        Integer current_map_time = entryManager.getTickTime(playerName);

        if (current_map_time==null || current_map_time<(current_play_time+additional_time)) {
            entryManager.saveEntry(playerName, (current_play_time+additional_time));
        }
    }
}