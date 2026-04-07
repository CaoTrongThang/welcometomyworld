package com.trongthang.welcometomyworld.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FernBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class CustomPlantBlock extends FernBlock {
    public CustomPlantBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOpaqueFullCube(world, pos);
    }
}
