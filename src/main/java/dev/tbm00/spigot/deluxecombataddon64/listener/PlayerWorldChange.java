package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;
import dev.tbm00.spigot.deluxecombataddon64.data.CooldownManager;

public class PlayerWorldChange implements Listener {
    private final DeluxeCombatAddon64 javaPlugin;
    private final DCHook dcHook;
    private final CooldownManager cooldownManager;

    public PlayerWorldChange(DeluxeCombatAddon64 javaPlugin, DCHook dcHook, CooldownManager cooldownManager) {
        this.javaPlugin = javaPlugin;
        this.dcHook = dcHook;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;

        Player target = event.getPlayer();
        String[] pair = cooldownManager.getActiveCooldown(target);
        Integer highest_map_ticks = Integer.parseInt(pair[0]);
        if (highest_map_ticks<1) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dcHook.hasPvPEnabled(target)) {
                    dcHook.togglePvP(target, true);
                }
            }
        }.runTaskLater(javaPlugin, 4);
    }
}