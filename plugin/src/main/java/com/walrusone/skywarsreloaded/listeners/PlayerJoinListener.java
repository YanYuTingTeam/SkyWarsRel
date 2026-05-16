package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.game.GameMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.ShowDamageManager;

public class PlayerJoinListener implements Listener
{
	@EventHandler
    public void onJoin(final PlayerJoinEvent a1) {
		a1.setJoinMessage(null);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (SkyWarsReloaded.getCfg().getSpawn() != null && SkyWarsReloaded.getCfg().teleportOnJoin()) {
					a1.getPlayer().teleport(SkyWarsReloaded.getCfg().getSpawn());
				}
			}
		}.runTaskLater(SkyWarsReloaded.get(), 1);

		new BukkitRunnable() {
			@Override
			public void run() {
				ShowDamageManager manager = SkyWarsReloaded.getShowDamageManager();
				boolean showDamage = manager.isShowDamageEnabled(a1.getPlayer().getUniqueId());
				String msg;
				if (showDamage) {
					msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShon_notif());
				} else {
					msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShoff_notif());
				}
				a1.getPlayer().sendMessage(msg);
			}
		}.runTaskLater(SkyWarsReloaded.get(), 1);

		if (SkyWarsReloaded.getCfg().promptForResource()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					a1.getPlayer().setResourcePack(SkyWarsReloaded.getCfg().getResourceLink());
				}
			}.runTaskLater(SkyWarsReloaded.get(), 20);
		}

		if (PlayerStat.getPlayerStats(a1.getPlayer()) != null) {
			PlayerStat.removePlayer(a1.getPlayer().getUniqueId().toString());
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				for(GameMap gMap: GameMap.getMaps()) {
					if (gMap.getCurrentWorld() != null && gMap.getCurrentWorld().equals(a1.getPlayer().getWorld())) {
						if (SkyWarsReloaded.getCfg().getSpawn() != null) {
							a1.getPlayer().teleport(SkyWarsReloaded.getCfg().getSpawn());
						}
					}
				}
			}
		}.runTaskLater(SkyWarsReloaded.get(), 1);

		PlayerStat.getPlayers().add(new PlayerStat(a1.getPlayer()));
    }
}
