package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InGameHud.class, priority = 0)
public class MythsOfTheSeaCrashFixMixin {

    /**
     * Fixes NPE crashes caused by Myths of the Sea mod.
     * The mod injects into getRiddenEntity and renderMountHealth but assumes
     * `MinecraftClient.getInstance().player` is not null.
     * By injecting first (priority 0) and canceling the call when player is null,
     * we skip their injected crashy methods altogether.
     */
    @Inject(method = "getRiddenEntity", at = @At("HEAD"), cancellable = true)
    private void earlyReturnIfPlayerNull(CallbackInfoReturnable<LivingEntity> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !(client.getCameraEntity() instanceof PlayerEntity)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void earlyReturnRenderIfPlayerNull(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !(client.getCameraEntity() instanceof PlayerEntity)) {
            ci.cancel();
        }
    }
}
