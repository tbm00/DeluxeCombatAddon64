package dev.tbm00.spigot.deluxecombataddon64;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class EntryManager {
    private DeluxeCombatAddon64 javaPlugin;
    private Map<String, Integer> entries;

    public EntryManager(DeluxeCombatAddon64 javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.entries = new ConcurrentHashMap<>();
    }

    // returns if the player/color entry for username exists
    public boolean entryExists(String username) {
        return entries.containsKey(username);
    }

    // returns time that command is re-enabled from player's entry from map
    // if not found, tries to get it from JSON
    // else returns null
    public Integer getTickTime(String username) {
        return entries.get(username);
    }

    // creates player/color entry in json & map if DNE
    // updates player/color entry in json & map if it does exist
    public void saveEntry(String username, Integer ticks) {
        entries.put(username, ticks);
    }

    // removes player/color entry from json & map
    public void deleteEntry(String username) {
        entries.remove(username);
    }

    // updates map with current_playtime+addition_time
    public boolean updateMapTime(Player player, int additional_time) {
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
                return false;
            }
        }
        Integer current_map_time = getTickTime(playerName);

        if (current_map_time==null || current_map_time<(current_play_time+additional_time)) {
            saveEntry(playerName, (current_play_time+additional_time));
            return true;
        } else return false;
    }
}