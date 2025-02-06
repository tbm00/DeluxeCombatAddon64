package dev.tbm00.spigot.deluxecombataddon64.data;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;

public class EntryManager {
    private final DeluxeCombatAddon64 javaPlugin;
    private final JSONHandler db;
    private Map<String, SortedMap<Integer, String>> playerMap; // key1=<username>, value1=<cooldownMap>, key2=<ticks>, value2=<preventedType>

    /**
     * Constructs an EntryManager instance.
     * Initializes the player map by loading data from the provided JSON handler.
     *
     * @param javaPlugin the main plugin instance
     * @param db the JSON handler for loading and saving data
     */
    public EntryManager(DeluxeCombatAddon64 javaPlugin, JSONHandler db) {
        this.javaPlugin = javaPlugin;
        this.db = db;
        this.playerMap = new ConcurrentHashMap<>(db.loadPlayerMap());
    }

    /**
     * Saves the player map data to JSON when the plugin is disabled.
     */
    public void saveDataToJson() {
        Map<String, SortedMap<Integer, String>> snapshot;
        synchronized (playerMap) {
            snapshot = new HashMap<>(playerMap);
        }
        db.savePlayerMap(snapshot);
    }

    /**
     * Saves a new cooldown entry for a player, clearing any existing entry.
     *
     * @param username the name of the player
     * @param preventedType the type of prevention
     * @param ticks the cooldown time in ticks
     */
    private void saveCooldownEntry(String username, String preventedType, Integer ticks) {
        if (playerMap.get(username)!=null)
            deletePlayerEntry(username);
        
        SortedMap<Integer, String> cooldownMap = new TreeMap<>();
        cooldownMap.put(ticks, preventedType);
        playerMap.put(username, cooldownMap);
    }

    /**
     * Deletes a player's entry from the player map.
     *
     * @param username the name of the player to be removed
     * @return true if the player entry was successfully deleted, false if an error occurred
     */
    public boolean deletePlayerEntry(String username) {
        try {
            playerMap.get(username).clear();
            playerMap.remove(username);
            return true;
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception deleting player entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds cooldown time to the player's cooldown map.
     * The cooldown time is calculated as the current play time + the additional time.
     *
     * @param player the player to add the cooldown for
     * @param preventedType the type of prevention
     * @param additional_ticks the additional cooldown time in ticks
     * @return true if the cooldown was successfully added, false otherwise
     */
    public boolean addMapTime(Player player, String preventedType, int additional_ticks) {
        String playerName = player.getName();

        int current_play_ticks;
        try {
            current_play_ticks = player.getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_ticks = player.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                return false;
            }
        }
        Integer highest_map_ticks = getHighestTick(playerName);
        int potential_cooldown = current_play_ticks+additional_ticks;

        if (highest_map_ticks==null || highest_map_ticks<potential_cooldown) {
            saveCooldownEntry(playerName, preventedType, potential_cooldown);
            return true;
        } return false;
    }

    /**
     * Retrieves the highest cooldown ticks from the player's cooldown map.
     *
     * @param username the name of the player
     * @return the highest cooldown time in ticks, or null if not found
     */
    private Integer getHighestTick(String username) {
        SortedMap<Integer, String> cooldownMap = playerMap.get(username);
        if (cooldownMap == null || cooldownMap.isEmpty()) return null;

        int highest_map_ticks;
        try {highest_map_ticks = cooldownMap.lastKey();}
        catch (Exception e) {return null;}
        return highest_map_ticks;
    }

    /**
     * Retrieves the highest cooldown ticks and its associated type from the player's cooldown map.
     *
     * @param username the name of the player
     * @return a string containing the highest cooldown time and type
     */
    public String getHighestTickAndType(String username) {
        SortedMap<Integer, String> cooldownMap = playerMap.get(username);
        if (cooldownMap == null || cooldownMap.isEmpty()) return "0 none";

        int highest_map_ticks;
        try {highest_map_ticks = cooldownMap.lastKey();}
        catch (Exception e) {highest_map_ticks = 0;}
        
        String highest_map_type;
        try {highest_map_type = cooldownMap.get(highest_map_ticks);}
        catch (Exception e) {highest_map_type = "null";}

        return (highest_map_ticks + " " + highest_map_type);
    }

    /**
     * Retrieves the highest cooldown type and its associated formatted time from the player's cooldown map.
     * 
     * @param username the name of the player
     * @return a string containing the cooldown type and formatted time remaining
     */
    public String getHighestTypeAndTime(String username) {
        SortedMap<Integer, String> cooldownMap = playerMap.get(username);
        if (cooldownMap == null || cooldownMap.isEmpty()) return "(no cooldowns)";

        int highest_map_ticks;
        try {highest_map_ticks = cooldownMap.lastKey();}
        catch (Exception e) {highest_map_ticks = 0;}
        
        String highest_map_type;
        try {highest_map_type = cooldownMap.get(highest_map_ticks);}
        catch (Exception e) {highest_map_type = "null";}

        int current_play_ticks;
        try {
            current_play_ticks = javaPlugin.getServer().getPlayer(username).getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_ticks = javaPlugin.getServer().getPlayer(username).getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                current_play_ticks = 0;
            }
        }

        int time_difference = (highest_map_ticks-current_play_ticks)/20;
        return (highest_map_type + " " + getFormattedTime(time_difference));
    }

    /**
     * Formats a given amount of time in seconds into a human-readable string (e.g., "12 hours, 4 minutes, 3 seconds").
     *
     * @param totalSeconds the total time in seconds
     * @return a formatted string representing the time
     */
    public String getFormattedTime(int totalSeconds) {
        final int SECONDS_IN_WEEK = 604800;
        final int SECONDS_IN_DAY = 86400;
        final int SECONDS_IN_HOUR = 3600;
        final int SECONDS_IN_MINUTE = 60;

        boolean wasNegative = false;
        if (totalSeconds<0) {
            wasNegative = true;
            totalSeconds = -1*totalSeconds;
        }

        // Calculate each time unit
        int weeks = totalSeconds / SECONDS_IN_WEEK;
        totalSeconds %= SECONDS_IN_WEEK;

        int days = totalSeconds / SECONDS_IN_DAY;
        totalSeconds %= SECONDS_IN_DAY;

        int hours = totalSeconds / SECONDS_IN_HOUR;
        totalSeconds %= SECONDS_IN_HOUR;

        int minutes = totalSeconds / SECONDS_IN_MINUTE;
        int seconds = totalSeconds % SECONDS_IN_MINUTE;

        // Build the formatted string
        StringBuilder formattedTime = new StringBuilder();
        if (wasNegative)
            formattedTime.append("-");
        if (weeks > 0) {
            formattedTime.append(weeks).append(" week");
            if (weeks > 1) formattedTime.append("s");
        } if (days > 0) {
            if (formattedTime.length()>1) formattedTime.append(", ");
            formattedTime.append(days).append(" day");
            if (days > 1) formattedTime.append("s");
        } if (hours > 0) {
            if (formattedTime.length()>1) formattedTime.append(", ");
            formattedTime.append(hours).append(" hour");
            if (hours > 1) formattedTime.append("s");
        } if (minutes > 0) {
            if (formattedTime.length()>1) formattedTime.append(", ");
            formattedTime.append(minutes).append(" minute");
            if (minutes > 1) formattedTime.append("s");
        } if (seconds > 0) {
            if (formattedTime.length()>1) formattedTime.append(", ");
            formattedTime.append(seconds).append(" second");
            if (seconds != 1) formattedTime.append("s");
        } else if (formattedTime.isEmpty())
            formattedTime.append("0 seconds");
        return formattedTime.toString();
    }
}