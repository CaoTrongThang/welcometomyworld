package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PurpleCrystalEntity extends Entity implements GeoEntity {
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(PurpleCrystalEntity.class,
            TrackedDataHandlerRegistry.FLOAT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private static final int MAX_AGE = 200;

    public PurpleCrystalEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public void setDamage(float dmg) {
        this.dataTracker.set(DAMAGE, dmg);
    }

    public float getDamage() {
        return this.dataTracker.get(DAMAGE);
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DAMAGE, 10.0f);
    }

    @Override
    public void tick() {
        super.tick();

        this.noClip = true;

        if (this.getWorld().isClient) {
            this.getWorld().addParticle(net.minecraft.particle.ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5), this.getY() + (this.random.nextDouble() - 0.5),
                    this.getZ() + (this.random.nextDouble() - 0.5), 0, 0, 0);
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            return;
        }

        if (this.age >= MAX_AGE) {
            this.discard();
            return;
        }

        if (this.age == 1 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 0.5,
                    this.getZ(), 30, 0.4, 0.4, 0.4, 0.1);
            sw.spawnParticles(net.minecraft.particle.ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5, this.getZ(),
                    30, 0.4, 0.4, 0.4, 0.1);
        }

        if (this.age < 40) {
            this.setVelocity(Vec3d.ZERO);
            this.velocityModified = true;
        } else {
            LivingEntity owner = resolveOwner();
            LivingEntity target = null;
            if (owner instanceof VoidWormEntity worm) {
                target = worm.getTarget();
            }

            if (target != null && target.isAlive()) {
                Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2.0, 0);
                Vec3d dir = targetPos.subtract(this.getPos());
                double dist = dir.length();

                // Higher speed for more impact and "straight" feel
                double currentSpeed = 2.0;

                if (dir.lengthSquared() > 0.001) {
                    dir = dir.normalize();
                    this.setVelocity(dir.multiply(currentSpeed));
                    this.velocityModified = true;
                }

                if (dist < 3.0) {
                    explode(owner);
                    this.discard();
                    return;
                }
            }
        }

        this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
    }

    private void explode(LivingEntity owner) {
        if (this.getWorld() instanceof ServerWorld sw) {
            // Visual explosion
            sw.createExplosion(this, null, null, this.getX(), this.getY(), this.getZ(), 2.0f, false,
                    World.ExplosionSourceType.NONE);

            Box hitBox = this.getBoundingBox().expand(5.0);
            this.getWorld()
                    .getEntitiesByClass(LivingEntity.class, hitBox,
                            e -> e.isAlive() && e != owner && !(e instanceof VoidWormPartEntity)
                                    && !(e instanceof VoidWormEntity))
                    .forEach(e -> e.damage(
                            this.getDamageSources().mobAttack(owner != null ? owner : (LivingEntity) (Object) this),
                            getDamage()));
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
        if (nbt.contains("Owner"))
            this.ownerUuid = nbt.getUuid("Owner");
        if (nbt.contains("Damage"))
            this.setDamage(nbt.getFloat("Damage"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null)
            nbt.putUuid("Owner", this.ownerUuid);
        nbt.putFloat("Damage", getDamage());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
