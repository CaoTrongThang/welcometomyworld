package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobSpawnerLogic.class)
public class VoidSpawnerLightMixin {

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/SpawnRestriction;canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z"))
    private boolean redirectSpawnRestriction(EntityType<?> type, ServerWorldAccess world, SpawnReason spawnReason,
            BlockPos pos, Random random) {
        if (world instanceof ServerWorld sw && sw.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY) {
            return true;
        }
        return SpawnRestriction.canSpawn(type, world, spawnReason, pos, random);
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;canSpawn(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;)Z"))
    private boolean redirectMobEntityCanSpawnReason(MobEntity instance, WorldAccess world, SpawnReason spawnReason) {
        if (world instanceof ServerWorld sw && sw.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY) {
            return true;
        }
        return instance.canSpawn(world, spawnReason);
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;canSpawn(Lnet/minecraft/world/WorldView;)Z"))
    private boolean redirectMobEntityCanSpawn(MobEntity instance, net.minecraft.world.WorldView world) {
        if (world instanceof ServerWorld sw && sw.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY) {
            return true;
        }
        return instance.canSpawn(world);
    }
}
