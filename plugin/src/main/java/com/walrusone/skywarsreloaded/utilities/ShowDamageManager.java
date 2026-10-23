package com.walrusone.skywarsreloaded.utilities;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;

public class ShowDamageManager {
    private static ShowDamageManager instance;
    private File dataFolder;
    private final ConcurrentHashMap<UUID, Boolean> showDamageCache = new ConcurrentHashMap<>();

    public ShowDamageManager() {
        instance = this;
        dataFolder = new File(SkyWarsReloaded.get().getDataFolder(), "playerdata_damage");
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
        Boolean cached = showDamageCache.get(uuid);
        if (cached != null) {
            return cached;
        }
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            showDamageCache.put(uuid, true);
            return true;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        boolean enabled = config.getBoolean("showDamage", true);
        showDamageCache.put(uuid, enabled);
        return enabled;
    }

    public void setShowDamageEnabled(UUID uuid, boolean enabled) {
        showDamageCache.put(uuid, enabled);
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
