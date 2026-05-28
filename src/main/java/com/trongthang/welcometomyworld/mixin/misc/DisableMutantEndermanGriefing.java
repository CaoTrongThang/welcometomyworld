package com.trongthang.welcometomyworld.mixin.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "fuzs.mutantmonsters.world.entity.mutant.MutantEnderman")
public class DisableMutantEndermanGriefing {

    // 1. Target the moment the Enderman tries to remove the block from the ground.
    // We use remap = true for the target because World.removeBlock is a completely
    // stable vanilla method.
    @Redirect(method = "updateBlockFrenzy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", remap = true), remap = false // updateBlockFrenzy
                                                                                                                                                                                             // is
                                                                                                                                                                                             // a
                                                                                                                                                                                             // mod
                                                                                                                                                                                             // method,
                                                                                                                                                                                             // do
                                                                                                                                                                                             // not
                                                                                                                                                                                             // remap
                                                                                                                                                                                             // it
    )
    private boolean preventBlockPickupRemoval(World instance, BlockPos pos, boolean move) {
        // Returning false tricks the Enderman into leaving the block on the ground.
        // It still visually grabs a block in its hands!
        return false;
    }

    // 2. Prevent the Enderman from placing blocks back down (which duplicates
    // them).
    // Instead of redirecting a Vanilla method, we inject directly into the mod's
    // custom "canPlaceBlock" method!
    @Inject(method = "canPlaceBlock", at = @At("HEAD"), cancellable = true, remap = false // canPlaceBlock is a mod
                                                                                          // method, do not remap it
    )
    private void preventBlockPlacement(CallbackInfoReturnable<Boolean> cir) {
        // Force the mod's canPlaceBlock check to always return false.
        // Because of this, the Enderman will skip placing the block and eventually
        // throw it as an attack instead!
        cir.setReturnValue(false);
    }
}