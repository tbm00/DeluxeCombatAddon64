package dev.tbm00.spigot.deluxecombataddon64;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigHandler {
    private static boolean enabled = false;

    // "fixes"
    private boolean checkAnchorExplosions = false;
    
    // "togglePvpCommand"
    private String chatPrefix = null;
    private String enabledMessage = null;
    private String enabledByOtherMessage = null;
    private String disabledMessage = null;
    private String disabledByOtherMessage = null;
    private String disabledGraceMessage = null;
    private String disabledGraceByOtherMessage = null;
    private String preventedToggleWorldsMessage = null;
    private String preventedToggleInCombatMessage = null;
    private String preventedToggleTimerMessage = null;
    private Set<String> disabledWorlds = new HashSet<>();
    private boolean disabledInCombat = false;
    private boolean disabledAfterJoin = false;
    private int disabledAfterJoinTicks = 0;
    private boolean disabledAfterCombat = false;
    private int disabledAfterCombatTicks = 0;
    private boolean disabledAfterMurder = false;
    private int disabledAfterMurderTicks = 0;
    private boolean commandCooldown = false;
    private int commandCooldownTicks = 0;

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
            if (togglePvpSection != null) {
                loadTogglePvp(togglePvpSection);
            }
        } catch (Exception e) {
            javaPlugin.logRed("Caught exception loading config: ");
            javaPlugin.getLogger().warning(e.getMessage());
            enabled = false;
        }
    }

    private void loadFixes(ConfigurationSection fixSection) {
        checkAnchorExplosions = fixSection.contains("anchorExplosionPvpCheck") ? fixSection.getBoolean("anchorExplosionPvpCheck") : false;
    }

    private void loadTogglePvp(ConfigurationSection togglePvpSection) {
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
            preventedToggleWorldsMessage = chatSection.contains("preventedToggleWorldsMessage") ? chatSection.getString("preventedToggleWorldsMessage") : null;
            preventedToggleInCombatMessage = chatSection.contains("preventedToggleInCombatMessage") ? chatSection.getString("preventedToggleInCombatMessage") : null;
            preventedToggleTimerMessage = chatSection.contains("preventedToggleTimerMessage") ? chatSection.getString("preventedToggleTimerMessage") : null;
        }
        
        // Load Disabled Worlds
        List<String> worldsHolder = togglePvpSection.contains("disabledInWorlds") ? togglePvpSection.getStringList("disabledInWorlds") : null;
        disabledWorlds.addAll(worldsHolder);

        // Load Disabled in Combat
        checkAnchorExplosions = togglePvpSection.contains("disabledInCombat") ? togglePvpSection.getBoolean("disabledInCombat") : false;

        // Load disabledAfterJoin
        ConfigurationSection afterJoinSec = togglePvpSection.contains("disabledAfterJoin") ? togglePvpSection.getConfigurationSection("disabledAfterJoin") : null;
        if (afterJoinSec != null) { 
            disabledAfterJoin = afterJoinSec.contains("enabled") ? afterJoinSec.getBoolean("enabled") : false;
            if (disabledAfterJoin) {
                disabledAfterJoinTicks = afterJoinSec.contains("time") ? afterJoinSec.getInt("time")*20 : 0;
            }
        }

        // Load disabledAfterCombat
        ConfigurationSection afterCombatSec = togglePvpSection.contains("disabledAfterCombat") ? togglePvpSection.getConfigurationSection("disabledAfterCombat") : null;
        if (afterCombatSec != null) {
            disabledAfterCombat = afterCombatSec.contains("enabled") ? afterCombatSec.getBoolean("enabled") : false;
            if (disabledAfterCombat) {
                disabledAfterCombatTicks = afterCombatSec.contains("time") ? afterCombatSec.getInt("time")*20 : 0;
            }
        }

        // Load disabledAfterMurder
        ConfigurationSection afterMurderSec = togglePvpSection.contains("disabledAfterMurder") ? togglePvpSection.getConfigurationSection("disabledAfterMurder") : null;
        if (afterMurderSec != null) {
            disabledAfterMurder = afterMurderSec.contains("enabled") ? afterMurderSec.getBoolean("enabled") : false;
            if (disabledAfterMurder) {
                disabledAfterMurderTicks = afterMurderSec.contains("time") ? afterMurderSec.getInt("time")*20 : 0;
            }
        }

        // Load commandCooldown
        ConfigurationSection cmdCooldownSec = togglePvpSection.contains("commandCooldown") ? togglePvpSection.getConfigurationSection("commandCooldown") : null;
        if (cmdCooldownSec != null) {
            commandCooldown = cmdCooldownSec.contains("enabled") ? cmdCooldownSec.getBoolean("enabled") : false;
            if (commandCooldown) {
                commandCooldownTicks = cmdCooldownSec.contains("time") ? cmdCooldownSec.getInt("time")*20 : 0;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
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

    public String getPreventedToggleWorldsMessage() {
        return preventedToggleWorldsMessage;
    }
    
    public String getPreventedToggleInCombatMessage() {
        return preventedToggleInCombatMessage;
    }
    
    public String getPreventedToggleTimerMessage() {
        return preventedToggleTimerMessage;
    }    

    public boolean getCheckAnchorExplosions() {
        return checkAnchorExplosions;
    }

    public Set<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public boolean getDisabledInCombat() {
        return disabledInCombat;
    }

    public boolean isDisabledAfterJoin() {
        return disabledAfterJoin;
    }

    public int getDisabledAfterJoinTicks() {
        return disabledAfterJoinTicks;
    }
    
    public boolean isDisabledAfterCombat() {
        return disabledAfterCombat;
    }

    public int getDisabledAfterCombatTicks() {
        return disabledAfterCombatTicks;
    }

    public boolean isDisabledAfterMurder() {
        return disabledAfterMurder;
    }

    public int getDisabledAfterMurderTicks() {
        return disabledAfterMurderTicks;
    }

    public boolean getCommandCooldown() {
        return commandCooldown;
    }

    public int getCommandCooldownTicks() {
        return commandCooldownTicks;
    }
}
