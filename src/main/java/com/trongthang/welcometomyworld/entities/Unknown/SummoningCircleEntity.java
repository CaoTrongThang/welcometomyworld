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

    private UUID ownerUuid;
    private int maxAge = 120;
    private int shootTimer = 40; // 2 seconds delay

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
        return 0.5f; // Center of the circle
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TARGET_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient)
            return;

        if (this.age > maxAge) {
            this.discard();
            return;
        }

        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        LivingEntity target = null;
        if (!this.getWorld().isClient) {
            if (owner instanceof net.minecraft.entity.mob.MobEntity mob) {
                target = mob.getTarget();
                if (target != null && target.isAlive()) {
                    this.dataTracker.set(TARGET_ID, target.getId());
                } else {
                    this.dataTracker.set(TARGET_ID, -1);
                }
            }
        } else {
            int id = this.dataTracker.get(TARGET_ID);
            if (id != -1) {
                Entity e = this.getWorld().getEntityById(id);
                if (e instanceof LivingEntity) {
                    target = (LivingEntity) e;
                }
            }
        }

        if (target != null && target.isAlive()) {
            // Point towards target
            double d = target.getX() - this.getX();
            double e = target.getEyeY() - this.getY();
            double f = target.getZ() - this.getZ();
            double g = Math.sqrt(d * d + f * f);
            this.setYaw((float) (Math.atan2(f, d) * 57.2957763671875) - 90.0F);
            this.setPitch((float) (-(Math.atan2(e, g) * 57.2957763671875)));

            if (this.age % 20 == 0) {
                String side = this.getWorld().isClient ? "Client" : "Server";
                System.out.println("[" + side + "] (targetPos, " + target.getPos() + ")");
                System.out.println("[" + side + "] (yaw, " + this.getYaw() + "), (prevYaw, " + this.prevYaw
                        + "), (pitch, " + this.getPitch() + ")");
            }

            // Shooting logic
            if (!this.getWorld().isClient && shootTimer <= 0) {
                shootProjectiles(target);
                shootTimer = 15 + this.random.nextInt(20);
            }
        }

        if (shootTimer > 0) {
            shootTimer--;
        }
    }

    private void shootProjectiles(LivingEntity target) {
        double r = this.random.nextDouble();
        Vec3d dir = target.getEyePos().subtract(this.getPos()).normalize();

        if (r < 0.45) { // Wanderer Arrow
            WandererArrow arrow = new WandererArrow(this.getWorld(), getOwner());
            arrow.setPosition(this.getEyePos().x, this.getEyePos().y, this.getEyePos().z);
            arrow.setVelocity(dir.x, dir.y, dir.z, 3.5F, 1.0F); // Increased force
            this.getWorld().spawnEntity(arrow);
        } else if (r < 0.65) { // Fireball (No block damage)
            FireballEntity fireball = new FireballEntity(this.getWorld(), getOwner(), dir.x, dir.y, dir.z, 0); // Power
                                                                                                               // 0
            fireball.setPosition(this.getEyePos().x, this.getEyePos().y, this.getEyePos().z);
            this.getWorld().spawnEntity(fireball);
        } else if (r < 0.85) { // Wither Skull
            WitherSkullEntity skull = new WitherSkullEntity(this.getWorld(), getOwner(), dir.x, dir.y, dir.z);
            skull.setPosition(this.getEyePos().x, this.getEyePos().y, this.getEyePos().z);
            // Note: Preventing Wither Skull block damage without Mixin is hard,
            // but we can try to set its damage manually or use a custom projectile.
            // For now, let's stick with it or use a mixin if the user insists.
            this.getWorld().spawnEntity(skull);
        } else { // Unknown Beam
            UnknownBeamEntity beam = new UnknownBeamEntity(EntitiesManager.UNKNOWN_BEAM, this.getWorld());
            beam.setPosOwner(this); // Follow the circle
            beam.setDamageOwner(getOwner()); // Boss gets credit
            beam.setPosition(this.getEyePos().x, this.getEyePos().y, this.getEyePos().z);
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
            return state.setAndContinue(RawAnimation.begin().thenPlay("summoned"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
