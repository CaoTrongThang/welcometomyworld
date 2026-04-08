package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.managers.EffectsManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

    private static boolean welcometomyworld$handling = false;

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onHasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (welcometomyworld$handling)
            return;

        if (effect == StatusEffects.NIGHT_VISION) {
            welcometomyworld$handling = true;
            try {
                LivingEntity entity = (LivingEntity) (Object) this;
                boolean inVoid = entity.getWorld().getRegistryKey().getValue().toString()
                        .equals("welcometomyworld:void_dim");
                boolean hasVoidSight = entity
                        .hasStatusEffect(com.trongthang.welcometomyworld.managers.EffectsManager.VOID_SIGHT);

                if (inVoid) {
                    cir.setReturnValue(hasVoidSight);
                } else if (hasVoidSight) {
                    cir.setReturnValue(true);
                }
            } finally {
                welcometomyworld$handling = false;
            }
        }
    }

    @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onGetStatusEffect(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if (welcometomyworld$handling)
            return;

        if (effect == StatusEffects.NIGHT_VISION) {
            welcometomyworld$handling = true;
            try {
                LivingEntity entity = (LivingEntity) (Object) this;
                boolean inVoid = entity.getWorld().getRegistryKey().getValue().toString()
                        .equals("welcometomyworld:void_dim");
                StatusEffectInstance voidSight = entity
                        .getStatusEffect(com.trongthang.welcometomyworld.managers.EffectsManager.VOID_SIGHT);

                if (inVoid) {
                    cir.setReturnValue(voidSight);
                } else if (voidSight != null) {
                    StatusEffectInstance vanillaNV = entity.getStatusEffect(StatusEffects.NIGHT_VISION);
                    if (vanillaNV == null) {
                        cir.setReturnValue(voidSight);
                    }
                }
            } finally {
                welcometomyworld$handling = false;
            }
        }
    }
}
