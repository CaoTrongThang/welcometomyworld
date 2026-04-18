package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InGameHud.class, priority = 500)
public class MythsOfTheSeaCrashFixMixin {

    /**
     * Fixes an NPE crash caused by Myths of the Sea mod.
     * The mod injects into getRiddenEntity but assumes
     * `MinecraftClient.getInstance().player` is not null.
     * By injecting first (priority 500) and canceling the call when player is null,
     * we skip their injected crashy method altogether.
     */
    @Inject(method = "getRiddenEntity", at = @At("HEAD"), cancellable = true)
    private void earlyReturnIfPlayerNull(CallbackInfoReturnable<LivingEntity> cir) {
        if (MinecraftClient.getInstance().player == null) {
            cir.setReturnValue(null);
        }
    }
}
