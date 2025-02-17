package com.trongthang.welcometomyworld.datagen;

import com.trongthang.welcometomyworld.managers.BlocksManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(BlocksManager.TOUGHER_IRON_BLOCK)
                .add(BlocksManager.RUSTED_IRON_BLOCK)
                .add(BlocksManager.RUSTED_IRON_BLOCK_STAGE2)
                .add(BlocksManager.RUSTED_IRON_BLOCK_STAGE3)
                .add(BlocksManager.RUSTED_IRON_BARS)
                .add(BlocksManager.TOUGHER_IRON_BARS)
                .add(BlocksManager.BURNED_PLANK)
                .add(BlocksManager.BURNING_PLANK);

        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(BlocksManager.TOUGHER_IRON_BLOCK)
                .add(BlocksManager.RUSTED_IRON_BLOCK)
                .add(BlocksManager.RUSTED_IRON_BLOCK_STAGE2)
                .add(BlocksManager.RUSTED_IRON_BLOCK_STAGE3)
                .add(BlocksManager.RUSTED_IRON_BARS)
                .add(BlocksManager.TOUGHER_IRON_BARS)
                .add(BlocksManager.BURNED_PLANK)
                .add(BlocksManager.BURNING_PLANK);

        getOrCreateTagBuilder(BlockTags.CLIMBABLE)
                .add(BlocksManager.CUSTOM_VINE);

        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, new Identifier("fabric", "needs_tool_level_4")));
    }
}
