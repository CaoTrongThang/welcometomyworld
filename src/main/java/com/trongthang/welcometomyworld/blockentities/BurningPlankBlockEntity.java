package com.trongthang.welcometomyworld.blockentities;

import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BurningPlankBlockEntity extends BlockEntity {

    private int counter = 0;

    public BurningPlankBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksEntitiesManager.BURNING_PLANK_BLOCK_ENTITY, pos, state);
    }

    public void incrementCounter(int amount) {
        this.counter += amount;
    }

    public int getCounter() {
        return this.counter;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Counter", counter);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.counter = nbt.getInt("Counter");
    }
}
