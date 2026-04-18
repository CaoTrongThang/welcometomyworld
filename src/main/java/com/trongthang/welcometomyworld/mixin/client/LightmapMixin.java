package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.render.LightmapTextureManager;

import com.trongthang.welcometomyworld.client.BloodMoonClient;
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

import net.minecraft.util.math.MathHelper;

@Mixin(LightmapTextureManager.class)
public class LightmapMixin {

    @Shadow
    private NativeImage image;

    @Unique
    private Identifier cachedDimension = null;

    @Unique
    private float cachedCurvePower = 1.0f;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImageBackedTexture;upload()V"))
    private void updateLightmap(float delta, CallbackInfo ci) {
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

        boolean useCurve = cachedCurvePower > 1.001f || cachedCurvePower < 0.999f;
        float bloodAlpha = BloodMoonClient.overlayAlpha;

        if (!useCurve && bloodAlpha <= 0f) {
            return;
        }

        // Dynamic darkness scaling:
        // We want pitch black at night (skyBrightness=0) or in caves (s=0),
        // but vanilla-like shadows during the day (skyBrightness=1, s=high).
        float skyBrightness = client.world.getDimension().hasSkyLight() ? client.world.getSkyBrightness(1.0f) : 0.0f;
        int[][] powerTables = null;

        // Lessen the effect of the lightmap curve during the day
        float effectiveCurve = MathHelper.lerp(skyBrightness, cachedCurvePower, 1.15f);

        if (useCurve) {
            powerTables = new int[16][256];
            for (int s = 0; s < 16; s++) {
                // powerFactor 1.0 means sun is hitting this spot directly during the day.
                // powerFactor 0.0 means it's pitch black (night or deep cave).
                float powerFactor = (s / 15.0f) * skyBrightness;
                float currentPower = MathHelper.lerp(powerFactor, effectiveCurve, 1.0f);

                for (int i = 0; i < 256; i++) {
                    float f = i / 255.0f;
                    f = (float) Math.pow(f, currentPower);
                    powerTables[s][i] = Math.min(255, Math.max(0, (int) (f * 255.0f)));
                }
            }
        }

        for (int b = 0; b < 16; b++) {
            for (int s = 0; s < 16; s++) {
                int color = this.image.getColor(b, s);

                int alpha = (color >> 24) & 0xFF;
                int blue = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int red = color & 0xFF;

                if (useCurve && !(b == 15 && s == 15)) {
                    int[] table = powerTables[s];
                    red = table[red];
                    green = table[green];
                    blue = table[blue];
                }

                // Fix blue tint in Void dimension by desaturating dark colors
                if (currentDim.toString().equals("welcometomyworld:void_dim") && !(b == 15 && s == 15)) {
                    // If it's dark (low block light), force it to be more grayscale
                    // This prevents the "blue" ambient light from vanilla/dimension effects
                    if (b < 8) {
                        int min = Math.min(red, Math.min(green, blue));
                        // Shift towards the minimum value to kill the tint and darken it further
                        red = green = blue = min;
                    }
                }

                int newColor = (alpha << 24) | (blue << 16) | (green << 8) | red;

                // Blood moon red tint: shift channels toward red proportional to overlayAlpha
                // We skip tinting the (15, 15) pixel because it's used by most HUD/Font
                // rendering
                if (bloodAlpha > 0f && !(b == 15 && s == 15)) {
                    red = (newColor & 0xFF);
                    green = (newColor >> 8) & 0xFF;
                    blue = (newColor >> 16) & 0xFF;

                    red = Math.max(0, red - (int) (10 * bloodAlpha));
                    green = Math.max(0, green - (int) (40 * bloodAlpha));
                    blue = Math.max(0, blue - (int) (40 * bloodAlpha));
                    newColor = (alpha << 24) | (blue << 16) | (green << 8) | red;
                }

                this.image.setColor(b, s, newColor);
            }
        }

        // Final override for (15, 15) to guarantee pure white emissives
        // Using 0xFFFFFFFF (Full Alpha, Full White) assuming NativeImage format is ARGB
        // or ABGR.
        // Minecraft's LightmapTextureManager usually uses 0xFFFFFFFF for max light.
        this.image.setColor(15, 15, 0xFFFFFFFF);
    }
}
