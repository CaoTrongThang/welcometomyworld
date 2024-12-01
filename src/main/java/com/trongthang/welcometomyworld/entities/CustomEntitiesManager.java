package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
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

    public static final EntityType<ALivingFlower> A_LIVING_FLOWER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "a_living_flower"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ALivingFlower::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)) // Adjust width and height
                    .build()
    );


    public static void register() {
        FabricDefaultAttributeRegistry.register(A_LIVING_LOG, LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 10));

        FabricDefaultAttributeRegistry.register(A_LIVING_FLOWER, LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 10));

        addSpawn();

        spawnRulesRegister();

        LOGGER.info("Registering mod entities...");
    }

    public static void addSpawn() {
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                A_LIVING_LOG,
                2, 1, 1
        );

        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                A_LIVING_FLOWER,
                2, 1, 1
        );
    }

    public static void spawnRulesRegister() {
        SpawnRestriction.register(
                A_LIVING_LOG,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                ALivingLog::canSpawn // Custom spawn condition predicate
        );

        SpawnRestriction.register(
                A_LIVING_FLOWER,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                ALivingFlower::canSpawn // Custom spawn condition predicate
        );
    }
}
