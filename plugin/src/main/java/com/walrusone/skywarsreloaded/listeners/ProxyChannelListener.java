package com.walrusone.skywarsreloaded.listeners;

import com.walrusone.skywarsreloaded.utilities.MatchUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ProxyChannelListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"BungeeCord".equals(channel)) return;
        if (message == null || message.length == 0) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in.readUTF();
            if (!"serverinvite".equals(subChannel)) {
                return;
            }
            String data = in.readUTF();
            MatchUtils.handleProxyChannelMessage(data);
        } catch (IOException ignored) {}
    }
}
