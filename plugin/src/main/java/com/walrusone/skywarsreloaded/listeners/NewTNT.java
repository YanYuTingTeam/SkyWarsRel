package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class NewTNT implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getItem().getType() != Material.TNT
                || (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)) {
            return;
        }
        if (event.useItemInHand() == Result.DENY
                || (event.getAction() == Action.LEFT_CLICK_BLOCK && event.useInteractedBlock() == Result.DENY)) {
            return;
        }
        Player player = event.getPlayer();
        GameMap gameMap = MatchManager.get().getPlayerMap(player);
        if (gameMap == null || gameMap.getMatchState() != MatchState.PLAYING
                || gameMap.getSpectators().contains(player.getUniqueId())
                || !player.getWorld().equals(gameMap.getCurrentWorld())) {
            return;
        }
        ItemStack itemInHand = SkyWarsReloaded.getNMS().getMainHandItem(player);
        if (itemInHand == null || itemInHand.getType() != Material.TNT || itemInHand.getAmount() < 1) {
            return;
        }
        event.setCancelled(true);
        TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(0.0, 2.0, 0.0), TNTPrimed.class);
        tnt.setVelocity(player.getLocation().getDirection().multiply(1.5));
        tnt.setFuseTicks(32);
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), itemInHand);
        } else {
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        }
        player.updateInventory();
    }
}
