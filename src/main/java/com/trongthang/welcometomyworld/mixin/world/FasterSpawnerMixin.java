package com.trongthang.welcometomyworld.mixin.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin({ MobSpawnerLogic.class })
public class FasterSpawnerMixin {

    @Shadow
    private int spawnDelay;

    // Inject at the very start of updateSpawns and cancel the default behavior
    @Inject(method = "updateSpawns", at = @At("TAIL"))
    private void onUpdateSpawns(World world, BlockPos pos, CallbackInfo ci) {
        // Overwrite the delay with our faster version
        this.spawnDelay = random.nextInt(150, 450);
    }
}
