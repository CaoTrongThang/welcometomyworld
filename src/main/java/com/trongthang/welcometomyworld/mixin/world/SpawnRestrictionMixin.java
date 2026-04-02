package com.trongthang.welcometomyworld.mixin.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class SpawnRestrictionMixin {

    // Chặn mobs khi spawn thông thường (từ lệnh, spawner, sinh sản tự nhiên)
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void removeDisabledEntitiesOnSpawn(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (entity instanceof LivingEntity) {
            String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            if (com.trongthang.welcometomyworld.Utilities.Utils.matchesPattern(entityId,
                    com.trongthang.welcometomyworld.ConfigLoader.getInstance().disabledMobs)) {
                entity.discard();
                ci.setReturnValue(false);
            }
        }
    }
}
