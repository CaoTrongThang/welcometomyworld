package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.blocks.DeathWaterBlock;
import com.trongthang.welcometomyworld.fluids.PurpleWaterFluid;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FluidsManager {
        public static final FlowableFluid STILL_DEATH_WATER = (FlowableFluid) Registry.register(Registries.FLUID,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "death_water"), new PurpleWaterFluid.Still());

        public static final FlowableFluid FLOWING_DEATH_WATER = (FlowableFluid) Registry.register(Registries.FLUID,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "flowing_death_water"), new PurpleWaterFluid.Flowing());

        public static final Block DEATH_WATER_BLOCK = Registry.register(Registries.BLOCK,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "death_water_block"),
                        new DeathWaterBlock(STILL_DEATH_WATER, FabricBlockSettings.copyOf(Blocks.WATER)));

        public static final Item DEATH_WATER_BUCKET = Registry.register(Registries.ITEM,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "death_water_bucket"),
                        new BucketItem(STILL_DEATH_WATER,
                                        new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

        public static void initialize() {
                WelcomeToMyWorld.LOGGER.info("Registering Fluids for " + WelcomeToMyWorld.MOD_ID);
        }
}
