package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.trongthang.welcometomyworld.entities.FallenKnight.spawnParticles;

public class WandererArrow extends PersistentProjectileEntity {

    public int explosionRange = 8;

    public boolean canExplode = true;

    public WandererArrow(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public WandererArrow(World world, LivingEntity owner) {
        super(EntitiesManager.WANDERER_ARROW, owner, world);

        if (owner != null) {
            // Set the arrow's base damage to the owner's attack damage
            this.setDamage(owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) return; // Only spawn particles on the client side

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
        if(!canExplode) return;
        createShockwave();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);

        Entity entity = entityHitResult.getEntity(); // The entity that was hit
        if (entity instanceof LivingEntity livingEntity) {
            // Check if the entity is blocking with a shield
            if (livingEntity.isBlocking() && livingEntity.getActiveItem().isOf(Items.SHIELD)) {
                this.canExplode = false;
            }
        }
    }

    @Override
    public boolean canHit(Entity target) {
        // Check if the target is the owner of the arrow
        return !(target instanceof LivingEntity && target == this.getOwner()) && super.canHit(target);
    }

    private void createShockwave() {
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            BlockPos center = this.getBlockPos();

            Box checkArea = new Box(this.getBlockPos()).expand(explosionRange);
            List<LivingEntity> damageTarget = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

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
                if (target == this.getOwner()) continue;
                if (this.getOwner() != null) {
                    if (target == this.getOwner()) continue;
                }
                if (target instanceof TameableEntity tameable) {
                    if (tameable.isTamed() && tameable.getOwner() != null) {
                        if (tameable.getOwner() == this.getOwner()) continue;
                    }
                }

                // Handle FallenKnight-specific logic
                if (target instanceof Wanderer wanderer) {
                    // Skip untamed vs. untamed damage
                    if (!wanderer.isTamed() && !wanderer.isTamed()) {
                        continue;
                    }

                    // Skip tamed vs. tamed damage if they have the same owner
                    if (wanderer.isTamed() && this.getOwner() != null && wanderer.isTamed() && wanderer.getOwner() != null) {
                        if (this.getOwner().equals(wanderer.getOwner())) {
                            continue;
                        }
                    }
                }

                if(((TameableEntity)this.getOwner()) != null){
                    float damage = (float) ((TameableEntity) this.getOwner()).getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f;
                    target.damage(this.getWorld().getDamageSources().mobAttack((LivingEntity) this.getOwner()), damage);
                }
            }
        }
    }
}
