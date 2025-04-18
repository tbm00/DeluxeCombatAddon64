package dev.tbm00.spigot.deluxecombataddon64;


import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import dev.tbm00.spigot.deluxecombataddon64.command.*;
import dev.tbm00.spigot.deluxecombataddon64.data.*;
import dev.tbm00.spigot.deluxecombataddon64.hook.*;
import dev.tbm00.spigot.deluxecombataddon64.listener.*;

public class DeluxeCombatAddon64 extends JavaPlugin {
    private ConfigHandler configHandler;
    private JSONHandler jsonHandler;
    private CooldownManager cooldownManager;
    private ProtectionManager protectionManager;
    private DCHook dcHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final PluginDescriptionFile pdf = getDescription();

		log(ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
            pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
		);

        if (getConfig().getBoolean("enabled")) {
            configHandler = new ConfigHandler(this);
            if (configHandler.isEnabled()) {
                
                setupHooks();

                try { // Connect jsonHandler
                    jsonHandler = new JSONHandler(this);
                    log(ChatColor.GREEN, "JSON connected.");
                } catch (Exception e) {
                    getLogger().severe("JSON connection failed -- disabling plugin!");
                    disablePlugin();
                    return;
                }
                
                if (configHandler.isBountyProtCommandEnabled()) {
                    // Connect protectionManager
                    protectionManager = new ProtectionManager(this, jsonHandler);

                    // Register BountyProtCmd
                    getCommand("bountyprot").setExecutor(new BountyProtCmd(this, configHandler, protectionManager));
                }

                if (configHandler.isTogglePVPCommandEnabled()) {
                    // Connect cooldownManager
                    cooldownManager = new CooldownManager(this, jsonHandler);

                    // Register TogglePvpCmd
                    getCommand("pvp").setExecutor(new TogglePvpCmd(this, configHandler, cooldownManager, dcHook));
                    //getCommand("togglepvp").setExecutor(new TogglePvpCmd(this, configHandler, cooldownManager, dcHook));

                    // Register world change listener
                    if (configHandler.getCooldownWorldChangeCheck())
                        getServer().getPluginManager().registerEvents(new PlayerWorldChange(this, dcHook, cooldownManager), this);

                    // Register listeners based on config
                    if (configHandler.getPreventDisableAfterCombat())
                        getServer().getPluginManager().registerEvents(new PlayerCombat(this, configHandler, cooldownManager), this);
                    if (configHandler.getPreventDisableAfterMurder()||configHandler.isForceEnabledAfterDeath()||configHandler.isForceEnabledAfterDeath())
                        getServer().getPluginManager().registerEvents(new PlayerDeath(this, configHandler, cooldownManager, dcHook), this);
                    if (configHandler.getPreventDisableAfterJoin()||configHandler.getPreventDisableAfterCombatLog())
                        getServer().getPluginManager().registerEvents(new PlayerConnection(this, configHandler, cooldownManager), this);

                    // Register bounty listener if any part requires it
                    if (configHandler.getPreventDisableAfterSetBounty()||configHandler.isForceEnabledAfterSetBounty()||configHandler.isBountyProtCommandEnabled())
                        getServer().getPluginManager().registerEvents(new PlayerSetBounty(this, configHandler, cooldownManager, protectionManager, dcHook), this);
                }

                // Register respawn anchor explosion listener based on config
                if (configHandler.getCheckAnchorExplosions())
                    getServer().getPluginManager().registerEvents(new PlayerAnchorInteraction(this, configHandler, dcHook), this);
                
            } else {
                log(ChatColor.YELLOW, "Either config.enabled is false, or there was an error in config... disabling plugin!");
                disablePlugin();
            }
        } else {
            getLogger().warning("Plugin disabled in config..!");
            disablePlugin();
        }
    }

    /**
     * Sets up the required hooks for plugin integration.
     * Disables the plugin if any required hook fails.
     */
    private void setupHooks() {
        if (!setupDeluxeCombat()) {
            getLogger().severe("DeluxeCombat hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }
    }

    /**
     * Attempts to hook into the DeluxeCombat plugin.
     *
     * @return true if the hook was successful, false otherwise.
     */
    private boolean setupDeluxeCombat() {
        if (getServer().getPluginManager().getPlugin("DeluxeCombat")==null) return false;

        dcHook = new DCHook();
        
        log(ChatColor.GREEN, "DeluxeCombat hooked.");
        return true;
    }

    /**
     * Checks if the specified plugin is available and enabled on the server.
     *
     * @param pluginName the name of the plugin to check
     * @return true if the plugin is available and enabled, false otherwise.
     */
    private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}

    /**
     * Logs one or more messages to the server console with the prefix & specified chat color.
     *
     * @param chatColor the chat color to use for the log messages
     * @param strings one or more message strings to log
     */
    public void log(ChatColor chatColor, String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + chatColor + s);
	}

    /**
     * Disables the plugin.
     */
    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
        log(ChatColor.RED, "DeluxeCombatAddon64 disabled..!");
    }

    /**
     * Called when the plugin is disabled.
     * Saves plugin data if the config specifies to do so.
     */
    @Override
    public void onDisable() {
        if (configHandler.isPVPDataSavedOnDisable() && cooldownManager!=null) cooldownManager.saveDataToJson();
        if (configHandler.isProtDataSavedOnDisable() && cooldownManager!=null) cooldownManager.saveDataToJson();
    }
}