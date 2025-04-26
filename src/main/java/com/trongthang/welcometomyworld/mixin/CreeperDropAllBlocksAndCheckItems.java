package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.managers.ItemsManager;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({CreeperEntity.class})
public abstract class CreeperDropAllBlocksAndCheckItems {
    @ModifyArg(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"
            ),
            index = 5 // The 6th parameter (0-based index 5) is ExplosionSourceType
    )
    private World.ExplosionSourceType modifyExplosionType(World.ExplosionSourceType original) {
        CreeperEntity creeper = (CreeperEntity) (Object) this;

        //If player's holding Creeper's talisman, they don't explode
        if (creeper.getTarget() != null) {
            if (creeper.getTarget() instanceof PlayerEntity player) {
                ItemStack offhand = player.getOffHandStack();
                ItemStack mainHand = player.getMainHandStack();
                if (offhand.isOf(ItemsManager.CREEPER_TALISMAN) || mainHand.isOf(ItemsManager.CREEPER_TALISMAN)) {
                    return World.ExplosionSourceType.NONE;
                }

                if ( TrinketsApi.getTrinketComponent(player)
                        .map(component -> component.isEquipped(ItemsManager.CREEPER_TALISMAN))
                        .orElse(false)) {
                    return World.ExplosionSourceType.NONE;
                }
            } else if(creeper.getTarget() instanceof TameableEntity){
                return World.ExplosionSourceType.NONE;
            }
        }
        return World.ExplosionSourceType.TNT;
    }
}
