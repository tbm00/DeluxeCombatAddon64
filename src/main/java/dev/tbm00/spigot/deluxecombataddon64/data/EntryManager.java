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

    public EntryManager(DeluxeCombatAddon64 javaPlugin, JSONHandler db) {
        this.javaPlugin = javaPlugin;
        this.db = db;
        this.playerMap = new ConcurrentHashMap<>(db.loadPlayerMap());
    }

    // save playerMap to json on plugin disable
    public void saveDataToJson() {
        Map<String, SortedMap<Integer, String>> snapshot;
        synchronized (playerMap) {
            snapshot = new HashMap<>(playerMap);
        }
        db.savePlayerMap(snapshot);
    }

    // clears player from playermap then re-adds it with new cooldown
    private void saveCooldownEntry(String username, String preventedType, Integer ticks) {
        if (playerMap.get(username)!=null)
            deletePlayerEntry(username);
        
        SortedMap<Integer, String> cooldownMap = new TreeMap<>();
        cooldownMap.put(ticks, preventedType);
        playerMap.put(username, cooldownMap);
    }

    // removes player entry from entries
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

    // adds cooldown to player's cooldownMap:<cooldown,prevented_type>
    // (potential_cooldown == current_play_ticks + additional_ticks)
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

    // returns Integer used by EntryManager.java for checking if current highest time is less than potential
    // ...
    // returns time (in ticks) that command is re-enabled, using player's cooldown map
    // if not found returns null
    private Integer getHighestTick(String username) {
        SortedMap<Integer, String> cooldownMap = playerMap.get(username);
        if (cooldownMap == null || cooldownMap.isEmpty()) return null;

        int highest_map_ticks;
        try {highest_map_ticks = cooldownMap.lastKey();}
        catch (Exception e) {return null;}
        return highest_map_ticks;
    }

    // returns String used by TogglePvpCmd.java for preventing togglepvp usage
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

    // returns String used by TogglePvpCmd.java for output to CommandSender
    public String getHighestTypeAndTick(String username) {
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
                return (highest_map_type + " " + getFormattedTime(highest_map_ticks) + " playtime");
            }
        }

        int time_difference = (highest_map_ticks-current_play_ticks)/20;
        return (highest_map_type + " " + getFormattedTime(time_difference));
    }

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