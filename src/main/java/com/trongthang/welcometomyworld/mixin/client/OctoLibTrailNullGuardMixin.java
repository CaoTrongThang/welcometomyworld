package com.trongthang.welcometomyworld.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//Mixin to prevent crash when entity is removed from world
@Pseudo
@Environment(EnvType.CLIENT)
@Mixin(targets = "it.hurts.octostudios.octolib.modules.particles.trail.TrailProvider", remap = false)
public interface OctoLibTrailNullGuardMixin {

    @Inject(method = "getRenderPosition", at = @At("HEAD"), cancellable = true, remap = false)
    default void guardNullWorld(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        // Since TrailProvider is implemented by entities, 'this' is the entity
        // instance.
        if (this instanceof net.minecraft.entity.Entity entity) {
            if (entity.getEntityWorld() == null) {
                cir.setReturnValue(Vec3d.ZERO);
            }
        }
    }
}
