package com.trongthang.welcometomyworld.client;

/**
 * Manages the "heaven transition" white-screen overlay.
 *
 * Phase 1 (FADE_IN_TICKS): alpha 0 -> 1 (player still sees death water)
 * Phase 2 (HOLD_TICKS): alpha stays at 1 (server teleports during this window)
 * Phase 3 (FADE_OUT_TICKS): alpha 1 -> 0 (player has already arrived in White
 * Dim)
 */
public final class HeavenTransitionManager {

    private static final int FADE_IN_TICKS = 100; // 5 s fade to white
    private static final int HOLD_TICKS = 40; // 2 s full white (teleport happens here)
    private static final int FADE_OUT_TICKS = 100; // 5 s fade back in
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    private static int tick = TOTAL_TICKS; // start "done" so nothing renders initially
    private static float alpha = 0f;

    private HeavenTransitionManager() {
    }

    /** Called by the server packet — starts the animation from the beginning. */
    public static void start() {
        tick = 0;
        alpha = 0f;
    }

    /** Called every client tick (from WelcomeToMyWorldClient). */
    public static void clientTick() {
        if (tick >= TOTAL_TICKS) {
            alpha = 0f;
            return;
        }

        tick++;

        if (tick <= FADE_IN_TICKS) {
            alpha = (float) tick / FADE_IN_TICKS;
        } else if (tick <= FADE_IN_TICKS + HOLD_TICKS) {
            alpha = 1f;
        } else {
            int fadeOutProgress = tick - FADE_IN_TICKS - HOLD_TICKS;
            alpha = 1f - (float) fadeOutProgress / FADE_OUT_TICKS;
        }
    }

    /** Returns the current overlay alpha (0 = transparent, 1 = full white). */
    public static float getAlpha() {
        return alpha;
    }

    /** True while the animation is running. */
    public static boolean isActive() {
        return tick < TOTAL_TICKS;
    }
}
