package com.trongthang.welcometomyworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SleepTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SleepTask.class)
public class AIShouldSleep {
    @Inject(
            method = "shouldRun",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onShouldRun(ServerWorld world, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        // 1) pull the bed-pos out of the villager’s HOME memory
        Optional<GlobalPos> home = entity.getBrain().getOptionalMemory(MemoryModuleType.HOME);

        if (home.isEmpty()) {
            // no bed memory → defer to vanilla logic
            return;
        }
        BlockPos bedPos = home.get().getPos();

        // 2) if that bed’s chunk isn’t loaded, skip the rest of SleepTask.shouldRun
        //    this uses the BlockPos overload, which returns false if not loaded
        if (!world.isChunkLoaded(bedPos)) {
            cir.setReturnValue(false);
        }
    }
}
