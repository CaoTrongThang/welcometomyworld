package com.trongthang.welcometomyworld.items.Weapons;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.FallenKnight;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hammer extends SwordItem {
    public Hammer(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            BlockPos blockPos = user.getBlockPos();

            Utils.playSound(world, blockPos, SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY);

            createShockwave(user, stack);
        }



        user.getItemCooldownManager().set(this, 100);

        return TypedActionResult.success(stack);
    }

    private void createShockwave(PlayerEntity user, ItemStack stack) {
        if (!user.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) user.getWorld();
            BlockPos pos = user.getBlockPos();
            int radius = 9;

            Box checkArea = new Box(pos).expand(radius);
            List<LivingEntity> livingEntities = user.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

            Set<BlockPos> particleSpawnedBlocks = new HashSet<>();

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        for (int yOffset = -1; yOffset >= -2; yOffset--) {
                            BlockPos targetPos = pos.add(x, yOffset, z);
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

            for (LivingEntity entity : livingEntities) {
                if (entity == user) continue;
                if (entity instanceof TameableEntity tameable) {
                    if (tameable.isTamed() && tameable.getOwner() != null) {
                        if (tameable.getOwner() == user) continue;
                    }
                }

                float effectiveDamage = (float) user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

                entity.addVelocity(0, 1.4f, 0);
                entity.damage(user.getWorld().getDamageSources().mobAttack(user), effectiveDamage * 2);
            }
        }
    }

    private void spawnParticles(ServerWorld world, BlockPos pos, BlockState state) {
        ParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        world.spawnParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.5, 0.2, 0.1);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

        Text line3 = Text.literal("Creates a shockwave that knocks enemies into the air when used.")
                .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY));

        tooltip.add(Text.literal("").append(line3));
    }
}

