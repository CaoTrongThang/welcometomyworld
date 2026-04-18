package com.trongthang.welcometomyworld.blockentities;

import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VoidanSummonerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int state = 0; // 0: idle, 1: mouth_close, 2: disappearing
    private int tickCounter = 0;

    public VoidanSummonerBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksEntitiesManager.VOIDAN_SUMMONER_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<VoidanSummonerBlockEntity> event) {
        if (state == 1) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("mouth_close"));
        } else if (state == 2) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("disappear"));
        } else {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean activateSummoning() {
        if (state == 0) {
            state = 1;
            tickCounter = 0;
            if (world != null && !world.isClient) {
                world.updateListeners(pos, getCachedState(), getCachedState(),
                        net.minecraft.block.Block.NOTIFY_LISTENERS);
            }
            return true;
        }
        return false;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (this.state > 0) {
            tickCounter++;

            if (this.state == 1) {
                // At tick 10 play effects
                if (tickCounter == 10) {
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.playSound(null, pos, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS,
                                1.0f, 1.0f);
                        serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0,
                                pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.1);
                    }
                }

                // Wait 50 ticks to trigger disappear
                if (tickCounter >= 50) {
                    this.state = 2; // disappearing
                    this.tickCounter = 0;
                    if (world != null && !world.isClient) {
                        world.updateListeners(pos, getCachedState(), getCachedState(),
                                net.minecraft.block.Block.NOTIFY_LISTENERS);
                    }
                }
            } else if (this.state == 2) {
                // At tick 5, disappear logic happens according to animation
                if (tickCounter == 5) {
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.playSound(null, pos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE,
                                1.0f, 1.0f);
                        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, pos.getX() + 0.5, pos.getY() + 0.5,
                                pos.getZ() + 0.5, 1, 0, 0, 0, 0);

                        net.minecraft.entity.Entity voidan = EntitiesManager.VOIDAN.spawn(serverWorld, pos.up(),
                                SpawnReason.TRIGGERED);
                        if (voidan instanceof com.trongthang.welcometomyworld.entities.Voidan.Voidan boss) {
                            net.minecraft.util.math.Direction facing = state
                                    .get(com.trongthang.welcometomyworld.blocks.VoidanSummonerBlock.FACING);
                            float yaw = facing.asRotation();
                            boss.setYaw(yaw);
                            boss.setBodyYaw(yaw);
                            boss.setHeadYaw(yaw);
                        }

                        world.breakBlock(pos, false);
                    }
                }
            }
        }
    }

    @Override
    public void readNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("SummonerState")) {
            this.state = nbt.getInt("SummonerState");
        }
        if (nbt.contains("SummonerTick")) {
            this.tickCounter = nbt.getInt("SummonerTick");
        }
    }

    @Override
    protected void writeNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("SummonerState", this.state);
        nbt.putInt("SummonerTick", this.tickCounter);
    }

    @Override
    public net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
