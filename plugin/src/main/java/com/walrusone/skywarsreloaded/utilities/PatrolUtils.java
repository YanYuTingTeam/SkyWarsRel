package com.walrusone.skywarsreloaded.utilities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class PatrolUtils {

    public static boolean isPatrolling(Player player) {
        if (player == null) return false;
        for (MetadataValue value : player.getMetadata("aerreport:patrol")) {
            if (value.getOwningPlugin() != null && "AerReport".equals(value.getOwningPlugin().getName())
                    && value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPatrolling(UUID uuid) {
        if (uuid == null) return false;
        return isPatrolling(Bukkit.getPlayer(uuid));
    }
}
