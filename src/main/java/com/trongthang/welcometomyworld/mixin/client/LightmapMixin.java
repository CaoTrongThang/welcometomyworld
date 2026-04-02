package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.render.LightmapTextureManager;

import com.trongthang.welcometomyworld.ConfigLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LightmapTextureManager.class)
public class LightmapMixin {

    @Shadow
    private NativeImage image;

    @Unique
    private static final Map<Float, int[]> CURVE_CACHE = new HashMap<>();

    @Unique
    private Identifier cachedDimension = null;

    @Unique
    private float cachedCurvePower = 1.0f;

    @Unique
    private int[] getCurveTable(float curvePower) {
        if (!CURVE_CACHE.containsKey(curvePower)) {
            int[] table = new int[256];
            for (int i = 0; i < 256; i++) {
                float f = i / 255.0f;
                // Apply a steep power curve.
                // This forces dim light (night) to pure black, but keeps bright light
                // (day/torches) bright.
                f = (float) Math.pow(f, curvePower);
                table[i] = Math.min(255, Math.max(0, (int) (f * 255.0f)));
            }
            CURVE_CACHE.put(curvePower, table);
        }
        return CURVE_CACHE.get(curvePower);
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImageBackedTexture;upload()V"))
    private void rustStyleDarkness(float delta, CallbackInfo ci) {
        if (this.image == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        Identifier currentDim = client.world.getRegistryKey().getValue();
        if (!currentDim.equals(cachedDimension)) {
            cachedDimension = currentDim;
            cachedCurvePower = ConfigLoader.getInstance().darknessLevels.dimensions.getOrDefault(currentDim.toString(),
                    1.0f);
        }

        if (cachedCurvePower <= 1.001f && cachedCurvePower >= 0.999f) {
            return; // Skip if curve is 1.0 (vanilla)
        }

        int[] curveTable = getCurveTable(cachedCurvePower);

        for (int b = 0; b < 16; b++) {
            for (int s = 0; s < 16; s++) {
                int color = this.image.getColor(b, s);

                int alpha = (color >> 24) & 0xFF;
                int blue = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int red = color & 0xFF;

                red = curveTable[red];
                green = curveTable[green];
                blue = curveTable[blue];

                int newColor = (alpha << 24) | (blue << 16) | (green << 8) | red;
                this.image.setColor(b, s, newColor);
            }
        }
    }
}
