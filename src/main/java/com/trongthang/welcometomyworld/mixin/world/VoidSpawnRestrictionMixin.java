package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnRestriction.class)
public class VoidSpawnRestrictionMixin {

    @Inject(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z", at = @At("HEAD"), cancellable = true)
    private static void void_canSpawn(EntityType<?> type, ServerWorldAccess world, SpawnReason spawnReason,
            BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (spawnReason == SpawnReason.SPAWNER && world instanceof World w
                && w.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY) {
            cir.setReturnValue(true);
        }
    }
}
