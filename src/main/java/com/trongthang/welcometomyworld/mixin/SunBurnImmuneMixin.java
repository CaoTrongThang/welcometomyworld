package com.trongthang.welcometomyworld.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.trongthang.welcometomyworld.GlobalConfig.canSunburnImmune;

@Mixin(MobEntity.class)
public abstract class SunBurnImmuneMixin extends LivingEntity {
    protected SunBurnImmuneMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isAffectedByDaylight", at = @At("HEAD"), cancellable = true)
    private void modifyDaylightEffect(CallbackInfoReturnable<Boolean> cir) {
        if(!canSunburnImmune) return;
        
        if ((Object) this instanceof MobEntity) {
            cir.setReturnValue(false);
        }
    }
}
