package us.myles.ViaVersion.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;

import java.util.Collections;

/*
 * This patches https://github.com/ViaVersion/ViaVersion/issues/555
 */
public class ElytraPatch {

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        UserConnection user = Via.getManager().getConnection(event.getPlayer().getUniqueId());
        if (user == null) return;

        try {
            if (user.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
                int entityId = user.get(EntityTracker1_9.class).getProvidedEntityId();

                PacketWrapper wrapper = new PacketWrapper(0x39, null, user);

                wrapper.write(Type.VAR_INT, entityId);
                wrapper.write(Types1_9.METADATA_LIST, Collections.singletonList(new Metadata(0, MetaType1_9.Byte, (byte) 0)));

                wrapper.send(Protocol1_9To1_8.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
