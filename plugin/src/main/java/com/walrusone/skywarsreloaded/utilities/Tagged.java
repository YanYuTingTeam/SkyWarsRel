package com.walrusone.skywarsreloaded.utilities;

import org.bukkit.entity.Player;


public class Tagged {
	private Player player;
	private Long time;
	private boolean isProjectile;
	
	public Tagged(Player player, Long time) {
		this(player, time, false);
	}
	
	public Tagged(Player player, Long time, boolean isProjectile) {
		this.player = player;
		this.time = time;
		this.isProjectile = isProjectile;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Long getTime() {
		return time;
	}
	
	public boolean isProjectile() {
		return isProjectile;
	}
}
