package com.trongthang.welcometomyworld.blockentities;

import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class JustACounterBlockEntity extends BlockEntity {

    public int counter = 0;

    public JustACounterBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksEntitiesManager.RUSTED_IRON_BLOCK_ENTITY, pos, state);
    }

    public void incrementCounter(int amount) {
        this.counter += amount;
    }

    public int getCounter() {
        return this.counter;
    }

    public void setCounter(int amount) {
        counter = amount;
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
