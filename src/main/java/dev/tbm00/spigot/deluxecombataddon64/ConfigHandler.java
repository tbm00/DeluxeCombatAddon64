package dev.tbm00.spigot.deluxecombataddon64;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.ChatColor;

public class ConfigHandler {
    private static boolean enabled = false;

    // "fixes"
    private boolean checkAnchorExplosions = false;
    
    // "togglePvpCommand"
    private boolean togglePvpCommandEnabled = false;
    private boolean saveDataOnDisable = false;
    private String chatPrefix = null;
    private String enabledMessage = null;
    private String enabledByOtherMessage = null;
    private String disabledMessage = null;
    private String disabledByOtherMessage = null;
    private String disabledGraceMessage = null;
    private String disabledGraceByOtherMessage = null;
    private String preventedToggleInWorldsMessage = null;
    private String preventedToggleInCombatMessage = null;
    private Map<String, String> preventedToggleAfterMessages;
    private boolean forceEnabledAfterDeath = false;
    private String forceEnabledAfterDeathMessage = null;
    private Set<String> preventedWorlds = new HashSet<>();
    private boolean preventedInCombat = false;
    private boolean preventedAfterCombat = false;
    private int preventedAfterCombatTicks = 0;
    private boolean preventedAfterMurder = false;
    private int preventedAfterMurderTicks = 0;
    private boolean preventedAfterDeath = false;
    private int preventedAfterDeathTicks = 0;
    private boolean preventedAfterCombatLog = false;
    private int preventedAfterCombatLogTicks = 0;
    private boolean preventedAfterJoin = false;
    private int preventedAfterJoinTicks = 0;
    private boolean preventedAfterEnable = false;
    private int preventedAfterEnableTicks = 0;
    private boolean preventedAfterDisable = false;
    private int preventedAfterDisableTicks = 0;

    public ConfigHandler(DeluxeCombatAddon64 javaPlugin) {
        try {
            enabled = javaPlugin.getConfig().contains("enabled") ? javaPlugin.getConfig().getBoolean("enabled") : false;

            // Load Fixes
            ConfigurationSection fixSection = javaPlugin.getConfig().getConfigurationSection("fixes");
            if (fixSection != null) {
                loadFixes(fixSection);
            }

            // Load TogglePvp Command Mechanics
            ConfigurationSection togglePvpSection = javaPlugin.getConfig().getConfigurationSection("togglePvpCommand");
            if (togglePvpSection.getBoolean("enabled")) {
                togglePvpCommandEnabled = true;
                loadTogglePvp(togglePvpSection);
            }
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception loading config: ");
            javaPlugin.getLogger().warning(e.getMessage());
            enabled = false;
        }
    }

    private void loadFixes(ConfigurationSection fixSection) {
        checkAnchorExplosions = fixSection.contains("anchorExplosionPvpCheck") ? fixSection.getBoolean("anchorExplosionPvpCheck") : false;
    }

    private void loadTogglePvp(ConfigurationSection togglePvpSection) {
        // Load force enabled after death 
        saveDataOnDisable = togglePvpSection.contains("saveMapOnPluginDisable") ? togglePvpSection.getBoolean("saveMapOnPluginDisable") : true;

        // Load Chat
        ConfigurationSection chatSection = togglePvpSection.getConfigurationSection("chat");
        if (chatSection != null) {
            chatPrefix = chatSection.contains("prefix") ? chatSection.getString("prefix") : null;
            enabledMessage = chatSection.contains("enabledMessage") ? chatSection.getString("enabledMessage") : null;
            enabledByOtherMessage = chatSection.contains("enabledByOtherMessage") ? chatSection.getString("enabledByOtherMessage") : null;
            disabledMessage = chatSection.contains("disabledMessage") ? chatSection.getString("disabledMessage") : null;
            disabledByOtherMessage = chatSection.contains("disabledByOtherMessage") ? chatSection.getString("disabledByOtherMessage") : null;
            disabledGraceMessage = chatSection.contains("disabledGraceMessage") ? chatSection.getString("disabledGraceMessage") : null;
            disabledGraceByOtherMessage = chatSection.contains("disabledGraceByOtherMessage") ? chatSection.getString("disabledGraceByOtherMessage") : null;
            forceEnabledAfterDeathMessage = chatSection.contains("forceEnabledAfterDeathMessage") ? chatSection.getString("forceEnabledAfterDeathMessage") : null;
            preventedToggleInCombatMessage = chatSection.contains("preventedToggleInCombatMessage") ? chatSection.getString("preventedToggleInCombatMessage") : null;
            preventedToggleInWorldsMessage = chatSection.contains("preventedToggleInWorldsMessage") ? chatSection.getString("preventedToggleInWorldsMessage") : null;
            preventedToggleAfterMessages = new ConcurrentHashMap<>();
            if (chatSection.contains("preventedToggleAfterCombatMessage"))
                savePreventedMessage("COMBAT", chatSection.getString("preventedToggleAfterCombatMessage"));
            if (chatSection.contains("preventedToggleAfterMurderMessage"))
                savePreventedMessage("MURDER", chatSection.getString("preventedToggleAfterMurderMessage"));
            if (chatSection.contains("preventedToggleAfterDeathMessage"))
                savePreventedMessage("DEATH", chatSection.getString("preventedToggleAfterDeathMessage"));
            if (chatSection.contains("preventedToggleAfterCombatLogMessage"))
                savePreventedMessage("COMBATLOG", chatSection.getString("preventedToggleAfterCombatLogMessage"));
            if (chatSection.contains("preventedToggleAfterJoinMessage"))
                savePreventedMessage("JOIN", chatSection.getString("preventedToggleAfterJoinMessage"));
            if (chatSection.contains("preventedToggleAfterToggleMessage"))
                savePreventedMessage("TOGGLE", chatSection.getString("preventedToggleAfterToggleMessage"));
            if (chatSection.contains("preventedToggleAfterBonusMessage"))
                savePreventedMessage("BONUS", chatSection.getString("preventedToggleAfterBonusMessage"));
        }

        // Load force enabled after death 
        forceEnabledAfterDeath = togglePvpSection.contains("forceEnabledAfterDeath") ? togglePvpSection.getBoolean("forceEnabledAfterDeath") : false;

        // Load Disabled Worlds
        List<String> worldsHolder = togglePvpSection.contains("preventedInWorlds") ? togglePvpSection.getStringList("preventedInWorlds") : null;
        preventedWorlds.addAll(worldsHolder);

        // Load Disabled in Combat
        preventedInCombat = togglePvpSection.contains("preventedInCombat") ? togglePvpSection.getBoolean("preventedInCombat") : false;

        // Load preventedAfterCombat
        ConfigurationSection afterCombatSec = togglePvpSection.contains("preventedAfterCombat") ? togglePvpSection.getConfigurationSection("preventedAfterCombat") : null;
        if (afterCombatSec != null) {
            preventedAfterCombat = afterCombatSec.contains("enabled") ? afterCombatSec.getBoolean("enabled") : false;
            if (preventedAfterCombat) {
                preventedAfterCombatTicks = afterCombatSec.contains("time") ? afterCombatSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterMurder
        ConfigurationSection afterMurderSec = togglePvpSection.contains("preventedAfterMurder") ? togglePvpSection.getConfigurationSection("preventedAfterMurder") : null;
        if (afterMurderSec != null) {
            preventedAfterMurder = afterMurderSec.contains("enabled") ? afterMurderSec.getBoolean("enabled") : false;
            if (preventedAfterMurder) {
                preventedAfterMurderTicks = afterMurderSec.contains("time") ? afterMurderSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterDeath
        ConfigurationSection afterDeathSec = togglePvpSection.contains("preventedAfterDeath") ? togglePvpSection.getConfigurationSection("preventedAfterDeath") : null;
        if (afterDeathSec != null) {
            preventedAfterDeath = afterDeathSec.contains("enabled") ? afterDeathSec.getBoolean("enabled") : false;
            if (preventedAfterDeath) {
                preventedAfterDeathTicks = afterDeathSec.contains("time") ? afterDeathSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterCombatLog
        ConfigurationSection afterCombatLogSec = togglePvpSection.contains("preventedAfterCombatLog") ? togglePvpSection.getConfigurationSection("preventedAfterCombatLog") : null;
        if (afterCombatLogSec != null) {
            preventedAfterCombatLog = afterCombatLogSec.contains("enabled") ? afterCombatLogSec.getBoolean("enabled") : false;
            if (preventedAfterCombatLog) {
                preventedAfterCombatLogTicks = afterCombatLogSec.contains("time") ? afterCombatLogSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterJoin
        ConfigurationSection afterJoinSec = togglePvpSection.contains("preventedAfterJoin") ? togglePvpSection.getConfigurationSection("preventedAfterJoin") : null;
        if (afterJoinSec != null) { 
            preventedAfterJoin = afterJoinSec.contains("enabled") ? afterJoinSec.getBoolean("enabled") : false;
            if (preventedAfterJoin) {
                preventedAfterJoinTicks = afterJoinSec.contains("time") ? afterJoinSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterEnable
        ConfigurationSection afterEnableSec = togglePvpSection.contains("preventedAfterEnable") ? togglePvpSection.getConfigurationSection("preventedAfterEnable") : null;
        if (afterEnableSec != null) {
            preventedAfterEnable = afterEnableSec.contains("enabled") ? afterEnableSec.getBoolean("enabled") : false;
            if (preventedAfterEnable) {
                preventedAfterEnableTicks = afterEnableSec.contains("time") ? afterEnableSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterDisable
        ConfigurationSection afterDisableSec = togglePvpSection.contains("preventedAfterDisable") ? togglePvpSection.getConfigurationSection("preventedAfterDisable") : null;
        if (afterDisableSec != null) {
            preventedAfterDisable = afterDisableSec.contains("enabled") ? afterDisableSec.getBoolean("enabled") : false;
            if (preventedAfterDisable) {
                preventedAfterDisableTicks = afterDisableSec.contains("time") ? afterDisableSec.getInt("time")*20 : 0;
            }
        }
    }

    // preventedType can be "COMBAT", "MURDER", "DEATH", "COMBATLOG", "JOIN", "TOGGLE", "BONUS", "none"
    public String getPreventedMessage(String preventedType) {
        return preventedToggleAfterMessages.get(preventedType);
    }

    public void savePreventedMessage(String preventedType, String preventedMessage) {
        preventedToggleAfterMessages.put(preventedType, preventedMessage);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDataSavedOnDisable() {
        return saveDataOnDisable;
    }

    public boolean isTogglePvpCommandEnabled() {
        return togglePvpCommandEnabled;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public String getEnabledMessage() {
        return enabledMessage;
    }
    
    public String getEnabledByOtherMessage() {
        return enabledByOtherMessage;
    }

    public String getDisabledMessage() {
        return disabledMessage;
    }

    public String getDisabledByOtherMessage() {
        return disabledByOtherMessage;
    }
    
    public String getDisabledGraceMessage() {
        return disabledGraceMessage;
    }
    
    public String getDisabledGraceByOtherMessage() {
        return disabledGraceByOtherMessage;
    }

    public String getPreventedToggleInWorldsMessage() {
        return preventedToggleInWorldsMessage;
    }
    
    public String getPreventedToggleInCombatMessage() {
        return preventedToggleInCombatMessage;
    }

    public boolean getCheckAnchorExplosions() {
        return checkAnchorExplosions;
    }

    public boolean isForceEnabledAfterDeath() {
        return forceEnabledAfterDeath;
    }

    public String getForceEnabledAfterDeathMessage() {
        return forceEnabledAfterDeathMessage;
    }

    public Set<String> getPreventedWorlds() {
        return preventedWorlds;
    }

    public boolean isPreventedInCombat() {
        return preventedInCombat;
    }
    
    public boolean isPreventedAfterCombat() {
        return preventedAfterCombat;
    }

    public int getPreventedAfterCombatTicks() {
        return preventedAfterCombatTicks;
    }

    public boolean isPreventedAfterMurder() {
        return preventedAfterMurder;
    }

    public int getPreventedAfterMurderTicks() {
        return preventedAfterMurderTicks;
    }

    public boolean isPreventedAfterDeath() {
        return preventedAfterDeath;
    }

    public int getPreventedAfterDeathTicks() {
        return preventedAfterDeathTicks;
    }

    public boolean isPreventedAfterCombatLog() {
        return preventedAfterCombatLog;
    }

    public int getPreventedAfterCombatLogTicks() {
        return preventedAfterCombatLogTicks;
    }

    public boolean isPreventedAfterJoin() {
        return preventedAfterJoin;
    }

    public int getPreventedAfterJoinTicks() {
        return preventedAfterJoinTicks;
    }

    public boolean isPreventedAfterEnable() {
        return preventedAfterEnable;
    }

    public int getPreventedAfterEnableTicks() {
        return preventedAfterEnableTicks;
    }

    public boolean isPreventedAfterDisable() {
        return preventedAfterDisable;
    }

    public int getPreventedAfterDisableTicks() {
        return preventedAfterDisableTicks;
    }
}
