package com.trongthang.welcometomyworld.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.trongthang.welcometomyworld.client.BloodMoonClient;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererFogMixin {
    @Shadow
    private static float red;
    @Shadow
    private static float green;
    @Shadow
    private static float blue;

    @Inject(method = "render", at = @At("RETURN"))
    private static void onRender(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness,
            CallbackInfo ci) {
        if (BloodMoonClient.overlayAlpha > 0.01f) {
            float alpha = BloodMoonClient.overlayAlpha;
            // Target dark red color for fog (#5A0000 -> r=0.353f, g=0, b=0)
            red = MathHelper.lerp(alpha, red, 0.353f);
            green = MathHelper.lerp(alpha, green, 0.00f);
            blue = MathHelper.lerp(alpha, blue, 0.00f);

            RenderSystem.clearColor(red, green, blue, 0.0f);
        }
    }

    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance,
            boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (BloodMoonClient.overlayAlpha > 0.01f) {
            float alpha = BloodMoonClient.overlayAlpha;

            float defaultStart = RenderSystem.getShaderFogStart();
            float defaultEnd = RenderSystem.getShaderFogEnd();

            // Make fog dense and gradual so it doesn't look like a wall
            float targetStart = -8.0f;
            float targetEnd = defaultEnd * 0.5f;

            RenderSystem.setShaderFogStart(MathHelper.lerp(alpha, defaultStart, targetStart));
            RenderSystem.setShaderFogEnd(MathHelper.lerp(alpha, defaultEnd, targetEnd));
            RenderSystem.setShaderFogShape(net.minecraft.client.render.FogShape.CYLINDER);
        }
    }
}
