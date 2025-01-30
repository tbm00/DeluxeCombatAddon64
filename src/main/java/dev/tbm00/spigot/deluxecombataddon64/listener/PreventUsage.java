package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.hook.*;

public class PreventUsage implements Listener {
    private final DCHook dcHook;
    private final boolean checkAnchorExplosions;

    public PreventUsage(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, DCHook dcHook) {
        checkAnchorExplosions = configHandler.getCheckAnchorExplosions();
        this.dcHook = dcHook;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) return;
        if (dcHook==null || !checkAnchorExplosions) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Location location = clickedBlock.getLocation();
        boolean passDCPvpPlayerCheck = true, passDCPvpLocCheck = true;

        if (!passDCPvpLocCheck(location, 6.0)) passDCPvpLocCheck = false;
        else if (!passDCPvpPlayerCheck(player)) passDCPvpPlayerCheck = false;

        if (!passDCPvpPlayerCheck || !passDCPvpLocCheck) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Anchor explosion blocked -- pvp protection!"));
            event.setCancelled(true);
        }
    }

    private boolean passDCPvpLocCheck(Location location, double radius) {
        if (dcHook==null) return true;
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .noneMatch(player -> dcHook.hasProtection(player) || !dcHook.hasPvPEnabled(player));
    }
    
    private boolean passDCPvpPlayerCheck(Player player) {
        if (dcHook==null) return true;
        else if (dcHook.hasProtection(player) || !dcHook.hasPvPEnabled(player)) return false;
        return true;
    }
}
