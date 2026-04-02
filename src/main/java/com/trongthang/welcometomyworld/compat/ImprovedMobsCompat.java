package com.trongthang.welcometomyworld.compat;

import net.minecraft.world.World;
import net.minecraft.entity.LivingEntity;

public class ImprovedMobsCompat {

    /**
     * Safely gets the difficulty from Improved Mobs if the mod is present.
     */
    public static float getDifficulty(World world, LivingEntity entity) {
        if (CompatManager.isImprovedMobsLoaded()) {
            return io.github.flemmli97.improvedmobs.difficulty.DifficultyData.getDifficulty(world, entity);
        }
        return -1;
    }
}
