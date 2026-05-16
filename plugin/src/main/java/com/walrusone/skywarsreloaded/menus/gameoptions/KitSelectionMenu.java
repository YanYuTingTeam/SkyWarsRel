package com.walrusone.skywarsreloaded.menus.gameoptions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.menus.gameoptions.objects.GameKit;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Util;

public class KitSelectionMenu {

    private static int menuSize = SkyWarsReloaded.getCfg().getKitMenuSize();
    private static final String menuName = new Messaging.MessageFormatter().format("menu.kit-section-menu");
    
    public KitSelectionMenu(final Player player) {
    	GameMap gMap = MatchManager.get().getPlayerMap(player);
        List<GameKit> availableItems = GameKit.getAvailableKits();
        if (availableItems.size() > 0) {
        	ArrayList<Inventory> invs = new ArrayList<>();

        	for (GameKit kit: availableItems) {
                int pos = kit.getPosition();
                int page = kit.getPage() - 1;

                if(invs.isEmpty() || invs.size() < page + 1) {
                    while (invs.size() < page + 1) {
                        invs.add(Bukkit.createInventory(null, menuSize + 9, menuName));
                    }
                }
                List<String> loreList = kit.getColorLores();
                ItemStack item = kit.getIcon();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getName()));
                    meta.setLore(loreList);
                    item.setItemMeta(meta);
                }
                invs.get(page).setItem(pos, item);
            }
            if (gMap != null) {
            	SkyWarsReloaded.getIC().create(player, invs, event -> {
                    String name = event.getName();
                    if (name.equalsIgnoreCase(SkyWarsReloaded.getNMS().getItemName(SkyWarsReloaded.getIM().getItem("exitMenuItem")))) {
                        player.closeInventory();
                        return;
                    }
                    GameKit kit = GameKit.getKit(name);
                    if (kit == null) {
                        return;
                    }

                    if (kit.needPermission()) {
                        if (!player.hasPermission("sw.kit." + kit.getFilename())) {
                            Util.get().playSound(player, player.getLocation(), SkyWarsReloaded.getCfg().getErrorSound(), 1, 1);
                            return;
                        }
                    }

                    player.closeInventory();
                    Util.get().playSound(player, player.getLocation(), SkyWarsReloaded.getCfg().getConfirmeSelctionSound(), 1, 1);
                    gMap.setKitVote(player, kit);
                    player.sendMessage(new Messaging.MessageFormatter().setVariable("kit", kit.getFilename()).format("game.select-kit"));
                });
            }
            if (player != null) {
                SkyWarsReloaded.getIC().show(player, null);
            }
        }
    }
}
