package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.entities.CustomEntitiesManager;
import com.trongthang.welcometomyworld.items.BuffTalisman;
import com.trongthang.welcometomyworld.items.RepairBox;
import com.trongthang.welcometomyworld.items.RepairTalisman;
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

    public static final Item REPAIR_KNOWLEDGE = registerItem(new RepairBox(new FabricItemSettings()), "repair_knowledge");

    public static final Item REPAIR_TALISMAN_IRON = registerItem(new RepairTalisman(new FabricItemSettings(), 5), "repair_talisman_iron");
    public static final Item REPAIR_TALISMAN_GOLD = registerItem(new RepairTalisman(new FabricItemSettings(), 15), "repair_talisman_gold");
    public static final Item REPAIR_TALISMAN_EMERALD = registerItem(new RepairTalisman(new FabricItemSettings(), 30), "repair_talisman_emerald");

    public static final Item ANCIENT_FRAGMENT = registerItem(new Item(new FabricItemSettings()), "ancient_fragment");
    public static final Item POWER_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.STRENGTH, 2), "power_talisman");
    public static final Item SPEED_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.SPEED, 1), "speed_talisman");
    public static final Item LIFE_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.REGENERATION, 1), "life_talisman");
    public static final Item RESISTANCE_TALISMAN = registerItem(new BuffTalisman(new FabricItemSettings(), StatusEffects.RESISTANCE, 2), "resistance_talisman");

    public static final Item A_LIVING_LOG_SPAWN_EGG = registerSpawnEgg(CustomEntitiesManager.A_LIVING_LOG, "a_living_log");
    public static final Item A_LIVING_FLOWER_SPAWN_EGG = registerSpawnEgg(CustomEntitiesManager.A_LIVING_FLOWER, "a_living_flower");

    public static final ItemGroup WELCOME_TO_MY_WORLD_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.welcometomyworld.general"))
            .icon(() -> new ItemStack(POWER_TALISMAN))  // Set the icon of the item group
            .build();
    
    public static Item registerSpawnEgg(EntityType entityType, String id){
        Item item = new SpawnEggItem(entityType, 0x6B4F0F, 0xC97C55, new Item.Settings());
        Item registeredItem = Registry.register(Registries.ITEM, new Identifier("welcometomyworld", id), item);

        return registeredItem;
    }

    public static Item registerItem(Item item, String id) {
        LOGGER.info("Registered: " + id);
        // Create the identifier for the item.
        Identifier itemID = new Identifier(WelcomeToMyWorld.MOD_ID, id);

        // Register the item
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
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
                    itemGroup.add(ItemsManager.ANCIENT_FRAGMENT);
                    itemGroup.add(ItemsManager.REPAIR_KNOWLEDGE);

                    itemGroup.add(ItemsManager.POWER_TALISMAN);
                    itemGroup.add(ItemsManager.SPEED_TALISMAN);
                    itemGroup.add(ItemsManager.LIFE_TALISMAN);
                    itemGroup.add(ItemsManager.RESISTANCE_TALISMAN);

                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_IRON);
                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_GOLD);
                    itemGroup.add(ItemsManager.REPAIR_TALISMAN_EMERALD);

                    itemGroup.add(ItemsManager.A_LIVING_LOG_SPAWN_EGG);
                    itemGroup.add(ItemsManager.A_LIVING_FLOWER_SPAWN_EGG);
                });
    }
}
