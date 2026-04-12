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

public class RiftPortalEntity extends PathAwareEntity implements GeoEntity {
    private static final TrackedData<Boolean> CLOSING = DataTracker.registerData(RiftPortalEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int currentFrame = 1;
    private int frameCounter = 0;

    public RiftPortalEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CLOSING, false);
    }

    public boolean isClosing() {
        return this.dataTracker.get(CLOSING);
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
            if (this.age >= 196 && !isClosing()) {
                this.dataTracker.set(CLOSING, true);
            }
            if (this.age >= 200) {
                this.discard();
                return;
            }
        }

        if (this.getWorld().isClient) {
            frameCounter++;
            if (frameCounter >= 2) {
                frameCounter = 0;
                currentFrame++;
                if (currentFrame > 30) {
                    currentFrame = 1;
                }
            }

            if (this.age % 2 == 0 && !isClosing()) {
                for (int i = 0; i < 5; i++) {
                    double rx = (this.random.nextDouble() - 0.5) * 6.0;
                    double ry = this.random.nextDouble() * 6.0 - 1.0;
                    double rz = (this.random.nextDouble() - 0.5) * 6.0;
                    this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX() + rx, this.getY() + ry,
                            this.getZ() + rz, 0, 0, 0);
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

    private static final RawAnimation SHOWED_UP = RawAnimation.begin().thenPlay("showed_up").thenLoop("idle");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.isClosing()) {
                return state.setAndContinue(CLOSE);
            }
            return state.setAndContinue(SHOWED_UP);
        }));
    }
}
