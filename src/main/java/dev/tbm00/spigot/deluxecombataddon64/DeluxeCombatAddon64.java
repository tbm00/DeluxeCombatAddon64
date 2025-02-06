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
                    // Connect JSON
                    try {
                        jsonHandler = new JSONHandler(this);
                        log(ChatColor.GREEN, "JSON connected.");
                    } catch (Exception e) {
                        getLogger().severe("JSON connection failed -- disabling plugin!");
                        disablePlugin();
                        return;
                    }

                    // Connect EntryManager
                    entryManager = new EntryManager(this, jsonHandler);

                    // Register toggle pvp command
                    getCommand("pvp").setExecutor(new TogglePvpCmd(this, configHandler, entryManager, dcHook));

                    // Register listeners based on config
                    if (configHandler.isPreventedAfterCombat())
                        getServer().getPluginManager().registerEvents(new PlayerCombat(this, configHandler, entryManager), this);
                    if (configHandler.isPreventedAfterMurder()||configHandler.isForceEnabledAfterDeath()||configHandler.isForceEnabledAfterDeath())
                        getServer().getPluginManager().registerEvents(new PlayerDeath(this, configHandler, entryManager, dcHook), this);
                    if (configHandler.isPreventedAfterJoin()||configHandler.isPreventedAfterCombatLog())
                        getServer().getPluginManager().registerEvents(new PlayerConnection(this, configHandler, entryManager), this);
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

    private void setupHooks() {
        if (!setupDeluxeCombat()) {
            getLogger().severe("DeluxeCombat hook failed -- disabling plugin!");
            disablePlugin();
            return;
        }
    }

    private boolean setupDeluxeCombat() {
        if (getServer().getPluginManager().getPlugin("DeluxeCombat")==null) return false;

        dcHook = new DCHook();
        
        log(ChatColor.GREEN, "DeluxeCombat hooked.");
        return true;
    }

    private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}

    public void log(ChatColor chatColor, String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + chatColor + s);
	}

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
        log(ChatColor.RED, "DeluxeCombatAddon64 disabled..!");
    }

    @Override
    public void onDisable() {
        if (configHandler.isDataSavedOnDisable()) entryManager.saveDataToJson();
    }
}