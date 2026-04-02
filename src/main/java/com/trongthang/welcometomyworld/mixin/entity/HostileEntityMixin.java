package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    private static final double POWER_UP_CHANCE = 0.18;
    private static final int mobEffectDuration = 200;

    private int checkInterval = 80;
    private int counter = 0;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        counter++;
        if (counter < checkInterval)
            return;
        counter = 0;

        HostileEntity hostileEntity = (HostileEntity) (Object) this;

        if (hostileEntity.getWorld().isDay())
            return;

        if (hostileEntity.getTarget() instanceof PlayerEntity) {
            if (random.nextDouble() < POWER_UP_CHANCE) {
                MinecraftServer server = hostileEntity.getServer();
                if (server == null)
                    return;
                ServerWorld world = server.getOverworld();

                Utils.summonLightning(hostileEntity.getBlockPos(), world, true);
                Utils.spawnParticles(world, hostileEntity.getBlockPos(), ParticleTypes.FLAME);
                Utils.applyEffectForMobs(hostileEntity, 3, mobEffectDuration);
            }
        }
    }
}
