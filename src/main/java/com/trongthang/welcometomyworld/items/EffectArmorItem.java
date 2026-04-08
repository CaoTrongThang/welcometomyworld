package com.trongthang.welcometomyworld.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EffectArmorItem extends ArmorItem {

    private final StatusEffect statusEffect;
    private final int tickDelay = 40; // Only check every 40 ticks
    private int ticker = 0;

    public EffectArmorItem(ArmorMaterial material, Type type, Settings settings, StatusEffect statusEffect) {
        super(material, type, settings);
        this.statusEffect = statusEffect;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity player) {
            ticker++;
            if (ticker % tickDelay == 0) {
                // Check if the player is actually wearing this piece of armor
                if (hasCorrectArmorOn(player)) {
                    // Apply effect for 6 seconds (120 ticks) so it doesn't flicker
                    player.addStatusEffect(new StatusEffectInstance(this.statusEffect, 400, 0, false, false, true));
                }
            }
        }
    }

    private boolean hasCorrectArmorOn(PlayerEntity player) {
        for (ItemStack armorStack : player.getArmorItems()) {
            if (armorStack.getItem() == this) {
                return true;
            }
        }
        return false;
    }
}
