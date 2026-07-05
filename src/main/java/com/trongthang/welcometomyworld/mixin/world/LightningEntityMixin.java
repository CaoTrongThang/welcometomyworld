package com.trongthang.welcometomyworld.mixin.world;

import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {

    // Reduce the thunder sound volume to limit its max range.
    // Default is 10000.0F (which translates to global range).
    // Let's cap it at 15.0F which is roughly 15 * 16 = 240 blocks max range.
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V"), index = 5)
    private float limitThunderVolume(float originalVolume) {
        if (originalVolume > 100.0F) {
            return 15.0F; // Adjust this: 15.0F = 240 blocks radius
        }
        return originalVolume;
    }

    // Wrap the sky flash effect so it only triggers if a player is nearby.
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setLightningTicksLeft(I)V"))
    private void limitSkyFlashRange(World world, int ticks) {
        if (world.isClient()) {
            LightningEntity self = (LightningEntity) (Object) this;
            // Radius for seeing the flash.
            double maxRange = 250.0;

            // Check if there is any player within maxRange blocks
            PlayerEntity player = world.getClosestPlayer(
                    self, maxRange);

            if (player != null) {
                world.setLightningTicksLeft(ticks);
            }
        }
    }
}
