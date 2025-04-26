package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.SpawnSettings;

import java.util.List;
import java.util.function.Predicate;

public class ChangeCreeperSpawnWeight {

    public static void register() {
        BiomeModifications.create(new Identifier(WelcomeToMyWorld.MOD_ID, "creeper_adjustment"))
                .add(ModificationPhase.REPLACEMENTS,  // Changed phase
                        BiomeSelectors.foundInOverworld(),
                        context -> {
                            // 1. Remove existing creepers
                            boolean removed = context.getSpawnSettings().removeSpawnsOfEntityType(EntityType.CREEPER);

                            // 2. Add new entry only if removed from vanilla
                            if (removed) {
                                context.getSpawnSettings().addSpawn(
                                        SpawnGroup.MONSTER,
                                        new SpawnSettings.SpawnEntry(
                                                EntityType.CREEPER,
                                                20,
                                                1,
                                                1
                                        )
                                );
                            }
                        });
    }
}
