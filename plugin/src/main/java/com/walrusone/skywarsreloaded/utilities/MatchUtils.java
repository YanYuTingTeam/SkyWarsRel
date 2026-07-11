package com.walrusone.skywarsreloaded.utilities;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.GameType;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.game.TeamCard;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class MatchUtils {

    private static final Map<UUID, String> pendingPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> pendingTasks = new ConcurrentHashMap<>();
    private static final long PENDING_TIMEOUT_TICKS = 100L; // 5 seconds

    /**
     * 处理来自 BungeeCord 的 serverinvite 消息
     * 消息格式: playerUUID,inviterUUID
     */
    public static void handleProxyChannelMessage(String message) {
        Logger logger = SkyWarsReloaded.get().getLogger();
        String[] parts = message.split(",", -1);
        if (parts.length < 2) {
            logger.warning("MatchUtils > Proxy 消息格式错误: " + message);
            return;
        }
        logger.info("MatchUtils > 解析Proxy消息: " + message);

        UUID playerUUID;
        UUID inviterUUID;
        try {
            playerUUID = UUID.fromString(parts[0].trim());
            inviterUUID = UUID.fromString(parts[1].trim());
        } catch (IllegalArgumentException e) {
            logger.warning("MatchUtils > UUID 格式错误: " + message);
            return;
        }

        // 查找邀请者所在的游戏
        GameMap targetGame = null;
        Player inviter = Bukkit.getPlayer(inviterUUID);
        if (inviter != null) {
            targetGame = MatchManager.get().getPlayerMap(inviter);
        }

        // 如果邀请者不在游戏中，找一个最佳可用房间
        if (targetGame == null || targetGame.getMatchState() == MatchState.OFFLINE) {
            targetGame = getBestArena();
        }
        if (targetGame == null) {
            logger.info("MatchUtils > 没有可用的游戏房间");
            return;
        }

        logger.info("MatchUtils > 尝试将玩家 " + playerUUID + " 加入游戏: " + targetGame.getName());

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            // 玩家已在线，直接加入
            transferPlayer(player, targetGame);
        } else {
            // 玩家不在线，添加到待处理列表
            addPendingPlayer(playerUUID, targetGame.getName());
        }
    }

    /**
     * 处理来自大厅的 Redis 匹配消息（如果需要的话）
     * 消息格式: playerUUID,mode,arenaName
     */
    public static void handleRedisMessage(String message) {
        Logger logger = SkyWarsReloaded.get().getLogger();
        String[] parts = message.split(",", -1);
        if (parts.length < 3) {
            logger.warning("MatchUtils > Redis 消息格式错误: " + message);
            return;
        }

        UUID playerUUID;
        try {
            playerUUID = UUID.fromString(parts[0].trim());
        } catch (IllegalArgumentException e) {
            logger.warning("MatchUtils > UUID 格式错误: " + message);
            return;
        }

        String arenaName = parts[2].trim();
        logger.info("MatchUtils > 解析Redis消息: " + message);

        if (!arenaName.isEmpty()) {
            addPendingPlayer(playerUUID, arenaName);
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        GameMap targetGame = null;
        if (!arenaName.isEmpty()) {
            targetGame = GameMap.getMap(arenaName);
        }
        if (targetGame == null || !targetGame.isRegistered() || targetGame.getMatchState() != MatchState.WAITINGSTART) {
            targetGame = getBestArena();
        }
        if (targetGame == null) return;

        transferPlayer(player, targetGame);
    }

    /**
     * 将玩家转移到指定游戏
     */
    public static void transferPlayer(Player player, GameMap targetGame) {
        if (player == null || !player.isOnline()) return;
        if (targetGame == null) return;

        // 检查玩家是否已经在其他游戏中
        GameMap currentGame = MatchManager.get().getPlayerMap(player);
        if (currentGame != null) {
            // 已经在一个游戏中，先离开
            MatchManager.get().playerLeave(player, null, true, false, false);
        }

        // 检查是否在旁观
        if (MatchManager.get().isSpectating(player)) {
            MatchManager.get().removeSpectator(player);
        }

        // 加入新游戏
        if (targetGame.canAddPlayer()) {
            boolean joined = targetGame.addPlayers(null, player);
            if (joined) {
                SkyWarsReloaded.get().getLogger().info("MatchUtils > 玩家 " + player.getName() + " 已加入游戏: " + targetGame.getName());
            } else {
                SkyWarsReloaded.get().getLogger().info("MatchUtils > 玩家 " + player.getName() + " 加入游戏失败: " + targetGame.getName());
            }
        } else {
            SkyWarsReloaded.get().getLogger().info("MatchUtils > 游戏已满: " + targetGame.getName());
        }
    }

    /**
     * 获取最佳可用竞技场（人数最多的等待中的房间）
     */
    public static GameMap getBestArena() {
        ArrayList<GameMap> games = GameMap.getPlayableArenas(GameType.ALL);
        GameMap best = null;
        int maxPlayers = -1;
        for (GameMap gameMap : games) {
            if (gameMap.getMatchState() == MatchState.WAITINGSTART && gameMap.canAddPlayer()) {
                int count = gameMap.getPlayerCount();
                if (count > maxPlayers) {
                    maxPlayers = count;
                    best = gameMap;
                }
            }
        }
        // 如果没有等待中的房间，返回任意可加入的
        if (best == null) {
            for (GameMap gameMap : games) {
                if (gameMap.canAddPlayer()) {
                    return gameMap;
                }
            }
        }
        return best;
    }

    /**
     * 添加待处理玩家
     */
    public static void addPendingPlayer(UUID uuid, String gameName) {
        BukkitTask oldTask = pendingTasks.remove(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }
        pendingPlayers.put(uuid, gameName);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(SkyWarsReloaded.get(),
                () -> {
                    pendingPlayers.remove(uuid);
                    pendingTasks.remove(uuid);
                }, PENDING_TIMEOUT_TICKS);
        pendingTasks.put(uuid, task);
    }

    /**
     * 消费待处理玩家记录（玩家加入时调用）
     * @return 玩家对应的游戏名，不存在则返回 null
     */
    public static String consumePendingPlayer(UUID uuid) {
        BukkitTask task = pendingTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        return pendingPlayers.remove(uuid);
    }

    /**
     * 检查是否有待处理的玩家
     */
    public static boolean hasPendingPlayer(UUID uuid) {
        return pendingPlayers.containsKey(uuid);
    }
}
