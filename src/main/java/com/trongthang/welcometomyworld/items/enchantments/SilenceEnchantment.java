package com.trongthang.welcometomyworld.items.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class SilenceEnchantment extends Enchantment {
    public SilenceEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_FEET, new EquipmentSlot[] {EquipmentSlot.FEET});
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }


}
