package com.trongthang.welcometomyworld.mixin.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.sculk_worm.worm.additions.WormListener", remap = false)
public class SculkWormHangFixMixin {

    /**
     * @author Antigravity
     * @reason Prevent Sculk Worm mod from causing server hangs by loading chunks
     *         synchronously during vibration occlusion checks.
     */
    @Inject(method = "isOccluded", at = @At("HEAD"), cancellable = true, require = 0)
    private void onIsOccluded(World world, BlockPos p1, BlockPos p2, CallbackInfoReturnable<Boolean> cir) {
        if (world == null || p1 == null || p2 == null)
            return;

        // Check if the chunks for both positions are loaded.
        // If either is not loaded, we return true (occluded) to avoid synchronous chunk
        // loading/hangs.
        if (!world.isChunkLoaded(p1.getX() >> 4, p1.getZ() >> 4)
                || !world.isChunkLoaded(p2.getX() >> 4, p2.getZ() >> 4)) {
            cir.setReturnValue(true);
        }
    }
}
