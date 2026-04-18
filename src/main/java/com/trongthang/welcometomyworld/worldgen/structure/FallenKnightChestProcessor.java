package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.managers.StructuresManager;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

public class FallenKnightChestProcessor extends StructureProcessor {
    public static final Codec<FallenKnightChestProcessor> CODEC = Codec.unit(() -> FallenKnightChestProcessor.INSTANCE);
    public static final FallenKnightChestProcessor INSTANCE = new FallenKnightChestProcessor();

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        if (currentBlockInfo.state().isOf(Blocks.CHEST)) {
            Identifier lootTable = new Identifier(WelcomeToMyWorld.MOD_ID, "chests/fallen_knight_castle");
            return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), currentBlockInfo.state(),
                    this.setLootTable(currentBlockInfo.nbt(), lootTable));
        }
        return currentBlockInfo;
    }

    private NbtCompound setLootTable(@Nullable NbtCompound nbt, Identifier lootTable) {
        NbtCompound compound = nbt != null ? nbt : new NbtCompound();
        compound.putString("LootTable", lootTable.toString());
        compound.putLong("LootTableSeed", WelcomeToMyWorld.random.nextLong());
        return compound;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructuresManager.FALLEN_KNIGHT_CHEST_PROCESSOR;
    }
}
