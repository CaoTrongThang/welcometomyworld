package com.trongthang.welcometomyworld.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;

public class UnknownBeamEntity extends Entity {
    private static final TrackedData<Integer> OWNER_ID = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> LENGTH = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    private java.util.UUID posOwnerUuid;
    private java.util.UUID damageOwnerUuid;
    private int maxAge = 60; // 4 seconds
    private float maxLength = 50.0f; // Tracks the maximum theoretical length

    public UnknownBeamEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.ignoreCameraFrustum = true;
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    public void setPosOwner(Entity owner) {
        this.posOwnerUuid = owner.getUuid();
        this.dataTracker.set(OWNER_ID, owner.getId());
    }

    public void setDamageOwner(LivingEntity owner) {
        this.damageOwnerUuid = owner.getUuid();
    }

    public void setLength(float length) {
        this.dataTracker.set(LENGTH, length);
        this.maxLength = length;
    }

    public float getLength() {
        return this.dataTracker.get(LENGTH);
    }

    public void setRadius(float radius) {
        this.dataTracker.set(RADIUS, radius);
    }

    public void setDamage(float damage) {
        this.dataTracker.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.dataTracker.get(DAMAGE);
    }

    public float getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(OWNER_ID, -1);
        this.dataTracker.startTracking(LENGTH, 20.0f);
        this.dataTracker.startTracking(RADIUS, 1.0f);
        this.dataTracker.startTracking(DAMAGE, 1.0f);
    }

    @Override
    public void tick() {
        if (this.age > maxAge) {
            this.discard();
            return;
        }

        // Both sides: resolve owner from tracked ID
        Entity posOwner = null;
        int ownerId = this.dataTracker.get(OWNER_ID);
        if (ownerId != -1) {
            posOwner = this.getWorld().getEntityById(ownerId);
        }

        // If owner is missing, discard on server
        if (!this.getWorld().isClient && (posOwner == null || !posOwner.isAlive())) {
            this.discard();
            return;
        }

        if (posOwner != null && posOwner.isAlive()) {
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();

            // Update position to posOwner's center
            Vec3d centerPos;
            if (posOwner instanceof com.trongthang.welcometomyworld.entities.Unknown.SummoningCircleEntity circle) {
                centerPos = circle.getPos().add(0,
                        com.trongthang.welcometomyworld.entities.Unknown.SummoningCircleEntity.CENTER_Y_OFFSET, 0);
            } else {
                centerPos = posOwner.getEyePos();
            }
            Vec3d lookVec = posOwner.getRotationVec(1.0f);
            this.setPosition(centerPos.add(lookVec.multiply(0.5))); // Offset slightly forward
            this.setYaw(posOwner.getYaw());
            this.setPitch(posOwner.getPitch());
        }

        if (this.getWorld().isClient)
            return;

        // Detect hit blocks and cap the beam length
        Vec3d start = this.getPos();
        Vec3d direction = this.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(this.maxLength));

        net.minecraft.util.hit.BlockHitResult hitResult = this.getWorld()
                .raycast(new net.minecraft.world.RaycastContext(
                        start, end, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                        net.minecraft.world.RaycastContext.FluidHandling.NONE, this));

        float actualLength = this.maxLength;
        if (hitResult.getType() != net.minecraft.util.hit.HitResult.Type.MISS) {
            actualLength = (float) start.distanceTo(hitResult.getPos());
        }
        this.dataTracker.set(LENGTH, actualLength); // updates visually and dynamically for damage check

        LivingEntity damageOwner = null;
        if (damageOwnerUuid != null) {
            Entity e = ((net.minecraft.server.world.ServerWorld) this.getWorld()).getEntity(damageOwnerUuid);
            if (e instanceof LivingEntity)
                damageOwner = (LivingEntity) e;
        }

        // Perform damage
        if (this.age % 2 == 0) {
            damageInBeam(damageOwner);
        }
    }

    private void damageInBeam(LivingEntity owner) {
        Vec3d start = this.getPos();
        Vec3d direction = this.getRotationVec(1.0f);
        float length = getLength();
        float radius = getRadius();

        // Simple box-based collision for the beam
        // We check several points along the beam for entities
        for (float i = 0; i < length; i += 2.0f) {
            Vec3d point = start.add(direction.multiply(i));
            Box box = new Box(point.subtract(radius, radius, radius), point.add(radius, radius, radius));
            List<LivingEntity> targets = this.getWorld().getEntitiesByClass(LivingEntity.class, box, e -> {
                if (owner != null && e.getUuid().equals(owner.getUuid()))
                    return false;
                return true;
            });

            for (LivingEntity target : targets) {

                Unknown.dealUnknownDamage(owner, target, getDamage());

                target.setFireTicks(20);
                // Knockback away from the beam center
                Vec3d diff = target.getPos().subtract(point);
                Vec3d knockback = new Vec3d(diff.x, 0, diff.z).normalize().multiply(0.5);
                target.addVelocity(knockback.x, 0, knockback.z);
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("PosOwner")) {
            this.posOwnerUuid = nbt.getUuid("PosOwner");
        }
        if (nbt.contains("DamageOwner")) {
            this.damageOwnerUuid = nbt.getUuid("DamageOwner");
        }
        this.maxAge = nbt.getInt("MaxAge");
        if (nbt.contains("MaxLength")) {
            this.maxLength = nbt.getFloat("MaxLength");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.posOwnerUuid != null) {
            nbt.putUuid("PosOwner", this.posOwnerUuid);
        }
        if (this.damageOwnerUuid != null) {
            nbt.putUuid("DamageOwner", this.damageOwnerUuid);
        }
        nbt.putInt("MaxAge", this.maxAge);
        nbt.putFloat("MaxLength", this.maxLength);
    }
}
