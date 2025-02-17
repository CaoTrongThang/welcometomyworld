package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Shadow
    protected GoalSelector targetSelector;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(CallbackInfo ci) {
        MobEntity mobEntity = (MobEntity) (Object) this;
        if (mobEntity instanceof HostileEntity hostileEntity) {
                this.targetSelector.add(6, new ActiveTargetGoal<>(hostileEntity, TameableEntity.class, true));
        }
    }
}
