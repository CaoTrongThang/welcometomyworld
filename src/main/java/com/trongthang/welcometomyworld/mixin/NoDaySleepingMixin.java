package com.trongthang.welcometomyworld.mixin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class NoDaySleepingMixin {
//    @Inject(
//            method = "trySleep",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onTrySleep(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
//        PlayerEntity player = (PlayerEntity) (Object) this;
//        World world = player.getWorld();
//
//        // Check if it's NOT nighttime (day or dusk/dawn)
//        if (!world.isRaining()) {
//            // Send feedback to the player
//            player.sendMessage(Text.literal("You can only sleep at night!"), true);
//            // Cancel the sleep attempt (but still allow spawn point setting)
//            cir.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW));
//        }
//    }

//    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
//    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir){
//        if(world.getTimeOfDay() < 12000 && !world.isRaining()){
//            cir.setReturnValue(ActionResult.FAIL);
//        }
//    }
}
