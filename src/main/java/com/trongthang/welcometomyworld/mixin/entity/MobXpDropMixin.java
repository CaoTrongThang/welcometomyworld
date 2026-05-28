package com.trongthang.welcometomyworld.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public class MobXpDropMixin {
    @ModifyReturnValue(method = "getXpToDrop()I", at = @At("RETURN"))
    private int scaleXpBasedOnStats(int originalXp) {
        MobEntity mob = (MobEntity) (Object) this;

        float maxHealth = mob.getMaxHealth();
        double armor = mob.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

        // Aggressive scaling based on health and armor
        double healthXP = Math.sqrt(maxHealth * 1.20f); // Doubled effective scaling
        double armorXP = armor * 0.45; // Slightly more than double (0.19 -> 0.45)

        int scaledXp = (int) (healthXP + armorXP);

        // Ensure at least double the original XP drop
        int finalXp = Math.max(scaledXp, originalXp * 2);

        // Cap at 500
        return Math.min(finalXp, 500);
    }
}
