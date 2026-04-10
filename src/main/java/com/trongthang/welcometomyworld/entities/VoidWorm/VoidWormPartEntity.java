package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

import java.util.List;
import java.util.UUID;

public class VoidWormPartEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private VoidWormEntity head;
    private UUID headUUID;

    // Which segment we are (1 = first body after head, 2 = next, etc.)
    private int segmentIndex;
    // Distance along the trail this segment should sit behind the head
    private float followDistance;

    // Visual rotation fields for CLIENT SIDE smoothing
    public float visualPitch = 0.0f;
    public float prevVisualPitch = 0.0f;
    public float visualYaw = 0.0f;
    public float prevVisualYaw = 0.0f;

    // Server-side persistent rotation state
    private float serverSidePitch = 0.0f;
    private float serverSideYaw = 0.0f;

    // Previous position used to derive movement direction for rotation
    private Vec3d prevPos = null;

    private static final net.minecraft.entity.data.TrackedData<Integer> HEAD_ID = net.minecraft.entity.data.DataTracker
            .registerData(VoidWormPartEntity.class, net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER);

    public VoidWormEntity getHead() {
        return this.head;
    }

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HEAD_ID, -1);
    }

    public VoidWormPartEntity(EntityType<? extends HostileEntity> entityType, World world, VoidWormEntity head,
            int segmentIndex, float followDistance) {
        super(entityType, world);
        this.noClip = true;
        this.setNoGravity(true);
        this.head = head;
        this.segmentIndex = segmentIndex;
        this.followDistance = followDistance;

        if (head != null)
            this.headUUID = head.getUuid();
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
            String animName = this.getType() == com.trongthang.welcometomyworld.managers.EntitiesManager.VOID_WORM_TAIL
                    ? "void_worm_tail_idle"
                    : "void_worm_body_idle";
            return state.setAndContinue(RawAnimation.begin().thenLoop(animName));
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
        nbt.putInt("SegmentIndex", this.segmentIndex);
        nbt.putFloat("FollowDistance", this.followDistance);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("HeadUUID")) {
            this.headUUID = nbt.getUuid("HeadUUID");
        }
        if (nbt.contains("SegmentIndex")) {
            this.segmentIndex = nbt.getInt("SegmentIndex");
        }
        if (nbt.contains("FollowDistance")) {
            this.followDistance = nbt.getFloat("FollowDistance");
        }
    }

    public void tick() {
        this.noClip = true;
        this.setNoGravity(true);

        if (this.getWorld().isClient) {
            this.prevVisualPitch = this.visualPitch;
            this.prevVisualYaw = this.visualYaw;

            if (this.head == null) {
                int id = this.dataTracker.get(HEAD_ID);
                if (id != -1) {
                    Entity ent = this.getWorld().getEntityById(id);
                    if (ent instanceof VoidWormEntity) {
                        this.head = (VoidWormEntity) ent;
                    }
                }
            }
        }

        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Keep the chunk loaded while the part is alive and ticking
            serverWorld.getChunkManager().addTicket(net.minecraft.server.world.ChunkTicketType.PORTAL,
                    new net.minecraft.util.math.ChunkPos(this.getBlockPos()), 3, this.getBlockPos());

            // Restore head reference after chunk reload
            if (this.head == null && this.headUUID != null) {
                Entity h = serverWorld.getEntity(this.headUUID);
                if (h instanceof VoidWormEntity) {
                    this.head = (VoidWormEntity) h;
                    this.head.registerPart(this);
                }
            }
            if (this.head != null) {
                this.dataTracker.set(HEAD_ID, this.head.getId());
            }

            if (this.head != null && this.head.isRemoved()) {
                if (this.head.getRemovalReason() != null && this.head.getRemovalReason().shouldDestroy()) {
                    this.discard();
                    return;
                }
                this.head = null;
            }

            if (this.head != null && this.head.getHealth() <= 0.0f && this.head.ticksSinceDeath >= 200) {
                this.discard();
                return;
            }

            if (this.head != null && this.head.isAlive()) {
                followByHistory();
            }
        } else {
            // Client side visual interpolation derived from actual position delta
            double dx = this.getX() - this.prevX;
            double dy = this.getY() - this.prevY;
            double dz = this.getZ() - this.prevZ;
            double distH = Math.sqrt(dx * dx + dz * dz);

            if (distH > 0.001 || Math.abs(dy) > 0.001) {
                float targetP = (float) (MathHelper.atan2(dy, distH) * (180 / Math.PI));
                float targetY = (float) (MathHelper.atan2(dz, dx) * (180 / Math.PI)) - 90.0F;

                this.visualPitch += MathHelper.wrapDegrees(targetP - this.visualPitch) * 0.3f;
                this.visualYaw += MathHelper.wrapDegrees(targetY - this.visualYaw) * 0.3f;
            }
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9D));
        }
    }

    /**
     * Walk the head's position history to find the exact point that is
     * {@code segmentIndex * followDistance} blocks along the trail, then snap
     * this segment there. No lerp = no gap at any speed.
     */
    private void followByHistory() {
        List<Vec3d> history = this.head.getPosHistory();
        if (history.isEmpty())
            return;

        float targetTrailDist = segmentIndex * followDistance;
        Vec3d target = findTrailPoint(history, targetTrailDist);
        if (target == null) {
            // History not long enough yet (worm just spawned) — stay put
            return;
        }

        Vec3d myPos = this.getPos();

        // Snap-back: if we're wildly displaced (e.g. chunk was unloaded), teleport
        // instantly
        double distSq = myPos.squaredDistanceTo(target);
        if (distSq > followDistance * followDistance * 25.0) {
            this.setPosition(target.x, target.y, target.z);
            this.velocityModified = true;
            prevPos = target;
            return;
        }

        // Snap directly to the trail point — no lerp, no gap
        this.setPosition(target.x, target.y, target.z);
        this.velocityModified = true;

        // Derive rotation from actual movement this tick
        if (prevPos != null) {
            double moveX = target.x - prevPos.x;
            double moveY = target.y - prevPos.y;
            double moveZ = target.z - prevPos.z;

            if (moveX * moveX + moveZ * moveZ > 0.001) {
                float targetYaw = (float) (MathHelper.atan2(moveZ, moveX) * (180 / Math.PI)) - 90.0F;
                this.serverSideYaw = wrapDegrees(this.serverSideYaw, targetYaw, 25.0f);
                this.setYaw(this.serverSideYaw);
                this.bodyYaw = this.getYaw();
                this.headYaw = this.getYaw();

                double horizontalDist = Math.sqrt(moveX * moveX + moveZ * moveZ);
                float targetPitch = (float) (MathHelper.atan2(moveY, horizontalDist) * (180 / Math.PI));
                this.serverSidePitch = wrapDegrees(this.serverSidePitch, targetPitch, 25.0f);
                this.setPitch(this.serverSidePitch);
            }
        }

        prevPos = target;
    }

    /**
     * Walks the history list (index 0 = newest) and returns the interpolated
     * position at exactly {@code targetDist} path-distance from the head.
     * Returns null if the history is shorter than targetDist.
     */
    private Vec3d findTrailPoint(List<Vec3d> history, float targetDist) {
        float accumulated = 0f;
        for (int i = 0; i < history.size() - 1; i++) {
            Vec3d a = history.get(i); // newer
            Vec3d b = history.get(i + 1); // older
            float segLen = (float) a.distanceTo(b);
            if (segLen < 0.0001f)
                continue; // skip duplicate positions (entity was still)

            if (accumulated + segLen >= targetDist) {
                float t = (targetDist - accumulated) / segLen;
                return a.lerp(b, t);
            }
            accumulated += segLen;
        }
        return null; // not enough history
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
        super.onRemoved();
    }

    @Override
    public void remove(RemovalReason reason) {
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

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
        // Disabling collisions between parts significantly improves performance
    }
}
