package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ZombieEntity.class, priority = 10000)
public class BabyZombieMixin {

    @Inject(method = "setBaby", at = @At("HEAD"), cancellable = true)
    private void setBaby(boolean baby, CallbackInfo ci) {
        if (baby && WelcomeToMyWorld.random.nextFloat() > 0.01) {
            ci.cancel();
        }
    }
}
