package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.entities.Wanderer.Wanderer;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.trongthang.welcometomyworld.entities.FallenKnight.FallenKnight.spawnParticles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WandererArrow extends PersistentProjectileEntity {

    public int explosionRange = 8;
    public boolean canExplode = true;
    private int lifeTime = 0;

    public WandererArrow(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public WandererArrow(World world, LivingEntity owner) {
        super(EntitiesManager.WANDERER_ARROW, owner, world);
        this.age();
    }

    @Override
    protected void age() {
        ++this.lifeTime;
        if (this.lifeTime >= 200) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient)
            return; // Only spawn particles on the client side

        // Get the arrow's current position
        Vec3d pos = this.getPos();

        // Calculate the direction vector (normalized velocity)
        Vec3d velocity = this.getVelocity();
        Vec3d direction = velocity.normalize();

        // Offset the particle position slightly in front of the arrow's head
        double offsetX = pos.x + direction.x * 0.5; // Offset by 0.5 blocks in the direction of travel
        double offsetY = pos.y + direction.y * 0.5;
        double offsetZ = pos.z + direction.z * 0.5;

        // Spawn the particle at the calculated position
        this.getWorld().addParticle(ParticleTypes.END_ROD, offsetX, offsetY, offsetZ, 0, 0, 0);
    }

    @Override
    protected ItemStack asItemStack() {
        return null;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!canExplode)
            return;
        createShockwave();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);

        Entity target = entityHitResult.getEntity();

        if (this.getOwner() instanceof LivingEntity owner) {
            float damage = (float) owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (target instanceof HostileEntity) {
                target.damage(this.getWorld().getDamageSources().mobAttack(owner), damage * 2);
            } else {
                target.damage(this.getWorld().getDamageSources().mobAttack(owner), damage);
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            // Check if the entity is blocking with a shield
            if (livingTarget.isBlocking() && livingTarget.getActiveItem().isOf(Items.SHIELD)) {
                this.canExplode = false;
            }
        }
    }

    @Override
    public boolean canHit(Entity target) {
        // Only hit LivingEntities to avoid hitting other projectiles (like other
        // arrows) or non-living objects
        if (!(target instanceof LivingEntity)) {
            return false;
        }

        Entity owner = this.getOwner();
        if (owner == null) {
            return true;
        }

        // Don't hit the owner or itself
        if (target == owner || target == this) {
            return false;
        }

        // Handle team-based or owner-based collision filtering for Tameable units like
        // Wanderers
        if (owner instanceof TameableEntity ownerTameable) {
            // Don't hit the owner of the shooter (e.g. the Player)
            if (ownerTameable.getOwner() == target) {
                return false;
            }

            // Don't hit other entities sharing the same owner
            if (target instanceof TameableEntity targetTameable) {
                if (targetTameable.isTamed() && targetTameable.getOwner() != null) {
                    if (targetTameable.getOwner() == ownerTameable.getOwner()) {
                        return false;
                    }
                }
            }

            // Specific check for Wanderer interactions
            if (target instanceof Wanderer targetWanderer) {
                // If both are untamed, they shouldn't hit each other (generic monster team)
                if (!targetWanderer.isTamed() && !ownerTameable.isTamed()) {
                    return false;
                }

                // If they have the same owner, they are on the same team
                if (targetWanderer.getOwner() != null && targetWanderer.getOwner() == ownerTameable.getOwner()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void createShockwave() {
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            BlockPos center = this.getBlockPos();

            Box checkArea = new Box(this.getBlockPos()).expand(explosionRange);
            List<LivingEntity> damageTarget = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea,
                    entity -> true);

            // Set to track blocks where particles have been spawned
            Set<BlockPos> particleSpawnedBlocks = new HashSet<>();

            for (int x = -explosionRange; x <= explosionRange; x++) {
                for (int z = -explosionRange; z <= explosionRange; z++) {
                    if (x * x + z * z <= explosionRange * explosionRange) {
                        for (int yOffset = 0; yOffset >= -1; yOffset--) {
                            BlockPos targetPos = center.add(x, yOffset, z);
                            BlockState state = serverWorld.getBlockState(targetPos);

                            BlockPos blockAbove = targetPos.up();
                            if (yOffset < 0 && particleSpawnedBlocks.contains(blockAbove)) {
                                continue;
                            }

                            if (!state.isAir()) {
                                spawnParticles(serverWorld, targetPos, state);
                                particleSpawnedBlocks.add(targetPos); // Mark this block as having particles spawned
                            }
                        }
                    }
                }
            }

            for (LivingEntity target : damageTarget) {
                if (this.getOwner() instanceof TameableEntity tameable) {
                    if (tameable.getOwner() == target)
                        continue;

                    if (target instanceof TameableEntity targetTameable) {
                        if (targetTameable.isTamed() && targetTameable.getOwner() != null) {
                            if (targetTameable.getOwner() == tameable.getOwner())
                                continue;
                        }
                    }
                }

                // Handle FallenKnight-specific logic
                if (target instanceof Wanderer wanderer) {
                    // Skip untamed vs. untamed damage
                    if (!wanderer.isTamed()) {
                        continue;
                    }

                    // Skip tamed vs. tamed damage if they have the same owner
                    if (wanderer.isTamed() && this.getOwner() != null && wanderer.getOwner() != null) {
                        if (this.getOwner().equals(wanderer.getOwner())) {
                            continue;
                        }
                    }
                }

                if (this.getOwner() instanceof LivingEntity ownerEntity) {
                    float damage = (float) ownerEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f;
                    if (target instanceof HostileEntity) {
                        target.damage(this.getWorld().getDamageSources().mobAttack(ownerEntity), damage * 2);
                    } else {
                        target.damage(this.getWorld().getDamageSources().mobAttack(ownerEntity), damage);
                    }
                }
            }
        }
    }
}
