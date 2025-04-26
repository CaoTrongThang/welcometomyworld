package com.trongthang.welcometomyworld.mixin;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public class BalanceEnchantments {
    @Inject(
            method = "getAttackDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifySharpnessDamage(int level, EntityGroup group, CallbackInfoReturnable<Float> cir) {
        DamageEnchantment instance = (DamageEnchantment) (Object) this;
        if (instance == Enchantments.SHARPNESS) {

            float damage = level * 0.5f;
            if (level <= 5) {
                // Triangular numbers for levels 1-5
                damage = level * (level + 1) / 2.0f;
            } else {
                // Accelerated scaling for levels 6+
                damage = 15 + 5 * (level - 5) * (level - 4);
            }

            cir.setReturnValue(damage);
            cir.cancel();
        }

        if (instance == Enchantments.SMITE) {
            float damage = 15 * (level);

            cir.setReturnValue(damage);
            cir.cancel();
        }
    }
}
