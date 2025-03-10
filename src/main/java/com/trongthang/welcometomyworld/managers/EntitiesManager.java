package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.*;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class EntitiesManager {

    public static final EntityType<ALivingFlower> A_LIVING_FLOWER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "a_living_flower"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ALivingFlower::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)) // Adjust width and height
                    .build()
    );

    public static final EntityType<AncientWhale> ANCIENT_WHALE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "ancient_whale"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AncientWhale::new)
                    .dimensions(EntityDimensions.fixed(2.0f, 0.5f)) // Adjust width and height
                    .build()
    );

    public static final EntityType<Enderchester> ENDERCHESTER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "enderchester"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Enderchester::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f)) // Adjust width and height
                    .build()
    );

    public static final EntityType<Chester> CHESTER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "chester"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Chester::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f)) // Adjust width and height
                    .build()
    );

    public static final EntityType<Portaler> PORTALER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "portaler"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Portaler::new)
                    .dimensions(EntityDimensions.changing(0.8f, 4f))
                    .build()
    );

    public static final EntityType<EnderPest> ENDER_PEST = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "ender_pest"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EnderPest::new)
                    .dimensions(EntityDimensions.changing(0.8f, 1f))
                    .build()
    );

    public static final EntityType<FallenKnight> FALLEN_KNIGHT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FallenKnight::new)
                    .fireImmune()
                    .dimensions(EntityDimensions.changing(1.5f, 5f))
                    .build()
    );

    public static final EntityType<Wanderer> WANDERER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "wanderer"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Wanderer::new)
                    .fireImmune()
                    .dimensions(EntityDimensions.changing(1.5f, 4f))
                    .build()
    );

    public static final EntityType<WandererArrow> WANDERER_ARROW = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "wanderer_arrow"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType<WandererArrow> type, World world) -> new WandererArrow(type, world))
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)) // Adjust size as needed
                    .trackRangeBlocks(4).trackedUpdateRate(20) // Tracking range and update rate
                    .build()
    );

    public static void register() {
//        FabricDefaultAttributeRegistry.register(A_LIVING_LOG, setAttributes(8, 0.1));

        FabricDefaultAttributeRegistry.register(A_LIVING_FLOWER, setAttributes(20, 0.2));

        FabricDefaultAttributeRegistry.register(ANCIENT_WHALE, AncientWhale.setAttributes());

        FabricDefaultAttributeRegistry.register(ENDERCHESTER, Enderchester.setAttributes());

        FabricDefaultAttributeRegistry.register(CHESTER, Enderchester.setAttributes());

        FabricDefaultAttributeRegistry.register(PORTALER, Portaler.setAttributes());

        FabricDefaultAttributeRegistry.register(ENDER_PEST, EnderPest.setAttributes());

        FabricDefaultAttributeRegistry.register(FALLEN_KNIGHT, FallenKnight.setAttributes());

        FabricDefaultAttributeRegistry.register(WANDERER, Wanderer.setAttributes());

        addSpawn();

        spawnRulesRegister();

        LOGGER.info("Registering mod entities...");
    }

    public static void addSpawn() {
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


        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                A_LIVING_FLOWER,
                1, 1, 1
        );

        BiomeModifications.addSpawn(
                context -> {
                    return context.getBiomeKey().getValue().equals(new Identifier("regions_unexplored", "redwoods"));
                },
                SpawnGroup.CREATURE,
                EntitiesManager.CHESTER,
                4,
                1,
                1
        );

        BiomeModifications.addSpawn(
                context -> {
                    return context.getBiomeKey().getValue().equals(new Identifier("betterend", "shadow_forest"));
                },
                SpawnGroup.CREATURE,
                EntitiesManager.ENDERCHESTER, 6,
                1,
                1
        );

        BiomeModifications.addSpawn(
                context -> BiomeSelectors.foundInOverworld().test(context) && isValidBiomeForSpawnPortaler(context),
                SpawnGroup.CREATURE,
                PORTALER,
                1, 1, 1
        );

        BiomeModifications.addSpawn(
                context -> {
                    // Check if we're in the End dimension but NOT in the main end islands biome
                    boolean isValidBiome = BiomeSelectors.foundInTheEnd().test(context) && !context.getBiomeKey().getValue().equals(new Identifier("minecraft", "the_end"));
                    return isValidBiome && EnderPest.totalEnderPests < EnderPest.MAX_ENDER_PESTS;
                },
                SpawnGroup.CREATURE,
                ENDER_PEST,
                1, 1, 1
        );


        BiomeModifications.addSpawn(
                context -> {
                    return BiomeSelectors.foundInTheNether().test(context) &&
                            (context.getBiomeKey().getValue().equals(new Identifier("minecraft", "crimson_forest"))
                                    || context.getBiomeKey().getValue().equals(new Identifier("betterend", "crimson_glowing_woods"))
                                    || context.getBiomeKey().getValue().equals(new Identifier("betterend", "crimson_pinewood"))
                                    || context.getBiomeKey().getValue().equals(new Identifier("regions_unexplored", "redstone_abyss"))
                                    || context.getBiomeKey().getValue().equals(new Identifier("regions_unexplored", "infernal_holt")))
                            ;
                },
                SpawnGroup.CREATURE,
                EntitiesManager.FALLEN_KNIGHT,
                50,
                1,
                1
        );

        BiomeModifications.addSpawn(
                context -> {
                    Identifier biomeId = context.getBiomeKey().getValue();
                    // Only allow spawns in biomes from the "twilightforest" namespace
                    // and exclude the "stream" biome.
                    return biomeId.getNamespace().equals("twilightforest") && !biomeId.getPath().equals("stream") && !biomeId.getPath().equals("swamp");
                },
                SpawnGroup.CREATURE,
                WANDERER,
                1,
                1,
                1
        );
    }

    public static void spawnRulesRegister() {

        SpawnRestriction.register(
                A_LIVING_FLOWER,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                ALivingFlower::canSpawn // Custom spawn condition predicate
        );

        SpawnRestriction.register(
                ENDER_PEST,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                EnderPest::canMobSpawn // Custom spawn condition predicate
        );

        SpawnRestriction.register(
                FALLEN_KNIGHT,
                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and avoids foliage
                FallenKnight::canMobSpawn // Custom spawn condition predicate
        );
    }

    public static DefaultAttributeContainer.Builder setAttributes(double maxHealth, double moveSpeed) {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, maxHealth)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, moveSpeed);
    }

    private static boolean isValidBiomeForSpawnPortaler(BiomeSelectionContext context) {
        if (context.hasTag(BiomeTags.IS_OCEAN) || context.hasTag(BiomeTags.IS_BEACH)) {
            return false;
        }

        if (context.getBiomeKey().equals(BiomeKeys.STONY_SHORE)
                || context.getBiomeKey().equals(BiomeKeys.RIVER)
                || context.getBiomeKey().equals(BiomeKeys.FROZEN_RIVER)
                || context.getBiomeKey().getValue().equals(new Identifier("regions_unexplored", "muddy_river"))
                || context.getBiomeKey().getValue().equals(new Identifier("regions_unexplored", "frozen_tundra"))
                || context.getBiomeKey().getValue().equals(new Identifier("terralith", "haze_mountain"))) {
            return false;
        }

        return true;
    }
}
