package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;

import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WitherSkullEntity.class)
public abstract class WitherSkullNoGriefMixin {
    @ModifyArg(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 6)
    private World.ExplosionSourceType modifyExplosionSourceType(World.ExplosionSourceType original) {
        WitherSkullEntity skull = (WitherSkullEntity) (Object) this;
        if (skull.getOwner() instanceof Unknown) {
            return World.ExplosionSourceType.NONE;
        }
        return original;
    }
}
