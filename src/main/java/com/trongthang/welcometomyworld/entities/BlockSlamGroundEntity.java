package com.trongthang.welcometomyworld.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class BlockSlamGroundEntity extends Entity {

    private static final TrackedData<BlockState> BLOCK_STATE = DataTracker.registerData(BlockSlamGroundEntity.class,
            TrackedDataHandlerRegistry.BLOCK_STATE);

    private static final TrackedData<Float> PITCH = DataTracker.registerData(BlockSlamGroundEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> YAW = DataTracker.registerData(BlockSlamGroundEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> ROLL = DataTracker.registerData(BlockSlamGroundEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    public BlockSlamGroundEntity(EntityType<? extends BlockSlamGroundEntity> type, World world) {
        super(type, world);

        // Random rotations for the block look
        this.setPitch(world.getRandom().nextFloat() * 20f - 10f);
        this.setYaw(world.getRandom().nextFloat() * 360f);
        this.setRoll(world.getRandom().nextFloat() * 20f - 10f);
        this.noClip = true; // Start as noClip for initial pop
    }

    @Override
    public void tick() {
        super.tick();

        // Apply physics logic on both client and server for maximum smoothness
        if (this.age < 12) {
            // Initial jump phase: noClip is false to allow landing
            this.noClip = false;
        }

        if (this.age < 60) {
            if (!this.isOnGround() && !this.noClip) {
                // Apply gravity + air friction
                this.setVelocity(this.getVelocity().add(0, -0.05, 0).multiply(0.98));
            } else if (this.isOnGround()) {
                // Landed on ground
                this.setVelocity(0, 0, 0);
            }
        } else {
            // Sinking phase
            this.noClip = true;
            this.setVelocity(0, -0.015, 0);
        }

        this.move(MovementType.SELF, this.getVelocity());

        if (!this.getWorld().isClient()) {
            this.velocityDirty = true;
            if (this.age >= 140) {
                this.discard();
            }
        }
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(BLOCK_STATE, Blocks.AIR.getDefaultState());
        this.dataTracker.startTracking(PITCH, 0f);
        this.dataTracker.startTracking(YAW, 0f);
        this.dataTracker.startTracking(ROLL, 0f);
    }

    public float getPitch() {
        return this.dataTracker.get(PITCH);
    }

    public void setPitch(float value) {
        this.dataTracker.set(PITCH, value);
    }

    public float getYaw() {
        return this.dataTracker.get(YAW);
    }

    public void setYaw(float value) {
        this.dataTracker.set(YAW, value);
    }

    public float getRoll() {
        return this.dataTracker.get(ROLL);
    }

    public void setRoll(float value) {
        this.dataTracker.set(ROLL, value);
    }

    public BlockState getBlockState() {
        return this.dataTracker.get(BLOCK_STATE);
    }

    public void setBlockState(BlockState blockState) {
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
        return false;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
