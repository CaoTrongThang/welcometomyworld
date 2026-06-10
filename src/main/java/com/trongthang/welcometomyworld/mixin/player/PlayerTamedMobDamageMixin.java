package com.trongthang.welcometomyworld.mixin.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class PlayerTamedMobDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventOwnerDamageDuringCombat(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {

        LivingEntity targetEntity = (LivingEntity) (Object) this;

        if (targetEntity.getWorld().isClient())
            return;

        Entity attacker = source.getAttacker();

        if (!(attacker instanceof PlayerEntity player))
            return;

        if (targetEntity instanceof TameableEntity tameable && tameable.isTamed()) {
            if (tameable.getTarget() != null
                    && !player.getAbilities().creativeMode) {
                if (com.trongthang.welcometomyworld.Utilities.AllyUtils.isAlly(player, targetEntity)) {
                    ci.setReturnValue(false);
                }
            }
        }
    }
}
