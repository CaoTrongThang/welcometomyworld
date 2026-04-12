package com.trongthang.welcometomyworld.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.trongthang.welcometomyworld.Utilities.Utils;

public class PurplePortalEntity extends PathAwareEntity implements GeoEntity {

    private static final TrackedData<Boolean> TURNING_OFF = DataTracker.registerData(PurplePortalEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int currentFrame = 1;
    private int frameCounter = 0;
    private int turnOffTicks = 0;

    private int lifeTicks = 0;
    private BlockPos activatorPos = null;

    private final Map<UUID, Integer> playersInPortal = new HashMap<>();

    public void setActivatorPos(BlockPos pos) {
        this.activatorPos = pos;
    }

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("LifeTicks", this.lifeTicks);
        if (this.activatorPos != null) {
            nbt.putLong("ActivatorPos", this.activatorPos.asLong());
        }
    }

    @Override
    public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("LifeTicks")) {
            this.lifeTicks = nbt.getInt("LifeTicks");
        }
        if (nbt.contains("ActivatorPos")) {
            this.activatorPos = BlockPos.fromLong(nbt.getLong("ActivatorPos"));
        }
    }

    public PurplePortalEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TURNING_OFF, false);
    }

    public boolean isTurningOff() {
        return this.dataTracker.get(TURNING_OFF);
    }

    public void turnOff() {
        this.dataTracker.set(TURNING_OFF, true);
        this.turnOffTicks = 0;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
        // No pushing
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.setVelocity(0, 0, 0);
        this.bodyYaw = this.getYaw();
        this.headYaw = this.getYaw();

        if (!this.getWorld().isClient) {
            this.lifeTicks++;
            if (this.lifeTicks >= 12000 && !isTurningOff()) {
                this.turnOff();
            }

            if (this.activatorPos != null && this.age % 20 == 0 && !isTurningOff()) {
                if (!(this.getWorld().getBlockState(this.activatorPos)
                        .getBlock() instanceof com.trongthang.welcometomyworld.blocks.PurplePortalActivatorBlock)) {
                    this.turnOff();
                }
            }

            if (isTurningOff()) {
                turnOffTicks++;
                if (turnOffTicks >= 5) {
                    this.discard();
                    return;
                }
            } else {
                Box box = this.getBoundingBox().expand(0.5);
                List<ServerPlayerEntity> players = this.getWorld().getEntitiesByClass(ServerPlayerEntity.class, box,
                        p -> p.isAlive() && !p.isSpectator());
                Set<UUID> currentPlayers = new HashSet<>();

                for (ServerPlayerEntity player : players) {
                    UUID id = player.getUuid();
                    currentPlayers.add(id);
                    int ticks = playersInPortal.getOrDefault(id, 0) + 1;
                    playersInPortal.put(id, ticks);

                    if (ticks % 20 == 0) {
                        player.addStatusEffect(
                                new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false, true));
                    }

                    if (ticks >= 200) {
                        ServerWorld overworld = this.getWorld().getServer().getWorld(World.OVERWORLD);
                        BlockPos spawnPos = null;

                        if (player.getSpawnPointDimension() == World.OVERWORLD) {
                            spawnPos = player.getSpawnPointPosition();
                        }
                        if (spawnPos == null && overworld != null) {
                            spawnPos = overworld.getSpawnPos();
                        }

                        if (overworld != null && spawnPos != null) {
                            player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                                    player.getYaw(), player.getPitch());

                            Utils.grantAdvancement(player, "first_time_use_the_purple_portal");
                            playersInPortal.remove(id);
                        }
                    }
                }
                playersInPortal.keySet().retainAll(currentPlayers);
            }
        }

        if (this.getWorld().isClient) {
            if (!isTurningOff()) {
                frameCounter++;
                if (frameCounter >= 2) {
                    frameCounter = 0;
                    currentFrame++;
                    if (currentFrame > 20) {
                        currentFrame = 1;
                    }
                }

                if (this.age % 2 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double rx = (this.random.nextDouble() - 0.5) * 4.0;
                        double ry = this.random.nextDouble() * 4.0 + 0.5;
                        double rz = (this.random.nextDouble() - 0.5) * 4.0;
                        this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX() + rx, this.getY() + ry,
                                this.getZ() + rz, 0, 0, 0);

                        if (this.random.nextBoolean()) {
                            double rx2 = (this.random.nextDouble() - 0.5) * 3.0;
                            double ry2 = this.random.nextDouble() * 4.0 + 0.5;
                            double rz2 = (this.random.nextDouble() - 0.5) * 3.0;
                            this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL, this.getX() + rx2,
                                    this.getY() + ry2,
                                    this.getZ() + rz2, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static final RawAnimation SHOWED_UP_ANIM = RawAnimation.begin().thenPlay("showed_up");
    private static final RawAnimation TURNED_OFF_ANIM = RawAnimation.begin().thenPlay("turned_off");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isTurningOff()) {
                return state.setAndContinue(TURNED_OFF_ANIM);
            }
            return state.setAndContinue(SHOWED_UP_ANIM);
        }));
    }
}
