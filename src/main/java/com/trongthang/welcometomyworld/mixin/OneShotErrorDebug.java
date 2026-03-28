package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.ConfigLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

@Mixin(LivingEntity.class)
public class OneShotErrorDebug {
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if(!ConfigLoader.getInstance().oneShotDebugLog) return;

        LivingEntity livingEntity = (LivingEntity) (Object) this;
        Entity attacker = damageSource.getAttacker();

        LOGGER.info("WelcomeToMyWorld ==== DEATH DEBUG INFO ====");
        LOGGER.info("Entity Type: " + livingEntity.getName().getString());
        LOGGER.info("Final Damage Source: " + damageSource.getName());

        if (attacker instanceof PlayerEntity player) {
            LOGGER.info("Killed by Player: " + player.getName().getString());
            LOGGER.info("Weapon Used: " + player.getMainHandStack().getItem().toString());
        } else if (attacker != null) {
            LOGGER.info("Killed by: " + attacker.getType().getName().getString());
        } else {
            LOGGER.info("Killed by environmental damage (e.g., fire, fall, explosion)");
        }

        LOGGER.info("Entity Health Before Death: " + livingEntity.getHealth());
        LOGGER.info("==========================");
    }
}
