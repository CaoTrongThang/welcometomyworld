package com.trongthang.welcometomyworld.world.dimension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VoidDimensionEffect extends DimensionEffects {
    public VoidDimensionEffect() {
        // CHANGED: SkyType.END is now SkyType.NONE
        super(Float.NaN, false, DimensionEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity == null)
            return Vec3d.ZERO;

        double currentY = client.cameraEntity.getY();
        double bottomY = -64.0;
        double midY = 128.0;
        double topY = 700.0;

        // Base colors
        Vec3d topColor = new Vec3d(0.0, 0.0, 0.0); // Black (matching Overworld Void)
        Vec3d midColor = new Vec3d(0.8, 0.0, 0.0); // Red
        Vec3d bottomColor = new Vec3d(0.4, 0.0, 0.6); // Purple

        double r, g, b;

        if (currentY >= midY) {
            // Lerp from Black (topY) to Red (midY)
            double percentage = MathHelper.clamp((currentY - midY) / (topY - midY), 0.0, 1.0);
            r = MathHelper.lerp(percentage, midColor.x, topColor.x);
            g = MathHelper.lerp(percentage, midColor.y, topColor.y);
            b = MathHelper.lerp(percentage, midColor.z, topColor.z);
        } else {
            // Lerp from Purple (bottomY) to Red (midY)
            double percentage = MathHelper.clamp((currentY - bottomY) / (midY - bottomY), 0.0, 1.0);
            r = MathHelper.lerp(percentage, bottomColor.x, midColor.x);
            g = MathHelper.lerp(percentage, bottomColor.y, midColor.y);
            b = MathHelper.lerp(percentage, bottomColor.z, midColor.z);
        }

        // MULTIPLY BY 0.15 (15% intensity)
        // This makes the color very dark and subtle, simulating a faint "opacity"
        return new Vec3d(r, g, b).multiply(0.15);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        // CHANGED: Return true! This makes the fog thick and covers the sky natively
        // (like the Nether)
        return false;
    }

    @Override
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        return null;
    }
}