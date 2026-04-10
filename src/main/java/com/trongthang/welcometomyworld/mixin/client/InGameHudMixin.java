package com.trongthang.welcometomyworld.mixin.client;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;

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

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "getRiddenEntity", at = @At("RETURN"), cancellable = true)
    private void onGetRiddenEntity(CallbackInfoReturnable<net.minecraft.entity.LivingEntity> cir) {
        if (cir.getReturnValue() instanceof Unknown || cir.getReturnValue() instanceof PhantomEntity
                || cir.getReturnValue() instanceof VoidWormEntity) {
            cir.setReturnValue(null); // Trick the HUD into drawing normal health/food bars instead of the mount bar
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        if (message != null && message.getContent() instanceof TranslatableTextContent tc) {
            if (tc.getKey().equals("mount.onboard")) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && (player.getVehicle() instanceof Unknown
                        || player.getVehicle() instanceof PhantomEntity
                        || player.getVehicle() instanceof VoidWormEntity)) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderVoidSightGlitch(net.minecraft.client.gui.DrawContext context, float tickDelta,
            CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player != null
                && player.hasStatusEffect(com.trongthang.welcometomyworld.managers.EffectsManager.VOID_SIGHT)) {
            if (client.world.getRegistryKey().getValue().toString().equals("welcometomyworld:void_dim")) {
                net.minecraft.util.math.random.Random random = player.getRandom();

                // Generate low glitch bands procedurally
                if (random.nextFloat() > 0.90f) { // 10% chance per frame for glitch
                    int width = context.getScaledWindowWidth();
                    int height = context.getScaledWindowHeight();

                    int bands = random.nextInt(3) + 1; // 1 to 3 bands
                    for (int i = 0; i < bands; i++) {
                        int y = random.nextInt(height);
                        int h = random.nextInt(8) + 2;
                        int x = random.nextInt(width);
                        int w = random.nextInt(width / 3) + 20;

                        // Procedural distinct glitch colors: Cyan, Magenta, White with low opacity
                        int color;
                        int choice = random.nextInt(3);
                        if (choice == 0)
                            color = 0x1A00FFFF; // Cyan
                        else if (choice == 1)
                            color = 0x1AFF00FF; // Magenta
                        else
                            color = 0x1AFFFFFF; // White

                        context.fill(x, y, x + w, y + h, color);
                    }
                }
            }
        }
    }
}
