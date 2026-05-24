package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;

// trying to debug the bad mod that somehow plays there music as hostile
@Mixin(SoundSystem.class)
public class DebugSoundMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        if (sound == null)
            return;
        SoundCategory category = sound.getCategory();
        if (category == SoundCategory.MUSIC || category == SoundCategory.HOSTILE || category == SoundCategory.RECORDS) {
            WelcomeToMyWorld.LOGGER.info("[DebugMusic] ID: " + sound.getId() + " | Category: " + category
                    + " | Volume: " + sound.getVolume());
        }
    }
}
