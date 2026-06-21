
package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.managers.ItemsManager;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class AddLootTable {
    public void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, supplier, setter) -> {
            // Check if the loot table is an entity
            if (id.getPath().startsWith("entities/")) {
                LootPool rareItemPool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ItemsManager.ANCIENT_FRAGMENT)
                                .conditionally(RandomChanceLootCondition.builder(0.1f))) // 1% drop rate
                        .with(ItemEntry.builder(ItemsManager.REPAIR_KNOWLEDGE)
                                .conditionally(RandomChanceLootCondition.builder(0.1f))) // 1% drop rate
                        .build();
                supplier.pool(rareItemPool);
            }

            if (id.toString().equals("minecraft:entities/warden")) {
                Item wardenTendril = Registries.ITEM.get(new Identifier("zenith", "warden_tendril"));
                if (wardenTendril != Items.AIR) {
                    LootPool wardenPool = LootPool.builder()
                            .rolls(UniformLootNumberProvider.create(1.0f, 3.0f))
                            .with(ItemEntry.builder(wardenTendril))
                            .build();
                    supplier.pool(wardenPool);
                }
            }

        });
    }
}
