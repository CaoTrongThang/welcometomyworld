package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.managers.ItemsManager;
import com.trongthang.welcometomyworld.compat.TrinketsCompat;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({ CreeperEntity.class })
public abstract class CreeperBlockDropMixin {
    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"), index = 5)
    private World.ExplosionSourceType modifyExplosionType(World.ExplosionSourceType original) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;

        // If player's holding Creeper's talisman, they don't explode
        if (creeper.getTarget() != null) {
            if (creeper.getTarget() instanceof PlayerEntity player) {
                ItemStack offhand = player.getOffHandStack();
                ItemStack mainHand = player.getMainHandStack();
                if (offhand.isOf(ItemsManager.CREEPER_TALISMAN) || mainHand.isOf(ItemsManager.CREEPER_TALISMAN)) {
                    return World.ExplosionSourceType.NONE;
                }

                if (TrinketsCompat.isCreeperTalismanEquipped(player)) {
                    return World.ExplosionSourceType.NONE;
                }
            } else if (creeper.getTarget() instanceof TameableEntity) {
                return World.ExplosionSourceType.NONE;
            }
        }
        return World.ExplosionSourceType.TNT;
    }
}
