package com.trongthang.welcometomyworld.mixin.combat;

import com.trongthang.welcometomyworld.Utilities.DamageTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records damage dealt to LivingEntities by players and tamed mobs.
 * Data is consumed in TameableEntityKillMobMixin on death.
 */
@Mixin(LivingEntity.class)
public class LivingEntityDamageTrackerMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // Only record if damage actually landed
        if (!cir.getReturnValue())
            return;

        LivingEntity victim = (LivingEntity) (Object) this;

        // Server-side only
        if (victim.getWorld().isClient)
            return;

        // Skip players as victims — we only care about hostile/neutral mobs dying
        if (victim instanceof PlayerEntity)
            return;

        Entity attacker = source.getAttacker();
        if (attacker == null)
            return;

        // Record hits from players or tamed mobs
        if (attacker instanceof PlayerEntity || attacker instanceof TameableEntity) {
            DamageTracker.recordDamage(victim.getUuid(), attacker.getUuid(), amount);
        }
    }
}
