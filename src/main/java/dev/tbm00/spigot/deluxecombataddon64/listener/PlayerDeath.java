package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.data.EntryManager;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class PlayerDeath implements Listener {
    private final EntryManager entryManager;
    private final ConfigHandler configHandler;
    private final DCHook dcHook;

    public PlayerDeath(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, EntryManager entryManager, DCHook dcHook) {
        this.entryManager = entryManager;
        this.configHandler = configHandler;
        this.dcHook = dcHook;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer!=null && configHandler.isPreventedAfterMurder()) {
            entryManager.addMapTime(killer, "MURDER", configHandler.getPreventedAfterMurderTicks());
        }
        if (victim!=null) {
            if (configHandler.isPreventedAfterDeath()) {
                entryManager.addMapTime(victim, "DEATH", configHandler.getPreventedAfterDeathTicks());
            }
            if (configHandler.isForceEnabledAfterDeath() && !dcHook.hasPvPEnabled(victim)) {
                dcHook.togglePvP(victim, true);
                sendMessage(victim, configHandler.getForceEnabledAfterDeathMessage());
            }
        }
    }

    private void sendMessage(CommandSender target, String string) {
        if (!string.isBlank())
            target.spigot().sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', configHandler.getChatPrefix() + string)));
    }
}