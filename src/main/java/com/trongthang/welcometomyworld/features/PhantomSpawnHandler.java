package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class PhantomSpawnHandler {
    public void register() {
        // Add Phantoms to all biomes where monsters spawn at night
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        BiomeKeys.WINDSWEPT_HILLS,
                        BiomeKeys.WINDSWEPT_GRAVELLY_HILLS,
                        BiomeKeys.JAGGED_PEAKS,
                        BiomeKeys.FROZEN_PEAKS,
                        BiomeKeys.STONY_PEAKS,
                        BiomeKeys.SNOWY_SLOPES,
                        BiomeKeys.GROVE,
                        BiomeKeys.MEADOW
                ),
                SpawnGroup.MONSTER,
                EntityType.PHANTOM,
                3, 1, 3
        );

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PhantomEntity && world instanceof ServerWorld) {
                entity.teleport((int) entity.getX(), (int) entity.getY() + 64, (int) entity.getZ());
                ((PhantomEntity) entity).setPhantomSize(random.nextInt(2, 50));
            }
        });
    }
}
