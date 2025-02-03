package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.EntryManager;

public class PlayerMurder implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;

    public PlayerMurder(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
    }

    @EventHandler
    public void onPlayerMurder(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer==null) return;

        int additional_time = configHandler.getPreventedAfterMurderTicks();
        entryManager.setMapTime(killer, "MURDER", additional_time);

        if (configHandler.isPreventedAfterCombat()) {
            additional_time = configHandler.getPreventedAfterCombatTicks();
            entryManager.setMapTime(victim, "COMBAT", additional_time);
        }
    }
}