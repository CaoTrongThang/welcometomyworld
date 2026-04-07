package com.trongthang.welcometomyworld.blocks;

import com.trongthang.welcometomyworld.blockentities.VoidBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class VoidBlock extends BlockWithEntity {
    public VoidBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VoidBlockEntity(pos, state);
    }
}
