package us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.UUIDIntArrayType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;

import java.util.UUID;

public class InventoryPackets {

    public static void register(Protocol protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        // Open Window
        protocol.registerOutgoing(State.PLAY, 0x2F, 0x2E, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.VAR_INT);
                map(Type.STRING);

                handler(wrapper -> {
                    int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType >= 20) { // smithing added with id 20
                        wrapper.set(Type.VAR_INT, 1, ++windowType);
                    }
                });
            }
        });

        // Window Property
        protocol.registerOutgoing(State.PLAY, 0x16, 0x15, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.SHORT); // Property
                map(Type.SHORT); // Value

                handler(wrapper -> {
                    short property = wrapper.get(Type.SHORT, 0);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        short enchantmentId = wrapper.get(Type.SHORT, 1);
                        if (enchantmentId >= 11) { // soul_speed added with id 11
                            wrapper.set(Type.SHORT, 1, ++enchantmentId);
                        }
                    }
                });
            }
        });

        // Set cooldown
        itemRewriter.registerSetCooldown(0x18, 0x17, InventoryPackets::getNewItemId);

        // Window items packet
        itemRewriter.registerWindowItems(Type.FLAT_VAR_INT_ITEM_ARRAY, 0x15, 0x14);

        // Trade list packet
        protocol.registerOutgoing(State.PLAY, 0x28, 0x27, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.VAR_INT);
                    int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                    for (int i = 0; i < size; i++) {
                        Item input = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        toClient(input);

                        Item output = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        toClient(output);

                        if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                            // Second Item
                            toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                        }

                        wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                        wrapper.passthrough(Type.INT); // Number of tools uses
                        wrapper.passthrough(Type.INT); // Maximum number of trade uses

                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.INT);
                        wrapper.passthrough(Type.FLOAT);
                        wrapper.passthrough(Type.INT);
                    }

                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.BOOLEAN);
                });
            }
        });

        // Set slot packet
        itemRewriter.registerSetSlot(Type.FLAT_VAR_INT_ITEM, 0x17, 0x16);

        // Entity Equipment Packet
        itemRewriter.registerEntityEquipment(Type.FLAT_VAR_INT_ITEM, 0x47, 0x47);

        // Declare Recipes
        protocol.registerOutgoing(State.PLAY, 0x5B, 0x5A, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        String type = wrapper.passthrough(Type.STRING).replace("minecraft:", "");
                        String id = wrapper.passthrough(Type.STRING);
                        switch (type) {
                            case "crafting_shapeless": {
                                wrapper.passthrough(Type.STRING); // Group

                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
                                for (int j = 0; j < ingredientsNo; j++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                    for (Item item : items) toClient(item);
                                }
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
                                break;
                            }
                            case "crafting_shaped": {
                                int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                                wrapper.passthrough(Type.STRING); // Group

                                for (int j = 0; j < ingredientsNo; j++) {
                                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                    for (Item item : items) toClient(item);
                                }
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Result
                                break;
                            }
                            case "blasting":
                            case "smoking":
                            case "campfire_cooking":
                            case "smelting": {
                                wrapper.passthrough(Type.STRING); // Group

                                Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients

                                for (Item item : items) toClient(item);
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                wrapper.passthrough(Type.FLOAT); // EXP

                                wrapper.passthrough(Type.VAR_INT); // Cooking time
                                break;
                            }
                            case "stonecutting": {
                                wrapper.passthrough(Type.STRING);
                                Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT); // Ingredients
                                for (Item item : items) toClient(item);
                                toClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                                break;
                            }
                        }
                    }
                });
            }
        });

        // Click window packet
        itemRewriter.registerClickWindow(Type.FLAT_VAR_INT_ITEM, 0x09, 0x09);

        // Creative Inventory Action
        itemRewriter.registerCreativeInvAction(Type.FLAT_VAR_INT_ITEM, 0x26, 0x27);

        // Edit Book
        protocol.registerIncoming(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });
    }

    public static void toClient(Item item) {
        if (item == null) return;

        if (item.getIdentifier() == 771 && item.getTag() != null) {
            CompoundTag tag = item.getTag();
            Tag ownerTag = tag.get("SkullOwner");
            if (ownerTag instanceof CompoundTag) {
                CompoundTag ownerCompundTag = (CompoundTag) ownerTag;
                Tag idTag = ownerCompundTag.get("Id");
                if (idTag instanceof StringTag) {
                    UUID id = UUID.fromString((String) idTag.getValue());
                    ownerCompundTag.put(new IntArrayTag("Id", UUIDIntArrayType.uuidToIntArray(id)));
                }
            }
        }

        item.setIdentifier(getNewItemId(item.getIdentifier()));
    }

    public static void toServer(Item item) {
        if (item == null) return;

        item.setIdentifier(getOldItemId(item.getIdentifier()));

        if (item.getIdentifier() == 771 && item.getTag() != null) {
            CompoundTag tag = item.getTag();
            Tag ownerTag = tag.get("SkullOwner");
            if (ownerTag instanceof CompoundTag) {
                CompoundTag ownerCompundTag = (CompoundTag) ownerTag;
                Tag idTag = ownerCompundTag.get("Id");
                if (idTag instanceof IntArrayTag) {
                    UUID id = UUIDIntArrayType.uuidFromIntArray((int[]) idTag.getValue());
                    ownerCompundTag.put(new StringTag("Id", id.toString()));
                }
            }
        }
    }

    public static int getNewItemId(int id) {
        Integer newId = MappingData.oldToNewItems.get(id);
        if (newId == null) {
            Via.getPlatform().getLogger().warning("Missing 1.16 item for 1.15.2 item " + id);
            return 1;
        }
        return newId;
    }

    public static int getOldItemId(int id) {
        Integer oldId = MappingData.oldToNewItems.inverse().get(id);
        return oldId != null ? oldId : 1;
    }
}
