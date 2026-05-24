package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;

@Mixin(SoundManager.class)
public class MusicOverlapDebugMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void welcometomyworld$debugSoundPlaying(SoundInstance sound, CallbackInfo ci) {
        if (sound == null)
            return;
        try {
            if (sound.getCategory() == SoundCategory.HOSTILE) {
                WelcomeToMyWorld.LOGGER.info("====================================");
                WelcomeToMyWorld.LOGGER.info(
                        "HOSTILE sound detected: " + (sound.getId() != null ? sound.getId().toString() : "null id"));
                WelcomeToMyWorld.LOGGER.info("Volume: " + sound.getVolume());
                WelcomeToMyWorld.LOGGER.info("Looping: " + sound.isRepeatable());
                WelcomeToMyWorld.LOGGER.info("Stacktrace:");
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    if (element.toString().contains("com.trongthang.welcometomyworld"))
                        continue;
                    WelcomeToMyWorld.LOGGER.info("  " + element.toString());
                }
                WelcomeToMyWorld.LOGGER.info("====================================");
            }
        } catch (Exception e) {
            // Silently ignore crashes from malformed SoundInstance objects (e.g. from
            // PresenceFootsteps)
        }
    }
}
