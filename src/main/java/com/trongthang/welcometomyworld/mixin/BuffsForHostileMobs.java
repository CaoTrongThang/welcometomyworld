package com.trongthang.welcometomyworld.mixin;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HostileEntity.class)
public class BuffsForHostileMobs {

    private static final double POWER_UP_CHANCE = 0.17; // Chance for a mob to power up
    private static final int BUFF_DISTANCE = 64;  // Max radius to search for mobs (10 blocks around player)

    private static final int mobEffectDuration = 1200;

    private int checkInterval = 60; // Interval in ticks to check for mobs
    private int counter = 0; // Counter for checking

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {

        HostileEntity hostileEntity = (HostileEntity) (Object) this;

        if (hostileEntity.getTarget() instanceof PlayerEntity) {

        }
    }
}
