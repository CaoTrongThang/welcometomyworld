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
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
    private float followDistance = 1f; // distance to stay behind leader

    private UUID headUUID;
    private UUID leaderUUID;

    // Visual rotation fields for CLIENT SIDE smoothing
    public float visualPitch = 0.0f;
    public float prevVisualPitch = 0.0f;
    public float visualYaw = 0.0f;
    public float prevVisualYaw = 0.0f;

    // Server-side persistent rotation state
    private float serverSidePitch = 0.0f;
    private float serverSideYaw = 0.0f;

    private static final TrackedData<Float> TARGET_PITCH = DataTracker.registerData(VoidWormPartEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TARGET_PITCH, 0.0f);
    }

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world, VoidWormEntity head,
            LivingEntity leader, float followDistance) {
        super(entityType, world);
        this.noClip = true;
        this.setNoGravity(true);
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
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL
                && this.isDisallowedInPeaceful()) {
            this.discard();
        } else {
            this.despawnCounter = 0;
        }
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
        this.setNoGravity(true);

        if (this.getWorld().isClient) {
            this.prevVisualPitch = this.visualPitch;
            this.prevVisualYaw = this.visualYaw;
        }

        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Keep the chunk loaded while the part is alive and ticking
            serverWorld.getChunkManager().addTicket(net.minecraft.server.world.ChunkTicketType.PORTAL,
                    new net.minecraft.util.math.ChunkPos(this.getBlockPos()), 3, this.getBlockPos());

            // Restore references if needed
            if (this.head == null && this.headUUID != null) {
                Entity h = serverWorld.getEntity(this.headUUID);
                if (h instanceof VoidWormEntity) {
                    this.head = (VoidWormEntity) h;
                    this.head.registerPart(this); // Re-register with head after reload
                }
            }
            if (this.head != null && this.head.isRemoved()) {
                if (this.head.getRemovalReason() != null && this.head.getRemovalReason().shouldDestroy()) {
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("part_discard_head",
                            "head destroyed, discarding part", "head_reason", this.head.getRemovalReason());
                    this.discard();
                    return;
                }
                this.head = null;
            }

            if (this.leader == null && this.leaderUUID != null) {
                Entity l = serverWorld.getEntity(this.leaderUUID);
                if (l instanceof LivingEntity) {
                    this.leader = (LivingEntity) l;
                }
            }
            if (this.leader != null && this.leader.isRemoved()) {
                this.leader = null;
            }

            if (this.head != null && this.head.getHealth() <= 0.0f) {
                com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("part_discard_dead",
                        "head dead, discarding part");
                this.discard();
                return;
            }

            if (this.leader != null && this.leader.isAlive()) {
                followLeader();
            } else if (this.age % 40 == 0) {
                // Periodic debug if stalling
                com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("part_stall", "waiting for leader", "uuid",
                        this.getUuid(), "leader_uuid", this.leaderUUID);
            }
        } else {
            // Client side interpolation
            float targetP = this.dataTracker.get(TARGET_PITCH);
            this.visualPitch += MathHelper.wrapDegrees(targetP - this.visualPitch) * 0.3f;

            // We can also sync Yaw if needed, but Yaw is usually synced by vanilla.
            // However, to fix glitchiness, we use visualYaw chasing entity yaw.
            this.visualYaw += MathHelper.wrapDegrees(this.getYaw() - this.visualYaw) * 0.3f;
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9D)); // Apply custom air friction
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

            // SNAP BACK if way too far (e.g. was trapped in unloaded chunk)
            if (distSq > followDistance * followDistance * 25.0) { // > 5x follow distance
                // Snap directly behind the leader's current orientation
                Vec3d snapDir = Vec3d.fromPolar(this.leader.getPitch(), this.leader.getYaw()).normalize();
                Vec3d snapPos = leaderPos.subtract(snapDir.multiply(followDistance));
                this.setPosition(snapPos.x, snapPos.y, snapPos.z);
                this.serverSideYaw = this.leader.getYaw();
                this.serverSidePitch = this.leader.getPitch();
                this.setYaw(this.serverSideYaw);
                this.setPitch(this.serverSidePitch);
                this.bodyYaw = this.getYaw();
                this.headYaw = this.getYaw();
                this.dataTracker.set(TARGET_PITCH, this.serverSidePitch);
                this.velocityModified = true;
                return;
            }

            // Move smoothly towards the target position
            Vec3d newPos = myPos.lerp(targetPos, 0.4);

            this.setPosition(newPos.x, newPos.y, newPos.z);
            this.velocityModified = true;

            // Rotate towards the actual movement path, NOT directly at the leader
            double moveX = newPos.x - myPos.x;
            double moveY = newPos.y - myPos.y;
            double moveZ = newPos.z - myPos.z;

            if (moveX * moveX + moveZ * moveZ > 0.001) {
                float targetYaw = (float) (MathHelper.atan2(moveZ, moveX) * (180 / Math.PI)) - 90.0F;
                this.serverSideYaw = wrapDegrees(this.serverSideYaw, targetYaw, 25.0f);
                this.setYaw(this.serverSideYaw);
                this.bodyYaw = this.getYaw();
                this.headYaw = this.getYaw();

                double horizontalDist = Math.sqrt(moveX * moveX + moveZ * moveZ);
                float targetPitch = (float) (MathHelper.atan2(moveY, horizontalDist) * (180 / Math.PI));
                // Target pitch for bodies doesn't strictly need 1.5x amplification like the
                // head, but we apply smooth server wrapper
                this.serverSidePitch = wrapDegrees(this.serverSidePitch, targetPitch, 25.0f);
                this.setPitch(this.serverSidePitch);
                this.dataTracker.set(TARGET_PITCH, this.serverSidePitch);
            }
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
    public void onRemoved() {
        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("part_on_removed", "onRemoved called", "uuid",
                this.getUuid());
        super.onRemoved();
    }

    @Override
    public void remove(RemovalReason reason) {
        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("part_remove", "removing part", "reason", reason,
                "uuid", this.getUuid());
        super.remove(reason);
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
