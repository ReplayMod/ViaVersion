package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class PlayerPackets {

	public static void register(Protocol protocol) {
		// Respawn
		protocol.registerOutgoing(State.PLAY, 0x3A, 0x3B, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
						int dimensionId = wrapper.get(Type.INT, 0);
						clientWorld.setEnvironment(dimensionId);
					}
				});
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper wrapper) throws Exception {
						wrapper.write(Type.LONG, 0L); // Level Seed
					}
				});
			}
		});

		// Join Game
		protocol.registerOutgoing(State.PLAY, 0x25, 0x26, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT); // 0 - Entity ID
				map(Type.UNSIGNED_BYTE); // 1 - Gamemode
				map(Type.INT); // 2 - Dimension

				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						Entity1_15Types.EntityType entType = Entity1_15Types.EntityType.PLAYER;
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						tracker.addEntity(wrapper.get(Type.INT, 0), entType);
					}
				});
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper wrapper) throws Exception {
						// Store the player
						ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
						int dimensionId = wrapper.get(Type.INT, 1);
						clientChunks.setEnvironment(dimensionId);

						// Register Type ID
						EntityTracker tracker = wrapper.user().get(EntityTracker.class);
						int entityId = wrapper.get(Type.INT, 0);
						tracker.addEntity(entityId, Entity1_15Types.EntityType.PLAYER);
					}
				});
				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper wrapper) throws Exception {
						wrapper.write(Type.LONG, 0L); // Level Seed
					}
				});

				map(Type.UNSIGNED_BYTE); // 3 - Max Players
				map(Type.STRING); // 4 - Level Type
				map(Type.VAR_INT); // 5 - View Distance
				map(Type.BOOLEAN); // 6 - Reduce Debug Info

				create(new ValueCreator() {
					@Override
					public void write(PacketWrapper wrapper) throws Exception {
						wrapper.write(Type.BOOLEAN, !Via.getConfig().is1_15InstantRespawn()); // Show Death Screen
					}
				});
			}
		});
	}
}
