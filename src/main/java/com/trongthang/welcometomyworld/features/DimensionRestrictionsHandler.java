package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimension;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DimensionRestrictionsHandler {
    public static void registerEvents() {
        // Prevent placing or using blocks in White Dimension, and prevent water in Void
        // Dimension
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            boolean isWhiteDim = world.getRegistryKey() == WhiteDimension.WHITE_DIM_LEVEL_KEY;
            boolean isVoidDim = world.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY;

            if (isWhiteDim) {
                if (!player.isCreative()) {
                    return ActionResult.FAIL;
                }
            }

            if (isVoidDim) {
                if (!player.isCreative()) {
                    Item item = player.getStackInHand(hand).getItem();
                    boolean isWaterBucket = item == Items.WATER_BUCKET ||
                            item == Items.AXOLOTL_BUCKET ||
                            item == Items.COD_BUCKET ||
                            item == Items.PUFFERFISH_BUCKET ||
                            item == Items.SALMON_BUCKET ||
                            item == Items.TADPOLE_BUCKET;

                    Identifier itemId = Registries.ITEM.getId(item);
                    if (!isWaterBucket && itemId != null && itemId.getPath().contains("water")
                            && itemId.getPath().contains("bucket")) {
                        isWaterBucket = true;
                    }

                    if (isWaterBucket) {
                        if (!world.isClient) {
                            BlockPos targetPos = hitResult.getBlockPos();
                            if (!world.getBlockState(targetPos).isReplaceable()) {
                                targetPos = targetPos.offset(hitResult.getSide());
                            }

                            // Smoke and sound like the Nether
                            world.playSound(null, targetPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                                    0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
                            ((ServerWorld) world).spawnParticles(ParticleTypes.LARGE_SMOKE, targetPos.getX() + 0.5,
                                    targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.0);

                            // 50/50 chance to turn into packed ice or evaporate
                            if (world.random.nextFloat() < 0.5f) {
                                if (world.getBlockState(targetPos).isReplaceable()) {
                                    world.setBlockState(targetPos, Blocks.PACKED_ICE.getDefaultState());
                                }
                            }

                            // Return empty bucket
                            player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });

        // Prevent breaking blocks in White Dimension
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey() == WhiteDimension.WHITE_DIM_LEVEL_KEY) {
                if (!player.isCreative()) {
                    return false;
                }
            }
            return true;
        });
    }
}
