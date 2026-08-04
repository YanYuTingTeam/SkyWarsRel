package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.game.TeamCard;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.utilities.PatrolUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.EventExecutor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyListener implements Listener {

    private Object partyManager;
    private Method getPartyMethod;
    private Method hasPartyMethod;
    private Method isLeaderMethod;
    private Method getMembersMethod;
    private Method getLeaderMethod;
    private Method getAllMembersMethod;
    private boolean initialized = false;

    public boolean init() {
        try {
            Plugin partyPlugin = Bukkit.getPluginManager().getPlugin("PartyAPI");
            if (partyPlugin == null) {
                SkyWarsReloaded.get().getLogger().warning("PartyAPI plugin not found.");
                return false;
            }
            Class<?> partyAPIClass = Class.forName("cn.linmoyu.partyapi.PartyAPI");
            Method getPluginMethod = partyAPIClass.getMethod("getPlugin");
            Object partyAPIInstance = getPluginMethod.invoke(null);
            Method getPMMethod = partyAPIInstance.getClass().getMethod("getPartyManager");
            partyManager = getPMMethod.invoke(partyAPIInstance);
            hasPartyMethod = partyManager.getClass().getMethod("hasParty", UUID.class);
            getPartyMethod = partyManager.getClass().getMethod("getParty", UUID.class);
            Class<?> partyInfoClass = Class.forName("cn.linmoyu.partyapi.model.PartyInfo");
            isLeaderMethod = partyInfoClass.getMethod("isLeader", UUID.class);
            getMembersMethod = partyInfoClass.getMethod("getMembers");
            getLeaderMethod = partyInfoClass.getMethod("getLeader");
            getAllMembersMethod = partyInfoClass.getMethod("getAllMembers");

            Class<?> partyLoadedEventClass = Class.forName("cn.linmoyu.partyapi.event.PartyLoadedEvent");
            EventExecutor executor = (listener, event) -> handlePartyLoadedEvent(event);
            Bukkit.getPluginManager().registerEvent(
                    (Class<? extends Event>) partyLoadedEventClass,
                    this,
                    EventPriority.NORMAL,
                    executor,
                    SkyWarsReloaded.get()
            );
            initialized = true;
            SkyWarsReloaded.get().getLogger().info("PartyListener initialized via reflection.");
            return true;
        } catch (Exception e) {
            SkyWarsReloaded.get().getLogger().warning("Failed to initialize PartyListener: " + e.getMessage());
            return false;
        }
    }

    private void handlePartyLoadedEvent(org.bukkit.event.Event event) {
        if (!initialized) return;

        try {
            Method getPartyInfoMethod = event.getClass().getMethod("getPartyInfo");
            Object partyInfo = getPartyInfoMethod.invoke(event);

            UUID leaderId = (UUID) getLeaderMethod.invoke(partyInfo);
            Player leader = Bukkit.getPlayer(leaderId);

            if (leader == null || !leader.isOnline()) return;

            GameMap gameMap = MatchManager.get().getPlayerMap(leader);
            if (gameMap == null) return;
            if (gameMap.getMatchState() != MatchState.WAITINGSTART) return;

            final Object fiPartyInfo = partyInfo;
            final GameMap fiGameMap = gameMap;
            final Player fiLeader = leader;
            new BukkitRunnable() {
                @Override
                public void run() {
                    assignAndTransferMembers(fiLeader, fiGameMap, fiPartyInfo);
                }
            }.runTaskLater(SkyWarsReloaded.get(), 2L);

        } catch (Exception e) {
            SkyWarsReloaded.get().getLogger().warning("Error in onPartyLoaded: " + e.getMessage());
        }
    }

    public void onPlayerJoinGame(Player joiner, GameMap gameMap) {
        if (!initialized) return;
        if (gameMap.getMatchState() != MatchState.WAITINGSTART) return;

        try {
            UUID joinerId = joiner.getUniqueId();
            Boolean hasParty = (Boolean) hasPartyMethod.invoke(partyManager, joinerId);
            if (hasParty == null || !hasParty) return;

            Object partyInfo = getPartyMethod.invoke(partyManager, joinerId);
            if (partyInfo == null) return;

            Boolean isLeader = (Boolean) isLeaderMethod.invoke(partyInfo, joinerId);
            if (isLeader == null || !isLeader) return;

            final Object fiPartyInfo = partyInfo;
            final GameMap fiGameMap = gameMap;
            final Player fiJoiner = joiner;
            new BukkitRunnable() {
                @Override
                public void run() {
                    assignAndTransferMembers(fiJoiner, fiGameMap, fiPartyInfo);
                }
            }.runTaskLater(SkyWarsReloaded.get(), 5L);

        } catch (Exception e) {
            SkyWarsReloaded.get().getLogger().warning("Error in onPlayerJoinGame: " + e.getMessage());
        }
    }

    /**
     * 将队伍成员分配到队长所在的游戏和队伍
     */
    @SuppressWarnings("unchecked")
    private void assignAndTransferMembers(Player leader, GameMap gameMap, Object partyInfo) {
        try {
            List<UUID> members = (List<UUID>) getMembersMethod.invoke(partyInfo);
            if (members == null || members.isEmpty()) return;

            UUID leaderId = (UUID) getLeaderMethod.invoke(partyInfo);
            TeamCard leaderTeam = gameMap.getTeamCard(leader);

            for (UUID memberId : members) {
                if (memberId.equals(leaderId)) continue;

                Player member = Bukkit.getPlayer(memberId);
                if (member == null || !member.isOnline()) continue;

                if (PatrolUtils.isPatrolling(member)) {
                    SkyWarsReloaded.get().getLogger().info("PartyListener > 队员处于巡查模式, 跳过加入: " + member.getName());
                    continue;
                }

                GameMap memberCurrentGame = MatchManager.get().getPlayerMap(member);
                if (memberCurrentGame != null) {
                    if (memberCurrentGame == gameMap) continue; // 已经在同一个游戏中
                    MatchManager.get().playerLeave(member, null, true, false, false);
                }

                if (MatchManager.get().isSpectating(member)) {
                    MatchManager.get().removeSpectator(member);
                }

                if (gameMap.canAddPlayer()) {
                    TeamCard targetTeam = null;
                    if (leaderTeam != null && leaderTeam.getFullCount() > 0) {
                        targetTeam = leaderTeam;
                    }
                    boolean joined = gameMap.addPlayers(targetTeam, member);
                    if (joined) {
                        SkyWarsReloaded.get().getLogger().info("PartyListener > 队员 " + member.getName() + " 已加入游戏: " + gameMap.getName());
                    } else {
                        SkyWarsReloaded.get().getLogger().info("PartyListener > 队员 " + member.getName() + " 加入游戏失败");
                    }
                } else {
                    SkyWarsReloaded.get().getLogger().info("PartyListener > 游戏已满，无法加入: " + gameMap.getName());
                }
            }
        } catch (Exception e) {
            SkyWarsReloaded.get().getLogger().warning("Error in assignAndTransferMembers: " + e.getMessage());
        }
    }
}
