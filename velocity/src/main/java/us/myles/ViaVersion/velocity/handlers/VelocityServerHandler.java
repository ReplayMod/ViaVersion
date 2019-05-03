package us.myles.ViaVersion.velocity.handlers;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolPipeline;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.util.ReflectionUtil;
import us.myles.ViaVersion.velocity.service.ProtocolDetectorService;
import us.myles.ViaVersion.velocity.storage.VelocityStorage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class VelocityServerHandler {
    private static Method setProtocolVersion;
    private static Method setNextProtocolVersion;
    private static Method getMinecraftConnection;
    private static Method getNextProtocolVersion;
    private static Method getKnownChannels;
    private static Class<?> clientPlaySessionHandler;

    static {
        try {
            setProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("setProtocolVersion", ProtocolVersion.class);
            setNextProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("setNextProtocolVersion", ProtocolVersion.class);
            getMinecraftConnection = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer")
                    .getDeclaredMethod("getMinecraftConnection");
            getNextProtocolVersion = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                    .getDeclaredMethod("getNextProtocolVersion");
            clientPlaySessionHandler = Class.forName("com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler");
            getKnownChannels = clientPlaySessionHandler
                    .getDeclaredMethod("getKnownChannels");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void preServerConnect(ServerPreConnectEvent e) {
        try {
            UserConnection user = Via.getManager().getConnection(e.getPlayer().getUniqueId());
            if (user == null) return;
            if (!user.has(VelocityStorage.class)) {
                user.put(new VelocityStorage(user, e.getPlayer()));
            }

            int protocolId = ProtocolDetectorService.getProtocolId(e.getOriginalServer().getServerInfo().getName());
            List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(user.get(ProtocolInfo.class).getProtocolVersion(), protocolId);

            // Check if ViaVersion can support that version
            Object connection = getMinecraftConnection.invoke(e.getPlayer());
            setNextProtocolVersion.invoke(connection, ProtocolVersion.getProtocolVersion(protocols == null
                    ? user.get(ProtocolInfo.class).getProtocolVersion()
                    : protocolId));

        } catch (IllegalAccessException | InvocationTargetException e1) {
            e1.printStackTrace();
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void connectedEvent(ServerConnectedEvent e) {
        UserConnection user = Via.getManager().getConnection(e.getPlayer().getUniqueId());
        try {
            checkServerChange(e, Via.getManager().getConnection(e.getPlayer().getUniqueId()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void checkServerChange(ServerConnectedEvent e, UserConnection user) throws Exception {
        if (user == null) return;
        // Handle server/version change
        if (user.has(VelocityStorage.class)) {
            // Wait all the scheduled packets be sent
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquireUninterruptibly();
            user.getChannel().eventLoop().submit((Runnable) semaphore::release);
            semaphore.acquireUninterruptibly();
            semaphore.release();

            user.getVelocityLock().writeLock().lock();

            try {
                VelocityStorage storage = user.get(VelocityStorage.class);

                if (e.getServer() != null) {
                    if (!e.getServer().getServerInfo().getName().equals(storage.getCurrentServer())) {
                        String serverName = e.getServer().getServerInfo().getName();

                        storage.setCurrentServer(serverName);

                        int protocolId = ProtocolDetectorService.getProtocolId(serverName);

                        if (protocolId <= ProtocolVersion.MINECRAFT_1_8.getProtocol()) { // 1.8 doesn't have BossBar packet
                            if (storage.getBossbar() != null) {
                                for (UUID uuid : storage.getBossbar()) {
                                    PacketWrapper wrapper = new PacketWrapper(0x0C, null, user);
                                    wrapper.write(Type.UUID, uuid);
                                    wrapper.write(Type.VAR_INT, 1); // remove
                                    wrapper.send(Protocol1_9To1_8.class, true, true);
                                }
                                storage.getBossbar().clear();
                            }
                        }

                        ProtocolInfo info = user.get(ProtocolInfo.class);
                        int previousServerProtocol = info.getServerProtocolVersion();

                        // Refresh the pipes
                        List<Pair<Integer, Protocol>> protocols = ProtocolRegistry.getProtocolPath(info.getProtocolVersion(), protocolId);
                        ProtocolPipeline pipeline = user.get(ProtocolInfo.class).getPipeline();
                        user.clearStoredObjects();
                        pipeline.cleanPipes();
                        if (protocols == null) {
                            // TODO Check Bungee Supported Protocols? *shrugs*
                            protocolId = info.getProtocolVersion();
                        } else {
                            for (Pair<Integer, Protocol> prot : protocols) {
                                pipeline.add(prot.getValue());
                            }
                        }

                        info.setServerProtocolVersion(protocolId);
                        // Add version-specific base Protocol
                        pipeline.add(ProtocolRegistry.getBaseProtocol(protocolId));

                        // Workaround 1.13 server change
                        Object sessionHandler = ReflectionUtil.invoke(
                                getMinecraftConnection.invoke(e.getPlayer()),
                                "getSessionHandler"
                        );

                        if (clientPlaySessionHandler.isInstance(sessionHandler)) { // It may be InitialConnectSessionHandler on the first server connection
                            Set<String> knownChannels = (Set<String>) getKnownChannels.invoke(sessionHandler);
                            if (previousServerProtocol != -1) {
                                int id1_13 = ProtocolVersion.MINECRAFT_1_13.getProtocol();
                                if (previousServerProtocol < id1_13 && protocolId >= id1_13) {
                                    ArrayList<String> newChannels = new ArrayList<>();
                                    for (String oldChannel : knownChannels) {
                                        String transformed = InventoryPackets.getNewPluginChannelId(oldChannel);
                                        if (transformed != null) {
                                            newChannels.add(transformed);
                                        }
                                    }
                                    knownChannels.clear();
                                    knownChannels.addAll(newChannels);
                                } else if (previousServerProtocol >= id1_13 && protocolId < id1_13) {
                                    ArrayList<String> newChannels = new ArrayList<>();
                                    for (String oldChannel : knownChannels) {
                                        String transformed = InventoryPackets.getOldPluginChannelId(oldChannel);
                                        if (transformed != null) {
                                            newChannels.add(transformed);
                                        }
                                    }
                                    knownChannels.clear();
                                    knownChannels.addAll(newChannels);
                                }
                            }
                        }

                        user.put(info);
                        user.put(storage);

                        user.setActive(protocols != null);

                        // Init all protocols TODO check if this can get moved up to the previous for loop, and doesn't require the pipeline to already exist.
                        for (Protocol protocol : pipeline.pipes()) {
                            protocol.init(user);
                        }

                        Object connection = getMinecraftConnection.invoke(e.getPlayer());
                        ProtocolVersion version = (ProtocolVersion) getNextProtocolVersion.invoke(connection);
                        setProtocolVersion.invoke(connection, version);
                    }
                }
            } finally {
                user.getVelocityLock().writeLock().unlock();
            }
        }
    }
}
