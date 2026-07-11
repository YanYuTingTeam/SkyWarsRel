package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.utilities.MatchUtils;
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
		new BukkitRunnable() {
			@Override
			public void run() {
				String pendingGame = MatchUtils.consumePendingPlayer(a1.getPlayer().getUniqueId());
				if (pendingGame != null) {
					GameMap targetGame = GameMap.getMap(pendingGame);
					if (targetGame != null && targetGame.isRegistered()
							&& targetGame.getMatchState() == MatchState.WAITINGSTART
							&& targetGame.canAddPlayer()) {
						boolean joined = targetGame.addPlayers(null, a1.getPlayer());
						if (joined) {
							SkyWarsReloaded.get().getLogger().info("PlayerJoin > 自动加入待处理游戏: " + a1.getPlayer().getName() + " -> " + pendingGame);
						} else {
							SkyWarsReloaded.get().getLogger().info("PlayerJoin > 自动加入失败: " + a1.getPlayer().getName() + " -> " + pendingGame);
						}
					} else {
						GameMap bestGame = MatchUtils.getBestArena();
						if (bestGame != null) {
							bestGame.addPlayers(null, a1.getPlayer());
							SkyWarsReloaded.get().getLogger().info("PlayerJoin > 自动加入最佳房间: " + a1.getPlayer().getName() + " -> " + bestGame.getName());
						}
					}
				}
			}
		}.runTaskLater(SkyWarsReloaded.get(), 5L);
    }
}
