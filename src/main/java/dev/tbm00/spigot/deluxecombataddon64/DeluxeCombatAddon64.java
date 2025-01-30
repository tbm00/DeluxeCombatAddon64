package dev.tbm00.spigot.deluxecombataddon64;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
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
            ChatColor.DARK_PURPLE + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
		);

        if (getConfig().getBoolean("enabled")) {
            configHandler = new ConfigHandler(this);
            if (configHandler.isEnabled()) {
                setupHooks();

                if (configHandler.isDisabledAfterCombat() || configHandler.isDisabledAfterJoin()) {
                    // Connect LogManager
                    entryManager = new EntryManager(this);

                    // Register commands and listeners
                    getCommand("togglepvp").setExecutor(new TogglePvpCmd(this, configHandler, entryManager, dcHook));
                    getServer().getPluginManager().registerEvents(new PlayerConnection(this, configHandler, entryManager), this);
                }

                // respawn anchor explosion check listener
                if (configHandler.getCheckAnchorExplosions())
                    getServer().getPluginManager().registerEvents(new PreventUsage(this, configHandler, dcHook), this);
                
            } else {
                getLogger().severe("Either itemEntries is disabled or there was an error in config... disabling plugin!");
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