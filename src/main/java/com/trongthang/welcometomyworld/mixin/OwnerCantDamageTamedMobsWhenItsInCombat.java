package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class OwnerCantDamageTamedMobsWhenItsInCombat {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventOwnerDamageDuringCombat(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {

        LivingEntity targetEntity = (LivingEntity) (Object) this;

        if (targetEntity.getWorld().isClient()) return;

        Entity attacker = source.getAttacker();

        if (!(attacker instanceof PlayerEntity player)) return;


        if (targetEntity instanceof TameableEntity tameable && tameable.isTamed()) {
            LivingEntity owner = tameable.getOwner();

            if (owner != null && owner == attacker) {
                if (tameable.getTarget() != null
                        && !player.getAbilities().creativeMode) {
                    ci.setReturnValue(false);
                    ci.cancel();
                }
            }
        }
    }
}