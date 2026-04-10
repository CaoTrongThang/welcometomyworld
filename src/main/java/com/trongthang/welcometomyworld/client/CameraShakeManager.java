package com.trongthang.welcometomyworld.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class CameraShakeManager {
    private static float currentIntensity = 0;
    private static int ticksRemaining = 0;
    private static int totalDuration = 0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.isPaused())
                return;
            tick();
        });
    }

    public static void addShake(float intensity, int duration) {
        // Only override if the new shake is stronger or the old one is almost finished
        if (intensity >= currentIntensity || ticksRemaining <= totalDuration * 0.1) {
            currentIntensity = intensity;
            ticksRemaining = duration;
            totalDuration = duration;
        }
    }

    private static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        } else {
            currentIntensity = 0;
        }
    }

    public static float getIntensity(float tickDelta) {
        if (ticksRemaining <= 0)
            return 0;

        float progress = (ticksRemaining - tickDelta) / (float) totalDuration;
        // Optional: cubic easing for smoother fade out
        // progress = progress * progress * (3 - 2 * progress);

        return currentIntensity * MathHelper.clamp(progress, 0, 1);
    }

    public static void applyShake(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        float intensity = getIntensity(tickDelta);
        if (intensity <= 0)
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null)
            return;

        float time = (client.world.getTime() + tickDelta);

        // Use multiple sine waves for more "natural" feeling shake
        float yaw = (MathHelper.sin(time * 1.5f) * 0.5f + MathHelper.sin(time * 2.1f) * 0.3f) * intensity;
        float pitch = (MathHelper.cos(time * 1.7f) * 0.5f + MathHelper.cos(time * 1.3f) * 0.3f) * intensity;
        float roll = (MathHelper.sin(time * 0.9f) * 0.5f) * intensity * 0.5f;

        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(roll));
    }
}
