package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
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
}
