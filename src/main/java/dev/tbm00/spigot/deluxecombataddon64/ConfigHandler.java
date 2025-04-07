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
    
    // "bountyProtectionCommand"
    private boolean bountyProtCommandEnabled = false;
    private boolean saveProtDataOnDisable = true;
    private String protChatPrefix = null;
    private String protectionEnabledMessage = null;
    private String currentProtectionTimeMessage = null;
    private String noCurrentProtectionMessage = null;
    private String cannotSetBountyMessage = null;

    // "togglePVPCommand"
    private boolean togglePVPCommandEnabled = false;
    private boolean savePVPDataOnDisable = false;
    private String pvpChatPrefix = null;
    private String enabledMessage = null;
    private String enabledByOtherMessage = null;
    private String disabledMessage = null;
    private String disabledByOtherMessage = null;
    private String disabledGraceMessage = null;
    private String disabledGraceByOtherMessage = null;
    private String preventedToggleInWorldsMessage = null;
    private String preventedToggleWithBountyMessage = null;
    private String preventedToggleInCombatMessage = null;
    private Map<String, String> preventedToggleAfterMessages;
    private boolean forceEnabledAfterDeath = false;
    private boolean forceEnabledAfterSetBounty = false;
    private String forceEnabledAfterMessage = null;
    private Set<String> preventedWorlds = new HashSet<>();
    private boolean preventedWithBounty = false;
    private boolean preventedInCombat = false;
    private boolean preventedAfterCombat = false;
    private int preventedAfterCombatTicks = 0;
    private boolean preventedAfterMurder = false;
    private int preventedAfterMurderTicks = 0;
    private boolean preventedAfterPVPDeath = false;
    private int preventedAfterPVPDeathTicks = 0;
    private boolean preventedAfterPVEDeath = false;
    private int preventedAfterPVEDeathTicks = 0;
    private boolean preventedAfterCombatLog = false;
    private int preventedAfterCombatLogTicks = 0;
    private boolean preventedAfterJoin = false;
    private int preventedAfterJoinTicks = 0;
    private boolean preventedAfterSetBounty = false;
    private int preventedAfterSetBountyTicks = 0;
    private boolean preventedAfterEnable = false;
    private int preventedAfterEnableTicks = 0;

    /**
     * Constructs a ConfigHandler instance.
     * Loads configuration values for the plugin, including settings for PvP toggling and fixes.
     *
     * @param javaPlugin the main plugin instance
     */
    public ConfigHandler(DeluxeCombatAddon64 javaPlugin) {
        try {
            enabled = javaPlugin.getConfig().contains("enabled") ? javaPlugin.getConfig().getBoolean("enabled") : false;

            // Load Fixes
            ConfigurationSection fixSection = javaPlugin.getConfig().getConfigurationSection("fixes");
            if (fixSection != null) {
                loadFixes(fixSection);
            }

            // Load Bounty Protection Command Mechanics
            ConfigurationSection bountyProtSection = javaPlugin.getConfig().getConfigurationSection("bountyProtectionCommand");
            if (bountyProtSection.getBoolean("enabled")) {
                bountyProtCommandEnabled = true;
                loadBountyProt(bountyProtSection);
            }

            // Load TogglePVP Command Mechanics
            ConfigurationSection togglePVPSection = javaPlugin.getConfig().getConfigurationSection("togglePvpCommand");
            if (togglePVPSection.getBoolean("enabled")) {
                togglePVPCommandEnabled = true;
                loadTogglePVP(togglePVPSection);
            }
        } catch (Exception e) {
            javaPlugin.log(ChatColor.RED, "Caught exception loading config: ");
            javaPlugin.getLogger().warning(e.getMessage());
            enabled = false;
        }
    }

    /**
     * Loads the "fixes" section of the configuration.
     * 
     * @param fixSection the configuration section for "fixes"
     */
    private void loadFixes(ConfigurationSection fixSection) {
        checkAnchorExplosions = fixSection.contains("anchorExplosionPVPCheck") ? fixSection.getBoolean("anchorExplosionPVPCheck") : false;
    }

    /**
     * Loads the "bountyProtectionCommand" section of the configuration.
     * 
     * @param bountyProtSection the configuration section for "bountyProtectionCommand"
     */
    private void loadBountyProt(ConfigurationSection bountyProtSection) {
        // Load saveMapOnPluginDisable
        savePVPDataOnDisable = bountyProtSection.contains("saveMapOnPluginDisable") ? bountyProtSection.getBoolean("saveMapOnPluginDisable") : true;

        // Load Chat Messages
        ConfigurationSection chatSection = bountyProtSection.getConfigurationSection("chat");
        if (chatSection != null) {
            protChatPrefix = chatSection.contains("prefix") ? chatSection.getString("prefix") : null;
            protectionEnabledMessage = chatSection.contains("protectionEnabledMessage") ? chatSection.getString("protectionEnabledMessage") : null;
            currentProtectionTimeMessage = chatSection.contains("currentProtectionTimeMessage") ? chatSection.getString("currentProtectionTimeMessage") : null;
            noCurrentProtectionMessage = chatSection.contains("noCurrentProtectionMessage") ? chatSection.getString("noCurrentProtectionMessage") : null;
            cannotSetBountyMessage = chatSection.contains("cannotSetBountyMessage") ? chatSection.getString("cannotSetBountyMessage") : null;
        }
    }

    /**
     * Loads the "togglePVPCommand" section of the configuration.
     * 
     * @param togglePVPSection the configuration section for "togglePVPCommand"
     */
    private void loadTogglePVP(ConfigurationSection togglePVPSection) {
        // Load saveMapOnPluginDisable
        savePVPDataOnDisable = togglePVPSection.contains("saveMapOnPluginDisable") ? togglePVPSection.getBoolean("saveMapOnPluginDisable") : true;

        // Load Chat Messages
        ConfigurationSection chatSection = togglePVPSection.getConfigurationSection("chat");
        if (chatSection != null) {
            pvpChatPrefix = chatSection.contains("prefix") ? chatSection.getString("prefix") : null;
            enabledMessage = chatSection.contains("enabledMessage") ? chatSection.getString("enabledMessage") : null;
            enabledByOtherMessage = chatSection.contains("enabledByOtherMessage") ? chatSection.getString("enabledByOtherMessage") : null;
            disabledMessage = chatSection.contains("disabledMessage") ? chatSection.getString("disabledMessage") : null;
            disabledByOtherMessage = chatSection.contains("disabledByOtherMessage") ? chatSection.getString("disabledByOtherMessage") : null;
            disabledGraceMessage = chatSection.contains("disabledGraceMessage") ? chatSection.getString("disabledGraceMessage") : null;
            disabledGraceByOtherMessage = chatSection.contains("disabledGraceByOtherMessage") ? chatSection.getString("disabledGraceByOtherMessage") : null;
            forceEnabledAfterMessage = chatSection.contains("forceEnabledAfterMessage") ? chatSection.getString("forceEnabledAfterMessage") : null;
            preventedToggleInCombatMessage = chatSection.contains("preventedToggleInCombatMessage") ? chatSection.getString("preventedToggleInCombatMessage") : null;
            preventedToggleInWorldsMessage = chatSection.contains("preventedToggleInWorldsMessage") ? chatSection.getString("preventedToggleInWorldsMessage") : null;
            preventedToggleWithBountyMessage = chatSection.contains("preventedToggleWithBountyMessage") ? chatSection.getString("preventedToggleWithBountyMessage") : null;
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
            if (chatSection.contains("preventedToggleAfterEnablingMessage"))
                savePreventedMessage("ENABLE", chatSection.getString("preventedToggleAfterEnablingMessage"));
            if (chatSection.contains("preventedToggleAfterSetBountyMessage"))
                savePreventedMessage("SETBOUNTY", chatSection.getString("preventedToggleAfterSetBountyMessage"));
            if (chatSection.contains("preventedToggleAfterBonusMessage"))
                savePreventedMessage("BONUS", chatSection.getString("preventedToggleAfterBonusMessage"));
        }

        // Load forceEnabledAfterDeath
        forceEnabledAfterDeath = togglePVPSection.contains("forceEnabledAfterDeath") ? togglePVPSection.getBoolean("forceEnabledAfterDeath") : false;

        // Load forceEnabledAfterSetBounty
        forceEnabledAfterSetBounty = togglePVPSection.contains("forceEnabledAfterSetBounty") ? togglePVPSection.getBoolean("forceEnabledAfterSetBounty") : false;

        // Load preventedWorlds
        List<String> worldsHolder = togglePVPSection.contains("preventedInWorlds") ? togglePVPSection.getStringList("preventedInWorlds") : null;
        preventedWorlds.addAll(worldsHolder);

        // Load preventedWithBounty
        preventedWithBounty = togglePVPSection.contains("preventedWithBounty") ? togglePVPSection.getBoolean("preventedWithBounty") : false;

        // Load preventedInCombat
        preventedInCombat = togglePVPSection.contains("preventedInCombat") ? togglePVPSection.getBoolean("preventedInCombat") : false;

        // Load preventedAfterCombat
        ConfigurationSection afterCombatSec = togglePVPSection.contains("preventedAfterCombat") ? togglePVPSection.getConfigurationSection("preventedAfterCombat") : null;
        if (afterCombatSec != null) {
            preventedAfterCombat = afterCombatSec.contains("enabled") ? afterCombatSec.getBoolean("enabled") : false;
            if (preventedAfterCombat) {
                preventedAfterCombatTicks = afterCombatSec.contains("time") ? afterCombatSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterMurder
        ConfigurationSection afterMurderSec = togglePVPSection.contains("preventedAfterMurder") ? togglePVPSection.getConfigurationSection("preventedAfterMurder") : null;
        if (afterMurderSec != null) {
            preventedAfterMurder = afterMurderSec.contains("enabled") ? afterMurderSec.getBoolean("enabled") : false;
            if (preventedAfterMurder) {
                preventedAfterMurderTicks = afterMurderSec.contains("time") ? afterMurderSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterPVPDeath
        ConfigurationSection afterPVPDeathSec = togglePVPSection.contains("preventedAfterPVPDeath") ? togglePVPSection.getConfigurationSection("preventedAfterPVPDeath") : null;
        if (afterPVPDeathSec != null) {
            preventedAfterPVPDeath = afterPVPDeathSec.contains("enabled") ? afterPVPDeathSec.getBoolean("enabled") : false;
            if (preventedAfterPVPDeath) {
                preventedAfterPVPDeathTicks = afterPVPDeathSec.contains("time") ? afterPVPDeathSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterPVEDeath
        ConfigurationSection afterPVEDeathSec = togglePVPSection.contains("preventedAfterPVEDeath") ? togglePVPSection.getConfigurationSection("preventedAfterPVEDeath") : null;
        if (afterPVEDeathSec != null) {
            preventedAfterPVEDeath = afterPVEDeathSec.contains("enabled") ? afterPVEDeathSec.getBoolean("enabled") : false;
            if (preventedAfterPVEDeath) {
                preventedAfterPVEDeathTicks = afterPVEDeathSec.contains("time") ? afterPVEDeathSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterCombatLog
        ConfigurationSection afterCombatLogSec = togglePVPSection.contains("preventedAfterCombatLog") ? togglePVPSection.getConfigurationSection("preventedAfterCombatLog") : null;
        if (afterCombatLogSec != null) {
            preventedAfterCombatLog = afterCombatLogSec.contains("enabled") ? afterCombatLogSec.getBoolean("enabled") : false;
            if (preventedAfterCombatLog) {
                preventedAfterCombatLogTicks = afterCombatLogSec.contains("time") ? afterCombatLogSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterJoin
        ConfigurationSection afterJoinSec = togglePVPSection.contains("preventedAfterJoin") ? togglePVPSection.getConfigurationSection("preventedAfterJoin") : null;
        if (afterJoinSec != null) { 
            preventedAfterJoin = afterJoinSec.contains("enabled") ? afterJoinSec.getBoolean("enabled") : false;
            if (preventedAfterJoin) {
                preventedAfterJoinTicks = afterJoinSec.contains("time") ? afterJoinSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterEnable
        ConfigurationSection afterEnableSec = togglePVPSection.contains("preventedAfterEnable") ? togglePVPSection.getConfigurationSection("preventedAfterEnable") : null;
        if (afterEnableSec != null) {
            preventedAfterEnable = afterEnableSec.contains("enabled") ? afterEnableSec.getBoolean("enabled") : false;
            if (preventedAfterEnable) {
                preventedAfterEnableTicks = afterEnableSec.contains("time") ? afterEnableSec.getInt("time")*20 : 0;
            }
        }

        // Load preventedAfterSetBounty
        ConfigurationSection afterSetBountySec = togglePVPSection.contains("preventedAfterSetBounty") ? togglePVPSection.getConfigurationSection("preventedAfterSetBounty") : null;
        if (afterSetBountySec != null) { 
            preventedAfterSetBounty = afterSetBountySec.contains("enabled") ? afterSetBountySec.getBoolean("enabled") : false;
            if (preventedAfterSetBounty) {
                preventedAfterSetBountyTicks = afterSetBountySec.contains("time") ? afterSetBountySec.getInt("time")*20 : 0;
            }
        }
    }

    /**
     * Saves the message associated with a prevented type.
     *
     * @param preventedType the type/reason of prevention (e.g., "COMBAT", "MURDER", etc.)
     * @param preventedMessage the message to be saved
     */
    public void savePreventedMessage(String preventedType, String preventedMessage) {
        preventedToggleAfterMessages.put(preventedType, preventedMessage);
    }

    /**
     * Retrieves the message associated with a specific prevented type.
     * 
     * @param preventedType the type/reason of prevention (e.g., "COMBAT", "MURDER", etc.)
     * @return the message associated with the provided prevented type, or null if not found
     */
    public String getPreventedMessage(String preventedType) {
        return preventedToggleAfterMessages.get(preventedType);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isProtDataSavedOnDisable() {
        return saveProtDataOnDisable;
    }

    public boolean isBountyProtCommandEnabled() {
        return bountyProtCommandEnabled;
    }

    public String getProtChatPrefix() {
        return protChatPrefix;
    }

    public String getProtectionEnabledMessage() {
        return protectionEnabledMessage;
    }

    public String getCurrentProtectionTimeMessage() {
        return currentProtectionTimeMessage;
    }

    public String getNoCurrentProtectionMessage() {
        return noCurrentProtectionMessage;
    }

    public String getCannotSetBountyMessage() {
        return cannotSetBountyMessage;
    }

    public boolean isPVPDataSavedOnDisable() {
        return savePVPDataOnDisable;
    }

    public boolean isTogglePVPCommandEnabled() {
        return togglePVPCommandEnabled;
    }

    public String getPVPChatPrefix() {
        return pvpChatPrefix;
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

    public String getPreventedToggleWithBountyMessage() {
        return preventedToggleWithBountyMessage;
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

    public boolean isForceEnabledAfterSetBounty() {
        return forceEnabledAfterSetBounty;
    }

    public String getForceEnabledAfterMessage() {
        return forceEnabledAfterMessage;
    }

    public Set<String> getPreventedWorlds() {
        return preventedWorlds;
    }

    public boolean isPreventedWithBounty() {
        return preventedWithBounty;
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

    public boolean isPreventedAfterPVPDeath() {
        return preventedAfterPVPDeath;
    }

    public int getPreventedAfterPVPDeathTicks() {
        return preventedAfterPVPDeathTicks;
    }

    public boolean isPreventedAfterPVEDeath() {
        return preventedAfterPVEDeath;
    }

    public int getPreventedAfterPVEDeathTicks() {
        return preventedAfterPVEDeathTicks;
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

    public boolean isPreventedAfterSetBounty() {
        return preventedAfterSetBounty;
    }

    public int getPreventedAfterSetBountyTicks() {
        return preventedAfterSetBountyTicks;
    }
}
