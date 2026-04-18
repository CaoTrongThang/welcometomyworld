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
        boolean isVoid = currentDim.toString().equals("welcometomyworld:void_dim");

        if (!useCurve && bloodAlpha <= 0f && !isVoid) {
            return;
        }

        // Dynamic darkness scaling:
        // We want pitch black at night (skyBrightness=0) or in caves (s=0),
        // but vanilla-like shadows during the day (skyBrightness=1, s=high).
        float skyBrightness = client.world.getDimension().hasSkyLight() ? client.world.getSkyBrightness(1.0f) : 0.0f;

        // Lessen the effect of the lightmap curve during the day
        float effectiveCurve = MathHelper.lerp(skyBrightness, cachedCurvePower, 1.15f);

        for (int b = 0; b < 16; b++) {
            for (int s = 0; s < 16; s++) {
                int color = this.image.getColor(b, s);

                int alpha = (color >> 24) & 0xFF;
                int blue = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int red = color & 0xFF;

                if (!(b == 15 && s == 15)) {
                    // Combine both sky light and block light for the power factor
                    float sPower = (s / 15.0f) * skyBrightness;
                    float bPower = (b / 15.0f);
                    float powerFactor = Math.max(sPower, bPower);

                    if (useCurve) {
                        float currentPower = MathHelper.lerp(powerFactor, effectiveCurve, 1.0f);

                        float luminance = (0.2126f * red + 0.7152f * green + 0.0722f * blue) / 255.0f;
                        if (currentPower != 1.0f && luminance > 0.001f) {
                            float newLuminance = (float) Math.pow(luminance, currentPower);

                            // Scale all channels equally to preserve original hue/saturation
                            float scale = newLuminance / luminance;
                            red = Math.min(255, Math.max(0, (int) (red * scale)));
                            green = Math.min(255, Math.max(0, (int) (green * scale)));
                            blue = Math.min(255, Math.max(0, (int) (blue * scale)));
                        }
                    }

                    // Handle Ambient Blue Tint and Force True Darkness:
                    // Vanilla Minecraft adds an ambient minimum brightness (often bluish) even at
                    // light level 0.
                    // When powerFactor is very low, we fade out that ambient light to get pure
                    // pitch black.
                    if (isVoid || useCurve) {
                        float crushEnd = isVoid ? 0.3f : 0.15f;

                        if (powerFactor < crushEnd) {
                            float lightRatio = powerFactor / crushEnd; // 0 at pure dark, 1 at edge of dark

                            // 1. Remove the blue tint by desaturating towards the minimum channel value
                            int minChannel = Math.min(red, Math.min(green, blue));

                            // Blend towards grayscale (minChannel) as it gets darker
                            float desatFactor = 1.0f - lightRatio; // 1.0 at pure dark, 0.0 at edge
                            red = (int) MathHelper.lerp(desatFactor, (float) red, (float) minChannel);
                            green = (int) MathHelper.lerp(desatFactor, (float) green, (float) minChannel);
                            blue = (int) MathHelper.lerp(desatFactor, (float) blue, (float) minChannel);

                            // 2. Crush brightness towards 0 for a true pitch-black darkness
                            float brightnessCrush = (float) Math.pow(lightRatio, isVoid ? 1.5f : 1.25f);
                            red = (int) (red * brightnessCrush);
                            green = (int) (green * brightnessCrush);
                            blue = (int) (blue * brightnessCrush);
                        }
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
