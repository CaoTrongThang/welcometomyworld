package com.trongthang.welcometomyworld.blockentities;

import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class VoidBlockEntity extends BlockEntity {
    public VoidBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksEntitiesManager.VOID_BLOCK_ENTITY, pos, state);
    }
}
