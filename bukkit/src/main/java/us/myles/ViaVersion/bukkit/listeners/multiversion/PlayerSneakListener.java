package us.myles.ViaVersion.bukkit.listeners.multiversion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.bukkit.platform.BukkitViaLoader;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PlayerSneakListener extends ViaBukkitListener {
    private static final float STANDING_HEIGHT = 1.8F;
    private static final float HEIGHT_1_14 = 1.5F;
    private static final float HEIGHT_1_9 = 1.6F;
    private static final float DEFAULT_WIDTH = 0.6F;

    private Map<Player, Boolean> sneaking; // true = 1.14+, else false
    private Method getHandle;
    private Method setSize;
    private boolean is1_9Fix;
    private boolean is1_14Fix;
    private boolean useCache;

    public PlayerSneakListener(ViaVersionPlugin plugin, BukkitViaLoader viaLoader, boolean is1_9Fix, boolean is1_14Fix) {
        super(plugin, null);
        this.is1_9Fix = is1_9Fix;
        this.is1_14Fix = is1_14Fix;
        try {
            getHandle = Class.forName(plugin.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer").getMethod("getHandle");
            setSize = Class.forName(plugin.getServer().getClass().getPackage().getName()
                    .replace("org.bukkit.craftbukkit", "net.minecraft.server") + ".EntityPlayer").getMethod("setSize", Float.TYPE, Float.TYPE);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        // From 1.9 upwards the server hitbox is set in every entity tick, so we have to reset it everytime
        if (ProtocolRegistry.SERVER_PROTOCOL >= ProtocolVersion.v1_9.getId()) {
            sneaking = new HashMap<>();
            useCache = true;
            viaLoader.storeListener(new ViaBukkitListener(plugin, null) {
                @EventHandler
                public void playerQuit(PlayerQuitEvent event) {
                    sneaking.remove(event.getPlayer());
                }
            }).register();
            plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<Player, Boolean> entry : sneaking.entrySet()) {
                        setHeight(entry.getKey(), entry.getValue() ? HEIGHT_1_14 : HEIGHT_1_9);
                    }
                }
            }, 1, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UserConnection userConnection = getUserConnection(player);
        if (userConnection == null) return;
        ProtocolInfo info = userConnection.get(ProtocolInfo.class);
        if (info == null) return;

        int protocolVersion = info.getProtocolVersion();
        if (is1_14Fix && protocolVersion >= ProtocolVersion.v1_14.getId()) {
            setHeight(player, event.isSneaking() ? HEIGHT_1_14 : STANDING_HEIGHT);
            if (!useCache) return;
            if (event.isSneaking())
                sneaking.put(player, true);
            else
                sneaking.remove(player);
        } else if (is1_9Fix && protocolVersion >= ProtocolVersion.v1_9.getId()) {
            setHeight(player, event.isSneaking() ? HEIGHT_1_9 : STANDING_HEIGHT);
            if (!useCache) return;
            if (event.isSneaking())
                sneaking.put(player, false);
            else
                sneaking.remove(player);
        }
    }

    private void setHeight(Player player, float height) {
        try {
            setSize.invoke(getHandle.invoke(player), DEFAULT_WIDTH, height);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
