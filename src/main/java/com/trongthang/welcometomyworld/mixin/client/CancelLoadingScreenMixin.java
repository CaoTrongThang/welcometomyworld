package com.trongthang.welcometomyworld.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class CancelLoadingScreenMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen modifyScreen(Screen screen) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (screen != null && client.world != null) {
            String screenName = screen.getClass().getSimpleName();
            boolean isTransitionScreen = screenName.contains("Downloading") || screenName.contains("Progress")
                    || screenName.contains("Message");

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
