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

        LOGGER.info("==== DEATH DEBUG INFO ====");
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

//    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
//    public void damage(DamageSource source, float amount, CallbackInfoReturnable cir) {
//        if(!ConfigLoader.getInstance().oneShotDebugLog) return;
//
//        Entity attacker = source.getAttacker(); // The entity that attacked (if any)
//        LivingEntity livingEntity = (LivingEntity) (Object) this;
//
//        if(livingEntity.getWorld().isClient) return;
//
//        LOGGER.info("==== DAMAGE DEBUG INFO ====");
//        LOGGER.info("Entity Type: " + livingEntity.getName().getString());
//        LOGGER.info("Health Before Hit: " + livingEntity.getHealth());
//        LOGGER.info("Damage Amount: " + amount);
//        LOGGER.info("Expected Health After Hit: " + (livingEntity.getHealth() - amount));
//
//        if (attacker instanceof PlayerEntity player) {
//            LOGGER.info("Attacker: " + player.getName().getString());
//            LOGGER.info("Item Used: " + player.getMainHandStack().getItem().toString());
//
//            // Check if the attack is critical
//            boolean isCritical = player.fallDistance > 0.0F && !player.isOnGround();
//            LOGGER.info("Critical Hit? " + isCritical);
//
//        } else if (attacker != null) {
//            LOGGER.info("Attacker: " + attacker.getType().getName().getString());
//        } else {
//            LOGGER.info("Attacker: None (Environmental damage?)");
//        }
//
//        LOGGER.info("Damage Cause: " + source.getName());
//        LOGGER.info("==========================");
//    }
}
