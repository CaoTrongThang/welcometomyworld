package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.blocks.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlocksManager {


    public static final Block TOUGHER_IRON_BARS = registerBlock("tougher_iron_bars",
            new CustomIronBars(FabricBlockSettings.copyOf(Blocks.IRON_BARS)
                    .strength(6.0f, 6.0f)
                    .requiresTool()));

    public static final Block RUSTED_IRON_BARS = registerBlock("rusted_iron_bars",
            new CustomIronBars(FabricBlockSettings.copyOf(Blocks.IRON_BARS)
                    .strength(6.0f, 6.0f)
                    .requiresTool()));

    public static final Block TOUGHER_IRON_BLOCK = registerBlock("tougher_iron_block",
            new CustomIronBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(12.0f, 6.0f)
                    .requiresTool()));

    public static final Block RUSTED_IRON_BLOCK = registerBlock("rusted_iron_block",
            new CustomIronBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(6.0f, 6.0f)
                    .requiresTool()));

    public static final Block RUSTED_IRON_BLOCK_STAGE2 = registerBlock("rusted_iron_block_stage2",
            new CustomIronBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(5.0f, 6.0f)
                    .requiresTool()));

    public static final Block RUSTED_IRON_BLOCK_STAGE3 = registerBlock("rusted_iron_block_stage3",
            new CustomIronBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(4.0f, 6.0f)
                    .requiresTool()));


    public static final Block BURNING_PLANK = registerBlock("burning_plank",
            new BurningPlankBlock(FabricBlockSettings.copyOf(Blocks.MAGMA_BLOCK)
                    .strength(3.0f, 6.0f)
                    .luminance((state) -> 15)
                    .requiresTool()));


    public static final Block BURNED_PLANK = registerBlock("burned_plank",
            new Block(FabricBlockSettings.copyOf(Blocks.COAL_BLOCK)
                    .strength(3.0f, 6.0f)
                    .requiresTool()));

    public static final Block CUSTOM_VINE = registerBlock("custom_vine",
            new CustomVine(FabricBlockSettings.copyOf(Blocks.VINE)));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(WelcomeToMyWorld.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(WelcomeToMyWorld.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        WelcomeToMyWorld.LOGGER.info("Registering ModBlocks for " + WelcomeToMyWorld.MOD_ID);
    }


}
