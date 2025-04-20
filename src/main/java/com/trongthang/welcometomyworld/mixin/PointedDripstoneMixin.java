package com.trongthang.welcometomyworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.block.PointedDripstoneBlock.THICKNESS;
import static net.minecraft.block.PointedDripstoneBlock.VERTICAL_DIRECTION;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneMixin {

    @Inject(method = "onLandedUpon", at = @At("HEAD"))
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP) {
            entity.handleFallDamage(fallDistance + 2.0F, 4.0F, world.getDamageSources().stalagmite());
        }
    }

}
