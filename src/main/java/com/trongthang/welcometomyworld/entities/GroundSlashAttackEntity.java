package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GroundSlashAttackEntity extends Entity implements GeoEntity {

    private static final TrackedData<Integer> OWNER_ID = DataTracker.registerData(
            GroundSlashAttackEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(
            GroundSlashAttackEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * Entities already struck by this slash - avoids double-hit within its
     * lifetime.
     */
    private final Set<UUID> hitEntities = new HashSet<>();

    private UUID ownerUuid;
    private Vec3d travelDir = Vec3d.ZERO;
    private static final float SPEED = 1.5f;
    private static final int MAX_AGE = 28; // ~4 blocks at 1.5/tick

    // Hit-box half-size
    private static final double HIT_W = 2.4;
    private static final double HIT_H = 3.0;

    public GroundSlashAttackEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    /**
     * Set travel direction from a yaw angle (degrees). Entity is already at spawn
     * position.
     */
    public void setDirection(float yawDegrees) {
        double rad = Math.toRadians(yawDegrees);
        this.travelDir = new Vec3d(-Math.sin(rad), 0, Math.cos(rad)).normalize().multiply(SPEED);
        this.setYaw(yawDegrees);
        this.setVelocity(this.travelDir);
        this.velocityModified = true;
    }

    public void setDamage(float dmg) {
        this.dataTracker.set(DAMAGE, dmg);
    }

    public float getDamage() {
        return this.dataTracker.get(DAMAGE);
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
        this.dataTracker.set(OWNER_ID, owner.getId());
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(OWNER_ID, -1);
        this.dataTracker.startTracking(DAMAGE, 10.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.noClip = true;

        if (this.age >= MAX_AGE) {
            this.discard();
            return;
        }

        // Move forward normally
        if (this.getWorld().isClient) {
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            return;
        }

        if (!travelDir.equals(Vec3d.ZERO)) {
            this.setVelocity(this.travelDir);
            this.velocityModified = true;
        }

        this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());

        // Resolve owner for damage attribution
        LivingEntity owner = resolveOwner();

        // Particle trail (server spawns, propagated to clients)
        if (this.getWorld() instanceof ServerWorld sw) {
            if (this.age % 2 == 0) {
                // Smoke at ground level
                sw.spawnParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 0.1, this.getZ(),
                        3, 0.3, 0.05, 0.3, 0.02);
                // Sweep at mid height
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK,
                        this.getX(), this.getY() + 0.7, this.getZ(),
                        1, 0.2, 0.1, 0.2, 0.0);
            }
        }

        // Damage entities in path
        Box hitBox = new Box(
                this.getX() - HIT_W, this.getY(), this.getZ() - HIT_W,
                this.getX() + HIT_W, this.getY() + HIT_H, this.getZ() + HIT_W);

        List<LivingEntity> targets = this.getWorld().getEntitiesByClass(LivingEntity.class, hitBox, e -> {
            if (owner != null && e.getUuid().equals(owner.getUuid()))
                return false;
            return !hitEntities.contains(e.getUuid());
        });

        for (LivingEntity target : targets) {
            hitEntities.add(target.getUuid());
            Unknown.dealUnknownDamage(owner, target, getDamage());
            // Small knockback away from slash center
            Vec3d diff = target.getPos().subtract(this.getPos());
            Vec3d kb = new Vec3d(diff.x, 0, diff.z).normalize().multiply(0.4);
            target.addVelocity(kb.x, 0.2, kb.z);
        }
    }

    private LivingEntity resolveOwner() {
        if (ownerUuid != null && this.getWorld() instanceof ServerWorld sw) {
            Entity e = sw.getEntity(ownerUuid);
            if (e instanceof LivingEntity le)
                return le;
        }
        return null;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
        if (nbt.contains("DirX")) {
            this.travelDir = new Vec3d(nbt.getDouble("DirX"), 0, nbt.getDouble("DirZ"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
        nbt.putDouble("DirX", travelDir.x);
        nbt.putDouble("DirZ", travelDir.z);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animation — static model, yaw set to travel direction
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
