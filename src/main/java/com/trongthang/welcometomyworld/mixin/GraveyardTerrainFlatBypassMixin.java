package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bypasses TGJigsawStructure.isTerrainFlat() from The Graveyard mod.
 *
 * Root cause: isTerrainFlat() makes expensive noise/density sampling calls
 * (NoiseChunkGenerator.getColumnSample, ChunkNoiseSampler.sampleDensity) on
 * chunk builder worker threads. When a player teleports via Waystone into an
 * ungenerated chunk, these calls cascade and deadlock the main server thread,
 * triggering the Watchdog crash.
 *
 * Fix: Always return true so the structure skips the terrain check entirely.
 * Graveyard structures will still generate, just without the flatness guard.
 *
 * @Pseudo tells Mixin the target class doesn't need to be on the compile
 *         classpath — it resolves at runtime when The Graveyard mod is loaded.
 */
@Pseudo
@Mixin(targets = "com.lion.graveyard.world.structures.TGJigsawStructure", remap = false)
public class GraveyardTerrainFlatBypassMixin {

    // Added "static" to the method signature to match the target method
    @Inject(method = "isTerrainFlat", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void bypassIsTerrainFlat(CallbackInfoReturnable<Boolean> cir) {
        WelcomeToMyWorld.LOGGER.debug("[GraveyardBypass] isTerrainFlat bypassed to prevent Watchdog deadlock");
        cir.setReturnValue(true);
    }
}