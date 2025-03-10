package com.trongthang.welcometomyworld.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class VehicleRemoveHostileMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        if((Entity) (Object) this instanceof HostileEntity mob){
            if(mob.getWorld().isClient) return;
            if(mob.getVehicle() != null && mob.getTarget() != null){
                mob.stopRiding();
            }
        }
    }

    @Inject(
            method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventHostileMobsFromRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        // Only cancel for hostile mobs with a target
        if (entity instanceof HostileEntity hostileMob && hostileMob.getTarget() != null) {
            cir.setReturnValue(false); // Block mounting
        }
    }

//    @Inject(
//            method = "startRiding",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void preventHostileMobsFromRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
//
//        Entity entity = (Entity) (Object) this;
//
//        // Only cancel for hostile mobs with a target
//        if (entity instanceof HostileEntity hostileMob && hostileMob.getTarget() != null) {
//            cir.setReturnValue(false); // Block mounting
//        }
//    }
}
