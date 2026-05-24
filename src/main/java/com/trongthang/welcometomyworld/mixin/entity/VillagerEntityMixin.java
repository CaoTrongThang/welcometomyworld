package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.getWorld().isClient && this.age % 100 == 0) {
            if (!this.isPersistent()) {
                WelcomeToMyWorld.LOGGER.info("Forcing persistence for villager: " + this.getUuidAsString());
                this.setPersistent();
            }
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void onCheckDespawn(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            WelcomeToMyWorld.LOGGER.info("Villager " + this.getUuidAsString() + " removed. Reason: " + reason);
            // Log stack trace to see what called remove
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 15); i++) {
                WelcomeToMyWorld.LOGGER.info("  at " + stackTrace[i]);
            }
        }
    }

    @Inject(method = "discard", at = @At("HEAD"))
    private void onDiscard(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            WelcomeToMyWorld.LOGGER.info("Villager " + this.getUuidAsString() + " discarded.");
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 15); i++) {
                WelcomeToMyWorld.LOGGER.info("  at " + stackTrace[i]);
            }
        }
    }
}
