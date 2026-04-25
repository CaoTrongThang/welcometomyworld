package com.trongthang.welcometomyworld.datagen;

import com.trongthang.welcometomyworld.classes.ModTagsManager;
import com.trongthang.welcometomyworld.managers.ItemsManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    private static TagKey<Item> cTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier("c", name));
    }

    @Override
    public void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.SWORDS)
                .add(ItemsManager.THE_FALLEN_HAMMER);

        getOrCreateTagBuilder(cTag("helmets"))
                .add(ItemsManager.UNKNOWN_HOOD)
                .add(ItemsManager.VOID_WORM_BONE_HELMET);

        getOrCreateTagBuilder(cTag("chestplates"))
                .add(ItemsManager.UNKNOWN_CLOAK)
                .add(ItemsManager.VOID_WORM_BONE_CHESTPLATE);

        getOrCreateTagBuilder(cTag("leggings"))
                .add(ItemsManager.UNKNOWN_LEGGINGS)
                .add(ItemsManager.VOID_WORM_BONE_LEGGINGS);

        getOrCreateTagBuilder(cTag("boots"))
                .add(ItemsManager.UNKNOWN_BOOTS)
                .add(ItemsManager.VOID_WORM_BONE_BOOTS);

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
