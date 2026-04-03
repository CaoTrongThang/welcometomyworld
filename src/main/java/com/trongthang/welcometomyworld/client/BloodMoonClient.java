package com.trongthang.welcometomyworld.client;

/**
 * Client-side holder for blood moon state.
 * Updated by the BLOOD_MOON_SYNC packet received from the server.
 */
public class BloodMoonClient {
    public static boolean isBloodMoon = false;
    /**
     * Smoothly interpolated alpha (0-1) for the red screen overlay. Driven by
     * client tick.
     */
    public static float overlayAlpha = 0f;
}
