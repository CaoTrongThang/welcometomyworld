package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.mixin.accessor.MutantZombieAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.mutantmonsters.world.entity.mutant.MutantZombie")
public abstract class MutantZombieDeathMixin extends LivingEntity {

    protected MutantZombieDeathMixin(net.minecraft.entity.EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @Inject(method = "method_6078", at = @At("HEAD"), cancellable = true, remap = false)
    private void welcometomyworld_onDie(DamageSource cause, CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            ((MutantZombieAccessor) this).callSetLives(0);
            super.onDeath(cause);
            ci.cancel();
        }
    }

    @Inject(method = "method_6108", at = @At("HEAD"), cancellable = true, remap = false)
    private void welcometomyworld_onTickDeath(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            super.updatePostDeath();
            ci.cancel();
        }
    }
}
