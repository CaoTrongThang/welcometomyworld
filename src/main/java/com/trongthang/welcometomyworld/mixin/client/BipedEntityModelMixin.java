package com.trongthang.welcometomyworld.mixin.client;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    public void onSetAngles(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (livingEntity.hasVehicle() && (livingEntity.getVehicle() instanceof PhantomEntity
                || livingEntity.getVehicle() instanceof Unknown)) {
            ((BipedEntityModel<?>) (Object) this).riding = false;
        }
    }
}
