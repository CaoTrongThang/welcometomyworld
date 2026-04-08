package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
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

import java.util.UUID;

public class VoidWormPartEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private LivingEntity leader;
    private VoidWormEntity head;
    private float followDistance = 1.8f; // distance to stay behind leader

    private UUID headUUID;
    private UUID leaderUUID;

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world, VoidWormEntity head,
            LivingEntity leader, float followDistance) {
        super(entityType, world);
        this.noClip = true;
        this.head = head;
        this.leader = leader;
        this.followDistance = followDistance;

        if (head != null)
            this.headUUID = head.getUuid();
        if (leader != null)
            this.leaderUUID = leader.getUuid();
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            // we can check if it's body or tail based on its type if needed, or both use
            // same anim name
            return state.setAndContinue(RawAnimation.begin().thenLoop("void_worm_body_idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false; // No fall damage
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state,
            net.minecraft.util.math.BlockPos landedPosition) {
        // No fall damage
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.headUUID != null) {
            nbt.putUuid("HeadUUID", this.headUUID);
        }
        if (this.leaderUUID != null) {
            nbt.putUuid("LeaderUUID", this.leaderUUID);
        }
        nbt.putFloat("FollowDistance", this.followDistance);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("HeadUUID")) {
            this.headUUID = nbt.getUuid("HeadUUID");
        }
        if (nbt.containsUuid("LeaderUUID")) {
            this.leaderUUID = nbt.getUuid("LeaderUUID");
        }
        if (nbt.contains("FollowDistance")) {
            this.followDistance = nbt.getFloat("FollowDistance");
        }
    }

    @Override
    public void tick() {
        this.noClip = true;
        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Restore references if needed
            if (this.head == null && this.headUUID != null) {
                Entity h = serverWorld.getEntity(this.headUUID);
                if (h instanceof VoidWormEntity) {
                    this.head = (VoidWormEntity) h;
                }
            }
            if (this.leader == null && this.leaderUUID != null) {
                Entity l = serverWorld.getEntity(this.leaderUUID);
                if (l instanceof LivingEntity) {
                    this.leader = (LivingEntity) l;
                }
            }

            // If head is completely dead or doesn't exist, we should probably die
            if (this.head == null || this.head.isRemoved() || this.head.isDead()) {
                this.discard();
                return;
            }

            if (this.leader != null && this.leader.isAlive()) {
                followLeader();
            }
        }
    }

    private void followLeader() {
        Vec3d leaderPos = this.leader.getPos();
        Vec3d myPos = this.getPos();

        double distSq = leaderPos.squaredDistanceTo(myPos);

        // If we are too far, catch up
        if (distSq > followDistance * followDistance) {
            Vec3d direction = leaderPos.subtract(myPos).normalize();

            // Expected position is followDistance away from the leader in the opposite
            // direction of the leader's movement or just towards us
            Vec3d targetPos = leaderPos.subtract(direction.multiply(followDistance));

            // Move smoothly towards the target position
            Vec3d newPos = myPos.lerp(targetPos, 0.4);

            this.setPosition(newPos.x, newPos.y, newPos.z);
            this.velocityModified = true;

            // Rotate towards leader
            float targetYaw = (float) (MathHelper.atan2(direction.z, direction.x) * (180 / Math.PI)) - 90.0F;
            this.setYaw(wrapDegrees(this.getYaw(), targetYaw, 25.0f));
            this.bodyYaw = this.getYaw();
            this.headYaw = this.getYaw();

            double horizontalDist = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            float targetPitch = (float) -(MathHelper.atan2(direction.y, horizontalDist) * (180 / Math.PI));
            this.setPitch(wrapDegrees(this.getPitch(), targetPitch, 25.0f));
        }
    }

    private float wrapDegrees(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (delta > maxStep)
            delta = maxStep;
        if (delta < -maxStep)
            delta = -maxStep;
        return current + delta;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.FALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.DROWN)) {
            return false;
        }

        // Delegate damage to head
        if (this.head != null && this.head.isAlive()) {
            return this.head.damage(source, amount);
        }

        return super.damage(source, amount);
    }
}
