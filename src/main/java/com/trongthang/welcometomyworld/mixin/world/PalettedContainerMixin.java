package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.Utilities.ChunkCrashTracker;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.PalettedContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PalettedContainer.class)
public class PalettedContainerMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("WtmW/PalettedContainerMixin");

    // Track recursion depth per instance via a field
    @Unique
    private int wtmw_resizeDepth = 0;

    @Inject(method = "onResize", at = @At("HEAD"), cancellable = true)
    private void preventInfiniteResize(int newBits, Object object, CallbackInfoReturnable<Integer> cir) {
        wtmw_resizeDepth++;

        // Log every call so we can see what entry is triggering the loop
        LOGGER.debug("onResize depth={} bits={} entry={}", wtmw_resizeDepth, newBits, object);

        if (wtmw_resizeDepth > 20) {
            ChunkPos badChunk = ChunkCrashTracker.CURRENT_LOADING_CHUNK.get();
            String chunkInfo = (badChunk != null)
                    ? "x=" + badChunk.x + " z=" + badChunk.z
                    : "UNKNOWN (chunk coords not captured)";

            LOGGER.error("==================WELCOMETOMYWORLD, FIXING CRASH=========================");
            LOGGER.error("CRITICAL: Prevented PalettedContainer StackOverflow!");
            LOGGER.error("Corrupted chunk at: {}", chunkInfo);
            // 'object' is the block state or biome that could not be added to the palette.
            // Its toString() will show the mod ID / registry name, e.g.
            // "mythsofthesea:block[...]"
            // A raw integer (e.g. "0" or some number) means it's an orphaned/shifted ID
            // with no mod owner.
            LOGGER.error("Offending palette entry (block state or biome): {}", object);
            LOGGER.error("If the entry shows a mod ID, that mod caused the corruption.");
            LOGGER.error("If it shows a plain integer, a mod was removed or IDs shifted.");
            LOGGER.error("Delete region file for this chunk to permanently fix the world.");
            LOGGER.error("======================================================");

            wtmw_resizeDepth = 0;
            // Return 0 bits — the affected 16x16x16 section becomes air/default.
            // The server survives and the chunk coords are logged so you can delete the
            // chunk.
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "onResize", at = @At("RETURN"))
    private void decrementResizeDepth(int newBits, Object object, CallbackInfoReturnable<Integer> cir) {
        if (wtmw_resizeDepth > 0) {
            wtmw_resizeDepth--;
        }
    }
}
