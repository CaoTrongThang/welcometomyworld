package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.BuffTalisman;
import com.trongthang.welcometomyworld.items.ModToolMaterial;
import com.trongthang.welcometomyworld.items.RepairKnowledge;
import com.trongthang.welcometomyworld.items.RepairTalisman;
import com.trongthang.welcometomyworld.items.Weapons.Hammer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class ItemsManager {

    public static final Item THE_FALLEN_HAMMER = registerItem(new Hammer(ModToolMaterial.HAMMER, 15, -2.5f, new FabricItemSettings()), "fallen_hammer");

    public static final Item REPAIR_KNOWLEDGE = registerItem(new RepairKnowledge(new FabricItemSettings()), "repair_knowledge");

    public static final Item REPAIR_TALISMAN_IRON = registerItem(new RepairTalisman(new FabricItemSettings(), 15), "repair_talisman_iron");
    public static final Item REPAIR_TALISMAN_GOLD = registerItem(new RepairTalisman(new FabricItemSettings(), 30), "repair_talisman_gold");
    public static final Item REPAIR_TALISMAN_EMERALD = registerItem(new RepairTalisman(new FabricItemSettings(), 60), "repair_talisman_emerald");

    public static final Item ANCIENT_FRAGMENT = registerItem(new Item(new FabricItemSettings()), "ancient_fragment");
    public static final Item POWER_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.STRENGTH, 2), "power_talisman");
    public static final Item SPEED_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.SPEED, 1), "speed_talisman");
    public static final Item LIFE_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.REGENERATION, 1), "life_talisman");
    public static final Item RESISTANCE_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.RESISTANCE, 2), "resistance_talisman");

//    public static final Item A_LIVING_LOG_SPAWN_EGG = registerSpawnEgg(EntitiesManager.A_LIVING_LOG, "a_living_log");
    public static final Item A_LIVING_FLOWER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.A_LIVING_FLOWER, "a_living_flower");
    public static final Item ANCIENT_WHALE_SPAWN_EGG = registerSpawnEgg(EntitiesManager.ANCIENT_WHALE, "ancient_whale");
    public static final Item ENDERCHESTER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.ENDERCHESTER, "enderchester");
    public static final Item CHESTER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.CHESTER, "chester");
    public static final Item PORTALER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.PORTALER, "portaler");
    public static final Item THE_ENDER_CHEST_SPAWN_EGG = registerSpawnEgg(EntitiesManager.ENDER_PEST, "ender_pest");
    public static final Item THE_FALLEN_KNIGHT_SPAWN_EGG = registerSpawnEgg(EntitiesManager.FALLEN_KNIGHT, "fallen_knight");
    public static final Item WANDERER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.WANDERER, "wanderer");

    public static final ItemGroup WELCOME_TO_MY_WORLD_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.welcometomyworld.general"))
            .icon(() -> new ItemStack(POWER_TALISMAN))  // Set the icon of the item group
            .build();

    public static Item registerSpawnEgg(EntityType entityType, String id){
        Item item = new SpawnEggItem(entityType, 0xFFFFFF, 0xFFFFFF, new Item.Settings());
        Item registeredItem = Registry.register(Registries.ITEM, new Identifier(WelcomeToMyWorld.MOD_ID, id), item);

        return registeredItem;
    }

    public static Item registerItem(Item item, String id) {
        Identifier itemID = new Identifier(WelcomeToMyWorld.MOD_ID, id);
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        return registeredItem;
    }

    public static void initialize() {
        // Register the custom item group with the registry
        Identifier itemGroupId = new Identifier(WelcomeToMyWorld.MOD_ID, "general");
        Registry.register(Registries.ITEM_GROUP, itemGroupId, WELCOME_TO_MY_WORLD_GROUP);

        // Create a RegistryKey for the custom item group
        RegistryKey<ItemGroup> groupKey = RegistryKey.of(Registries.ITEM_GROUP.getKey(), itemGroupId);


        // Modify entries for your custom item group using the RegistryKey
        ItemGroupEvents.modifyEntriesEvent(groupKey)
                .register(itemGroup -> {
                    itemGroup.add(ItemsManager.THE_FALLEN_HAMMER);

                    itemGroup.add(ItemsManager.ANCIENT_FRAGMENT);
                    itemGroup.add(ItemsManager.REPAIR_KNOWLEDGE);

                    itemGroup.add(ItemsManager.POWER_TALISMAN);
                    itemGroup.add(ItemsManager.SPEED_TALISMAN);
                    itemGroup.add(ItemsManager.LIFE_TALISMAN);
                    itemGroup.add(ItemsManager.RESISTANCE_TALISMAN);

                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_IRON);
                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_GOLD);
                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_EMERALD);

//                    itemGroup.add(ItemsManager.A_LIVING_LOG_SPAWN_EGG);
                    itemGroup.add(ItemsManager.A_LIVING_FLOWER_SPAWN_EGG);
                    itemGroup.add(ItemsManager.ENDERCHESTER_SPAWN_EGG);
                    itemGroup.add(ItemsManager.CHESTER_SPAWN_EGG);
                    itemGroup.add(ItemsManager.PORTALER_SPAWN_EGG);
                    itemGroup.add(ItemsManager.THE_ENDER_CHEST_SPAWN_EGG);
                    itemGroup.add(ItemsManager.THE_FALLEN_KNIGHT_SPAWN_EGG);
                    itemGroup.add(ItemsManager.WANDERER_SPAWN_EGG);

                    itemGroup.add(BlocksManager.TOUGHER_IRON_BLOCK);
                    itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK);
                    itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK_STAGE2);
                    itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK_STAGE3);

                    itemGroup.add(BlocksManager.TOUGHER_IRON_BARS);
                    itemGroup.add(BlocksManager.RUSTED_IRON_BARS);

                    itemGroup.add(BlocksManager.BURNING_PLANK);
                    itemGroup.add(BlocksManager.BURNED_PLANK);
                    itemGroup.add(BlocksManager.CUSTOM_VINE);
                });
    }
}
