package com.trongthang.welcometomyworld.mixin.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LifestealNerfMixin {

    @Unique
    private static StatusEffect welcometomyworld$cachedBleedingEffect = null;
    @Unique
    private static boolean welcometomyworld$attemptedResolve = false;

    @Unique
    private static void welcometomyworld$resolve() {
        if (welcometomyworld$attemptedResolve)
            return;
        welcometomyworld$attemptedResolve = true;
        welcometomyworld$cachedBleedingEffect = Registries.STATUS_EFFECT.get(new Identifier("bleed", "bleeding"));
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float nerfHealWhenBleeding(float amount) {
        welcometomyworld$resolve();
        LivingEntity entity = (LivingEntity) (Object) this;
        if (welcometomyworld$cachedBleedingEffect != null
                && entity.hasStatusEffect(welcometomyworld$cachedBleedingEffect)) {
            return amount * 0.3f;
        }
        return amount;
    }

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float nerfSetHealthWhenBleeding(float value) {
        welcometomyworld$resolve();
        LivingEntity entity = (LivingEntity) (Object) this;
        float current = entity.getHealth();
        // Only intercept upward changes (healing), not damage
        if (value > current && welcometomyworld$cachedBleedingEffect != null
                && entity.hasStatusEffect(welcometomyworld$cachedBleedingEffect)) {
            return current + (value - current) * 0.3f;
        }
        return value;
    }
}
