/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_3;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.UUIDIntArrayType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.BlockItemPacketRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.EntityPacketRewriter1_20_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20_3To1_20_2 extends AbstractProtocol<ClientboundPackets1_20_2, ClientboundPackets1_20_3, ServerboundPackets1_20_2, ServerboundPackets1_20_3> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.20.2", "1.20.3");
    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList(
            "interpret",
            "bold",
            "italic",
            "underlined",
            "strikethrough",
            "obfuscated"
    ));
    private final BlockItemPacketRewriter1_20_3 itemRewriter = new BlockItemPacketRewriter1_20_3(this);
    private final EntityPacketRewriter1_20_3 entityRewriter = new EntityPacketRewriter1_20_3(this);

    public Protocol1_20_3To1_20_2() {
        super(ClientboundPackets1_20_2.class, ClientboundPackets1_20_3.class, ServerboundPackets1_20_2.class, ServerboundPackets1_20_3.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        cancelServerbound(ServerboundPackets1_20_3.CONTAINER_SLOT_STATE_CHANGED);

        final TagRewriter<ClientboundPackets1_20_2> tagRewriter = new TagRewriter<>(this);
        tagRewriter.registerGeneric(ClientboundPackets1_20_2.TAGS);

        final SoundRewriter<ClientboundPackets1_20_2> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_20_2.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_20_2.ENTITY_SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_20_2.STATISTICS);
        new CommandRewriter1_19_4<>(this).registerDeclareCommands1_19(ClientboundPackets1_20_2.DECLARE_COMMANDS);

        registerClientbound(ClientboundPackets1_20_2.UPDATE_SCORE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Owner

            final byte action = wrapper.read(Type.BYTE);
            final String objectiveName = wrapper.read(Type.STRING);

            if (action == 1) { // Reset score
                wrapper.write(Type.OPTIONAL_STRING, objectiveName.isEmpty() ? null : objectiveName);
                wrapper.setPacketType(ClientboundPackets1_20_3.RESET_SCORE);
                return;
            }

            wrapper.write(Type.STRING, objectiveName);
            wrapper.passthrough(Type.VAR_INT); // Score

            // Null display and number format
            wrapper.write(Type.OPTIONAL_TAG, null);
            wrapper.write(Type.BOOLEAN, false);
        });
        registerClientbound(ClientboundPackets1_20_2.SCOREBOARD_OBJECTIVE, wrapper -> {
            wrapper.passthrough(Type.STRING); // Objective Name
            final byte action = wrapper.passthrough(Type.BYTE); // Method
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Type.VAR_INT); // Render type
                wrapper.write(Type.BOOLEAN, false); // Null number format
            }
        });

        registerServerbound(ServerboundPackets1_20_3.UPDATE_JIGSAW_BLOCK, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.STRING); // Name
            wrapper.passthrough(Type.STRING); // Target
            wrapper.passthrough(Type.STRING); // Pool
            wrapper.passthrough(Type.STRING); // Final state
            wrapper.passthrough(Type.STRING); // Joint type
            wrapper.read(Type.VAR_INT); // Selection priority
            wrapper.read(Type.VAR_INT); // Placement priority
        });

        // Components are now (mostly) written as nbt instead of json strings
        registerClientbound(ClientboundPackets1_20_2.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier

                // Parent
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.STRING);
                }

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    convertComponent(wrapper); // Title
                    convertComponent(wrapper); // Description
                    itemRewriter.handleItemToClient(wrapper.passthrough(Type.ITEM1_20_2)); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Type.INT);
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                final int requirements = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY);
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_COMPLETE, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Transaction id
            wrapper.passthrough(Type.VAR_INT); // Start
            wrapper.passthrough(Type.VAR_INT); // Length

            final int suggestions = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < suggestions; i++) {
                wrapper.passthrough(Type.STRING); // Suggestion
                convertOptionalComponent(wrapper); // Tooltip
            }
        });
        registerClientbound(ClientboundPackets1_20_2.MAP_DATA, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Map id
            wrapper.passthrough(Type.BYTE); // Scale
            wrapper.passthrough(Type.BOOLEAN); // Locked
            if (wrapper.passthrough(Type.BOOLEAN)) {
                final int icons = wrapper.passthrough(Type.VAR_INT);
                for (int i = 0; i < icons; i++) {
                    wrapper.passthrough(Type.VAR_INT); // Type
                    wrapper.passthrough(Type.BYTE); // X
                    wrapper.passthrough(Type.BYTE); // Y
                    wrapper.passthrough(Type.BYTE); // Rotation
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });
        registerClientbound(ClientboundPackets1_20_2.BOSSBAR, wrapper -> {
            wrapper.passthrough(Type.UUID); // Id

            final int action = wrapper.passthrough(Type.VAR_INT);
            if (action == 0 || action == 3) {
                convertComponent(wrapper);
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_CHAT, wrapper -> {
            wrapper.passthrough(Type.UUID); // Sender
            wrapper.passthrough(Type.VAR_INT); // Index
            wrapper.passthrough(Type.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Type.STRING); // Plain content
            wrapper.passthrough(Type.LONG); // Timestamp
            wrapper.passthrough(Type.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Type.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Type.SIGNATURE_BYTES);
                }
            }

            convertOptionalComponent(wrapper); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Type.VAR_INT);
            if (filterMaskType == 2) {
                wrapper.passthrough(Type.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(Type.VAR_INT); // Chat type
            convertComponent(wrapper); // Sender
            convertOptionalComponent(wrapper); // Target
        });
        registerClientbound(ClientboundPackets1_20_2.TEAMS, wrapper -> {
            wrapper.passthrough(Type.STRING); // Team Name
            final byte action = wrapper.passthrough(Type.BYTE); // Mode
            if (action == 0 || action == 2) {
                convertComponent(wrapper); // Display Name
                wrapper.passthrough(Type.BYTE); // Flags
                wrapper.passthrough(Type.STRING); // Name Tag Visibility
                wrapper.passthrough(Type.STRING); // Collision rule
                wrapper.passthrough(Type.VAR_INT); // Color
                convertComponent(wrapper); // Prefix
                convertComponent(wrapper); // Suffix
            }
        });

        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), ClientboundConfigurationPackets1_20_2.DISCONNECT.getId(), this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISCONNECT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.RESOURCE_PACK, ClientboundPackets1_20_3.RESOURCE_PACK_PUSH, resourcePackHandler(ClientboundPackets1_20_3.RESOURCE_PACK_POP));
        registerClientbound(ClientboundPackets1_20_2.SERVER_DATA, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.ACTIONBAR, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_TEXT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.TITLE_SUBTITLE, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.DISGUISED_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.SYSTEM_CHAT, this::convertComponent);
        registerClientbound(ClientboundPackets1_20_2.OPEN_WINDOW, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id

            final int containerTypeId = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, MAPPINGS.getMenuMappings().getNewId(containerTypeId));

            convertComponent(wrapper);
        });
        registerClientbound(ClientboundPackets1_20_2.TAB_LIST, wrapper -> {
            convertComponent(wrapper);
            convertComponent(wrapper);
        });

        registerClientbound(ClientboundPackets1_20_2.COMBAT_KILL, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Duration
                handler(wrapper -> convertComponent(wrapper));
            }
        });
        registerClientbound(ClientboundPackets1_20_2.PLAYER_INFO_UPDATE, wrapper -> {
            final BitSet actions = wrapper.passthrough(Type.PROFILE_ACTIONS_ENUM);
            final int entries = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < entries; i++) {
                wrapper.passthrough(Type.UUID);
                if (actions.get(0)) {
                    wrapper.passthrough(Type.STRING); // Player Name

                    final int properties = wrapper.passthrough(Type.VAR_INT);
                    for (int j = 0; j < properties; j++) {
                        wrapper.passthrough(Type.STRING); // Name
                        wrapper.passthrough(Type.STRING); // Value
                        wrapper.passthrough(Type.OPTIONAL_STRING); // Signature
                    }
                }
                if (actions.get(1) && wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.UUID); // Session UUID
                    wrapper.passthrough(Type.PROFILE_KEY);
                }
                if (actions.get(2)) {
                    wrapper.passthrough(Type.VAR_INT); // Gamemode
                }
                if (actions.get(3)) {
                    wrapper.passthrough(Type.BOOLEAN); // Listed
                }
                if (actions.get(4)) {
                    wrapper.passthrough(Type.VAR_INT); // Latency
                }
                if (actions.get(5)) {
                    convertOptionalComponent(wrapper); // Display name
                }
            }
        });

        registerServerbound(ServerboundPackets1_20_3.RESOURCE_PACK_STATUS, resourcePackStatusHandler());

        registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.RESOURCE_PACK, resourcePackStatusHandler());
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_PUSH.getId(), resourcePackHandler(ClientboundConfigurationPackets1_20_3.RESOURCE_PACK_POP));
        // TODO Auto map via packet types provider
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.UPDATE_ENABLED_FEATURES.getId(), ClientboundConfigurationPackets1_20_3.UPDATE_ENABLED_FEATURES.getId());
        registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.UPDATE_TAGS.getId(), ClientboundConfigurationPackets1_20_3.UPDATE_TAGS.getId());
    }

    private PacketHandler resourcePackStatusHandler() {
        return wrapper -> {
            wrapper.read(Type.UUID); // Pack UUID

            final int action = wrapper.read(Type.VAR_INT);
            if (action == 4) { // Downloaded
                wrapper.cancel();
            } else if (action > 4) { // Invalid url, failed reload, and discarded
                wrapper.write(Type.VAR_INT, 2); // Failed download
            } else {
                wrapper.write(Type.VAR_INT, action);
            }
        };
    }

    private PacketHandler resourcePackHandler(final ClientboundPacketType popType) {
        return wrapper -> {
            wrapper.write(Type.UUID, UUID.randomUUID());
            wrapper.passthrough(Type.STRING); // Url
            wrapper.passthrough(Type.STRING); // Hash
            wrapper.passthrough(Type.BOOLEAN); // Required
            convertOptionalComponent(wrapper);

            // Drop old resource packs first
            final PacketWrapper dropPacksPacket = wrapper.create(popType);
            dropPacksPacket.write(Type.OPTIONAL_UUID, null);
            dropPacksPacket.send(Protocol1_20_3To1_20_2.class);
        };
    }

    private void convertComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.TAG, jsonComponentToTag(wrapper.read(Type.COMPONENT)));
    }

    private void convertOptionalComponent(final PacketWrapper wrapper) throws Exception {
        wrapper.write(Type.OPTIONAL_TAG, jsonComponentToTag(wrapper.read(Type.OPTIONAL_COMPONENT)));
    }

    public static @Nullable JsonElement tagComponentToJson(@Nullable final Tag tag) {
        try {
            return convertToJson(null, tag);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + tag, e);
            return new JsonPrimitive("<error>");
        }
    }

    public static @Nullable Tag jsonComponentToTag(@Nullable final JsonElement component) {
        try {
            return convertToTag(component);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + component, e);
            return new StringTag("<error>");
        }
    }

    private static @Nullable Tag convertToTag(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonObject()) {
            final CompoundTag tag = new CompoundTag();
            for (final Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                // Not strictly needed, but might as well make it more compact
                convertObjectEntry(entry.getKey(), entry.getValue(), tag);
            }
            return tag;
        } else if (element.isJsonArray()) {
            return convertJsonArray(element);
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new StringTag(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new ByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            }

            final Number number = primitive.getAsNumber();
            if (number instanceof Integer) {
                return new IntTag(number.intValue());
            } else if (number instanceof Byte) {
                return new ByteTag(number.byteValue());
            } else if (number instanceof Short) {
                return new ShortTag(number.shortValue());
            } else if (number instanceof Long) {
                return new LongTag(number.longValue());
            } else if (number instanceof Double) {
                return new DoubleTag(number.doubleValue());
            } else if (number instanceof Float) {
                return new FloatTag(number.floatValue());
            }
            return new StringTag(primitive.getAsString()); // ???
        }
        throw new IllegalArgumentException("Unhandled json type " + element.getClass().getSimpleName() + " with value " + element.getAsString());
    }

    private static ListTag convertJsonArray(final JsonElement element) {
        // TODO Number arrays?
        final ListTag listTag = new ListTag();
        boolean singleType = true;
        for (final JsonElement entry : element.getAsJsonArray()) {
            final Tag convertedEntryTag = convertToTag(entry);
            if (listTag.getElementType() != null && listTag.getElementType() != convertedEntryTag.getClass()) {
                singleType = false;
                break;
            }

            listTag.add(convertedEntryTag);
        }

        if (singleType) {
            return listTag;
        }

        // Generally, vanilla-esque serializers should not produce this format, so it should be rare
        // Lists are only used for lists of components ("extra" and "with")
        final ListTag processedListTag = new ListTag();
        for (final JsonElement entry : element.getAsJsonArray()) {
            final Tag convertedTag = convertToTag(entry);
            if (convertedTag instanceof CompoundTag) {
                processedListTag.add(listTag);
                continue;
            }

            // Wrap all entries in compound tags as lists can only consist of one type of tag
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("text", new StringTag());
            compoundTag.put("extra", convertedTag);
        }
        return processedListTag;
    }

    private static void convertObjectEntry(final String key, final JsonElement element, final CompoundTag tag) {
        if ((key.equals("contents")) && element.isJsonObject()) {
            // Store show_entity id as int array instead of uuid string
            final JsonObject hoverEvent = element.getAsJsonObject();
            final JsonElement id = hoverEvent.get("id");
            final UUID uuid;
            if (id != null && id.isJsonPrimitive() && (uuid = parseUUID(id.getAsString())) != null) {
                hoverEvent.remove("id");

                final CompoundTag convertedTag = (CompoundTag) convertToTag(element);
                convertedTag.put("id", new IntArrayTag(UUIDIntArrayType.uuidToIntArray(uuid)));
                tag.put(key, convertedTag);
                return;
            }
        }

        tag.put(key, convertToTag(element));
    }

    private static @Nullable UUID parseUUID(final String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable JsonElement convertToJson(final @Nullable String key, final @Nullable Tag tag) {
        if (tag == null) {
            return null;
        } else if (tag instanceof CompoundTag) {
            final JsonObject object = new JsonObject();
            for (final Map.Entry<String, Tag> entry : ((CompoundTag) tag).entrySet()) {
                convertCompoundTagEntry(entry.getKey(), entry.getValue(), object);
            }
            return object;
        } else if (tag instanceof ListTag) {
            final ListTag list = (ListTag) tag;
            final JsonArray array = new JsonArray();
            for (final Tag listEntry : list) {
                array.add(convertToJson(null, listEntry));
            }
            return array;
        } else if (tag instanceof NumberTag) {
            final NumberTag numberTag = (NumberTag) tag;
            if (key != null && BOOLEAN_TYPES.contains(key)) {
                // Booleans don't have a direct representation in nbt
                return new JsonPrimitive(numberTag.asBoolean());
            }
            return new JsonPrimitive(numberTag.getValue());
        } else if (tag instanceof StringTag) {
            return new JsonPrimitive(((StringTag) tag).getValue());
        } else if (tag instanceof ByteArrayTag) {
            final ByteArrayTag arrayTag = (ByteArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final byte num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof IntArrayTag) {
            final IntArrayTag arrayTag = (IntArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final int num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof LongArrayTag) {
            final LongArrayTag arrayTag = (LongArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final long num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        }
        throw new IllegalArgumentException("Unhandled tag type " + tag.getClass().getSimpleName());
    }

    private static void convertCompoundTagEntry(final String key, final Tag tag, final JsonObject object) {
        if ((key.equals("contents")) && tag instanceof CompoundTag) {
            // Back to a UUID string
            final CompoundTag showEntity = (CompoundTag) tag;
            final Tag idTag = showEntity.get("id");
            if (idTag instanceof IntArrayTag) {
                showEntity.remove("id");

                final JsonObject convertedElement = (JsonObject) convertToJson(key, tag);
                convertedElement.addProperty("id", uuidIntsToString(((IntArrayTag) idTag).getValue()));
                object.add(key, convertedElement);
                return;
            }
        }

        // "":1 is a valid tag, but not a valid json component
        object.add(key.isEmpty() ? "text" : key, convertToJson(key, tag));
    }

    private static String uuidIntsToString(final int[] parts) {
        if (parts.length != 4) {
            return new UUID(0, 0).toString();
        }
        return UUIDIntArrayType.uuidFromIntArray(parts).toString();
    }

    @Override
    protected void onMappingDataLoaded() {
        super.onMappingDataLoaded();
        EntityTypes1_20_3.initialize(this);
        Types1_20_3.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.ITEM1_20_2)
                .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);
    }

    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_3.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public BlockItemPacketRewriter1_20_3 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public EntityPacketRewriter1_20_3 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    protected ServerboundPacketType serverboundFinishConfigurationPacket() {
        return ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }

    @Override
    protected ClientboundPacketType clientboundFinishConfigurationPacket() {
        return ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION;
    }
}
