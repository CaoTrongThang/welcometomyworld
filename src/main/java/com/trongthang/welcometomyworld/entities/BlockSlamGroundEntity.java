package com.trongthang.welcometomyworld.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class BlockSlamGroundEntity extends Entity {
    private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(BlockSlamGroundEntity.class, TrackedDataHandlerRegistry.BLOCK_STATE);

    private static final TrackedData<Float> PITCH = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> YAW = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROLL = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> Y_OFFSET = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.FLOAT);

    public BlockSlamGroundEntity(EntityType<? extends BlockSlamGroundEntity> type, World world) {
        super(type, world);

        this.setPitch(world.getRandom().nextFloat() * 30f - 15f);
        this.setYaw(world.getRandom().nextFloat() * 360f);
        this.setRoll(world.getRandom().nextFloat() * 15f - 7.5f);
        this.setYOffset(world.getRandom().nextFloat() * 0.2f - 0.1f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age >= 100) {
            this.discard();
        }
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(BLOCK_STATE, Blocks.AIR.getDefaultState());
        this.dataTracker.startTracking(PITCH, 0f);
        this.dataTracker.startTracking(YAW, 0f);
        this.dataTracker.startTracking(ROLL, 0f);
        this.dataTracker.startTracking(Y_OFFSET, 0f);
    }

    public float getPitch(){
        return this.dataTracker.get(PITCH);
    }
    public void setPitch(float value){
        this.dataTracker.set(PITCH, value);
    }

    public float getYaw(){
        return this.dataTracker.get(YAW);
    }
    public void setYaw(float value){
        this.dataTracker.set(YAW, value);
    }

    public float getRoll(){
        return this.dataTracker.get(ROLL);
    }
    public void setRoll(float value){
        this.dataTracker.set(ROLL, value);
    }

    public float getYOffset(){
        return this.dataTracker.get(Y_OFFSET);
    }
    public void setYOffset(float value){
        this.dataTracker.set(Y_OFFSET, value);
    }


    public BlockState getBlockState(){
        return this.dataTracker.get(BLOCK_STATE);
    }
    public void setBlockState(BlockState blockState){
        this.dataTracker.set(BLOCK_STATE, blockState);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public boolean isCollidable() {
        return false;  // Disable general collision checks
    }

    @Override
    public boolean canHit() {
        return false;  // Disable projectile/attack interactions
    }
}
