package us.myles.ViaVersion.protocols.protocol1_13_1to1_13;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.metadata.MetadataRewriter1_13_1To1_13;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_13_1to1_13.packets.WorldPackets;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.EntityTracker1_13;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_13_1To1_13 extends Protocol<ClientboundPackets1_13, ClientboundPackets1_13, ServerboundPackets1_13, ServerboundPackets1_13> {

    public Protocol1_13_1To1_13() {
        super(ClientboundPackets1_13.class, ClientboundPackets1_13.class, ServerboundPackets1_13.class, ServerboundPackets1_13.class);
    }

    @Override
    protected void registerPackets() {
        new MetadataRewriter1_13_1To1_13(this);

        EntityPackets.register(this);
        InventoryPackets.register(this);
        WorldPackets.register(this);

        registerIncoming(ServerboundPackets1_13.TAB_COMPLETE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.STRING, new ValueTransformer<String, String>(Type.STRING) {
                    @Override
                    public String transform(PacketWrapper wrapper, String inputValue) {
                        // 1.13 starts sending slash at start, so we remove it for compatibility
                        return inputValue.startsWith("/") ? inputValue.substring(1) : inputValue;
                    }
                });
            }
        });

        registerIncoming(ServerboundPackets1_13.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLAT_ITEM);
                map(Type.BOOLEAN);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item item = wrapper.get(Type.FLAT_ITEM, 0);
                        InventoryPackets.toServer(item);
                    }
                });
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);
                        if (hand == 1) {
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        registerOutgoing(ClientboundPackets1_13.TAB_COMPLETE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Transaction id
                map(Type.VAR_INT); // Start
                map(Type.VAR_INT); // Length
                map(Type.VAR_INT); // Count
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int start = wrapper.get(Type.VAR_INT, 1);
                        wrapper.set(Type.VAR_INT, 1, start + 1); // Offset by +1 to take into account / at beginning
                        // Passthrough suggestions
                        int count = wrapper.get(Type.VAR_INT, 3);
                        for (int i = 0; i < count; i++) {
                            wrapper.passthrough(Type.STRING);
                            boolean hasTooltip = wrapper.passthrough(Type.BOOLEAN);
                            if (hasTooltip) {
                                wrapper.passthrough(Type.STRING); // JSON Tooltip
                            }
                        }
                    }
                });
            }
        });

        registerOutgoing(ClientboundPackets1_13.BOSSBAR, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UUID);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);
                        if (action == 0) {
                            wrapper.passthrough(Type.COMPONENT);
                            wrapper.passthrough(Type.FLOAT);
                            wrapper.passthrough(Type.VAR_INT);
                            wrapper.passthrough(Type.VAR_INT);
                            short flags = wrapper.read(Type.BYTE);
                            if ((flags & 0x02) != 0) flags |= 0x04;
                            wrapper.write(Type.UNSIGNED_BYTE, flags);
                        }
                    }
                });
            }
        });

        registerOutgoing(ClientboundPackets1_13.TAGS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int blockTagsSize = wrapper.passthrough(Type.VAR_INT); // block tags
                        for (int i = 0; i < blockTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            int[] blocks = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                            for (int j = 0; j < blocks.length; j++) {
                                blocks[j] = getNewBlockId(blocks[j]);
                            }
                        }
                        int itemTagsSize = wrapper.passthrough(Type.VAR_INT); // item tags
                        for (int i = 0; i < itemTagsSize; i++) {
                            wrapper.passthrough(Type.STRING);
                            int[] items = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                            for (int j = 0; j < items.length; j++) {
                                items[j] = InventoryPackets.getNewItemId(items[j]);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_13(userConnection));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld(userConnection));
        }
    }


    public static int getNewBlockStateId(int blockId) {
        if (blockId > 8573) {
            blockId += 17;
        } else if (blockId > 8463) {
            blockId += 16;
        } else if (blockId > 8458) {
            blockId = 8470 + (blockId - 8459) * 2;
        } else if (blockId > 1126) {
            blockId += 1;
        }

        return blockId;
    }

    public static int getNewBlockId(final int oldBlockId) {
        int blockId = oldBlockId;
        if (oldBlockId >= 561) {
            blockId += 5;
        }
        return blockId;
    }
}
