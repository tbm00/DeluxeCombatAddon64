package dev.tbm00.spigot.deluxecombataddon64;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class EntryManager {
    private DeluxeCombatAddon64 javaPlugin;
    private Map<String, SortedMap<Integer, String>> entries; // key1=<username>, value1=<userMap>, key2=<ticks>, value2=<preventedType>

    public EntryManager(DeluxeCombatAddon64 javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.entries = new ConcurrentHashMap<>();
    }

    // creates player map if DNE
    // adds cooldown entry in player map
    private void saveCooldownEntry(String username, String preventedType, Integer ticks) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null) {
            userMap = new TreeMap<>();
            entries.put(username, userMap);
        }
        userMap.put(ticks, preventedType);
    }

    // removes player map from entries
    private void deleteEntry(String username) {
        entries.remove(username);
    }

    private void trimUserMap(String username, String highest_map_type, Integer highest_map_ticks) {
        clearUserMap(username);
        saveCooldownEntry(username, highest_map_type, highest_map_ticks);
    }

    public boolean clearUserMap(String username) {
        try {
            entries.get(username).clear();
            deleteEntry(username);
            return true;
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception clearing user map: " + e.getMessage());
            return false;
        }
    }

    // returns time (in ticks) that command is re-enabled, using player's cooldown map
    // if not found returns null
    private Integer getTickTime(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap != null && !userMap.isEmpty()) {
            Integer highest_map_ticks;
            try {highest_map_ticks = userMap.lastKey();}
            catch (Exception e) {
                highest_map_ticks = 0;
            }
            
            String highest_map_type;
            try {highest_map_type = userMap.get(highest_map_ticks);}
            catch (Exception e) {
                highest_map_type = "null";
            }

            trimUserMap(username,  highest_map_type, highest_map_ticks);
            return highest_map_ticks;
        } return null;
    }

    // adds entry to player's userMap:<map_time,prevented_type>
    // (map time == preventedTypes' time + additional time)
    public boolean addMapTime(Player player, String preventedType, int additional_time) {
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
        Integer highest_map_ticks = getTickTime(playerName);
        int potential_play_time = current_play_ticks+additional_time;

        if (highest_map_ticks==null || highest_map_ticks<potential_play_time) {
            saveCooldownEntry(playerName, preventedType, potential_play_time);
            return true;
        } return false;
    }



    // returns String used by TogglePvpCmd.java for preventing togglepvp usage
    public String getHighestTickAndType(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null || userMap.isEmpty()) return "0 none";

        int highest_map_ticks = userMap.lastKey();
        String preventedType = userMap.get(highest_map_ticks);
        return (highest_map_ticks + " " + preventedType);
    }

    // returns String used by TogglePvpCmd.java for output to CommandSender
    public String getHighestTypeAndTick(String username) {
        SortedMap<Integer, String> userMap = entries.get(username);
        if (userMap == null || userMap.isEmpty()) return "(no cooldowns)";

        int highest_map_ticks = userMap.lastKey();
        String preventedType = userMap.get(highest_map_ticks);
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
                return (preventedType + " " + getFormattedTime(highest_map_ticks) + " playtime");
            }
        }

        int time_difference = (highest_map_ticks-current_play_ticks)/20;
        return (preventedType + " " + getFormattedTime(time_difference));
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