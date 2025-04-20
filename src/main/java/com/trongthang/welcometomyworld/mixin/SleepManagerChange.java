package com.trongthang.welcometomyworld.mixin;

import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepManager.class)
public class SleepManagerChange {
    @Inject(method = "canSkipNight", at = @At("HEAD"), cancellable = true)
    private void canSkipNight(int percentage, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(false);
        ci.cancel();
    }
}
