package com.trongthang.welcometomyworld.entities.Unknown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.trongthang.welcometomyworld.entities.UnknownBeamEntity;
import com.trongthang.welcometomyworld.entities.WandererArrow;
import com.trongthang.welcometomyworld.managers.EntitiesManager;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

import java.util.UUID;

public class SummoningCircleEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final TrackedData<Integer> TARGET_ID = DataTracker.registerData(SummoningCircleEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> DISCARDING = DataTracker.registerData(SummoningCircleEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    public static final double BASE_CENTER_Y_OFFSET = 1.546875; // Base path to visual center
    public static final double CENTER_Y_OFFSET = BASE_CENTER_Y_OFFSET * 2.0; // Scaled visual center

    private UUID ownerUuid;
    private int maxAge = 300;
    private int shootTimer = 40; // 2 seconds delay
    private int discardTimer = 5; // 5 ticks for animation

    public SummoningCircleEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public float getEyeHeight(net.minecraft.entity.EntityPose pose) {
        return (float) CENTER_Y_OFFSET;
    }

    public float getScale() {
        return 2.0f;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TARGET_ID, -1);
        this.dataTracker.startTracking(DISCARDING, false);
    }

    @Override
    public void tick() {
        super.tick();

        // Server-only: lifecycle checks and target tracking
        if (!this.getWorld().isClient) {
            if (this.dataTracker.get(DISCARDING)) {
                discardTimer--;
                if (discardTimer <= 0) {
                    this.discard();
                }
                return;
            }

            if (this.age > maxAge) {
                this.dataTracker.set(DISCARDING, true);
                return;
            }

            LivingEntity owner = getOwner();
            if (owner == null || !owner.isAlive()) {
                this.dataTracker.set(DISCARDING, true);
                return;
            }

            if (owner instanceof net.minecraft.entity.mob.MobEntity mob) {
                LivingEntity ownerTarget = mob.getTarget();
                if (ownerTarget != null && ownerTarget.isAlive()) {
                    this.dataTracker.set(TARGET_ID, ownerTarget.getId());
                } else {
                    this.dataTracker.set(TARGET_ID, -1);
                }
            }
        }

        // Both sides: resolve target from tracked ID and compute rotation
        int targetId = this.dataTracker.get(TARGET_ID);
        if (targetId == -1)
            return;

        Entity found = this.getWorld().getEntityById(targetId);
        if (!(found instanceof LivingEntity target) || !target.isAlive())
            return;

        double dx = target.getX() - this.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - (this.getY() + CENTER_Y_OFFSET);
        double dz = target.getZ() - this.getZ();
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
        this.setYaw((float) (Math.atan2(dz, dx) * 57.2957763671875) - 90.0F);
        this.setPitch((float) (-(Math.atan2(dy, horizDist) * 57.2957763671875)));

        // Server-only: shooting
        if (!this.getWorld().isClient) {
            if (shootTimer <= 0) {
                shootProjectiles(target);
                shootTimer = 15 + this.random.nextInt(20);
            }
            if (shootTimer > 0)
                shootTimer--;
        }
    }

    private void shootProjectiles(LivingEntity target) {
        double r = this.random.nextDouble();
        Vec3d center = this.getPos().add(0, CENTER_Y_OFFSET, 0);
        Vec3d targetCenter = new Vec3d(target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ());
        Vec3d dir = targetCenter.subtract(center).normalize();

        if (r < 0.6) { // Wanderer Arrow
            WandererArrow arrow = new WandererArrow(this.getWorld(), getOwner());
            arrow.setPosition(center.x, center.y, center.z);
            arrow.setVelocity(dir.x, dir.y, dir.z, 3.5F, 1.0F);
            this.getWorld().spawnEntity(arrow);
        } else { // Unknown Beam
            UnknownBeamEntity beam = new UnknownBeamEntity(EntitiesManager.UNKNOWN_BEAM,
                    this.getWorld());
            beam.setPosOwner(this); // Follow the circle
            beam.setDamageOwner(getOwner()); // Boss gets credit
            beam.setPosition(center.x, center.y, center.z);
            beam.setYaw(this.getYaw());
            beam.setPitch(this.getPitch());
            beam.setLength(40.0f);
            beam.setRadius(1.2f);
            beam.setDamage(10.0f);
            this.getWorld().spawnEntity(beam);
        }
    }

    private LivingEntity getOwner() {
        if (ownerUuid != null && this.getWorld() instanceof ServerWorld serverWorld) {
            Entity entity = serverWorld.getEntity(ownerUuid);
            if (entity instanceof LivingEntity)
                return (LivingEntity) entity;
        }
        return null;
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (this.dataTracker.get(DISCARDING)) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("discarded"));
            }
            if (this.getOwner() != null && this.getOwner().getAttacker() != null) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("shoot"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("summoned"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
