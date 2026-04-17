package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.*;
import com.trongthang.welcometomyworld.items.Weapons.Hammer;
import com.trongthang.welcometomyworld.items.enchantments.SilenceEnchantment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ItemsManager {

        public static final Item THE_FALLEN_HAMMER = registerItem(
                        new Hammer(ModToolMaterial.HAMMER, 15, -2.5f, new FabricItemSettings()), "fallen_hammer");

        public static final Item REPAIR_KNOWLEDGE = registerItem(new RepairKnowledge(new FabricItemSettings()),
                        "repair_knowledge");

        public static final Item REPAIR_TALISMAN_IRON = registerItem(new RepairTalisman(new FabricItemSettings(), 15),
                        "repair_talisman_iron");
        public static final Item REPAIR_TALISMAN_GOLD = registerItem(new RepairTalisman(new FabricItemSettings(), 30),
                        "repair_talisman_gold");
        public static final Item REPAIR_TALISMAN_EMERALD = registerItem(
                        new RepairTalisman(new FabricItemSettings(), 60),
                        "repair_talisman_emerald");

        public static final Item ANCIENT_FRAGMENT = registerItem(new Item(new FabricItemSettings()),
                        "ancient_fragment");
        public static final Item POWER_TALISMAN = registerItem(
                        new BuffTalisman(new FabricItemSettings(), StatusEffects.STRENGTH, 2), "power_talisman");
        public static final Item SPEED_TALISMAN = registerItem(
                        new BuffTalisman(new FabricItemSettings(), StatusEffects.SPEED, 1), "speed_talisman");
        public static final Item LIFE_TALISMAN = registerItem(
                        new BuffTalisman(new FabricItemSettings(), StatusEffects.REGENERATION, 1), "life_talisman");
        public static final Item RESISTANCE_TALISMAN = registerItem(
                        new BuffTalisman(new FabricItemSettings(), StatusEffects.RESISTANCE, 2), "resistance_talisman");
        public static final Item CREEPER_TALISMAN = registerItem(new CreeperTalisman(new FabricItemSettings()),
                        "creeper_talisman");

        public static final Item ENDERCHESTER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.ENDERCHESTER,
                        "enderchester");
        public static final Item CHESTER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.CHESTER, "chester");
        public static final Item THE_ENDER_CHEST_SPAWN_EGG = registerSpawnEgg(EntitiesManager.ENDER_PEST, "ender_pest");

        public static final Item PORTALER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.PORTALER, "portaler", 0x7b7b7b,
                        0xb500d1);
        public static final Item THE_FALLEN_KNIGHT_SPAWN_EGG = registerSpawnEgg(EntitiesManager.FALLEN_KNIGHT,
                        "fallen_knight", 0x525252, 0x727272);
        public static final Item WANDERER_SPAWN_EGG = registerSpawnEgg(EntitiesManager.WANDERER, "wanderer", 0x000000,
                        0xFFFFFF);
        public static final Item BLOSSOM_SPAWN_EGG = registerSpawnEgg(EntitiesManager.BLOSSOM, "blossom", 0x00b00b,
                        0x00d120);
        public static final Item UNKNOWN_SPAWN_EGG = registerSpawnEgg(EntitiesManager.UNKNOWN, "unknown", 0x000000,
                        0x000000);
        public static final Item VOID_WORM_SPAWN_EGG = registerSpawnEgg(EntitiesManager.VOID_WORM, "void_worm",
                        0x140122,
                        0x6a008c);
        public static final Item VOIDAN_SPAWN_EGG = registerSpawnEgg(EntitiesManager.VOIDAN, "voidan", 0x041820,
                        0x61c3cb);
        public static final Item VOIDAN_TENTACLE_SPAWN_EGG = registerSpawnEgg(EntitiesManager.VOIDAN_TENTACLE,
                        "voidan_tentacle", 0x041820,
                        0x2f114c);

        public static final Item UNKNOWN_HOOD = registerItem(new EffectArmorItem(CustomArmorMaterial.UNKNOWN_GEAR,
                        ArmorItem.Type.HELMET, new FabricItemSettings().rarity(Rarity.EPIC), EffectsManager.VOID_SIGHT),
                        "unknown_hood");
        public static final Item UNKNOWN_CLOAK = registerItem(new EffectArmorItem(CustomArmorMaterial.UNKNOWN_GEAR,
                        ArmorItem.Type.CHESTPLATE, new FabricItemSettings().rarity(Rarity.EPIC),
                        StatusEffects.FIRE_RESISTANCE),
                        "unknown_cloak");
        public static final Item UNKNOWN_LEGGINGS = registerItem(new EffectArmorItem(CustomArmorMaterial.UNKNOWN_GEAR,
                        ArmorItem.Type.LEGGINGS, new FabricItemSettings().rarity(Rarity.EPIC),
                        StatusEffects.JUMP_BOOST),
                        "unknown_leggings");
        public static final Item UNKNOWN_BOOTS = registerItem(new EffectArmorItem(CustomArmorMaterial.UNKNOWN_GEAR,
                        ArmorItem.Type.BOOTS, new FabricItemSettings().rarity(Rarity.EPIC), StatusEffects.SPEED),
                        "unknown_boots");
        public static final Item VOID_WORM_BONE_HELMET = registerItem(
                        new VoidWormBoneArmorItem(CustomArmorMaterial.VOID_WORM_BONE_ARMOR, ArmorItem.Type.HELMET,
                                        new FabricItemSettings().rarity(Rarity.EPIC)),
                        "void_worm_bone_helmet");
        public static final Item VOID_WORM_BONE_CHESTPLATE = registerItem(
                        new VoidWormBoneArmorItem(CustomArmorMaterial.VOID_WORM_BONE_ARMOR, ArmorItem.Type.CHESTPLATE,
                                        new FabricItemSettings().rarity(Rarity.EPIC)),
                        "void_worm_bone_chestplate");
        public static final Item VOID_WORM_BONE_LEGGINGS = registerItem(
                        new VoidWormBoneArmorItem(CustomArmorMaterial.VOID_WORM_BONE_ARMOR, ArmorItem.Type.LEGGINGS,
                                        new FabricItemSettings().rarity(Rarity.EPIC)),
                        "void_worm_bone_leggings");
        public static final Item VOID_WORM_BONE_BOOTS = registerItem(
                        new VoidWormBoneArmorItem(CustomArmorMaterial.VOID_WORM_BONE_ARMOR, ArmorItem.Type.BOOTS,
                                        new FabricItemSettings().rarity(Rarity.EPIC)),
                        "void_worm_bone_boots");

        public static final Item CAPTURE_CAGE = registerItem(
                        new CaptureCageItem(new FabricItemSettings().rarity(Rarity.RARE)),
                        "capture_cage");

        public static final Item VOIDAN_HORN = registerItem(
                        new VoidanHornItem(new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1)),
                        "voidan_horn");

        public static Enchantment silenceEnchantment = new SilenceEnchantment();

        public static final ItemGroup WELCOME_TO_MY_WORLD_GROUP = FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.welcometomyworld.general"))
                        .icon(() -> new ItemStack(POWER_TALISMAN)) // Set the icon of the item group
                        .build();

        public static Item registerSpawnEgg(EntityType entityType, String id) {
                Item item = new SpawnEggItem(entityType, 0xFFFFFF, 0xFFFFFF, new Item.Settings());
                Item registeredItem = Registry.register(Registries.ITEM, new Identifier(WelcomeToMyWorld.MOD_ID, id),
                                item);

                return registeredItem;
        }

        public static <T extends MobEntity> Item registerSpawnEgg(EntityType<T> entityType, String id, int primaryColor,
                        int secondaryColor) {
                SpawnEggItem item = new SpawnEggItem(
                                entityType,
                                primaryColor,
                                secondaryColor,
                                new FabricItemSettings());

                return Registry.register(
                                Registries.ITEM,
                                new Identifier(WelcomeToMyWorld.MOD_ID, id + "_spawn_egg"),
                                item);
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

                Registry.register(Registries.ENCHANTMENT, new Identifier(WelcomeToMyWorld.MOD_ID, "silence"),
                                silenceEnchantment);

                com.trongthang.welcometomyworld.compat.TrinketsCompat.registerTrinket(CREEPER_TALISMAN);

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

                                        itemGroup.add(ItemsManager.CREEPER_TALISMAN);

                                        itemGroup.add(ItemsManager.ENDERCHESTER_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.CHESTER_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.PORTALER_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.THE_ENDER_CHEST_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.THE_FALLEN_KNIGHT_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.WANDERER_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.BLOSSOM_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.UNKNOWN_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.VOID_WORM_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.VOIDAN_SPAWN_EGG);
                                        itemGroup.add(ItemsManager.VOIDAN_TENTACLE_SPAWN_EGG);

                                        itemGroup.add(ItemsManager.UNKNOWN_HOOD);
                                        itemGroup.add(ItemsManager.UNKNOWN_CLOAK);
                                        itemGroup.add(ItemsManager.UNKNOWN_LEGGINGS);
                                        itemGroup.add(ItemsManager.UNKNOWN_BOOTS);

                                        itemGroup.add(ItemsManager.VOID_WORM_BONE_HELMET);
                                        itemGroup.add(ItemsManager.VOID_WORM_BONE_CHESTPLATE);
                                        itemGroup.add(ItemsManager.VOID_WORM_BONE_LEGGINGS);
                                        itemGroup.add(ItemsManager.VOID_WORM_BONE_BOOTS);

                                        itemGroup.add(ItemsManager.CAPTURE_CAGE);
                                        itemGroup.add(ItemsManager.VOIDAN_HORN);

                                        itemGroup.add(FluidsManager.DEATH_WATER_BUCKET);

                                        itemGroup.add(BlocksManager.TOUGHER_IRON_BLOCK);
                                        itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK);
                                        itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK_STAGE2);
                                        itemGroup.add(BlocksManager.RUSTED_IRON_BLOCK_STAGE3);

                                        itemGroup.add(BlocksManager.TOUGHER_IRON_BARS);
                                        itemGroup.add(BlocksManager.RUSTED_IRON_BARS);

                                        itemGroup.add(BlocksManager.BURNING_PLANK);
                                        itemGroup.add(BlocksManager.BURNED_PLANK);
                                        itemGroup.add(BlocksManager.CUSTOM_VINE);

                                        itemGroup.add(BlocksManager.EASYCRAFT_TROPHY);
                                        itemGroup.add(BlocksManager.GAMING_DISC_TROPHY);
                                        itemGroup.add(BlocksManager.CHALLENGER_TROPHY);
                                        itemGroup.add(BlocksManager.MUSIC_TROPHY);

                                        itemGroup.add(BlocksManager.GLOWING_WHITE_BLOCK);
                                        itemGroup.add(BlocksManager.PURE_BLACK_BLOCK);
                                        itemGroup.add(BlocksManager.GLOWING_WHITE_GRASS);

                                        itemGroup.add(BlocksManager.VOID_BLOCK);
                                });
        }
}
