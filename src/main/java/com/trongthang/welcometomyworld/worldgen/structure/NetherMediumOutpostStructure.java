package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class NetherMediumOutpostStructure extends Structure {
    public static final Codec<NetherMediumOutpostStructure> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Structure.configCodecBuilder(instance)).apply(instance, NetherMediumOutpostStructure::new));

    public NetherMediumOutpostStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int cx = chunkPos.getStartX() + 8;
        int cz = chunkPos.getStartZ() + 8;

        // Target middle to top (100-150 is the ceiling range in void_dim)
        // We'll search from the top down and try to find a surface above Y = 50 first.
        int y = context.chunkGenerator().getHeight(cx, cz, Heightmap.Type.WORLD_SURFACE_WG, context.world(),
                context.noiseConfig());

        // If the surface found is too low (likely the floor), we try to force a check
        // for the ceiling
        // if it exists but was somehow missed, or just keep it if that's the only
        // option.
        // However, the user specifically wants it higher.
        // Let's ensure we are at a "high" surface if possible.

        BlockPos centerPos = new BlockPos(cx, y, cz);

        // If Y is too low, we might want to skip spawning this specific instance to
        // keep it "high"
        if (y < 50) {
            return Optional.empty();
        }

        return Optional.of(new StructurePosition(centerPos, collector -> {
            addPieces(collector, context, centerPos);
        }));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos centerPos) {
        Identifier id = new Identifier(WelcomeToMyWorld.MOD_ID, "nether_medium_outpost");
        collector.addPiece(new NetherMediumOutpostPiece(context.structureTemplateManager(), id, centerPos));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.NETHER_MEDIUM_OUTPOST;
    }
}
