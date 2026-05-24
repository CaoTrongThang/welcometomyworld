package com.trongthang.welcometomyworld.mixin.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LivingEntity.class)
public abstract class LifestealNerfMixin {

    @Unique
    private static EntityAttribute welcometomyworld$cachedLifestealAttr = null;
    @Unique
    private static StatusEffect welcometomyworld$cachedBleedingEffect = null;
    @Unique
    private static boolean welcometomyworld$attemptedResolve = false;

    @Unique
    private static void welcometomyworld$resolve() {
        if (welcometomyworld$attemptedResolve)
            return;
        welcometomyworld$attemptedResolve = true;
        try {
            welcometomyworld$cachedLifestealAttr = Registries.ATTRIBUTE
                    .get(new Identifier("zenith_attributes", "life_steal"));
            welcometomyworld$cachedBleedingEffect = Registries.STATUS_EFFECT.get(new Identifier("bleed", "bleeding"));
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", at = @At("RETURN"), cancellable = true)
    private void nerfLifeSteal(EntityAttribute attribute, CallbackInfoReturnable<Double> cir) {
        welcometomyworld$resolve();

        if (welcometomyworld$cachedLifestealAttr != null && attribute == welcometomyworld$cachedLifestealAttr) {
            LivingEntity entity = (LivingEntity) (Object) this;

            if (welcometomyworld$cachedBleedingEffect != null
                    && entity.hasStatusEffect(welcometomyworld$cachedBleedingEffect)) {
                // Nerf the value by 50% only when bleeding
                cir.setReturnValue(cir.getReturnValue() * 0.3);
            }
        }
    }
}
