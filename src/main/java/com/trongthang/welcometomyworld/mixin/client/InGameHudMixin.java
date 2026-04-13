package com.trongthang.welcometomyworld.mixin.client;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import com.trongthang.welcometomyworld.interfaces.IScaleEntity;
import com.trongthang.welcometomyworld.managers.EffectsManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

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
    public void onRenderGlitchEffects(net.minecraft.client.gui.DrawContext context, float tickDelta,
            CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null)
            return;

        boolean isVoidSightGlitch = player
                .hasStatusEffect(EffectsManager.VOID_SIGHT)
                && client.world.getRegistryKey().getValue().toString().equals("welcometomyworld:void_dim");

        boolean isIntroScaleGlitch = false;
        if (player instanceof IScaleEntity scaleEntity) {
            if (scaleEntity.getScale() < 1f
                    && client.world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)) {
                isIntroScaleGlitch = true;
            }
        }

        if (isVoidSightGlitch || isIntroScaleGlitch) {
            net.minecraft.util.math.random.Random random = player.getRandom();

            float glitchChance = isIntroScaleGlitch ? 0.70f : 0.90f; // 30% chance per frame for intro, 10% for void
                                                                     // sight

            if (random.nextFloat() > glitchChance) {
                int width = context.getScaledWindowWidth();
                int height = context.getScaledWindowHeight();

                int maxBands = isIntroScaleGlitch ? 10 : 3;
                int bands = random.nextInt(maxBands) + 1;

                for (int i = 0; i < bands; i++) {
                    int y = random.nextInt(height);
                    int h = isIntroScaleGlitch ? random.nextInt(20) + 5 : random.nextInt(8) + 2;
                    int x = random.nextInt(width);
                    int w = isIntroScaleGlitch ? random.nextInt(width / 2) + 50 : random.nextInt(width / 3) + 20;

                    // Colors with variable opacity
                    int alpha = isIntroScaleGlitch ? 0x4D : 0x1A; // ~30% alpha vs ~10% alpha
                    int color;
                    int choice = random.nextInt(3);
                    if (choice == 0)
                        color = (alpha << 24) | 0x00FFFF; // Cyan
                    else if (choice == 1)
                        color = (alpha << 24) | 0xFF00FF; // Magenta
                    else
                        color = (alpha << 24) | 0xFFFFFF; // White

                    context.fill(x, y, x + w, y + h, color);
                }
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderHeavenTransition(net.minecraft.client.gui.DrawContext context, float tickDelta,
            CallbackInfo ci) {
        float alpha = com.trongthang.welcometomyworld.client.HeavenTransitionManager.getAlpha();
        if (alpha <= 0f)
            return;

        int a = (int) (alpha * 255);
        int color = (a << 24) | 0xFFFFFF; // white with variable alpha
        context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), color);
    }
}
