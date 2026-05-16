package com.walrusone.skywarsreloaded.utilities.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class SWRPlaceholderAPI extends PlaceholderExpansion {

	private SkyWarsReloaded swr;
	
	public SWRPlaceholderAPI(Plugin plugin) {
		this.swr = (SkyWarsReloaded) plugin;
	}

	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public boolean canRegister(){
		return true;
	}

	@Override
	public String getAuthor(){
		return swr.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier(){
		return "swr";
	}

	@Override
	public String getVersion(){
		return swr.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (identifier.equals("elo")) {
			return "" + swr.getPlayerStat(player).getElo();
		}
		
		if (identifier.equals("wins")) {
			return "" + swr.getPlayerStat(player).getWins();
		}
		
		if (identifier.equals("losses")) {
			return "" + swr.getPlayerStat(player).getLosses();
		}
		
		if (identifier.equals("kills")) {
			return "" + swr.getPlayerStat(player).getKills();
		}
		
		if (identifier.equals("deaths")) {
			return "" + swr.getPlayerStat(player).getDeaths();
		}
		
		if (identifier.equals("xp")) {
			return "" + swr.getPlayerStat(player).getXp();
		}
		
		if (identifier.equals("games_played")) {
			return "" + (swr.getPlayerStat(player).getLosses() + swr.getPlayerStat(player).getWins());
		}
		
		if (identifier.equals("kill_death")) {
			double stat = (double)swr.getPlayerStat(player).getKills()/(double)swr.getPlayerStat(player).getDeaths();
			return String.format("%1$,.2f", stat);
		}
		
		if (identifier.equals("win_loss")) {
			double stat = (double)swr.getPlayerStat(player).getWins()/(double)swr.getPlayerStat(player).getLosses();
			return String.format("%1$,.2f", stat);
		}
		
		return null;
	}
}
