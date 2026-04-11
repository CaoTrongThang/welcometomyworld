package com.trongthang.welcometomyworld.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(value = MinecraftClient.class, priority = 10000)
public abstract class CancelLoadingScreenMixin {

    // cancle the "joining world" screen when going to the void
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen modifyScreen(Screen screen) {
        if (screen == null)
            return null;

        MinecraftClient client = (MinecraftClient) (Object) this;

        if (client.world != null) {
            // Robust check for transition screens using instanceof
            boolean isTransitionScreen = screen instanceof net.minecraft.client.gui.screen.DownloadingTerrainScreen
                    || screen instanceof net.minecraft.client.gui.screen.ProgressScreen
                    || screen instanceof net.minecraft.client.gui.screen.LevelLoadingScreen
                    || screen instanceof net.minecraft.client.gui.screen.MessageScreen;

            // Fallback for wrapped or modded screens
            if (!isTransitionScreen) {
                String screenName = screen.getClass().getSimpleName();
                isTransitionScreen = screenName.contains("Downloading") || screenName.contains("Progress")
                        || screenName.contains("Message") || screenName.contains("Loading");
            }

            if (isTransitionScreen) {
                // Scenario 1: The player is in the current world (e.g. Overworld) and is
                // physically falling out of the map.
                // This catches the 'ProgressScreen' that fires BEFORE the client world swaps!
                if (client.player != null && client.player.getY() < client.world.getBottomY() - 20) {
                    return null;
                }

                // Scenario 2: The client world has already swapped to the void_dim.
                // This catches 'DownloadingTerrainScreen' that fires AFTER the client world
                // swaps!
                String dimPath = client.world.getRegistryKey().getValue().getPath();
                if ("void_dim".equals(dimPath)) {
                    return null;
                }
            }
        }

        return screen;
    }
}
