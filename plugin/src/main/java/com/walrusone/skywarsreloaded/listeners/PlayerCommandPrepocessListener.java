package com.walrusone.skywarsreloaded.listeners;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.Listener;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.utilities.ShowDamageManager;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.utilities.Messaging;

public class PlayerCommandPrepocessListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPrepocess(final PlayerCommandPreprocessEvent e) {
    	GameMap gMap = MatchManager.get().getSpectatorMap(e.getPlayer());
    	String[] splited = e.getMessage().split("\\s+");
		if (splited[0].equalsIgnoreCase("/swi")) {
        	e.setCancelled(true);
        	e.getPlayer().sendMessage("§3§lS§lk§ly§lw§la§lr§ls§lR§le§ll §r§7f§7o§7r §b§lY§la§ln§lY§lu§lT§li§ln§lg§8 |§r §fb§fy§e §ew§fa§fl§fr§fu§fs§fo§fn§fe§f,§f §eA§ee§er§eM§ei§en§ei");
        	return;
    	}
		if (splited[0].equalsIgnoreCase("/shon")) {
    		e.setCancelled(true);
    		ShowDamageManager manager = SkyWarsReloaded.getShowDamageManager();
    		boolean currentState = manager.isShowDamageEnabled(e.getPlayer().getUniqueId());
    		if (currentState) {
    			String msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShon_e());
    			e.getPlayer().sendMessage(msg);
    		} else {
    			manager.setShowDamageEnabled(e.getPlayer().getUniqueId(), true);
    			String msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShon());
    			e.getPlayer().sendMessage(msg);
    		}
    		return;
    	}
	
    	if (splited[0].equalsIgnoreCase("/shoff")) {
    		e.setCancelled(true);
    		ShowDamageManager manager = SkyWarsReloaded.getShowDamageManager();
    		boolean currentState = manager.isShowDamageEnabled(e.getPlayer().getUniqueId());
		
    		if (!currentState) {
    			String msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShoff_e());
    			e.getPlayer().sendMessage(msg);
    		} else {
    			manager.setShowDamageEnabled(e.getPlayer().getUniqueId(), false);
    			String msg = ChatColor.translateAlternateColorCodes('&', SkyWarsReloaded.getExtConfig().getShoff());
    			e.getPlayer().sendMessage(msg);
    		}
    		return;
    	}
    	if (gMap != null) {
    		if (splited[0].equalsIgnoreCase("/spawn")) {
            	e.setCancelled(true);
            	gMap.getSpectators().remove(e.getPlayer().getUniqueId());
            	MatchManager.get().removeSpectator(e.getPlayer());
            	return;
    		}
    		
        	if (SkyWarsReloaded.getCfg().disableCommandsSpectate()) {
				if (e.getPlayer().hasPermission("sw.allowcommands")) {
					return;
				}
				for (final String a1 : SkyWarsReloaded.getCfg().getEnabledCommandsSpectate()) {
					if (splited.length == 1) {
						if (splited[0].equalsIgnoreCase("/" + a1)) {
							return;
						}
					} else {
						if (splited[0].equalsIgnoreCase("/" + a1) || (splited[0] + " " + splited[1]).equalsIgnoreCase("/" + a1)) {
							return;
						}
					}
				}
				e.getPlayer().sendMessage(new Messaging.MessageFormatter().format("game.command-disabled-spec"));
				e.setCancelled(true);
				return;
        	}
    	}
    		  	
    	if (MatchManager.get().getPlayerMap(e.getPlayer()) != null) {
        	if (e.getPlayer().hasPermission("sw.allowcommands")) {
        		return;
        	}
            for (final String a1 : SkyWarsReloaded.getCfg().getEnabledCommands()) {
            	if (splited.length == 1) {
                	if (splited[0].equalsIgnoreCase("/" + a1)) {
                        return;
                    }
            	} else if (splited.length > 1) {
                	if (splited[0].equalsIgnoreCase("/" + a1) || (splited[0] + " " + splited[1]).equalsIgnoreCase("/" + a1)) {
                        return;
                    }
            	}
            }
            e.getPlayer().sendMessage(new Messaging.MessageFormatter().format("game.command-disabled"));
            e.setCancelled(true);
        }
    }
}
