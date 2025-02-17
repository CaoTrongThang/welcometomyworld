package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.GlobalVariables;
import com.trongthang.welcometomyworld.classes.TameableEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalVariables.EXP_MULTIPLIER_EACH_LEVEL_MOB;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

@Mixin(LivingEntity.class)
public class TameableEntityKillMobMixin {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        // Ensure we're on the server side
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity.getWorld().isClient) return;

        // Get the attacker
        Entity attacker = damageSource.getAttacker();

        // Check if the attacker is a tameable mob
        if (attacker instanceof TameableEntity tameableEntity) {

            TameableEntityInterface entityInterface = (TameableEntityInterface) tameableEntity;

            float targetHealth = livingEntity.getMaxHealth();
            float scaleFactor = 20;

            // Prevent division by zero
            float expGained = Math.max(1, (targetHealth) / scaleFactor);

            // Increase the current experience
            float newExp = entityInterface.getCurrentLevelExp() + expGained;
            entityInterface.setCurrentLevelExp(newExp);

            // Check if the mob levels up
            if (newExp >= entityInterface.getNextLevelRequireExp()) {
                int newPoints = entityInterface.getPointAvailalble() + 1;
                entityInterface.setPointAvailalble(newPoints);

                float overflowExp = newExp - entityInterface.getNextLevelRequireExp();
                entityInterface.setCurrentLevelExp(overflowExp);

                float newRequiredExp = entityInterface.getNextLevelRequireExp() * EXP_MULTIPLIER_EACH_LEVEL_MOB; // Scale by 1.5x
                entityInterface.setNextLevelRequireExp(newRequiredExp);

                float newLevel = entityInterface.getCurrentLevel() + 1;
                entityInterface.setCurrentLevel(newLevel);
            }
        }
    }
}
