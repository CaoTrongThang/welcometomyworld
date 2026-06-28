package com.trongthang.welcometomyworld.items.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class LifeStealEnchantment extends Enchantment {

    public LifeStealEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    /**
     * Returns the steal fraction for the given level.
     * Level 1 → 0.0001 (0.01%), level 2 → 0.000125 (0.0125%), +0.000025 per level.
     */
    public static float getStealFraction(int level) {
        return 0.0001f + (level - 1) * 0.000025f;
    }

    /**
     * Returns the heal cap for the given level.
     * Level 1 → 1.0 HP, level 2 → 1.1 HP, +0.1 per level.
     */
    public static float getHealCap(int level) {
        return 2.0f + (level - 1) * (6.0f / 9.0f);
    }
}
