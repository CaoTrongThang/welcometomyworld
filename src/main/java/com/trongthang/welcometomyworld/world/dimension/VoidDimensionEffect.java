package com.trongthang.welcometomyworld.world.dimension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VoidDimensionEffect extends DimensionEffects {
    private float sculkTransition = 0.0f;
    private long lastTime = -1; // Initialize to -1 to detect first frame

    public VoidDimensionEffect() {
        // CHANGED: SkyType.END is now SkyType.NONE
        super(Float.NaN, false, DimensionEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity == null)
            return Vec3d.ZERO;

        long currentTime = Util.getMeasuringTimeMs();

        // Handle first frame or long gaps (dimension teleport)
        if (lastTime == -1 || currentTime - lastTime > 1000) {
            lastTime = currentTime;
            // Initialize sculkTransition based on current state to prevent flashing
            sculkTransition = checkSculkBiome(client) ? 1.0f : 0.0f;
        }

        float deltaSecs = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        // Cap deltaSecs for stability
        if (deltaSecs > 0.1f)
            deltaSecs = 0.1f;

        boolean isSculkBiome = checkSculkBiome(client);

        if (isSculkBiome) {
            sculkTransition += deltaSecs * 0.5f; // 2 seconds to transition in
        } else {
            sculkTransition -= deltaSecs * 0.5f; // 2 seconds to transition out
        }
        sculkTransition = MathHelper.clamp(sculkTransition, 0.0f, 1.0f);

        double currentY = client.cameraEntity.getY();
        double midY = 128.0;
        double topY = 700.0;
        double bottomY = -64.0;

        // Base red dimension colors
        Vec3d topColor = new Vec3d(0.0, 0.0, 0.0); // Black at Y=700
        Vec3d midColor = new Vec3d(0.9, 0.0, 0.0); // Pure Red at Y=128
        Vec3d bottomColor = new Vec3d(0.6, 0.0, 0.4); // Red Purple at bottom

        double r, g, b;

        if (currentY >= midY) {
            double percentage = MathHelper.clamp((currentY - midY) / (topY - midY), 0.0, 1.0);
            r = MathHelper.lerp(percentage, midColor.x, topColor.x);
            g = MathHelper.lerp(percentage, midColor.y, topColor.y);
            b = MathHelper.lerp(percentage, midColor.z, topColor.z);
        } else {
            double percentage = MathHelper.clamp((currentY - bottomY) / (midY - bottomY), 0.0, 1.0);
            r = MathHelper.lerp(percentage, bottomColor.x, midColor.x);
            g = MathHelper.lerp(percentage, bottomColor.y, midColor.y);
            b = MathHelper.lerp(percentage, bottomColor.z, midColor.z);
        }

        Vec3d redGradientColor = new Vec3d(r, g, b).multiply(0.25);
        Vec3d sculkColor = new Vec3d(2.0 / 255.0, 64.0 / 255.0, 80.0 / 255.0);

        return new Vec3d(
                MathHelper.lerp(sculkTransition, redGradientColor.x, sculkColor.x),
                MathHelper.lerp(sculkTransition, redGradientColor.y, sculkColor.y),
                MathHelper.lerp(sculkTransition, redGradientColor.z, sculkColor.z));
    }

    /**
     * Helper to check if we are in the sculk biome AND within valid height.
     */
    private boolean checkSculkBiome(MinecraftClient client) {
        if (client.world == null || client.cameraEntity == null)
            return false;

        double currentY = client.cameraEntity.getY();
        // The sculk biome shouldn't override the high altitude black fog (Y=700 portal
        // exit)
        if (currentY >= 200.0)
            return false;

        var biomeKey = client.world.getBiome(client.cameraEntity.getBlockPos()).getKey();
        return biomeKey.isPresent() && biomeKey.get().getValue().getPath().equals("void_sculk_biome");
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }

    @Override
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        return null;
    }
}