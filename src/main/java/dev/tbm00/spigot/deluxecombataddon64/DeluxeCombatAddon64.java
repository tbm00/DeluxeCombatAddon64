package dev.tbm00.spigot.deluxecombataddon64;


import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import dev.tbm00.spigot.deluxecombataddon64.command.TogglePvpCmd;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.data.JSONHandler;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;
import dev.tbm00.spigot.deluxecombataddon64.listener.*;

public class DeluxeCombatAddon64 extends JavaPlugin {
    private ConfigHandler configHandler;
    private JSONHandler jsonHandler;
    private EntryManager entryManager;
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
                if (configHandler.isTogglePvpCommandEnabled()) {
                    // Connect jsonHandler
                    try {
                        jsonHandler = new JSONHandler(this);
                        log(ChatColor.GREEN, "JSON connected.");
                    } catch (Exception e) {
                        getLogger().severe("JSON connection failed -- disabling plugin!");
                        disablePlugin();
                        return;
                    }

                    // Connect entryManager
                    entryManager = new EntryManager(this, jsonHandler);

                    // Register TogglePvpCmd
                    getCommand("pvp").setExecutor(new TogglePvpCmd(this, configHandler, entryManager, dcHook));
                    //getCommand("togglepvp").setExecutor(new TogglePvpCmd(this, configHandler, entryManager, dcHook));

                    // Register listeners based on config
                    if (configHandler.isPreventedAfterCombat())
                        getServer().getPluginManager().registerEvents(new PlayerCombat(this, configHandler, entryManager), this);
                    if (configHandler.isPreventedAfterMurder()||configHandler.isForceEnabledAfterDeath()||configHandler.isForceEnabledAfterDeath())
                        getServer().getPluginManager().registerEvents(new PlayerDeath(this, configHandler, entryManager, dcHook), this);
                    if (configHandler.isPreventedAfterJoin()||configHandler.isPreventedAfterCombatLog())
                        getServer().getPluginManager().registerEvents(new PlayerConnection(this, configHandler, entryManager), this);
                    if (configHandler.isPreventedAfterSetBounty()||configHandler.isForceEnabledAfterSetBounty())
                        getServer().getPluginManager().registerEvents(new PlayerSetBounty(this, configHandler, entryManager, dcHook), this);
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
        if (configHandler.isDataSavedOnDisable()) entryManager.saveDataToJson();
    }
}