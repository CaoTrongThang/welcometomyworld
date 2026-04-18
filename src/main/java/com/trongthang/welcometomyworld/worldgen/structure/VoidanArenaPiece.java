package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.managers.StructuresManager;

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

public class VoidanArenaPiece extends SimpleStructurePiece {
    public VoidanArenaPiece(StructureTemplateManager manager, Identifier templateId, BlockPos pos) {
        super(StructuresManager.VOIDAN_ARENA_PIECE, 0, manager, templateId, templateId.toString(),
                createPlacementData(), pos);
    }

    public VoidanArenaPiece(StructureContext context, NbtCompound nbt) {
        super(StructuresManager.VOIDAN_ARENA_PIECE, nbt, context.structureTemplateManager(),
                (id) -> createPlacementData());
    }

    private static StructurePlacementData createPlacementData() {
        return new StructurePlacementData()
                .setRotation(BlockRotation.NONE)
                .setMirror(BlockMirror.NONE)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        super.writeNbt(context, nbt);
    }

    @Override
    protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random,
            BlockBox boundingBox) {
        // No custom handling needed since the arena only places blocks directly.
    }
}
