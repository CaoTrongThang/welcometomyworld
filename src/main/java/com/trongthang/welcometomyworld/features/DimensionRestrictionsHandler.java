package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimension;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

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
                        return ActionResult.FAIL;
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
