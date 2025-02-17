package com.trongthang.welcometomyworld.datagen;

import com.trongthang.welcometomyworld.classes.ModTagsManager;
import com.trongthang.welcometomyworld.managers.ItemsManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    public void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ModTagsManager.TALISMANS_TAG)
                .add(ItemsManager.REPAIR_TALISMAN_IRON)
                .add(ItemsManager.REPAIR_TALISMAN_GOLD)
                .add(ItemsManager.REPAIR_TALISMAN_EMERALD)
                .add(ItemsManager.POWER_TALISMAN)
                .add(ItemsManager.SPEED_TALISMAN)
                .add(ItemsManager.LIFE_TALISMAN)
                .add(ItemsManager.RESISTANCE_TALISMAN);
    }
}
