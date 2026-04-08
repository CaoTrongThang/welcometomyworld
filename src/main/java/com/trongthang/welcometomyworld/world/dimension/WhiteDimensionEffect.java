package com.trongthang.welcometomyworld.world.dimension;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class WhiteDimensionEffect extends DimensionEffects {
    public WhiteDimensionEffect() {
        // SkyType.NONE ensures no sun/moon/stars
        super(Float.NaN, false, DimensionEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        // Pure White Fog
        return new Vec3d(1.0, 1.0, 1.0);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        // Thick fog helps cover the sky area with the fog color
        return true;
    }

    @Override
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        return null;
    }
}
