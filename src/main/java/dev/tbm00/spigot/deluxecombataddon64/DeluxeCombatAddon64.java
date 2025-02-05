package dev.tbm00.spigot.deluxecombataddon64;


import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import dev.tbm00.spigot.deluxecombataddon64.command.TogglePvpCmd;
import dev.tbm00.spigot.deluxecombataddon64.hook.*;
import dev.tbm00.spigot.deluxecombataddon64.listener.*;

public class DeluxeCombatAddon64 extends JavaPlugin {
    private ConfigHandler configHandler;
    private EntryManager entryManager;
    private DCHook dcHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        final PluginDescriptionFile pdf = getDescription();

		log(
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
            pdf.getName() + " v" + pdf.getVersion() + " created by tbm00",
            pdf.getDescription(),
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
		);

        if (getConfig().getBoolean("enabled")) {
            configHandler = new ConfigHandler(this);
            if (configHandler.isEnabled()) {
                
                setupHooks();
                if (configHandler.isTogglePvpCommandEnabled()) {
                    // Connect EntryManager
                    entryManager = new EntryManager(this);

                    // Register toggle pvp command
                    getCommand("pvp").setExecutor(new TogglePvpCmd(this, configHandler, entryManager, dcHook));

                    // Register listeners based on config
                    if (configHandler.isPreventedAfterCombat())
                        getServer().getPluginManager().registerEvents(new PlayerCombat(this, configHandler, entryManager), this);
                    if (configHandler.isPreventedAfterMurder())
                        getServer().getPluginManager().registerEvents(new PlayerMurder(this, configHandler, entryManager), this);
                    if (configHandler.isPreventedAfterJoin())
                        getServer().getPluginManager().registerEvents(new PlayerJoin(this, configHandler, entryManager), this);
                }

                // Register respawn anchor explosion listener based on config
                if (configHandler.getCheckAnchorExplosions())
                    getServer().getPluginManager().registerEvents(new PlayerAnchorInteraction(this, configHandler, dcHook), this);
                
            } else {
                logYellow("Either config.enabled is false, or there was an error in config... disabling plugin!");
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
        
        logGreen("DeluxeCombat hooked.");
        return true;
    }

    private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}

    public void log(String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + ChatColor.LIGHT_PURPLE + s);
	}

    public void logGreen(String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + ChatColor.GREEN + s);
	}

    public void logYellow(String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + ChatColor.YELLOW + s);
	}

    public void logRed(String... strings) {
		for (String s : strings)
            getServer().getConsoleSender().sendMessage("[DeluxeCombatAddon64] " + ChatColor.RED + s);
	}

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
        logRed("DeluxeCombatAddon64 disabled..!");
    }
}