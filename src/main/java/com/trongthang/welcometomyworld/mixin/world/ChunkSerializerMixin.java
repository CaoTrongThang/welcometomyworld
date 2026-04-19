package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.Utilities.ChunkCrashTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "deserialize", at = @At("HEAD"))
    private static void trackChunkLoad(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos chunkPos,
            NbtCompound nbt, CallbackInfoReturnable<Chunk> cir) {
        ChunkCrashTracker.CURRENT_LOADING_CHUNK.set(chunkPos);
    }
}
