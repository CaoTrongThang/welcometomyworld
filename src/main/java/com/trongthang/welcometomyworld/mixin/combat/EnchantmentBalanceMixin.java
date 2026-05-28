package com.trongthang.welcometomyworld.mixin.combat;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public class EnchantmentBalanceMixin {
    @Inject(method = "getAttackDamage", at = @At("HEAD"), cancellable = true)
    private void modifyEnchantmentDamage(int level, EntityGroup group, CallbackInfoReturnable<Float> cir) {
        DamageEnchantment instance = (DamageEnchantment) (Object) this;

        if (instance == Enchantments.SMITE) {
            float damage = 12 * (level);

            cir.setReturnValue(damage);
            cir.cancel();
        }
    }
}
