package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin {
    @ModifyVariable(method = "setVariant(Lnet/minecraft/entity/vehicle/BoatEntity$Type;)V", at = @At("HEAD"), argsOnly = true)
    private BoatEntity.Type ensureVariantNotNull(BoatEntity.Type type) {
        if (type != null) {
            return type;
        } else {
            WelcomeToMyWorld.LOGGER.info("Player's placing a NULL variant boat, set the boat back to OAK BOAT");
            return BoatEntity.Type.OAK;
        }
    }
}
