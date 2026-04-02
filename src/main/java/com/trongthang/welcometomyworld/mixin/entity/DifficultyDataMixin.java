package com.trongthang.welcometomyworld.mixin.entity;

import io.github.flemmli97.improvedmobs.difficulty.DifficultyData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = DifficultyData.class, remap = false)
public abstract class DifficultyDataMixin {

    @Shadow
    public abstract void setPaused(boolean paused);

    @Inject(method = "increaseDifficultyBy", at = @At("HEAD"))
    private void pauseWhenNoPlayers(Function<Float, Float> increase, long time, MinecraftServer server,
            CallbackInfo ci) {

        boolean noPlayers = server.getPlayerManager().getPlayerList().isEmpty();
        this.setPaused(noPlayers);
    }
}
