package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class VoidDimensionDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelVoidDimDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getWorld().getRegistryKey().equals(VoidDimension.VOID_DIM_LEVEL_KEY)) {
            if (source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.OUT_OF_WORLD)) {
                cir.setReturnValue(false);
            }
        }
    }
}
