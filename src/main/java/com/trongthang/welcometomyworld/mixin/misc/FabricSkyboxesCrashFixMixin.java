package com.trongthang.welcometomyworld.mixin.misc;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.amerebagatelle.fabricskyboxes.skyboxes.AbstractSkybox", remap = false)
public class FabricSkyboxesCrashFixMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void onTick(net.minecraft.client.world.ClientWorld world, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || client.cameraEntity == null) {
            ci.cancel();
        }
    }
}
