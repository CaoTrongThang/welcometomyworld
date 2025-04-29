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

public class MobsGearsUp {

    public enum EnchantmentCategory {
        ARMOR,
        WEAPON,
        BOW,
        CROSSBOW,
        SHIELD,
    }

    public static float DEFAULT_EQUIP_CHANCE = 0.1f;

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

    public static final HashMap<Identifier, MobSettings> validMobs = new HashMap<>();
    public static final HashSet<Identifier> validEnchantments = new HashSet<>();

    static {
        validMobs.put(new Identifier("minecraft:zombie"), new MobSettings());
        validMobs.put(new Identifier("minecraft:zombie_villager"), new MobSettings());
        validMobs.put(new Identifier("minecraft:vindicator"), new MobSettings());
        validMobs.put(new Identifier("minecraft:giant"), new MobSettings());
        validMobs.put(new Identifier("palegardenabackport:creaking"), new MobSettings());
        validMobs.put(new Identifier("minecraft:enderman"), new MobSettings().setOffhand(false));
        validMobs.put(new Identifier("minecraft:spider"), new MobSettings().setOffhand(false));
        validMobs.put(new Identifier("minecraft:skeleton"), new MobSettings().setRange(true).setMelee(false));

        validEnchantments.add(new Identifier("minecraft:protection"));
        validEnchantments.add(new Identifier("minecraft:fire_protection"));
        validEnchantments.add(new Identifier("minecraft:feather_falling"));
        validEnchantments.add(new Identifier("minecraft:blast_protection"));
        validEnchantments.add(new Identifier("minecraft:projectile_protection"));
        validEnchantments.add(new Identifier("minecraft:respiration"));
        validEnchantments.add(new Identifier("minecraft:aqua_affinity"));
        validEnchantments.add(new Identifier("minecraft:thorns"));
        validEnchantments.add(new Identifier("minecraft:depth_strider"));
        validEnchantments.add(new Identifier("minecraft:frost_walker"));
        validEnchantments.add(new Identifier("minecraft:binding_curse"));
        validEnchantments.add(new Identifier("minecraft:soul_speed"));
        validEnchantments.add(new Identifier("minecraft:swift_sneak"));
        validEnchantments.add(new Identifier("minecraft:sharpness"));
        validEnchantments.add(new Identifier("minecraft:smite"));
        validEnchantments.add(new Identifier("minecraft:bane_of_arthropods"));
        validEnchantments.add(new Identifier("minecraft:knockback"));
        validEnchantments.add(new Identifier("minecraft:fire_aspect"));
        validEnchantments.add(new Identifier("minecraft:looting"));
        validEnchantments.add(new Identifier("minecraft:sweeping"));
        validEnchantments.add(new Identifier("minecraft:efficiency"));
        validEnchantments.add(new Identifier("minecraft:silk_touch"));
        validEnchantments.add(new Identifier("minecraft:unbreaking"));
        validEnchantments.add(new Identifier("minecraft:power"));
        validEnchantments.add(new Identifier("minecraft:punch"));
        validEnchantments.add(new Identifier("minecraft:flame"));
        validEnchantments.add(new Identifier("minecraft:infinity"));
        validEnchantments.add(new Identifier("minecraft:impaling"));
        validEnchantments.add(new Identifier("minecraft:riptide"));
        validEnchantments.add(new Identifier("minecraft:channeling"));
        validEnchantments.add(new Identifier("minecraft:multishot"));
        validEnchantments.add(new Identifier("minecraft:quick_charge"));
        validEnchantments.add(new Identifier("minecraft:piercing"));
        validEnchantments.add(new Identifier("minecraft:vanishing_curse"));
        validEnchantments.add(new Identifier("mcda:burning"));
        validEnchantments.add(new Identifier("mcdw:ambush"));
        validEnchantments.add(new Identifier("mcdw:multi_shot"));
        validEnchantments.add(new Identifier("mcdw:cobweb_shot"));
        validEnchantments.add(new Identifier("mcda:poison_focus"));
        validEnchantments.add(new Identifier("mcdw:weakening"));
        validEnchantments.add(new Identifier("mcdw:fuse_shot"));
        validEnchantments.add(new Identifier("mcdw:gravity"));
        validEnchantments.add(new Identifier("mcdw:levitation_shot"));

        OFF_HANDS.add(Registries.ITEM.get(new Identifier("minecraft:flint_and_steel")));
        OFF_HANDS.add(Registries.ITEM.get(new Identifier("minecraft:ender_pearl")));
        OFF_HANDS.add(Registries.ITEM.get(new Identifier("minecraft:lava_bucket")));
    }

    public static void register() {
        Registries.ITEM.forEach(item -> {
            if (item instanceof ArmorItem armor) {
                switch (armor.getSlotType()) {
                    case HEAD -> HELMETS.add(item);
                    case CHEST -> CHESTPLATES.add(item);
                    case LEGS -> LEGGINGS.add(item);
                    case FEET -> BOOTS.add(item);
                }
            } else if (item instanceof SwordItem swordItem) {
                if (swordItem.getAttackDamage() <= 12.0F) {
                    MELEE_WEAPONS.add(item);
                }
            } else if (item instanceof AxeItem axeItem) {
                if (axeItem.getAttackDamage() <= 12.0F) {
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
            if (ench == null) continue;

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
