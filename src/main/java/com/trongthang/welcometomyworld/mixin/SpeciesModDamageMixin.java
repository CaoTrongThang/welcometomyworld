package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.ConfigLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

@Mixin(LivingEntity.class)
public class SpeciesModDamageMixin {
    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", // Explicit method descriptor
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float modifyDamageAmount(float originalDamage, DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker != null && "species:cruncher".equals(
                Registries.ENTITY_TYPE.getId(attacker.getType()).toString())) {
            LivingEntity livingEntity = (LivingEntity) attacker;
            return (float) (originalDamage + Math.min(200, livingEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.009f));
        }
        return originalDamage;
    }
}
