/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_17Types;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.api.type.types.version.Types1_17;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.metadata.MetadataRewriter1_17To1_16_4;

public class EntityPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        MetadataRewriter1_17To1_16_4 metadataRewriter = protocol.get(MetadataRewriter1_17To1_16_4.class);
        metadataRewriter.registerTrackerWithData(ClientboundPackets1_16_2.SPAWN_ENTITY, Entity1_17Types.FALLING_BLOCK);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_MOB);
        metadataRewriter.registerTracker(ClientboundPackets1_16_2.SPAWN_PLAYER, Entity1_17Types.PLAYER);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_16_2.ENTITY_METADATA, Types1_14.METADATA_LIST, Types1_17.METADATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_16_2.DESTROY_ENTITIES, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int[] entityIds = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
                    wrapper.cancel();

                    EntityTracker entityTracker = wrapper.user().getEntityTracker(Protocol1_17To1_16_4.class);
                    for (int entityId : entityIds) {
                        entityTracker.removeEntity(entityId);

                        // Send individual remove packets
                        PacketWrapper newPacket = wrapper.create(ClientboundPackets1_17.REMOVE_ENTITY);
                        newPacket.write(Type.VAR_INT, entityId);
                        newPacket.send(Protocol1_17To1_16_4.class);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.ENTITY_PROPERTIES, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Entity id
                handler(wrapper -> {
                    // Collection length is now a var int
                    wrapper.write(Type.VAR_INT, wrapper.read(Type.INT));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.PLAYER_POSITION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.DOUBLE);
                map(Type.FLOAT);
                map(Type.FLOAT);
                map(Type.BYTE);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    // Dismount vehicle
                    wrapper.write(Type.BOOLEAN, false);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.COMBAT_EVENT, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Combat packet actions have been split into individual packets (the content hasn't changed)
                    int type = wrapper.read(Type.VAR_INT);
                    ClientboundPacketType packetType;
                    switch (type) {
                        case 0:
                            packetType = ClientboundPackets1_17.COMBAT_ENTER;
                            break;
                        case 1:
                            packetType = ClientboundPackets1_17.COMBAT_END;
                            break;
                        case 2:
                            packetType = ClientboundPackets1_17.COMBAT_KILL;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid combat type received: " + type);
                    }

                    wrapper.setId(packetType.getId());
                });
            }
        });

        // The parent class of the other entity move packets that is never actually used has finally been removed from the id list
        protocol.cancelClientbound(ClientboundPackets1_16_2.ENTITY_MOVEMENT);
    }
}
