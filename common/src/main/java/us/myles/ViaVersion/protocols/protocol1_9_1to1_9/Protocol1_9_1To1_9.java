package us.myles.ViaVersion.protocols.protocol1_9_1to1_9;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_9_1To1_9 extends Protocol {
    @Override
    protected void registerPackets() {
        // Currently supports 1.9.1 and 1.9.2
        // Join Game Packet
        registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Player ID
                map(Type.UNSIGNED_BYTE); // 1 - Player Gamemode
                // 1.9.1 PRE 2 Changed this
                map(Type.BYTE, Type.INT); // 2 - Player Dimension
                map(Type.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Type.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Type.STRING); // 5 - Level Type
                map(Type.BOOLEAN); // 6 - Reduced Debug info
            }
        });

        // Sound Effect Packet
        registerOutgoing(State.PLAY, 0x47, 0x47, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound ID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int sound = wrapper.get(Type.VAR_INT, 0);

                        if (sound >= 415) // Add 1 to every sound id since there is no Elytra sound on a 1.9 server
                            wrapper.set(Type.VAR_INT, 0, sound + 1);
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}
