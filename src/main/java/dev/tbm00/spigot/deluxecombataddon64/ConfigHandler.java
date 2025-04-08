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
    private String preventDisableInWorldsMessage = null;
    private String preventDisableWithBountyMessage = null;
    private String preventDisableInCombatMessage = null;
    private Map<String, String> preventDisableAfterMessages;
    private boolean forceEnabledAfterDeath = false;
    private boolean forceEnabledAfterSetBounty = false;
    private String forceEnabledAfterMessage = null;
    private boolean cooldownWorldChangeCheck = false;
    private Set<String> preventDisableWorlds = new HashSet<>();
    private Set<String> cooldownNotAppliedInWorlds = new HashSet<>();
    private boolean preventDisableWithBounty = false;
    private boolean preventDisableInCombat = false;
    private boolean preventDisableAfterCombat = false;
    private int preventDisableAfterCombatTicks = 0;
    private boolean preventDisableAfterMurder = false;
    private int preventDisableAfterMurderTicks = 0;
    private boolean preventDisableAfterPVPDeath = false;
    private int preventDisableAfterPVPDeathTicks = 0;
    private boolean preventDisableAfterPVEDeath = false;
    private int preventDisableAfterPVEDeathTicks = 0;
    private boolean preventDisableAfterCombatLog = false;
    private int preventDisableAfterCombatLogTicks = 0;
    private boolean preventDisableAfterJoin = false;
    private int preventDisableAfterJoinTicks = 0;
    private boolean preventDisableAfterSetBounty = false;
    private int preventDisableAfterSetBountyTicks = 0;
    private boolean preventDisableAfterEnable = false;
    private int preventDisableAfterEnableTicks = 0;

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
            preventDisableInCombatMessage = chatSection.contains("preventDisableInCombatMessage") ? chatSection.getString("preventDisableInCombatMessage") : null;
            preventDisableInWorldsMessage = chatSection.contains("preventDisableInWorldsMessage") ? chatSection.getString("preventDisableInWorldsMessage") : null;
            preventDisableWithBountyMessage = chatSection.contains("preventDisableWithBountyMessage") ? chatSection.getString("preventDisableWithBountyMessage") : null;
            preventDisableAfterMessages = new ConcurrentHashMap<>();
            if (chatSection.contains("preventDisableAfterCombatMessage"))
                savePreventDisableMessage("COMBAT", chatSection.getString("preventDisableAfterCombatMessage"));
            if (chatSection.contains("preventDisableAfterMurderMessage"))
                savePreventDisableMessage("MURDER", chatSection.getString("preventDisableAfterMurderMessage"));
            if (chatSection.contains("preventDisableAfterDeathMessage"))
                savePreventDisableMessage("DEATH", chatSection.getString("preventDisableAfterDeathMessage"));
            if (chatSection.contains("preventDisableAfterCombatLogMessage"))
                savePreventDisableMessage("COMBATLOG", chatSection.getString("preventDisableAfterCombatLogMessage"));
            if (chatSection.contains("preventDisableAfterJoinMessage"))
                savePreventDisableMessage("JOIN", chatSection.getString("preventDisableAfterJoinMessage"));
            if (chatSection.contains("preventDisableAfterEnablingMessage"))
                savePreventDisableMessage("ENABLE", chatSection.getString("preventDisableAfterEnablingMessage"));
            if (chatSection.contains("preventDisableAfterSetBountyMessage"))
                savePreventDisableMessage("SETBOUNTY", chatSection.getString("preventDisableAfterSetBountyMessage"));
            if (chatSection.contains("preventDisableAfterBonusMessage"))
                savePreventDisableMessage("BONUS", chatSection.getString("preventDisableAfterBonusMessage"));
        }

        // Load forceEnabledAfterDeath
        forceEnabledAfterDeath = togglePVPSection.contains("forceEnabledAfterDeath") ? togglePVPSection.getBoolean("forceEnabledAfterDeath") : false;

        // Load forceEnabledAfterSetBounty
        forceEnabledAfterSetBounty = togglePVPSection.contains("forceEnabledAfterSetBounty") ? togglePVPSection.getBoolean("forceEnabledAfterSetBounty") : false;

        // Load cooldownWorldChangeCheck
        cooldownWorldChangeCheck = togglePVPSection.contains("cooldownWorldChangeCheck") ? togglePVPSection.getBoolean("cooldownWorldChangeCheck") : false;

        // Load preventDisableWorlds
        List<String> worldsHolder = togglePVPSection.contains("preventDisableInWorlds") ? togglePVPSection.getStringList("preventDisableInWorlds") : null;
        preventDisableWorlds.addAll(worldsHolder);

        // Load preventDisableWorlds
        List<String> worldsHolder2 = togglePVPSection.contains("cooldownNotAppliedInWorlds") ? togglePVPSection.getStringList("cooldownNotAppliedInWorlds") : null;
        cooldownNotAppliedInWorlds.addAll(worldsHolder2);

        // Load preventDisableWithBounty
        preventDisableWithBounty = togglePVPSection.contains("preventDisableWithBounty") ? togglePVPSection.getBoolean("preventDisableWithBounty") : false;

        // Load preventDisableInCombat
        preventDisableInCombat = togglePVPSection.contains("preventDisableInCombat") ? togglePVPSection.getBoolean("preventDisableInCombat") : false;

        // Load preventDisableAfterCombat
        ConfigurationSection afterCombatSec = togglePVPSection.contains("preventDisableAfterCombat") ? togglePVPSection.getConfigurationSection("preventDisableAfterCombat") : null;
        if (afterCombatSec != null) {
            preventDisableAfterCombat = afterCombatSec.contains("enabled") ? afterCombatSec.getBoolean("enabled") : false;
            if (preventDisableAfterCombat) {
                preventDisableAfterCombatTicks = afterCombatSec.contains("time") ? afterCombatSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterMurder
        ConfigurationSection afterMurderSec = togglePVPSection.contains("preventDisableAfterMurder") ? togglePVPSection.getConfigurationSection("preventDisableAfterMurder") : null;
        if (afterMurderSec != null) {
            preventDisableAfterMurder = afterMurderSec.contains("enabled") ? afterMurderSec.getBoolean("enabled") : false;
            if (preventDisableAfterMurder) {
                preventDisableAfterMurderTicks = afterMurderSec.contains("time") ? afterMurderSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterPVPDeath
        ConfigurationSection afterPVPDeathSec = togglePVPSection.contains("preventDisableAfterPVPDeath") ? togglePVPSection.getConfigurationSection("preventDisableAfterPVPDeath") : null;
        if (afterPVPDeathSec != null) {
            preventDisableAfterPVPDeath = afterPVPDeathSec.contains("enabled") ? afterPVPDeathSec.getBoolean("enabled") : false;
            if (preventDisableAfterPVPDeath) {
                preventDisableAfterPVPDeathTicks = afterPVPDeathSec.contains("time") ? afterPVPDeathSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterPVEDeath
        ConfigurationSection afterPVEDeathSec = togglePVPSection.contains("preventDisableAfterPVEDeath") ? togglePVPSection.getConfigurationSection("preventDisableAfterPVEDeath") : null;
        if (afterPVEDeathSec != null) {
            preventDisableAfterPVEDeath = afterPVEDeathSec.contains("enabled") ? afterPVEDeathSec.getBoolean("enabled") : false;
            if (preventDisableAfterPVEDeath) {
                preventDisableAfterPVEDeathTicks = afterPVEDeathSec.contains("time") ? afterPVEDeathSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterCombatLog
        ConfigurationSection afterCombatLogSec = togglePVPSection.contains("preventDisableAfterCombatLog") ? togglePVPSection.getConfigurationSection("preventDisableAfterCombatLog") : null;
        if (afterCombatLogSec != null) {
            preventDisableAfterCombatLog = afterCombatLogSec.contains("enabled") ? afterCombatLogSec.getBoolean("enabled") : false;
            if (preventDisableAfterCombatLog) {
                preventDisableAfterCombatLogTicks = afterCombatLogSec.contains("time") ? afterCombatLogSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterJoin
        ConfigurationSection afterJoinSec = togglePVPSection.contains("preventDisableAfterJoin") ? togglePVPSection.getConfigurationSection("preventDisableAfterJoin") : null;
        if (afterJoinSec != null) { 
            preventDisableAfterJoin = afterJoinSec.contains("enabled") ? afterJoinSec.getBoolean("enabled") : false;
            if (preventDisableAfterJoin) {
                preventDisableAfterJoinTicks = afterJoinSec.contains("time") ? afterJoinSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterEnable
        ConfigurationSection afterEnableSec = togglePVPSection.contains("preventDisableAfterEnable") ? togglePVPSection.getConfigurationSection("preventDisableAfterEnable") : null;
        if (afterEnableSec != null) {
            preventDisableAfterEnable = afterEnableSec.contains("enabled") ? afterEnableSec.getBoolean("enabled") : false;
            if (preventDisableAfterEnable) {
                preventDisableAfterEnableTicks = afterEnableSec.contains("time") ? afterEnableSec.getInt("time")*20 : 0;
            }
        }

        // Load preventDisableAfterSetBounty
        ConfigurationSection afterSetBountySec = togglePVPSection.contains("preventDisableAfterSetBounty") ? togglePVPSection.getConfigurationSection("preventDisableAfterSetBounty") : null;
        if (afterSetBountySec != null) { 
            preventDisableAfterSetBounty = afterSetBountySec.contains("enabled") ? afterSetBountySec.getBoolean("enabled") : false;
            if (preventDisableAfterSetBounty) {
                preventDisableAfterSetBountyTicks = afterSetBountySec.contains("time") ? afterSetBountySec.getInt("time")*20 : 0;
            }
        }
    }

    /**
     * Saves the message associated with a prevented type.
     *
     * @param cooldownType the type/reason of prevention (e.g., "COMBAT", "MURDER", etc.)
     * @param preventDisableMessage the message to be saved
     */
    public void savePreventDisableMessage(String cooldownType, String preventDisableMessage) {
        preventDisableAfterMessages.put(cooldownType, preventDisableMessage);
    }

    /**
     * Retrieves the message associated with a specific prevented type.
     * 
     * @param cooldownType the type/reason of prevention (e.g., "COMBAT", "MURDER", etc.)
     * @return the message associated with the provided preventDisable type, or null if not found
     */
    public String getPreventDisableMessage(String cooldownType) {
        return preventDisableAfterMessages.get(cooldownType);
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

    public String getPreventDisableInWorldsMessage() {
        return preventDisableInWorldsMessage;
    }

    public String getPreventDisableWithBountyMessage() {
        return preventDisableWithBountyMessage;
    }
    
    public String getPreventDisableInCombatMessage() {
        return preventDisableInCombatMessage;
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

    public Set<String> getPreventDisableWorlds() {
        return preventDisableWorlds;
    }

    public boolean getCooldownWorldChangeCheck() {
        return cooldownWorldChangeCheck;
    }

    public Set<String> getColdownNotAppliedInWorlds() {
        return cooldownNotAppliedInWorlds;
    }

    public boolean getPreventDisableWithBounty() {
        return preventDisableWithBounty;
    }

    public boolean getPreventDisableInCombat() {
        return preventDisableInCombat;
    }
    
    public boolean getPreventDisableAfterCombat() {
        return preventDisableAfterCombat;
    }

    public int getPreventDisableAfterCombatTicks() {
        return preventDisableAfterCombatTicks;
    }

    public boolean getPreventDisableAfterMurder() {
        return preventDisableAfterMurder;
    }

    public int getPreventDisableAfterMurderTicks() {
        return preventDisableAfterMurderTicks;
    }

    public boolean getPreventDisableAfterPVPDeath() {
        return preventDisableAfterPVPDeath;
    }

    public int getPreventDisableAfterPVPDeathTicks() {
        return preventDisableAfterPVPDeathTicks;
    }

    public boolean getPreventDisableAfterPVEDeath() {
        return preventDisableAfterPVEDeath;
    }

    public int getPreventDisableAfterPVEDeathTicks() {
        return preventDisableAfterPVEDeathTicks;
    }

    public boolean getPreventDisableAfterCombatLog() {
        return preventDisableAfterCombatLog;
    }

    public int getPreventDisableAfterCombatLogTicks() {
        return preventDisableAfterCombatLogTicks;
    }

    public boolean getPreventDisableAfterJoin() {
        return preventDisableAfterJoin;
    }

    public int getPreventDisableAfterJoinTicks() {
        return preventDisableAfterJoinTicks;
    }

    public boolean getPreventDisableAfterEnable() {
        return preventDisableAfterEnable;
    }

    public int getPreventDisableAfterEnableTicks() {
        return preventDisableAfterEnableTicks;
    }

    public boolean getPreventDisableAfterSetBounty() {
        return preventDisableAfterSetBounty;
    }

    public int getPreventDisableAfterSetBountyTicks() {
        return preventDisableAfterSetBountyTicks;
    }
}
