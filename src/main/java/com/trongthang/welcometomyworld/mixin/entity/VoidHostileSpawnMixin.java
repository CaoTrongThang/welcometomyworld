package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class VoidHostileSpawnMixin {

    @Inject(method = "canSpawn(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;)Z", at = @At("HEAD"), cancellable = true)
    private void void_canSpawn(WorldAccess world, SpawnReason spawnReason, CallbackInfoReturnable<Boolean> cir) {
        if (spawnReason == SpawnReason.SPAWNER && world instanceof World w
                && w.getRegistryKey() == VoidDimension.VOID_DIM_LEVEL_KEY) {
            cir.setReturnValue(true);
        }
    }
}
