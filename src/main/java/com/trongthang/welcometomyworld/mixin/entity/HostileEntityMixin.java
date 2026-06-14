package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
                ServerWorld world = (ServerWorld) hostileEntity.getWorld();

                world.playSound(null, hostileEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE,
                        SoundCategory.HOSTILE, 0.5f, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                world.spawnParticles(ParticleTypes.FLAME,
                        hostileEntity.getX(),
                        hostileEntity.getY() + hostileEntity.getHeight() / 2.0,
                        hostileEntity.getZ(),
                        5, 0.5, 0.6, 0.5, 0.1);

                Utils.applyEffectForMobs(hostileEntity, 3, mobEffectDuration);
            }
        }
    }
}
