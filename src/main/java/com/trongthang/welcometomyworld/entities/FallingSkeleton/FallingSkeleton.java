package com.trongthang.welcometomyworld.entities.FallingSkeleton;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FallingSkeleton extends HostileEntity implements GeoEntity {
    private static final TrackedData<Boolean> HAS_LANDED = DataTracker.registerData(FallingSkeleton.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int ticksSinceLanded = 0;
    private int survivalTicks = 0;

    public FallingSkeleton(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAS_LANDED, false);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier,
            DamageSource damageSource) {
        return false;
    }

    @Override
    public void tick() {
        if (!this.isOnGround() && this.getVelocity().y < 0) {
            this.setVelocity(this.getVelocity().multiply(1.0, 1.0, 1.0));
        }
        super.tick();

        if (this.getWorld().isClient())
            return;

        survivalTicks++;

        if (!this.dataTracker.get(HAS_LANDED)) {
            if (this.isOnGround()) {
                this.dataTracker.set(HAS_LANDED, true);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENTITY_SKELETON_DEATH, this.getSoundCategory(), 1.0F, 1.0F);
            }
        } else {
            ticksSinceLanded++;
        }

        if (ticksSinceLanded >= 100 || survivalTicks >= 3000) { // 5 seconds after landing or safety timeout
            this.discard();
        }
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        this.dropItem(Items.BONE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "fallingController", 0, state -> {
            if (!this.dataTracker.get(HAS_LANDED)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("skeleton_falling"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("skeleton_hit_the_ground"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
