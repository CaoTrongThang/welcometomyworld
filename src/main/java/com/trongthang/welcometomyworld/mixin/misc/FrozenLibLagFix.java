package com.trongthang.welcometomyworld.mixin.misc;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Target vanilla's WorldRenderer (which is LevelRenderer in Mojang mappings).
// Priority 1500 ensures this runs AFTER FrozenLib adds its method.
@Mixin(value = WorldRenderer.class, priority = 1500)
public class FrozenLibLagFix {

    /**
     * remap = false: Because frozenLib$renderEntityIcon is a modded method, not
     * vanilla.
     * require = 0: THIS IS VERY IMPORTANT. It means if you uninstall FrozenLib in
     * the future,
     * your game will NOT crash. The Mixin will just silently ignore this injection.
     */
    @Inject(method = "frozenLib$renderEntityIcon", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void fixFrozenLibLag(
            Entity entity,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            MatrixStack poseStack,
            VertexConsumerProvider bufferSource,
            CallbackInfo ci) {
        // Cancel the method entirely.
        // This stops the icon from rendering AND stops the laggy getLight()
        // calculation.
        ci.cancel();
    }
}