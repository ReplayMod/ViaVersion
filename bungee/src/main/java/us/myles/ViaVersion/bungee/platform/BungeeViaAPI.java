package us.myles.ViaVersion.bungee.platform;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.bungee.service.ProtocolDetectorService;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class BungeeViaAPI implements ViaAPI<ProxiedPlayer> {

    @Override
    public int getPlayerVersion(ProxiedPlayer player) {
        UserConnection conn = Via.getManager().getConnection(player.getUniqueId());
        if (conn == null) {
            return player.getPendingConnection().getVersion();
        }
        return conn.getProtocolInfo().getProtocolVersion();
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        return getPlayerVersion(ProxyServer.getInstance().getPlayer(uuid));
    }

    @Override
    public boolean isInjected(UUID playerUUID) {
        return Via.getManager().isClientConnected(playerUUID);
    }

    @Override
    public String getVersion() {
        return Via.getPlatform().getPluginVersion();
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) throws IllegalArgumentException {
        if (!isInjected(uuid)) {
            throw new IllegalArgumentException("This player is not controlled by ViaVersion!");
        }
        UserConnection ci = Via.getManager().getConnection(uuid);
        ci.sendRawPacket(packet);
    }

    @Override
    public void sendRawPacket(ProxiedPlayer player, ByteBuf packet) throws IllegalArgumentException {
        sendRawPacket(player.getUniqueId(), packet);
    }

    @Override
    public BossBar createBossBar(String title, BossColor color, BossStyle style) {
        return new BungeeBossBar(title, 1F, color, style);
    }

    @Override
    public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new BungeeBossBar(title, health, color, style);
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        SortedSet<Integer> outputSet = new TreeSet<>(ProtocolRegistry.getSupportedVersions());
        outputSet.removeAll(Via.getPlatform().getConf().getBlockedProtocols());

        return outputSet;
    }

    /**
     * Forces ViaVersion to probe a server
     *
     * @param serverInfo The serverinfo to probe
     */
    public void probeServer(ServerInfo serverInfo) {
        ProtocolDetectorService.probeServer(serverInfo);
    }
}
