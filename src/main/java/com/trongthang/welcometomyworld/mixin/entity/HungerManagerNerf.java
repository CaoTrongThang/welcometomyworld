package com.trongthang.welcometomyworld.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.player.HungerManager;

@Mixin(HungerManager.class)
public class HungerManagerNerf {
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 0))
    private float nerfOverflowHealing(float original) {
        return original * 0.5f;
    }
}
