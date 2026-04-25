package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.mixin.accessor.MobEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class HostileEntityTargetTameableMobsMixin {

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (!(entity instanceof HostileEntity)) {
            return;
        }

        HostileEntity mobEntity = (HostileEntity) entity;

        if (!(entity instanceof EndermanEntity) && !(entity instanceof Unknown)) {
            ((MobEntityAccessor) mobEntity).getTargetSelector().add(10, new ActiveTargetGoal<>(mobEntity,
                    TameableEntity.class, true,
                    target -> !Registries.ENTITY_TYPE.getId(target.getType()).getNamespace()
                            .contains("iceandfire")));
        }
    }
}
