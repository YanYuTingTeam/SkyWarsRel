package com.walrusone.skywarsreloaded.utilities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.aermini.report.AerReport;

public class PatrolUtils {

    public static boolean isPatrolling(Player player) {
        if (player == null) return false;
        return isPatrolling(player.getUniqueId());
    }

    public static boolean isPatrolling(UUID uuid) {
        if (uuid == null) return false;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AerReport");
        if (plugin == null || !plugin.isEnabled()) return false;
        if (!(plugin instanceof AerReport)) return false;
        return ((AerReport) plugin).isPatrolling(uuid);
    }
}
