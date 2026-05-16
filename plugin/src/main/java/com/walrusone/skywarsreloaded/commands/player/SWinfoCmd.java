package com.walrusone.skywarsreloaded.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.walrusone.skywarsreloaded.commands.BaseCmd;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.Messaging;

public class SWinfoCmd extends BaseCmd { 
	
	public SWinfoCmd(String t) {
		type = t;
		forcePlayer = true;
		cmdName = "info";
		alias = new String[]{"i","information","author"};
		argLength = 1; //counting cmdName
	}

	@Override
	public boolean run() {
		Player statPlayer = player;
        player.sendMessage("§f");
        player.sendMessage("§3§lSkywarsRel §r§7for §b§lYanYuTing §8| §r§fby §ewalrusone§f, §eAerMini");
        player.sendMessage("§f");
		return true;
	}

}
