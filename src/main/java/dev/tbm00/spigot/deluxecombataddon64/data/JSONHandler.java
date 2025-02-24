package dev.tbm00.spigot.deluxecombataddon64.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bukkit.plugin.java.JavaPlugin;

public class JSONHandler {
    private final JavaPlugin javaPlugin;

    private final Object cFileLock = new Object();
    private File cooldownFile;
    private Gson cooldownGson;

    private final Object pFileLock = new Object();
    private File protectionFile;
    private Gson protectionGson;

    /**
     * Constructs a JSONHandler instance for managing player cooldown data.
     * Initializes the Gson object and sets up the database (JSON file).
     *
     * @param javaPlugin the main plugin instance
     */
    public JSONHandler(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.cooldownGson = new Gson();
        this.protectionGson = new Gson();
        initializeCooldownDatabase();
        initializeProtectionDatabase();
    }

    /**
     * Initializes the cooldown database (JSON file).
     * Creates the file if it does not exist and saves an empty player map to it.
     */
    private void initializeCooldownDatabase() {
        synchronized (cFileLock) {
            cooldownFile = new File(javaPlugin.getDataFolder(), "player_cooldowns.json");
            if (!cooldownFile.exists()) {
                try {
                    cooldownFile.getParentFile().mkdirs();
                    cooldownFile.createNewFile();
                    saveCooldownMap(new HashMap<>());
                } catch (IOException e) {
                    javaPlugin.getLogger().severe("Exception when creating new JSON file!");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads and passes the cooldown player map from the JSON file.
     * 
     * @return the loaded player map, or an empty map if an error occurs
     */
    public Map<String, SortedMap<Integer, String>> loadCooldownMap() {
        synchronized (cFileLock) {
            try (FileReader reader = new FileReader(cooldownFile)) {
                Type type = new TypeToken<Map<String, SortedMap<Integer, String>>>() {}.getType();
                Map<String, SortedMap<Integer, String>> playerMap = cooldownGson.fromJson(reader, type);
                if (playerMap == null) {
                    playerMap = new HashMap<>();
                }
                return playerMap;
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Exception when loading JSON file!");
                e.printStackTrace();
                return new HashMap<>();
            }
        }
    }
    
    /**
     * Saves the given cooldown player map to the JSON file.
     * 
     * @param playerMap the player map to save
     */
    public void saveCooldownMap(Map<String, SortedMap<Integer, String>> playerMap) {
        synchronized (cFileLock) {
            try (FileWriter writer = new FileWriter(cooldownFile)) {
                cooldownGson.toJson(playerMap, writer);
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Exception when saving JSON file!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the protection database (JSON file).
     * Creates the file if it does not exist and saves an empty player map to it.
     */
    private void initializeProtectionDatabase() {
        synchronized (pFileLock) {
            protectionFile = new File(javaPlugin.getDataFolder(), "player_protections.json");
            if (!protectionFile.exists()) {
                try {
                    protectionFile.getParentFile().mkdirs();
                    protectionFile.createNewFile();
                    saveProtectionMap(new HashMap<>());
                } catch (IOException e) {
                    javaPlugin.getLogger().severe("Exception when creating new JSON file!");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads and passes the protection player map from the JSON file.
     * 
     * @return the loaded player map, or an empty map if an error occurs
     */
    public Map<String, Integer> loadProtectionMap() {
        synchronized (pFileLock) {
            try (FileReader reader = new FileReader(protectionFile)) {
                Type type = new TypeToken<Map<String, Integer>>() {}.getType();
                Map<String, Integer> playerMap = protectionGson.fromJson(reader, type);
                if (playerMap == null) {
                    playerMap = new HashMap<>();
                }
                return playerMap;
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Exception when loading JSON file!");
                e.printStackTrace();
                return new HashMap<>();
            }
        }
    }
    
    /**
     * Saves the given protection player map to the JSON file.
     * 
     * @param playerMap the player map to save
     */
    public void saveProtectionMap(Map<String, Integer> playerMap) {
        synchronized (pFileLock) {
            try (FileWriter writer = new FileWriter(protectionFile)) {
                protectionGson.toJson(playerMap, writer);
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Exception when saving JSON file!");
                e.printStackTrace();
            }
        }
    }
}

