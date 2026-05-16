package com.walrusone.skywarsreloaded.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;

public class ExtConfig {
    private static ExtConfig instance;
    private File configFile;
    private FileConfiguration config;

    private int fadein;
    private int stay;
    private int fadeout;
    private String healths;
    private String killSubtitle;
    private String hitSubtitle;
    private String bowHit;
    private String bowKill;
    private String shon;
    private String shon_e;
    private String shon_notif;
    private String shoff;
    private String shoff_e;
    private String shoff_notif;

    public ExtConfig() {
        instance = this;
        loadConfig();
    }

    public static ExtConfig get() {
        return instance;
    }

    private void loadConfig() {
        configFile = new File(SkyWarsReloaded.get().getDataFolder(), "ext_config.yml");

        if (!configFile.exists()) {
            SkyWarsReloaded.get().saveResource("ext_config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        fadein = config.getInt("fadein", 20);
        stay = config.getInt("stay", 40);
        fadeout = config.getInt("fadeout", 20);
        healths = config.getString("healths", "&7{player} &e剩余血量 &a{hurt_two} &eHP!");
        killSubtitle = config.getString("kill_subtitle", "&e击杀 +1");
        hitSubtitle = config.getString("hit_subtitle", "&f伤害 - &3{hurt_one}");
        bowHit = config.getString("bow_hit", "&b集中&c{target}&b造成&a{hurt_two}&b点伤害 (距离:{distance}米)");
        bowKill = config.getString("bow_kill", "&c{target} 被 {player} 射死了 ({distance}米远)");
        shon = config.getString("shon", "&a伤害显示开启!");
        shon_e = config.getString("shon_e", "&c你已经开启了伤害显示,请勿重复开启!");
        shon_notif = config.getString("shon_notif", "&a已为你自动开启 &c伤害显示&a，输入 &c/shoff &a关闭");
        shoff = config.getString("shoff", "&c伤害显示关闭!");
        shoff_e = config.getString("shoff_e", "&c你已经关闭了伤害显示,请勿重复关闭!");
        shoff_notif = config.getString("shoff_notif", "&a已为你自动关闭 &c伤害显示&a，输入 &c/shon &a开启");
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getFadein() {
        return fadein;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeout() {
        return fadeout;
    }

    public String getHealths() {
        return healths;
    }

    public String getKillSubtitle() {
        return killSubtitle;
    }

    public String getHitSubtitle() {
        return hitSubtitle;
    }

    public String getBowHit() {
        return bowHit;
    }

    public String getBowKill() {
        return bowKill;
    }

    public String getShon() {
        return shon;
    }

    public String getShon_e() {
        return shon_e;
    }

    public String getShon_notif() {
        return shon_notif;
    }

    public String getShoff() {
        return shoff;
    }

    public String getShoff_e() {
        return shoff_e;
    }

    public String getShoff_notif() {
        return shoff_notif;
    }
}
