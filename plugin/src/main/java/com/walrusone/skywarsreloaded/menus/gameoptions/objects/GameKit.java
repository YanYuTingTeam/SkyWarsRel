package com.walrusone.skywarsreloaded.menus.gameoptions.objects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Util;

public class GameKit {

	private static ArrayList<GameKit> kits = new ArrayList<>();
	private ItemStack[] inventory;
	private ItemStack[] armor;
	private ItemStack icon;
	private ItemStack lIcon;
	private String name;
	private String filename;
	private int position;
	private int page;
	private List<String> lores = new ArrayList<>();
	private boolean enabled;
	private boolean requirePermission;

    @SuppressWarnings("unchecked")
	public GameKit(File kitFile) {
        FileConfiguration storage = YamlConfiguration.loadConfiguration(kitFile);

        List<ItemStack> inventoryItems = (List<ItemStack>) storage.getList("inventory");
        if (inventoryItems != null) {
			inventory = inventoryItems.toArray(new ItemStack[0]);
		}

        List<ItemStack> armorItems = (List<ItemStack>) storage.getList("armor");

        if (armorItems != null) {
			armor = armorItems.toArray(new ItemStack[0]);
		}

        icon = storage.getItemStack("icon");
        
        lIcon = storage.getItemStack("lockedIcon");
        
        name = storage.getString("name");
        
        position = storage.getInt("position");
        
        page = storage.getInt("page");
        
        if (page == 0) {
        	page = 1;
        	storage.set("page", page);
        }
        
        lores = storage.getStringList("lores");
        
        enabled = storage.getBoolean("enabled");
        
        requirePermission = storage.getBoolean("requirePermission");
        
        filename = storage.getString("filename");
                
        try {
        	storage.save(kitFile);
		} catch (IOException e) {
        	SkyWarsReloaded.get().getLogger().info("Failed to save kit file!");
		}
	}
	
	private GameKit(String fnam, String nam, int pos, int pag, ItemStack ico, String lore) {
	    inventory = new ItemStack[41];
	    armor = new ItemStack[4];
	    icon = ico;    
	    lIcon = new ItemStack(Material.BARRIER, 1);
	    name = nam;
	    filename = fnam;
	    position = pos;
	    page = pag;
	    lores.add(lore); 
	    for (int x = 2; x < 17; x++) {
	        lores.add(" "); 
	    } 
	    enabled = true;
	    requirePermission = false;
	}
		
    public ItemStack[] getArmor() {
		return armor;
	}
    
    public void setArmor(ItemStack[] items) {
    	this.armor = items.clone();
    }
    
    public void setInventory(ItemStack[] inv) {
    	this.inventory = inv.clone();
    }

	public ItemStack[] getInventory() {
		return inventory;
	}
	
	public ItemStack getIcon() {
		return icon;
	}
	
	public void setIcon(ItemStack item) {
		this.icon = item;
	}
	
	public ItemStack getLIcon() {
		return lIcon;
	}
	
	public void setLIcon(ItemStack item) {
		this.lIcon = item;
	}
	
	public String getColorName() {
		return ChatColor.translateAlternateColorCodes('&', name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String string) {
		this.name = string;
	}
    
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int i) {
		this.position = i;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int i) {
		this.page = i;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public boolean needPermission() {
		return requirePermission;
	}
	
	public void setNeedPermission(boolean state) {
		this.requirePermission = state;
	}
	
	public void setEnabled(boolean state) {
		enabled = state;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public List<String> getColorLores() {
	    List<String> colorLores = new ArrayList<>();
	    for (String lore : lores) {
	        colorLores.add(ChatColor.translateAlternateColorCodes('&', lore));
	    }
	    return colorLores;
	}
	
	private List<String> getLores() {
	    return this.lores;
	}
	
	public void setLoreLine(int line, String lore) {
	    if (line - 1 >= 0 && line - 1 < lores.size()) {
	        lores.set(line - 1, lore);
	    } else {
	        lores.add(lore);
	    }
	}
	
	//STATIC METHODS
	public static ArrayList<GameKit> getKits() {
        return GameKit.kits;
    }
	
	public static GameKit getKit(String filename) {
		for (GameKit kit: GameKit.getKits()) {
			if (kit.getFilename().equalsIgnoreCase(filename) || kit.getColorName().equals(filename) || kit.getColorName().equals(ChatColor.translateAlternateColorCodes('&', filename))) {
				return kit;
			}
		}
		return null;
	}
	
	public static void giveKit(Player player, GameKit kit) {
	       if (player == null || !player.isOnline()) return;
	       player.getInventory().clear();
	       player.getInventory().setArmorContents(null);
	       
	       if (kit != null) {
	           ItemStack[] kitInv = kit.getInventory();
	           if (kitInv != null) {
	               int limit = Math.min(kitInv.length, player.getInventory().getSize());
	               for (int i = 0; i < limit; i++) {
	                   if (kitInv[i] != null && kitInv[i].getType() != Material.AIR) {
	                       player.getInventory().setItem(i, kitInv[i]);
	                   }
	               }
	           }
		              
	           if (kit.getArmor() != null) {
	               player.getInventory().setArmorContents(kit.getArmor());
	           }
		        
		       if (SkyWarsReloaded.getCfg().debugEnabled()) {
		           Util.get().logToFile(ChatColor.RED + "[skywars] " + ChatColor.YELLOW + player.getName() + " has recieved kit " + kit.getColorName());
		       }
	       }
	       player.updateInventory();
		}
	
	public static void newKit(Player player, String kitName) {
		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File kitsDirectory = new File(dataDirectory, "kits");
    	if (!kitsDirectory.exists()) {
            if (!kitsDirectory.mkdirs())  {
                return;
            }
        }
        
    	File kitFile = new File(kitsDirectory, kitName + ".yml");
        FileConfiguration storage = YamlConfiguration.loadConfiguration(kitFile);

        ItemStack[] inventory = player.getInventory().getContents();
        storage.set("inventory", inventory);
        
        ItemStack[] armor = player.getInventory().getArmorContents();
        storage.set("armor",  armor);
        
        storage.set("requirePermission", false);
        
        storage.set("icon", new ItemStack(Material.SNOW_BLOCK, 1));
        
        storage.set("lockedIcon", new ItemStack(Material.BARRIER, 1));
        
        storage.set("position", 0);
        
        storage.set("page", 1);
        
        storage.set("name", kitName);
        
        storage.set("enabled", false);
        
        storage.set("lores", new ArrayList<String>());
        
        storage.set("gameSettings.noRegen", false);
        storage.set("gameSettings.noPvp", false);
        storage.set("gameSettings.soupPvp", false);
        storage.set("gameSettings.noFallDamage", false);
        
        storage.set("filename", kitFile.getName().substring(0, kitFile.getName().lastIndexOf('.')));
        
        try {
        	storage.save(kitFile);
		} catch (IOException e) {
            SkyWarsReloaded.get().getLogger().info("Failed to save new kit file!");
		}
        GameKit.getKits().add(new GameKit(kitFile));
    }
	
	public static void saveKit(GameKit kit) {
		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File kitsDirectory = new File(dataDirectory, "kits");
    	if (!kitsDirectory.exists()) {
            if (!kitsDirectory.mkdirs())  {
                return;
            }
        }
        
    	File kitFile = new File(kitsDirectory, kit.getFilename() + ".yml");
        FileConfiguration storage = YamlConfiguration.loadConfiguration(kitFile);

        storage.set("inventory", kit.getInventory());
        
        storage.set("armor",  kit.getArmor());
        
        storage.set("requirePermission", kit.needPermission());
        
        storage.set("icon", kit.getIcon());
        storage.set("lockedIcon", kit.getLIcon());
        
        storage.set("position", kit.getPosition());
        
        storage.set("page", kit.getPage());
        
        storage.set("name", kit.getName());
        
        storage.set("enabled", kit.getEnabled());
        
        storage.set("lores", kit.getLores());
        
        storage.set("filename", kit.getFilename());
        
        try {
        	storage.save(kitFile);
		} catch (IOException e) {
            SkyWarsReloaded.get().getLogger().info("Failed to save kit file!");
		}
    }

	public static void loadkits() {
    	kits.clear();
    	if (SkyWarsReloaded.getCfg().kitVotingEnabled()) {
        	kits.add(new GameKit("rand",
        			new Messaging.MessageFormatter().format("kit.vote-random"), 
        			SkyWarsReloaded.getCfg().getRandPos(), 1,
        			new ItemStack(SkyWarsReloaded.getCfg().getRandMat(), 1),
        			new Messaging.MessageFormatter().format("kit.rand-lore")));
        	kits.add(new GameKit("nokit",
        			new Messaging.MessageFormatter().format("kit.vote-nokit"), 
        			SkyWarsReloaded.getCfg().getNoKitPos(), 1,
        			new ItemStack(SkyWarsReloaded.getCfg().getNoKitMat(), 1),
        			new Messaging.MessageFormatter().format("kit.nokit-lore")));
    	}
        File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File kitsDirectory = new File(dataDirectory, "kits");

        if (!kitsDirectory.exists()) {
            if (!kitsDirectory.mkdirs())  {
                return;
            }
        }

        File[] kitsFiles = kitsDirectory.listFiles();
        if (kitsFiles == null) {
            return;
        }

        for (File kitFile : kitsFiles) {
            if (!kitFile.getName().endsWith(".yml")) {
                continue;
            }

            String name = kitFile.getName().replace(".yml", "");

            if (!name.isEmpty()) {
            	kits.add(new GameKit(kitFile));
            }
        }
    }

	public static ArrayList<GameKit> getAvailableKits() {
		ArrayList<GameKit> availableKits = new ArrayList<>();
		for (GameKit kit: GameKit.getKits()) {
			if (kit.enabled) {
				availableKits.add(kit);
			}
		}
		return availableKits;
	}

}