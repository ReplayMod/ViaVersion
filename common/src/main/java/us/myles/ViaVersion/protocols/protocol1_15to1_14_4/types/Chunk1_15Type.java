package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.types;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.chunks.BaseChunk;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.type.PartialType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import us.myles.ViaVersion.api.type.types.version.Types1_13;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Chunk1_15Type extends PartialType<Chunk, ClientWorld> {

    public Chunk1_15Type(ClientWorld param) {
        super(param, Chunk.class);
    }

    @Override
    public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
        int chunkX = input.readInt();
        int chunkZ = input.readInt();

        boolean groundUp = input.readBoolean();
        int primaryBitmask = Type.VAR_INT.read(input);
        CompoundTag heightMap = Type.NBT.read(input);

        int[] biomeData = groundUp ? new int[1024] : null;
        if (groundUp) {
            for (int i = 0; i < 1024; i++) {
                biomeData[i] = input.readInt();
            }
        }

        Type.VAR_INT.read(input);

        BitSet usedSections = new BitSet(16);
        ChunkSection[] sections = new ChunkSection[16];
        // Calculate section count from bitmask
        for (int i = 0; i < 16; i++) {
            if ((primaryBitmask & (1 << i)) != 0) {
                usedSections.set(i);
            }
        }

        // Read sections
        for (int i = 0; i < 16; i++) {
            if (!usedSections.get(i)) continue; // Section not set
            short nonAirBlocksCount = input.readShort();
            ChunkSection section = Types1_13.CHUNK_SECTION.read(input);
            section.setNonAirBlocksCount(nonAirBlocksCount);
            sections[i] = section;
        }

        List<CompoundTag> nbtData = new ArrayList<>(Arrays.asList(Type.NBT_ARRAY.read(input)));

        // Read all the remaining bytes (workaround for #681)
        if (input.readableBytes() > 0) {
            byte[] array = Type.REMAINING_BYTES.read(input);
            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Found " + array.length + " more bytes than expected while reading the chunk: " + chunkX + "/" + chunkZ);
            }
        }

        return new BaseChunk(chunkX, chunkZ, groundUp, primaryBitmask, sections, biomeData, heightMap, nbtData);
    }

    @Override
    public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
        output.writeInt(chunk.getX());
        output.writeInt(chunk.getZ());

        output.writeBoolean(chunk.isGroundUp());
        Type.VAR_INT.write(output, chunk.getBitmask());
        Type.NBT.write(output, chunk.getHeightMap());

        // Write biome data
        if (chunk.isBiomeData()) {
            for (int value : chunk.getBiomeData()) {
                output.writeInt(value);
            }
        }

        ByteBuf buf = output.alloc().buffer();
        try {
            for (int i = 0; i < 16; i++) {
                ChunkSection section = chunk.getSections()[i];
                if (section == null) continue; // Section not set
                buf.writeShort(section.getNonAirBlocksCount());
                Types1_13.CHUNK_SECTION.write(buf, section);
            }
            buf.readerIndex(0);
            Type.VAR_INT.write(output, buf.readableBytes());
            output.writeBytes(buf);
        } finally {
            buf.release(); // release buffer
        }

        // Write Block Entities
        Type.NBT_ARRAY.write(output, chunk.getBlockEntities().toArray(new CompoundTag[0]));
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseChunkType.class;
    }
}
