package com.trongthang.welcometomyworld.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin({MobSpawnerLogic.class})
public class FasterSpawner {

    @Shadow
    private int spawnDelay;

    // Inject at the very start of updateSpawns and cancel the default behavior
    @Inject(method = "updateSpawns", at = @At("HEAD"), cancellable = true)
    private void onUpdateSpawns(World world, BlockPos pos, CallbackInfo ci) {
        // Directly set spawnDelay to 1 tick (or 0 if you truly want instant)
        this.spawnDelay = random.nextInt(150, 450);
        ci.cancel(); // Skip all vanilla delay-setting logic
    }
}
