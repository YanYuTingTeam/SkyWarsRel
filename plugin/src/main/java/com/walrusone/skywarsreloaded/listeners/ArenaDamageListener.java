package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.enums.GameType;
import com.walrusone.skywarsreloaded.utilities.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.Crate;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.game.PlayerData;
import com.walrusone.skywarsreloaded.managers.MatchManager;

public class ArenaDamageListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDamagedByAlly(EntityDamageByEntityEvent event) {
		Player target;
		Entity damager = event.getDamager();
		if (event.getEntity() instanceof Player) {
			target = (Player) event.getEntity();
			GameMap gameMap = MatchManager.get().getPlayerMap(target);
			if (gameMap != null) {
				if(!gameMap.getSpectators().contains(target.getUniqueId())) {
					if (gameMap.getMatchState() == MatchState.ENDING || gameMap.getMatchState() == MatchState.WAITINGSTART) {
						event.setCancelled(true);
					} else {
						event.setCancelled(false);
						if (gameMap.getProjectilesOnly()) {
							if (damager instanceof Projectile) {
								doProjectile(gameMap, damager, event, target);
							} else if (damager instanceof Player) {
								event.setCancelled(true);
							}
						} else {
							if (damager instanceof Projectile) {
								doProjectile(gameMap, damager, event, target);
							} else if (damager instanceof Player) {
								doPVP(damager, target, event, gameMap);
							}
						}
					}
				}
			}
		}
	}

	private void doProjectile(GameMap gMap, Entity damager, EntityDamageByEntityEvent event, Player target) {
		Projectile proj = (Projectile) damager;
		if (damager instanceof Snowball) {
			event.setDamage(SkyWarsReloaded.getCfg().getSnowDamage());
		}
		if (damager instanceof Egg) {
			event.setDamage(SkyWarsReloaded.getCfg().getEggDamage());
		}
		if (gMap.isDoubleDamageEnabled()) {
			event.setDamage(event.getDamage()*2);
		}
		if (proj.getShooter() instanceof Player) {
			Player hitter = (Player) proj.getShooter();
			if (hitter != null && hitter != target) {
				PlayerData pd = PlayerData.getPlayerData(target.getUniqueId());
				if (pd != null) {
					pd.setTaggedBy(hitter, true);
					if (damager instanceof Arrow) {
						double distance = hitter.getLocation().distance(target.getLocation());
						double damage = event.getFinalDamage();
						double hurtTwo = Math.round(damage * 100) / 100.0;
						double hurtOne = Math.round(damage * 10) / 10.0;
						double remainingHealth = target.getHealth() - damage;
						if (remainingHealth < 0) remainingHealth = 0;
						String bowHitMsg = SkyWarsReloaded.getExtConfig().getBowHit()
							.replace("{target}", target.getName())
							.replace("{hurt_two}", String.format("%.2f", hurtTwo))
							.replace("{hurt_one}", String.format("%.1f", hurtOne))
							.replace("{distance}", String.format("%.2f", distance));
						hitter.sendMessage(ChatColor.translateAlternateColorCodes('&', bowHitMsg));
						String healthsMsg = SkyWarsReloaded.getExtConfig().getHealths()
							.replace("{player}", target.getName())
							.replace("{hurt_two}", String.format("%.2f", remainingHealth))
							.replace("{hurt_one}", String.format("%.1f", Math.round(remainingHealth * 10) / 10.0));
						hitter.sendMessage(ChatColor.translateAlternateColorCodes('&', healthsMsg));
					} else if (damager instanceof FishHook) {
						double damage = event.getFinalDamage();
						double remainingHealth = target.getHealth() - damage;
						if (remainingHealth < 0) remainingHealth = 0;
						
						String healthsMsg = SkyWarsReloaded.getExtConfig().getHealths()
							.replace("{player}", target.getName())
							.replace("{hurt_two}", String.format("%.2f", remainingHealth))
							.replace("{hurt_one}", String.format("%.1f", Math.round(remainingHealth * 10) / 10.0));
						hitter.sendMessage(ChatColor.translateAlternateColorCodes('&', healthsMsg));
					}
				}
			}
		}
	}

	private void doPVP(Entity damager, Player target, EntityDamageByEntityEvent event, GameMap gMap) {
		Player hitter = (Player) damager;
		PlayerData pd = PlayerData.getPlayerData(target.getUniqueId());
		if (gMap.isDoubleDamageEnabled()) {
			event.setDamage(event.getDamage()*2);
		}
		if (pd != null) {
			pd.setTaggedBy(hitter);
			if (SkyWarsReloaded.getShowDamageManager().isShowDamageEnabled(hitter.getUniqueId())) {
				double damage = event.getFinalDamage();
				double hurtOne = Math.round(damage * 10) / 10.0;
				String hitSubtitleMsg = SkyWarsReloaded.getExtConfig().getHitSubtitle()
					.replace("{hurt_one}", String.format("%.1f", hurtOne));
				
				Util.get().sendTitle(hitter,
					SkyWarsReloaded.getExtConfig().getFadein(),
					SkyWarsReloaded.getExtConfig().getStay(),
					SkyWarsReloaded.getExtConfig().getFadeout(),
					"",
					ChatColor.translateAlternateColorCodes('&', hitSubtitleMsg));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerDamaged(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameMap gameMap = MatchManager.get().getPlayerMap(player);
			if (gameMap != null) {
				if (gameMap.getMatchState() == MatchState.ENDING || gameMap.getMatchState() == MatchState.WAITINGSTART) {
					event.setCancelled(true);
					return;
				}
				if (!gameMap.allowFallDamage()) {
					if (event.getCause().equals(DamageCause.FALL)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void satLoss(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameMap gameMap = MatchManager.get().getPlayerMap(player);
			if (gameMap != null) {
				if (gameMap.getMatchState() == MatchState.WAITINGSTART) {
					event.setCancelled(true);
				}
			}		
		}
	}
	
	@EventHandler
	public void regen(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		GameMap gameMap = MatchManager.get().getPlayerMap(player);
		if (gameMap != null) {
			if (!gameMap.allowRegen()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void bowEvent(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		GameMap gameMap = MatchManager.get().getPlayerMap(player);
		if (gameMap != null) {
			if (gameMap.getMatchState() == MatchState.WAITINGSTART || gameMap.getMatchState() == MatchState.ENDING) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void arrowEvent(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			final Arrow arrow = (Arrow) event.getEntity();
			if (arrow.getShooter() instanceof Player) {
				Player player = (Player) arrow.getShooter();
				GameMap gameMap = MatchManager.get().getPlayerMap(player);
				if (gameMap != null) {
						arrow.remove();
				}
			}
		}
	}
	
	@EventHandler
	public void onAnvilLand(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof FallingBlock) {
			FallingBlock fb = (FallingBlock) event.getEntity();
			if (SkyWarsReloaded.getNMS().checkMaterial(fb, Material.ANVIL)) {
				for (GameMap gMap: GameMap.getPlayableArenas(GameType.ALL)) {
					if (gMap.getAnvils().contains(event.getEntity().getUniqueId().toString())) {
						event.setCancelled(true);
						gMap.getAnvils().remove(event.getEntity().getUniqueId().toString());
						return;
					}
				}
			} else if (SkyWarsReloaded.getNMS().checkMaterial(fb, Material.SAND)) {
				for (GameMap gMap: GameMap.getPlayableArenas(GameType.ALL)) {
					for (Crate crate: gMap.getCrates()) {
						if (fb.equals(crate.getEntity())) {
							event.setCancelled(true);
							fb.setDropItem(false);
							fb.getWorld().getBlockAt(fb.getLocation()).setType(Material.ENDER_CHEST);
							crate.setLocation(fb.getWorld().getBlockAt(fb.getLocation()));
							fb.remove();
						}
					}

				}
			}
        }
	}
}
