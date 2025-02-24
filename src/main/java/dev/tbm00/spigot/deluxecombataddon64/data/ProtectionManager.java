package dev.tbm00.spigot.deluxecombataddon64.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;

public class ProtectionManager {
    private final DeluxeCombatAddon64 javaPlugin;
    private final JSONHandler db;
    private Map<String, Integer> protMap; // key=<username>, value=<ticks>

    /**
     * Constructs an protectionManager instance.
     * Initializes the protection map by loading data from the provided JSON handler.
     *
     * @param javaPlugin the main plugin instance
     * @param db the JSON handler for loading and saving data
     */
    public ProtectionManager(DeluxeCombatAddon64 javaPlugin, JSONHandler db) {
        this.javaPlugin = javaPlugin;
        this.db = db;
        this.protMap = new ConcurrentHashMap<>(db.loadProtectionMap());
    }

    /**
     * Saves the protection map data to JSON when the plugin is disabled.
     */
    public void saveDataToJson() {
        Map<String, Integer> snapshot;
        synchronized (protMap) {
            snapshot = new HashMap<>(protMap);
        }
        db.saveProtectionMap(snapshot);
    }

    /**
     * Sets protection time to the protection map.
     * The protection time is calculated as the current play time + the additional time.
     *
     * @param player the player to add the protection for
     * @param additional_ticks the additional protection time in ticks
     * @return true if the protection was successfully added, false otherwise
     */
    public boolean setMapTime(Player player, int additional_ticks) {
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
        Integer map_ticks = getMapTicks(playerName);
        int potential_protection = current_play_ticks+additional_ticks;

        if (map_ticks==null || map_ticks<potential_protection) {
            saveProtectionEntry(playerName, potential_protection);
            return true;
        } return false;
    }

    /**
     * Adds protection time to the protection map.
     * The protection time is calculated as the current map time + the additional time.
     *
     * @param player the player to add the protection for
     * @param additional_ticks the additional protection time in ticks
     * @return true if the protection was successfully added, false otherwise
     */
    public boolean addMapTime(Player player, int additional_ticks) {
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
        Integer map_ticks = getMapTicks(playerName);
        int potential_protection;
        
        if (map_ticks != null && map_ticks > 0) {
            potential_protection = map_ticks+additional_ticks;
        } else {
            potential_protection = current_play_ticks+additional_ticks;
        }

        saveProtectionEntry(playerName, potential_protection);
        return true;
    }

    /**
     * Retrieves the protection's formatted time from the protection map.
     * 
     * @param username the name of the player
     * @return a string containing the formatted time remaining
     */
    public String getMapTime(String username) {
        Integer map_ticks = getMapTicks(username);
        if (map_ticks == null || map_ticks == 0) return "(no protections)";

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

        int time_difference = (map_ticks-current_play_ticks)/20;
        return (formatTime(time_difference));
    }

    /**
     * Retrieves the protection's formatted time from the protection map.
     * 
     * @param username the name of the player
     * @return a string containing the formatted time remaining
     */
    public boolean hasActiveProtection(String username) {
        Integer map_ticks = getMapTicks(username);
        if (map_ticks == null || map_ticks == 0) return false;

        int current_play_ticks;
        try {
            current_play_ticks = javaPlugin.getServer().getPlayer(username).getStatistic(Statistic.valueOf("PLAY_ONE_MINUTE"));
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_MINUTE: " + e.getMessage());
            try {
                current_play_ticks = javaPlugin.getServer().getPlayer(username).getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
            } catch (Exception e2) {
                javaPlugin.log(ChatColor.RED, "Caught exception getting player statistic PLAY_ONE_TICK: " + e2.getMessage());
                return false;
            }
        }

        if (map_ticks>current_play_ticks) return true;
        else return false;
    }

    /**
     * Retrieves the protection ticks from the protection map.
     *
     * @param username the name of the player
     * @return the protection time in ticks, or null if not found
     */
    public Integer getMapTicks(String username) {
        Integer map_ticks = protMap.get(username);
        if (map_ticks == null) return 0;
        else return map_ticks;
    }

    /**
     * Saves protection entry for a player, clearing any existing entry.
     *
     * @param username the name of the player
     * @param ticks the protection time in ticks
     */
    private void saveProtectionEntry(String username, Integer ticks) {
        protMap.put(username, ticks);
    }

    /**
     * Deletes a protection entry from the protection map.
     *
     * @param username the name of the player to be removed
     * @return true if the player entry was successfully deleted, false if an error occurred
     */
    public boolean deleteProtectionEntry(String username) {
        try {
            protMap.remove(username);
            return true;
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception deleting player entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats a given amount of time in seconds into a human-readable string (e.g., "12 hours, 4 minutes, 3 seconds").
     *
     * @param totalSeconds the total time in seconds
     * @return a formatted string representing the time
     */
    public String formatTime(int totalSeconds) {
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