package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MobsGearsUp {

    public enum EnchantmentCategory {
        ARMOR,
        WEAPON,
        BOW,
        CROSSBOW,
        SHIELD,
    }

    public static float DEFAULT_EQUIP_CHANCE = 0.05f;

    public static final HashMap<EnchantmentCategory, List<Identifier>> CATEGORIZED_ENCHANTMENTS = new HashMap<>();

    public static List<Item> MELEE_WEAPONS = new ArrayList<>();
    public static List<Item> RANGE_WEAPONS = new ArrayList<>();

    public static List<Item> OFF_HANDS = new ArrayList<>();

    public static final List<Item> HELMETS = new ArrayList<>();
    public static final List<Item> CHESTPLATES = new ArrayList<>();
    public static final List<Item> LEGGINGS = new ArrayList<>();
    public static final List<Item> BOOTS = new ArrayList<>();

    public static class MobSettings {
        public boolean melee = true;
        public boolean range = false;
        public boolean offhand = true;
        public boolean helmet = true;
        public boolean chestplate = true;
        public boolean leggings = true;
        public boolean boots = true;

        public boolean enchantment = true;

        public MobSettings setMelee(boolean canUse) {
            this.melee = canUse;
            return this;
        }

        public MobSettings setRange(boolean canUse) {
            this.range = canUse;
            return this;
        }

        public MobSettings setOffhand(boolean canUse) {
            this.offhand = canUse;
            return this;
        }

        public MobSettings setHelmet(boolean canUse) {
            this.helmet = canUse;
            return this;
        }

        public MobSettings setChestplate(boolean canUse) {
            this.chestplate = canUse;
            return this;
        }

        public MobSettings setLeggings(boolean canUse) {
            this.leggings = canUse;
            return this;
        }

        public MobSettings setBoots(boolean canUse) {
            this.boots = canUse;
            return this;
        }

        public MobSettings setEnchantment(boolean canUse) {
            this.enchantment = canUse;
            return this;
        }
    }

    public static final HashMap<String, MobSettings> validMobs = new HashMap<>();
    public static final HashSet<Identifier> validEnchantments = new HashSet<>();

    static {
        // Initialized in register() via loadFromConfig()
    }

    private static void loadFromConfig() {
        com.trongthang.welcometomyworld.ConfigLoader.MobsGearsUpConfig config = com.trongthang.welcometomyworld.ConfigLoader
                .getInstance().mobsGearsUp;

        // Load valid mobs
        for (Map.Entry<String, com.trongthang.welcometomyworld.ConfigLoader.MobSettingsConfig> entry : config.validMobs
                .entrySet()) {
            com.trongthang.welcometomyworld.ConfigLoader.MobSettingsConfig cfg = entry.getValue();
            MobSettings settings = new MobSettings()
                    .setMelee(cfg.melee)
                    .setRange(cfg.range)
                    .setOffhand(cfg.offhand)
                    .setHelmet(cfg.helmet)
                    .setChestplate(cfg.chestplate)
                    .setLeggings(cfg.leggings)
                    .setBoots(cfg.boots)
                    .setEnchantment(cfg.enchantment);
            validMobs.put(entry.getKey(), settings);
        }

        // Load valid enchantments
        for (String enchId : config.validEnchantments) {
            validEnchantments.add(new Identifier(enchId));
        }

        // Load extra off-hands
        for (String itemId : config.extraOffHands) {
            Item item = Registries.ITEM.get(new Identifier(itemId));
            if (item != Items.AIR) {
                OFF_HANDS.add(item);
            }
        }
    }

    public static MobSettings getSettings(Identifier id) {
        String idStr = id.toString();
        // 1. Exact match
        if (validMobs.containsKey(idStr)) {
            return validMobs.get(idStr);
        }

        // 2. Namespace wildcard match (e.g., minecraft:*)
        String namespace = id.getNamespace();
        String wildcardPattern = namespace + ":*";
        if (validMobs.containsKey(wildcardPattern)) {
            return validMobs.get(wildcardPattern);
        }

        return null;
    }

    public static void reload() {
        validMobs.clear();
        validEnchantments.clear();
        CATEGORIZED_ENCHANTMENTS.clear();
        MELEE_WEAPONS.clear();
        RANGE_WEAPONS.clear();
        OFF_HANDS.clear();
        HELMETS.clear();
        CHESTPLATES.clear();
        LEGGINGS.clear();
        BOOTS.clear();

        register();
    }

    public static void register() {
        loadFromConfig();
        Registries.ITEM.forEach(item -> {
            if (item instanceof ArmorItem armor) {
                switch (armor.getSlotType()) {
                    case HEAD -> HELMETS.add(item);
                    case CHEST -> CHESTPLATES.add(item);
                    case LEGS -> LEGGINGS.add(item);
                    case FEET -> BOOTS.add(item);
                    default -> {
                    }
                }
            } else if (item instanceof SwordItem swordItem) {
                if (swordItem.getAttackDamage() <= 10.0F) {
                    MELEE_WEAPONS.add(item);
                }
            } else if (item instanceof AxeItem axeItem) {
                if (axeItem.getAttackDamage() <= 10.0F) {
                    MELEE_WEAPONS.add(item);
                }
            } else if (item instanceof BowItem || item instanceof CrossbowItem) {
                RANGE_WEAPONS.add(item);
            } else if (item instanceof ShieldItem) {
                OFF_HANDS.add(item);
            }
        });

        Item shieldItem = Registries.ITEM.get(new Identifier("minecraft:shield"));

        for (Identifier enchId : validEnchantments) {
            Enchantment ench = Registries.ENCHANTMENT.get(enchId);
            if (ench == null)
                continue;

            EnchantmentCategory category = null;

            // Check if enchantment applies to shields
            if (shieldItem != null && ench.isAcceptableItem(shieldItem.getDefaultStack())) {
                category = EnchantmentCategory.SHIELD;
            } else {
                // Categorize based on EnchantmentTarget
                category = getCategoryFromTarget(ench);
            }

            if (category != null) {
                CATEGORIZED_ENCHANTMENTS.computeIfAbsent(category, k -> new ArrayList<>()).add(enchId);
            }
        }
    }

    private static EnchantmentCategory getCategoryFromTarget(Enchantment enchantment) {
        EnchantmentTarget target = enchantment.target;
        if (target == EnchantmentTarget.ARMOR || target == EnchantmentTarget.ARMOR_HEAD ||
                target == EnchantmentTarget.ARMOR_CHEST || target == EnchantmentTarget.ARMOR_LEGS ||
                target == EnchantmentTarget.ARMOR_FEET) {
            return EnchantmentCategory.ARMOR;
        } else if (target == EnchantmentTarget.WEAPON) {
            return EnchantmentCategory.WEAPON;
        } else if (target == EnchantmentTarget.BOW) {
            return EnchantmentCategory.BOW;
        } else if (target == EnchantmentTarget.CROSSBOW) {
            return EnchantmentCategory.CROSSBOW;
        }

        return null;
    }

    private static void logItems() {
        for (Item i : HELMETS) {
            WelcomeToMyWorld.LOGGER.info("HEL: " + i);
        }

        for (Item i : CHESTPLATES) {
            WelcomeToMyWorld.LOGGER.info("CHEST: " + i);
        }

        for (Item i : LEGGINGS) {
            WelcomeToMyWorld.LOGGER.info("LEG: " + i);
        }

        for (Item i : BOOTS) {
            WelcomeToMyWorld.LOGGER.info("BOOTS: " + i);
        }

        for (Item i : MELEE_WEAPONS) {
            WelcomeToMyWorld.LOGGER.info("MELEE: " + i);
        }

        for (Item i : RANGE_WEAPONS) {
            WelcomeToMyWorld.LOGGER.info("RANGE: " + i);
        }

        for (Item i : OFF_HANDS) {
            WelcomeToMyWorld.LOGGER.info("OFF: " + i);
        }
    }
}
