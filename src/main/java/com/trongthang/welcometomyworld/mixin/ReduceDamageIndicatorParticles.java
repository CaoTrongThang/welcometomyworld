package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerEntity.class)
public abstract class ReduceDamageIndicatorParticles {
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"
            )
    )
    private int reduceParticleCount(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count,
                                    double deltaX, double deltaY, double deltaZ, double speed) {

        int newCount = Math.min(count, 20);
        return world.spawnParticles(particle, x, y, z, newCount, deltaX, deltaY, deltaZ, speed);
    }
}
