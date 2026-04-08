package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.trongthang.welcometomyworld.managers.EntitiesManager;

import java.util.ArrayList;
import java.util.List;

public class VoidWormEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Configurable size
    private static final int BODY_SEGMENTS = 12;
    private final List<VoidWormPartEntity> parts = new ArrayList<>();
    private boolean partsSpawned = false;

    public VoidWormEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true; // allow passing through blocks
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100f);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(2, new FlyTowardsTargetGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("void_worm_head_idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state,
            net.minecraft.util.math.BlockPos landedPosition) {
        // No fall damage
    }

    @Override
    public void tick() {
        this.noClip = true; // Ensure it stays true
        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (!partsSpawned) {
                spawnParts(serverWorld);
                partsSpawned = true;
            }
            updateParts();
        }
    }

    private void spawnParts(ServerWorld world) {
        LivingEntity previous = this;

        for (int i = 0; i < BODY_SEGMENTS; i++) {
            VoidWormPartEntity body = new VoidWormPartEntity(EntitiesManager.VOID_WORM_BODY, world, this, previous,
                    1.5f);
            body.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            world.spawnEntity(body);
            parts.add(body);
            previous = body;
        }

        VoidWormPartEntity tail = new VoidWormPartEntity(EntitiesManager.VOID_WORM_TAIL, world, this, previous, 1.5f);
        tail.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        world.spawnEntity(tail);
        parts.add(tail);
    }

    private void updateParts() {
        // Parts remove themselves if head is dead, but we also check here
        if (this.isRemoved() || this.isDead()) {
            for (VoidWormPartEntity part : parts) {
                part.discard();
            }
            return;
        }

        // Each part handles its own position following its leader
        // But we can also force updates if needed
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        for (VoidWormPartEntity part : parts) {
            part.remove(reason);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.FALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.DROWN)) {
            return false;
        }
        return super.damage(source, amount);
    }

    // A simple flight AI goal to chase targets smoothly through the air
    static class FlyTowardsTargetGoal extends Goal {
        private final VoidWormEntity worm;

        public FlyTowardsTargetGoal(VoidWormEntity worm) {
            this.worm = worm;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = worm.getTarget();
            if (target != null && target.isAlive()) {
                Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
                Vec3d dir = targetPos.subtract(worm.getPos()).normalize();

                // Smoothly adjust velocity towards target
                double speed = worm.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                Vec3d currentVel = worm.getVelocity();
                Vec3d newVel = currentVel.add(dir.multiply(0.05)).normalize().multiply(speed);

                worm.setVelocity(newVel);

                // Rotate visually
                float targetYaw = (float) (MathHelper.atan2(newVel.z, newVel.x) * (180 / Math.PI)) - 90.0F;
                worm.setYaw(wrapDegrees(worm.getYaw(), targetYaw, 5.0f));
                worm.bodyYaw = worm.getYaw();
                worm.headYaw = worm.getYaw();

                double horizontalDist = Math.sqrt(newVel.x * newVel.x + newVel.z * newVel.z);
                float targetPitch = (float) -(MathHelper.atan2(newVel.y, horizontalDist) * (180 / Math.PI));
                worm.setPitch(wrapDegrees(worm.getPitch(), targetPitch, 5.0f));

            } else {
                // If no target, fly forward slowly circling or just glide
                Vec3d currentVel = worm.getVelocity();
                if (currentVel.lengthSquared() < 0.01) {
                    // Add some small forward motion taking current yaw into account
                    float radYaw = worm.getYaw() * ((float) Math.PI / 180F);
                    worm.setVelocity(-MathHelper.sin(radYaw) * 0.2, 0, MathHelper.cos(radYaw) * 0.2);
                } else {
                    worm.setVelocity(currentVel.normalize().multiply(0.2));
                }
            }
            worm.velocityModified = true;
        }

        private float wrapDegrees(float current, float target, float maxStep) {
            float delta = MathHelper.wrapDegrees(target - current);
            if (delta > maxStep)
                delta = maxStep;
            if (delta < -maxStep)
                delta = -maxStep;
            return current + delta;
        }
    }
}
