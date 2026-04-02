package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Reduce the total DAMAGE_INDICATOR particles to reduce lags
@Mixin(PlayerEntity.class)
public abstract class ReduceDamageIndicatorParticles {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
    private int reduceParticleCount(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count,
            double deltaX, double deltaY, double deltaZ, double speed) {

        int newCount = Math.min(count, 10);
        return world.spawnParticles(particle, x, y, z, newCount, deltaX, deltaY, deltaZ, speed);
    }
}
