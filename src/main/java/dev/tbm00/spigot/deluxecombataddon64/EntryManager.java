package dev.tbm00.spigot.deluxecombataddon64;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntryManager {
    private Map<String, Integer> entries;

    public EntryManager(DeluxeCombatAddon64 javaPlugin) {
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
}