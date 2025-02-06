package dev.tbm00.spigot.deluxecombataddon64;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class EntryManager {
    private DeluxeCombatAddon64 javaPlugin;
    private Map<String, SortedMap<Integer, String>> entries; // key1=<username>, value1=<userMap>, key2=<ticks>, value2=<preventedType>

    public EntryManager(DeluxeCombatAddon64 javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.entries = new ConcurrentHashMap<>();
    }

    // returns time that command is re-enabled from player's unsorted map from parent map
    // if not found returns null
    public Integer getTickTime(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap != null && !userMap.isEmpty()) {
            Integer highest_map_time;
            try {highest_map_time = userMap.lastKey();}
            catch (Exception e) {
                highest_map_time = 0;
            }
            
            String highest_map_type;
            try {highest_map_type = userMap.get(highest_map_time);}
            catch (Exception e) {
                highest_map_type = "null";
            }

            trimUserMap(username,  highest_map_type, highest_map_time);
            return highest_map_time;
        } return null;
    }

    private void trimUserMap(String username, String highest_map_type, Integer highest_map_time) {
        try {
            entries.get(username).clear();
            deleteEntry(username);
            saveEntry(username, highest_map_type, highest_map_time);
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception trimming user map: " + e.getMessage());
        }
    }

    // creates player map if DNE
    // updates player entry in map if it does exist
    public void saveEntry(String username, String preventedType, Integer ticks) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null) {
            userMap = new TreeMap<>();
            entries.put(username, userMap);
        }
        userMap.put(ticks, preventedType);
    }

    // removes player/color entry from  map
    public void deleteEntry(String username) {
        entries.remove(username);
    }

    // adds entry to player's userMap:<map_time,prevented_type>
    // map time == preventedTypes' time + additional time
    public boolean addMapTime(Player player, String preventedType, int additional_time) {
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
                return false;
            }
        }
        Integer current_map_time = getTickTime(playerName);
        int potential_play_time = current_play_time+additional_time;

        if (current_map_time==null || current_map_time<potential_play_time) {
            saveEntry(playerName, preventedType, potential_play_time);
            return true;
        } return false;
    }

    public String getHighestTickAndType(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null || userMap.isEmpty()) return "0 none";
        if (userMap != null && !userMap.isEmpty()) {
            int highestTick = userMap.lastKey();
            String preventedType = userMap.get(highestTick);
            return (highestTick + " " + preventedType);
        } return null;
    }

    public String getHighestTimeAndType(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null || userMap.isEmpty()) return "0 none";
        if (userMap != null && !userMap.isEmpty()) {
            int highestCooldown = userMap.lastKey()/20;
            String preventedType = userMap.get(highestCooldown);
            return (preventedType + " " + highestCooldown +"sec");
        } return null;
    }
}