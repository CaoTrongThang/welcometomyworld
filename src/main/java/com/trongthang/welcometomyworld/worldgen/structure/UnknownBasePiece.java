package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.StructuresManager;

import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
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

public class UnknownBasePiece extends SimpleStructurePiece {
    public UnknownBasePiece(StructureTemplateManager manager, Identifier templateId, BlockPos pos) {
        super(StructuresManager.UNKNOWN_BASE_PIECE, 0, manager, templateId, templateId.toString(),
                createPlacementData(), pos);
    }

    public UnknownBasePiece(StructureContext context, NbtCompound nbt) {
        super(StructuresManager.UNKNOWN_BASE_PIECE, nbt, context.structureTemplateManager(),
                (id) -> createPlacementData());
    }

    private static StructurePlacementData createPlacementData() {
        return new StructurePlacementData()
                .setRotation(BlockRotation.NONE)
                .setMirror(BlockMirror.NONE)
                .addProcessor(UnknownBaseProcessor.INSTANCE)
                .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        super.writeNbt(context, nbt);
    }

    @Override
    protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random,
            BlockBox boundingBox) {
        if ("unknown_spawn".equals(metadata)) {
            // Replace structure block and spawn entity
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

            try {
                if (EntitiesManager.UNKNOWN != null) {
                    Unknown boss = (Unknown) EntitiesManager.UNKNOWN.create(world.toServerWorld());
                    if (boss != null) {
                        boss.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                        boss.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null, null);
                        world.spawnEntityAndPassengers(boss);
                    }
                }
            } catch (Exception e) {
                WelcomeToMyWorld.LOGGER.error("Failed to spawn Unknown boss at " + pos, e);
            }
        }
    }
}
