package com.trongthang.welcometomyworld.mixin.client;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "getRiddenEntity", at = @At("RETURN"), cancellable = true)
    private void onGetRiddenEntity(CallbackInfoReturnable<net.minecraft.entity.LivingEntity> cir) {
        if (cir.getReturnValue() instanceof Unknown || cir.getReturnValue() instanceof PhantomEntity) {
            cir.setReturnValue(null); // Trick the HUD into drawing normal health/food bars instead of the mount bar
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message != null && message.getContent() instanceof TranslatableTextContent tc) {
            if (tc.getKey().equals("mount.onboard")) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && player.getVehicle() instanceof Unknown) {
                    ci.cancel(); // Hide 'Press SHIFT to dismount' text
                }
            }
        }
    }
}
