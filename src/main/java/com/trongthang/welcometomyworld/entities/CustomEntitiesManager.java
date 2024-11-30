package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class CustomEntitiesManager {
    public static final EntityType<ALivingLog> A_LIVING_LOG = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "a_living_log"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ALivingLog::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f)) // Adjust width and height
                    .build()
    );

    public static final EntityType<StoneEntity> STONE_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "stone_entity"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, StoneEntity::new)
                    .dimensions(EntityDimensions.fixed(1f, 1f))
                    .build()
    );

    public static void register(){
        FabricDefaultAttributeRegistry.register(A_LIVING_LOG, AttributesManager.createDefaultAttributes());
        FabricDefaultAttributeRegistry.register(STONE_ENTITY, AttributesManager.createDefaultAttributes());

        addSpawn();

        spawnRulesRegister();

        LOGGER.info("Registering mod entities...");
    }

    public static void addSpawn(){
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                A_LIVING_LOG,
                2, 1, 1
        );
    }

    public static void spawnRulesRegister(){
        SpawnRestriction.register(
                A_LIVING_LOG,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                ALivingLog::canSpawn // Custom spawn condition predicate
        );
    }
}
