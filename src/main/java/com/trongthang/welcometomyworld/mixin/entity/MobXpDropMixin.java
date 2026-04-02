package com.trongthang.welcometomyworld.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public class MobXpDropMixin {
    @ModifyReturnValue(method = "getXpToDrop()I", at = @At("RETURN"))
    private int scaleXpBasedOnStats(int originalXp) {
        MobEntity mob = (MobEntity) (Object) this;
        if ((mob instanceof AnimalEntity))
            return originalXp;

        float maxHealth = mob.getMaxHealth();
        double armor = mob.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

        double healthXP = Math.sqrt(maxHealth * 0.30f);

        double armorXP = armor * 0.19;

        // Combine and round
        int scaledXp = (int) (healthXP + armorXP);

        scaledXp = Math.max(scaledXp, originalXp);
        scaledXp = Math.min(scaledXp, 100);

        if (scaledXp <= originalXp) {
            return originalXp;
        }

        return scaledXp;
    }
}
