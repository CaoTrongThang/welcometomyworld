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

            if (isWhiteDim && !player.isCreative()) {
                ItemStack stack = player.getStackInHand(hand);

                // Check if the item is a BlockItem (i.e., placing a block)
                if (stack.getItem() instanceof net.minecraft.item.BlockItem) {
                    return ActionResult.FAIL; // ❌ block placing
                }

                return ActionResult.PASS; // ✅ allow interaction
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
