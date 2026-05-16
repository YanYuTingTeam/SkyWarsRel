package com.walrusone.skywarsreloaded.utilities;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;

public class ShowDamageManager {
    private static ShowDamageManager instance;
    private File dataFolder;

    public ShowDamageManager() {
        instance = this;
        dataFolder = new File(SkyWarsReloaded.get().getDataFolder(), "player_data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public static ShowDamageManager get() {
        return instance;
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public boolean isShowDamageEnabled(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return true;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return config.getBoolean("showDamage", true);
    }

    public void setShowDamageEnabled(UUID uuid, boolean enabled) {
        File file = getPlayerFile(uuid);
        FileConfiguration config;
        
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = new YamlConfiguration();
        }
        
        config.set("showDamage", enabled);
        
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPlayerData(UUID uuid) {
        return getPlayerFile(uuid).exists();
    }
}
