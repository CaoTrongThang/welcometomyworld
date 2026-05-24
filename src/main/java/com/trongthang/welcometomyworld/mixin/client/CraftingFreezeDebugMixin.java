package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Debug watchdog: prints a full render-thread stack trace if setScreen
 * blocks for more than 4 seconds. Remove once the freeze is identified.
 */
@Mixin(MinecraftClient.class)
public class CraftingFreezeDebugMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("WelcomeToMyWorld-CraftingDebug");

    private static volatile long lastSetScreenStart = 0;
    private static volatile boolean insideSetScreen = false;
    private static volatile Thread watchedThread = null;
    private static volatile String currentScreenClass = "none";

    static {
        Thread watchdog = new Thread(() -> {
            LOGGER.info("[CraftingDebug] Watchdog thread started. Monitoring setScreen.");
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (insideSetScreen && System.currentTimeMillis() - lastSetScreenStart > 4000) {
                        LOGGER.error("[CraftingDebug] WATCHDOG DETECTED FREEZE inside setScreen! Screen: "
                                + currentScreenClass);
                        if (watchedThread != null) {
                            StackTraceElement[] trace = watchedThread.getStackTrace();
                            for (StackTraceElement element : trace) {
                                LOGGER.error("    at " + element.toString());
                            }
                        }
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "CraftingFreezeWatchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void logBeforeSetScreen(Screen screen, CallbackInfo ci) {
        watchedThread = Thread.currentThread();
        currentScreenClass = screen == null ? "null" : screen.getClass().getName();
        LOGGER.info("[CraftingDebug] setScreen BEFORE -> " + currentScreenClass);
        lastSetScreenStart = System.currentTimeMillis();
        insideSetScreen = true;
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void logAfterSetScreen(Screen screen, CallbackInfo ci) {
        insideSetScreen = false;
        long elapsed = System.currentTimeMillis() - lastSetScreenStart;
        LOGGER.info("[CraftingDebug] setScreen AFTER  -> " + currentScreenClass + " (" + elapsed + "ms)");
    }
}
