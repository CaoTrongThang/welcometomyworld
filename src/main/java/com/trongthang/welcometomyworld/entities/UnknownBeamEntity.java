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
import java.util.UUID;

public class UnknownBeamEntity extends Entity {
    private static final TrackedData<Float> LENGTH = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(UnknownBeamEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    private UUID ownerUuid;
    private int maxAge = 40; // 60 - 21 + 1

    public UnknownBeamEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    public void setLength(float length) {
        this.dataTracker.set(LENGTH, length);
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
        this.dataTracker.startTracking(LENGTH, 20.0f);
        this.dataTracker.startTracking(RADIUS, 1.0f);
        this.dataTracker.startTracking(DAMAGE, 1.0f);
    }

    @Override
    public void tick() {
        if (this.getWorld().isClient) {
            if (this.age > maxAge) {
                this.discard();
            }
            return;
        }

        if (this.age > maxAge) {
            this.discard();
            return;
        }

        LivingEntity owner = null;
        if (ownerUuid != null) {
            owner = (LivingEntity) ((net.minecraft.server.world.ServerWorld) this.getWorld()).getEntity(ownerUuid);
        }

        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        // Update position to owner's eyes
        Vec3d eyePos = owner.getEyePos();
        Vec3d lookVec = owner.getRotationVec(1.0f);
        this.setPosition(eyePos.add(lookVec.multiply(0.5))); // Offset slightly forward
        this.setYaw(owner.getYaw());
        this.setPitch(owner.getPitch());

        // Perform damage
        if (this.age % 2 == 0) {
            damageInBeam(owner);
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

                com.trongthang.welcometomyworld.entities.Unknown.Unknown.dealUnknownDamage(owner, target, getDamage());

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
        if (nbt.contains("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner");
        }
        this.maxAge = nbt.getInt("MaxAge");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", this.ownerUuid);
        }
        nbt.putInt("MaxAge", this.maxAge);
    }
}
