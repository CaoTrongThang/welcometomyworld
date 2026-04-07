package com.trongthang.welcometomyworld.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.function.Supplier;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;

public enum CustomArmorMaterial implements ArmorMaterial {
    UNKNOWN_GEAR(WelcomeToMyWorld.MOD_ID + ":unknown_gear", 25, new int[] { 3, 8, 6, 3 }, 15,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2.0f, 0.0f, () -> Ingredient.ofItems(Items.NETHERITE_INGOT));

    private static final EnumMap<ArmorItem.Type, Integer> BASE_DURABILITY = new EnumMap<>(ArmorItem.Type.class);

    static {
        BASE_DURABILITY.put(ArmorItem.Type.BOOTS, 13);
        BASE_DURABILITY.put(ArmorItem.Type.LEGGINGS, 15);
        BASE_DURABILITY.put(ArmorItem.Type.CHESTPLATE, 16);
        BASE_DURABILITY.put(ArmorItem.Type.HELMET, 11);
    }

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredientSupplier;

    CustomArmorMaterial(String name, int durabilityMultiplier, int[] protectionAmountsArray,
            int enchantability, SoundEvent equipSound, float toughness,
            float knockbackResistance, Supplier<Ingredient> repairIngredientSupplier) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;

        this.protectionAmounts = new EnumMap<>(ArmorItem.Type.class);
        this.protectionAmounts.put(ArmorItem.Type.BOOTS, protectionAmountsArray[0]);
        this.protectionAmounts.put(ArmorItem.Type.LEGGINGS, protectionAmountsArray[1]);
        this.protectionAmounts.put(ArmorItem.Type.CHESTPLATE, protectionAmountsArray[2]);
        this.protectionAmounts.put(ArmorItem.Type.HELMET, protectionAmountsArray[3]);

        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredientSupplier = repairIngredientSupplier;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY.getOrDefault(type, 1) * this.durabilityMultiplier;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return this.protectionAmounts.getOrDefault(type, 0);
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredientSupplier.get();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
