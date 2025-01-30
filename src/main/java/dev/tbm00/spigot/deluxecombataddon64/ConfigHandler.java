package dev.tbm00.spigot.deluxecombataddon64;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigHandler {
    private final DeluxeCombatAddon64 javaPlugin;
    private static boolean enabled = false;
    
    // "chat"
    private String chatPrefix = null;

    // "fixes"
    private boolean checkAnchorExplosions = false;
    
    // "togglePvpCommand"
    private Set<String> disabledWorlds = new HashSet<>();
    private boolean disabledAfterJoin = false;
    private int disabledAfterJoinTicks = 0;
    private String disabledAfterJoinMessage = null;
    private boolean disabledAfterCombat = false;
    private int disabledAfterCombatTicks = 0;
    private String disabledAfterCombatMessage = null;
    private boolean disabledAfterMurder = false;
    private int disabledAfterMurderTicks = 0;
    private String disabledAfterMurderMessage = null;

    public ConfigHandler(DeluxeCombatAddon64 javaPlugin) {
        this.javaPlugin = javaPlugin;
        try {
            // Load Chat
            ConfigurationSection chatSection = javaPlugin.getConfig().getConfigurationSection("chat");
            if (chatSection != null) {
                loadChat(chatSection);
            }

            // Load Fixes
            ConfigurationSection fixSection = javaPlugin.getConfig().getConfigurationSection("fixes");
            if (fixSection != null) {
                loadFixes(fixSection);
            }

            // Load TogglePvp Command Mechanics
            ConfigurationSection togglePvpSection = javaPlugin.getConfig().getConfigurationSection("togglePvpCommand");
            if (togglePvpSection != null) {
                loadTogglePvp(togglePvpSection);
            }
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception loading config: ");
            javaPlugin.getLogger().warning(e.getMessage());
            enabled = false;
        }
    }

    private void loadChat(ConfigurationSection chatSection) {
        chatPrefix = chatSection.contains("prefix") ? chatSection.getString("prefix") : null;
    }

    private void loadFixes(ConfigurationSection fixSection) {
        checkAnchorExplosions = fixSection.contains("anchorExplosionPvpCheck") ? fixSection.getBoolean("anchorExplosionPvpCheck") : false;
    }

    private void loadTogglePvp(ConfigurationSection togglePvpSection) {
        List<String> worldsHolder = togglePvpSection.contains("disabledInWorlds") ? togglePvpSection.getStringList("disabledInWorlds") : null;
        disabledWorlds.addAll(worldsHolder);

        ConfigurationSection afterJoinSec = togglePvpSection.contains("disabledAfterJoin") ? togglePvpSection.getConfigurationSection("disabledAfterJoin") : null;
        if (afterJoinSec != null) { 
            disabledAfterJoin = afterJoinSec.contains("enabled") ? afterJoinSec.getBoolean("enabled") : false;
            if (disabledAfterJoin) {
                disabledAfterJoinTicks = afterJoinSec.contains("time") ? afterJoinSec.getInt("time")*20 : 0;
                disabledAfterJoinMessage = afterJoinSec.contains("message") ? afterJoinSec.getString("message") : null;
            }
        }

        ConfigurationSection afterCombatSec = togglePvpSection.contains("disabledAfterCombat") ? togglePvpSection.getConfigurationSection("disabledAfterCombat") : null;
        if (afterCombatSec != null) {
            disabledAfterCombat = afterCombatSec.contains("enabled") ? afterCombatSec.getBoolean("enabled") : false;
            if (disabledAfterCombat) {
                disabledAfterCombatTicks = afterCombatSec.contains("time") ? afterCombatSec.getInt("time")*20 : 0;
                disabledAfterCombatMessage = afterCombatSec.contains("message") ? afterCombatSec.getString("message") : null;
            }
        }

        ConfigurationSection afterMurderSec = togglePvpSection.contains("disabledAfterMurder") ? togglePvpSection.getConfigurationSection("disabledAfterMurder") : null;
        if (afterMurderSec != null) {
            disabledAfterMurder = afterMurderSec.contains("enabled") ? afterMurderSec.getBoolean("enabled") : false;
            if (disabledAfterMurder) {
                disabledAfterMurderTicks = afterMurderSec.contains("time") ? afterMurderSec.getInt("time")*20 : 0;
                disabledAfterMurderMessage = afterMurderSec.contains("message") ? afterMurderSec.getString("message") : null;
            }
        }

    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public boolean getCheckAnchorExplosions() {
        return checkAnchorExplosions;
    }

    public Set<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public boolean isDisabledAfterJoin() {
        return disabledAfterJoin;
    }

    public int getDisabledAfterJoinTicks() {
        return disabledAfterJoinTicks;
    }

    public String getDisabledAfterJoinMessage() {
        return disabledAfterJoinMessage;
    }
    
    public boolean isDisabledAfterCombat() {
        return disabledAfterCombat;
    }

    public int getDisabledAfterCombatTicks() {
        return disabledAfterCombatTicks;
    }

    public String getDisabledAfterCombatMessage() {
        return disabledAfterCombatMessage;
    }

    public boolean isDisabledAfterMurder() {
        return disabledAfterMurder;
    }

    public int getDisabledAfterMurderTicks() {
        return disabledAfterMurderTicks;
    }

    public String getDisabledAfterMurderMessage() {
        return disabledAfterMurderMessage;
    }
}
