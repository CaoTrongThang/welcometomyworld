package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderWorld", at = @At("HEAD"), cancellable = true)
    private void welcometomyworld_onRenderWorldHead(float tickDelta, long limitTime, MatrixStack matrixStack,
            CallbackInfo ci) {
        if (this.client.player == null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = At.Shift.AFTER))
    private void welcometomyworld_applyCameraShake(float tickDelta, long limitTime, MatrixStack matrices,
            CallbackInfo ci) {
        com.trongthang.welcometomyworld.client.CameraShakeManager.applyShake(matrices, tickDelta);
    }
}
