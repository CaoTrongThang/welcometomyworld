package com.trongthang.welcometomyworld.mixin.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEntity.class)
public abstract class HostileInfightingPreventionMixin extends LivingEntity {

    protected HostileInfightingPreventionMixin(
            EntityType<? extends LivingEntity> entityType,
            World world) {
        super(entityType, world);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (this instanceof Monster && target instanceof Monster) {
            return false;
        }
        return super.canTarget(target);
    }
}
