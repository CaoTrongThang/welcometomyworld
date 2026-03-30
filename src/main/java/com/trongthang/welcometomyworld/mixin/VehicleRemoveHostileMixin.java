package com.trongthang.welcometomyworld.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class VehicleRemoveHostileMixin {
    int checkInterval = 40;
    int counter = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        counter++;
        if (counter < checkInterval)
            return;
        counter = 0;

        Entity entity = (Entity) (Object) this;

        if (entity instanceof HostileEntity mob) {
            if (mob.getWorld().isClient)
                return;
            if (mob.getVehicle() != null && mob.getTarget() != null && !(mob.getVehicle() instanceof LivingEntity)) {
                mob.stopRiding();
            }
        }

        if (entity instanceof TameableEntity tameable) {
            if (tameable.getWorld().isClient)
                return;
            if (tameable.getVehicle() != null && tameable.getTarget() != null
                    && !(tameable.getVehicle() instanceof LivingEntity)) {
                tameable.stopRiding();
            }
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void preventHostileMobsFromRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        // Only cancel for hostile mobs with a target and not a living vehicle
        if (entity instanceof HostileEntity hostileMob && hostileMob.getTarget() != null
                && !(vehicle instanceof LivingEntity)) {
            cir.setReturnValue(false);
        }

        if (entity instanceof TameableEntity tameable && tameable.getTarget() != null
                && !(vehicle instanceof LivingEntity)) {
            cir.setReturnValue(false);
        }
    }
}
