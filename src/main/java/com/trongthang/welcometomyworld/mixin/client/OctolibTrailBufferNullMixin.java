package com.trongthang.welcometomyworld.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class OctolibTrailBufferNullMixin { // CHANGED: 'interface' to 'abstract class'

    @Inject(method = "getTrailPosition", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void guardNullWorldTrail(float tickDelta, CallbackInfoReturnable<Vec3d> cir) { // CHANGED: 'default' to
                                                                                           // 'private'
        Entity entity = (Entity) (Object) this;

        if (entity.getEntityWorld() == null) {
            cir.setReturnValue(entity.getPos());
        }
    }
}