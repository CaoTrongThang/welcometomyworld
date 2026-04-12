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
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;

public class PurplePortal extends PathAwareEntity implements GeoEntity {

    // 30 minutes = 30 * 60 * 20 ticks
    public static final int MAX_LIFE_TICKS = 36000;

    private static final TrackedData<Integer> LIFE_TICKS = DataTracker.registerData(PurplePortal.class,
            TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int currentFrame = 1;
    private int frameCounter = 0;

    public PurplePortal(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(LIFE_TICKS, MAX_LIFE_TICKS);
    }

    public int getLifeTicks() {
        return this.dataTracker.get(LIFE_TICKS);
    }

    /** Returns a scale factor from 1.0 (full size) down to ~0.0 (expired). */
    public float getLifeScale() {
        return (float) getLifeTicks() / MAX_LIFE_TICKS;
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
            int remaining = getLifeTicks() - 1;
            if (remaining <= 0) {
                this.discard();
                return;
            }
            this.dataTracker.set(LIFE_TICKS, remaining);
        }

        if (this.getWorld().isClient) {
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
                        this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL, this.getX() + rx2, this.getY() + ry2,
                                this.getZ() + rz2, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations
    }
}
