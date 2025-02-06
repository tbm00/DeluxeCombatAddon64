package dev.tbm00.spigot.deluxecombataddon64.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import dev.tbm00.spigot.deluxecombataddon64.DeluxeCombatAddon64;
import dev.tbm00.spigot.deluxecombataddon64.ConfigHandler;
import dev.tbm00.spigot.deluxecombataddon64.hook.DCHook;

public class PlayerAnchorInteraction implements Listener {
    private final DCHook dcHook;

    public PlayerAnchorInteraction(DeluxeCombatAddon64 javaPlugin, ConfigHandler configHandler, DCHook dcHook) {
        this.dcHook = dcHook;
    }

    /**
     * Handles the player interaction with a respawn anchor.
     * Cancels the interaction if the player or location does not pass the PvP protection check.
     *
     * @param event the PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        boolean passDCPvpPlayerCheck = true, passDCPvpLocCheck = true;

        if (!passDCPvpLocCheck(location, 6.0)) passDCPvpLocCheck = false;
        else if (!passDCPvpPlayerCheck(player)) passDCPvpPlayerCheck = false;

        if (!passDCPvpPlayerCheck || !passDCPvpLocCheck) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Anchor explosion blocked -- pvp protection!"));
            event.setCancelled(true);
        }
    }

    /**
     * Checks if the location is within the PvP protection radius.
     * 
     * @param location the location to check
     * @param radius the radius within which to check for players
     * @return true if no protected players are found, false otherwise
     */
    private boolean passDCPvpLocCheck(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
            .filter(entity -> entity instanceof Player)
            .map(entity -> (Player) entity)
            .noneMatch(player -> dcHook.hasProtection(player) || !dcHook.hasPvPEnabled(player));
    }
    
    /**
     * Checks if the player passes the PvP protection check.
     * 
     * @param player the player to check
     * @return true if the player is not protected and PvP is enabled, false otherwise
     */
    private boolean passDCPvpPlayerCheck(Player player) {
        if (dcHook.hasProtection(player) || !dcHook.hasPvPEnabled(player)) return false;
        else return true;
    }
}
