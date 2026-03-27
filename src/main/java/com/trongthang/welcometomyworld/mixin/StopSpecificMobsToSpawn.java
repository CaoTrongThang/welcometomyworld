package com.trongthang.welcometomyworld.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Mixin(ServerWorld.class)
public abstract class StopSpecificMobsToSpawn {

    // Chặn mobs khi spawn thông thường (từ lệnh, spawner, sinh sản tự nhiên)
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void removeDisabledEntitiesOnSpawn(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (entity instanceof LivingEntity) {
            String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            if (com.trongthang.welcometomyworld.events.SpawnEvents.DISABLED_MOBS.contains(entityId)) {
                entity.discard();
                ci.setReturnValue(false);
            }
        }
    }
}
