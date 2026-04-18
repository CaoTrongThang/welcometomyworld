package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.managers.StructuresManager;

import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class VoidanArenaStructure extends Structure {
    public static final Codec<VoidanArenaStructure> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Structure.configCodecBuilder(instance)).apply(instance, VoidanArenaStructure::new));

    public VoidanArenaStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int cx = chunkPos.getStartX() + 8;
        int cz = chunkPos.getStartZ() + 8;

        int y = context.chunkGenerator().getHeight(cx, cz, Heightmap.Type.WORLD_SURFACE_WG, context.world(),
                context.noiseConfig());

        // Skip if the surface is near the void floor — means no real terrain was found.
        // Lower threshold than NetherMediumOutpost (50) since Void Sculk terrain sits
        // lower.
        if (y < 10) {
            return Optional.empty();
        }

        BlockPos centerPos = new BlockPos(cx, y, cz);

        return Optional.of(new StructurePosition(centerPos, collector -> {
            addPieces(collector, context, centerPos);
        }));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos centerPos) {
        StructureTemplateManager templateManager = context.structureTemplateManager();
        Identifier id = new Identifier(WelcomeToMyWorld.MOD_ID, "voidan_arena");

        collector.addPiece(new VoidanArenaPiece(templateManager, id, centerPos));
    }

    @Override
    public StructureType<?> getType() {
        return StructuresManager.VOIDAN_ARENA;
    }
}
