package com.trongthang.welcometomyworld.worldgen.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public class NetherMediumOutpostPiece extends SimpleStructurePiece {
    public NetherMediumOutpostPiece(StructureTemplateManager manager, Identifier templateId, BlockPos pos) {
        super(ModStructures.NETHER_MEDIUM_OUTPOST_PIECE, 0, manager, templateId, templateId.toString(),
                createPlacementData(), pos);
    }

    public NetherMediumOutpostPiece(StructureContext context, NbtCompound nbt) {
        super(ModStructures.NETHER_MEDIUM_OUTPOST_PIECE, nbt, context.structureTemplateManager(),
                (id) -> createPlacementData());
    }

    private static StructurePlacementData createPlacementData() {
        return new StructurePlacementData()
                .setRotation(BlockRotation.NONE)
                .setMirror(BlockMirror.NONE)
                .addProcessor(NetherMediumOutpostChestProcessor.INSTANCE)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
    }

    @Override
    protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world,
            Random random, BlockBox boundingBox) {
    }
}
