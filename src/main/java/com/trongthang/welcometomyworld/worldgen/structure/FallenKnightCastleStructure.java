package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public class FallenKnightCastleStructure extends Structure {
    public static final Codec<FallenKnightCastleStructure> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Structure.configCodecBuilder(instance)).apply(instance, FallenKnightCastleStructure::new));

    public FallenKnightCastleStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int cx = chunkPos.getStartX() + 8;
        int cz = chunkPos.getStartZ() + 8;

        int y = context.chunkGenerator().getHeight(cx, cz, Heightmap.Type.WORLD_SURFACE_WG, context.world(),
                context.noiseConfig());
        // In the void dimension, to ensure it doesn't spawn in the ceiling or in
        // mid-air
        // Let's clamp or verify. The void land is around -80 to -30 based on noise.
        // If Y is above 200, it's the roof. We might want to fix it at a specific
        // layer.
        // Let's just find the surface and use it.

        BlockPos centerPos = new BlockPos(cx, y, cz);

        return Optional.of(new StructurePosition(centerPos, collector -> {
            addPieces(collector, context, centerPos);
        }));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context, BlockPos centerPos) {
        StructureTemplateManager templateManager = context.structureTemplateManager();
        Identifier id1 = new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle1");
        Identifier id2 = new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle2");

        StructureTemplate template1 = templateManager.getTemplateOrBlank(id1);
        Vec3i size1 = template1.getSize();

        // Place piece 1
        collector.addPiece(new FallenKnightCastlePiece(templateManager, id1, centerPos));

        // Place piece 2 right next to it along the X axis.
        // Wait, "left" and "right" usually implies X axis, but could be Z.
        // We will default to X axis (shifting by the width of piece 1).
        BlockPos pos2 = centerPos.add(size1.getX(), 0, 0);

        collector.addPiece(new FallenKnightCastlePiece(templateManager, id2, pos2));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructures.FALLEN_KNIGHT_CASTLE;
    }
}
