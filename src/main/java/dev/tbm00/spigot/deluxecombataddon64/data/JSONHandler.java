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
    private final Object fileLock = new Object();
    private File jsonFile;
    private Gson gson;

    /**
     * Constructs a JSONHandler instance for managing player cooldown data.
     * Initializes the Gson object and sets up the database (JSON file).
     *
     * @param javaPlugin the main plugin instance
     */
    public JSONHandler(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.gson = new Gson();
        initializeDatabase();
    }

    /**
     * Initializes the database (JSON file).
     * Creates the file if it does not exist and saves an empty player map to it.
     */
    private void initializeDatabase() {
        synchronized (fileLock) {
            jsonFile = new File(javaPlugin.getDataFolder(), "player_cooldowns.json");
            if (!jsonFile.exists()) {
                try {
                    jsonFile.getParentFile().mkdirs();
                    jsonFile.createNewFile();
                    savePlayerMap(new HashMap<>());
                } catch (IOException e) {
                    javaPlugin.getLogger().severe("Exception when creating new JSON file!");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Loads and passes the player map from the JSON file.
     * 
     * @return the loaded player map, or an empty map if an error occurs
     */
    public Map<String, SortedMap<Integer, String>> loadPlayerMap() {
        synchronized (fileLock) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type type = new TypeToken<Map<String, SortedMap<Integer, String>>>() {}.getType();
                Map<String, SortedMap<Integer, String>> playerMap = gson.fromJson(reader, type);
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
     * Saves the given player map to the JSON file.
     * 
     * @param playerMap the player map to save
     */
    public void savePlayerMap(Map<String, SortedMap<Integer, String>> playerMap) {
        synchronized (fileLock) {
            try (FileWriter writer = new FileWriter(jsonFile)) {
                gson.toJson(playerMap, writer);
            } catch (IOException e) {
                javaPlugin.getLogger().severe("Exception when saving JSON file!");
                e.printStackTrace();
            }
        }
    }
}

