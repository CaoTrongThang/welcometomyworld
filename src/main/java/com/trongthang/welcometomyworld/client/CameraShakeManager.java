package com.trongthang.welcometomyworld.client;

public class CameraShakeManager {
    private static float shakeIntensity = 0.0f;
    private static float currentShakeIntensity = 0.0f;
    private static int totalTicks = 0;
    private static int currentTick = 0;

    public static void addShake(float intensity, int ticks) {
        if (currentShakeIntensity < intensity || currentTick >= totalTicks) {
            shakeIntensity = intensity;
            currentShakeIntensity = intensity;
            totalTicks = ticks;
            currentTick = 0;
        }
    }

    public static void tick() {
        if (currentTick < totalTicks) {
            currentTick++;
            // Fade out the shake intensity over time
            float progress = (float) currentTick / totalTicks;
            // Ease out quad
            currentShakeIntensity = shakeIntensity * (1.0f - (progress * progress));
        } else {
            currentShakeIntensity = 0.0f;
        }
    }

    public static float getPitchOffset() {
        if (currentShakeIntensity <= 0.0f)
            return 0.0f;
        return (float) ((Math.random() - 0.5) * 2.0 * currentShakeIntensity);
    }

    public static float getYawOffset() {
        if (currentShakeIntensity <= 0.0f)
            return 0.0f;
        return (float) ((Math.random() - 0.5) * 2.0 * currentShakeIntensity);
    }
}
