package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.*;
import com.trongthang.welcometomyworld.entities.Blossom.Blossom;
import com.trongthang.welcometomyworld.entities.FallenKnight.FallenKnight;
import com.trongthang.welcometomyworld.entities.Wanderer.Wanderer;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.entities.Unknown.SummoningCircleEntity;
import com.trongthang.welcometomyworld.entities.GroundSlashAttackEntity;
import com.trongthang.welcometomyworld.entities.FallingSkeleton.FallingSkeleton;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import com.trongthang.welcometomyworld.entities.VoidWorm.PurpleCrystalEntity;
import com.trongthang.welcometomyworld.entities.PurplePortalEntity;
import com.trongthang.welcometomyworld.entities.RiftPortalEntity;
import net.minecraft.entity.EntityDimensions;
import com.trongthang.welcometomyworld.entities.Voidan.Voidan;
import com.trongthang.welcometomyworld.entities.Voidan.VoidanTentacle;

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
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class EntitiesManager {

        public static final EntityType<Enderchester> ENDERCHESTER = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "enderchester"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Enderchester::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 0.6f)) // Adjust width and height
                                        .build());

        public static final EntityType<Chester> CHESTER = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "chester"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Chester::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 0.6f)) // Adjust width and height
                                        .build());

        public static final EntityType<Portaler> PORTALER = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "portaler"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Portaler::new)
                                        .dimensions(EntityDimensions.changing(1.5f, 4f))
                                        .build());

        public static final EntityType<EnderPest> ENDER_PEST = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "ender_pest"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, EnderPest::new)
                                        .dimensions(EntityDimensions.changing(0.8f, 1f))
                                        .build());

        public static final EntityType<FallenKnight> FALLEN_KNIGHT = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FallenKnight::new)
                                        .fireImmune()
                                        .dimensions(EntityDimensions.changing(1.5f, 5f))
                                        .build());

        public static final EntityType<Wanderer> WANDERER = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "wanderer"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Wanderer::new)
                                        .fireImmune()
                                        .dimensions(EntityDimensions.changing(1.5f, 4f))
                                        .build());

        public static final EntityType<WandererArrow> WANDERER_ARROW = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "wanderer_arrow"),
                        FabricEntityTypeBuilder
                                        .create(SpawnGroup.MISC,
                                                        (EntityType<WandererArrow> type,
                                                                        World world) -> new WandererArrow(type, world))
                                        .dimensions(EntityDimensions.fixed(0.5f, 0.5f)) // Adjust size as needed
                                        .trackRangeBlocks(128).trackedUpdateRate(20) // Tracking range and update rate
                                        .build());

        public static final EntityType<Blossom> BLOSSOM = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "blossom"),
                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, Blossom::new)
                                        .fireImmune()
                                        .dimensions(EntityDimensions.changing(1, 1f))
                                        .build());

        public static final EntityType<Unknown> UNKNOWN = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "unknown"),
                        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, Unknown::new)
                                        .dimensions(EntityDimensions.changing(0.8f, 2f)) // Basic dimensions
                                        .fireImmune()
                                        .build());

        public static final EntityType<com.trongthang.welcometomyworld.entities.TinyGolem.TinyGolem> TINY_GOLEM = Registry
                        .register(
                                        Registries.ENTITY_TYPE,
                                        new Identifier(WelcomeToMyWorld.MOD_ID, "tiny_golem"),
                                        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
                                                        com.trongthang.welcometomyworld.entities.TinyGolem.TinyGolem::new)
                                                        .dimensions(EntityDimensions.changing(0.6f, 1.1f))
                                                        .build());

        public static final EntityType<Voidan> VOIDAN = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "voidan"),
                        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, Voidan::new)
                                        .dimensions(EntityDimensions.changing(5f, 5f)) // Smaller base for
                                                                                       // navigation
                                        .fireImmune()
                                        .build());

        public static final EntityType<VoidanTentacle> VOIDAN_TENTACLE = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "voidan_tentacle"),
                        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VoidanTentacle::new)
                                        .dimensions(EntityDimensions.fixed(1.0f, 3.0f))
                                        .fireImmune()
                                        .build());

        public static final EntityType<BlockSlamGroundEntity> BLOCK_SLAM_GROUND = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "block_slam_ground"),
                        FabricEntityTypeBuilder.create(SpawnGroup.MISC, BlockSlamGroundEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.5f, 0.5f)) // Adjust width and height
                                        .build());

        public static final EntityType<UnknownBeamEntity> UNKNOWN_BEAM = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "unknown_beam"),
                        FabricEntityTypeBuilder.<UnknownBeamEntity>create(SpawnGroup.MISC, UnknownBeamEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                                        .trackRangeBlocks(256)
                                        .build());

        public static final EntityType<SummoningCircleEntity> SUMMONING_CIRCLE = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "summoning_circle"),
                        FabricEntityTypeBuilder
                                        .<SummoningCircleEntity>create(SpawnGroup.MISC, SummoningCircleEntity::new)
                                        .dimensions(EntityDimensions.fixed(3.0f, 3.0f))
                                        .build());

        public static final EntityType<GroundSlashAttackEntity> GROUND_SLASH_ATTACK = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "ground_slash_attack"),
                        FabricEntityTypeBuilder.<GroundSlashAttackEntity>create(SpawnGroup.MISC,
                                        GroundSlashAttackEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                                        .trackRangeBlocks(128)
                                        .build());

        public static final EntityType<FallingSkeleton> FALLING_SKELETON = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "falling_skeleton"),
                        FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, FallingSkeleton::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 1.99f))
                                        .build());

        public static final EntityType<VoidWormEntity> VOID_WORM = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_worm"),
                        FabricEntityTypeBuilder.<VoidWormEntity>create(SpawnGroup.MONSTER, VoidWormEntity::new)
                                        .dimensions(EntityDimensions.fixed(12.0f, 12.0f))
                                        .trackRangeBlocks(256)
                                        .trackedUpdateRate(2)
                                        .build());

        public static final EntityType<VoidWormPartEntity> VOID_WORM_BODY = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_worm_body"),
                        FabricEntityTypeBuilder.<VoidWormPartEntity>create(SpawnGroup.MONSTER, VoidWormPartEntity::new)
                                        .dimensions(EntityDimensions.fixed(10.0f, 10.0f))
                                        .trackRangeBlocks(256)
                                        .trackedUpdateRate(2)
                                        .build());

        public static final EntityType<VoidWormPartEntity> VOID_WORM_TAIL = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_worm_tail"),
                        FabricEntityTypeBuilder.<VoidWormPartEntity>create(SpawnGroup.MONSTER, VoidWormPartEntity::new)
                                        .dimensions(EntityDimensions.fixed(10.0f, 10.0f))
                                        .trackRangeBlocks(256)
                                        .trackedUpdateRate(2)
                                        .build());

        public static final EntityType<PurpleCrystalEntity> PURPLE_CRYSTAL = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "purple_crystal"),
                        FabricEntityTypeBuilder.<PurpleCrystalEntity>create(SpawnGroup.MISC, PurpleCrystalEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                                        .build());

        public static final EntityType<PurplePortalEntity> PURPLE_PORTAL_ENTITY = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "purple_portal"),
                        FabricEntityTypeBuilder.<PurplePortalEntity>create(SpawnGroup.MISC, PurplePortalEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.5f, 3.0f))
                                        .build());

        public static final EntityType<RiftPortalEntity> RIFT_PORTAL_ENTITY = Registry.register(
                        Registries.ENTITY_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "rift_portal"),
                        FabricEntityTypeBuilder.<RiftPortalEntity>create(SpawnGroup.MISC, RiftPortalEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.5f, 3.0f))
                                        .build());

        public static void register() {
                // FabricDefaultAttributeRegistry.register(A_LIVING_LOG, setAttributes(8, 0.1));

                FabricDefaultAttributeRegistry.register(ENDERCHESTER, Enderchester.setAttributes());

                FabricDefaultAttributeRegistry.register(CHESTER, Enderchester.setAttributes());

                FabricDefaultAttributeRegistry.register(PORTALER, Portaler.setAttributes());

                FabricDefaultAttributeRegistry.register(ENDER_PEST, EnderPest.setAttributes());

                FabricDefaultAttributeRegistry.register(FALLEN_KNIGHT, FallenKnight.setAttributes());

                FabricDefaultAttributeRegistry.register(WANDERER, Wanderer.setAttributes());

                FabricDefaultAttributeRegistry.register(BLOSSOM, Blossom.setAttributes());

                FabricDefaultAttributeRegistry.register(UNKNOWN, Unknown.setAttributes());
                FabricDefaultAttributeRegistry.register(TINY_GOLEM,
                                com.trongthang.welcometomyworld.entities.TinyGolem.TinyGolem.setAttributes());
                FabricDefaultAttributeRegistry.register(VOIDAN, Voidan.setAttributes());
                FabricDefaultAttributeRegistry.register(VOIDAN_TENTACLE, VoidanTentacle.setAttributes());

                FabricDefaultAttributeRegistry.register(FALLING_SKELETON, FallingSkeleton.setAttributes());

                FabricDefaultAttributeRegistry.register(VOID_WORM, VoidWormEntity.setAttributes());
                FabricDefaultAttributeRegistry.register(VOID_WORM_BODY, VoidWormPartEntity.setAttributes());
                FabricDefaultAttributeRegistry.register(VOID_WORM_TAIL, VoidWormPartEntity.setAttributes());
                FabricDefaultAttributeRegistry.register(PURPLE_PORTAL_ENTITY, PurplePortalEntity.setAttributes());
                FabricDefaultAttributeRegistry.register(RIFT_PORTAL_ENTITY, RiftPortalEntity.setAttributes());

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
                                                BiomeKeys.MEADOW),
                                SpawnGroup.MONSTER,
                                EntityType.PHANTOM,
                                1, 1, 3);

                BiomeModifications.addSpawn(
                                context -> {
                                        return context.getBiomeKey().getValue()
                                                        .equals(new Identifier("regions_unexplored", "redwoods"));
                                },
                                SpawnGroup.CREATURE,
                                EntitiesManager.CHESTER,
                                20,
                                1,
                                1);

                BiomeModifications.addSpawn(
                                context -> {
                                        return context.getBiomeKey().getValue()
                                                        .equals(new Identifier("betterend", "shadow_forest"));
                                },
                                SpawnGroup.CREATURE,
                                EntitiesManager.ENDERCHESTER, 10,
                                1,
                                1);

                BiomeModifications.addSpawn(
                                context -> BiomeSelectors.foundInOverworld().test(context)
                                                && isValidBiomeForSpawnPortaler(context),
                                SpawnGroup.CREATURE,
                                PORTALER,
                                1, 1, 1);

                BiomeModifications.addSpawn(
                                context -> {
                                        // Check if we're in the End dimension but NOT in the main end islands biome
                                        boolean isValidBiome = BiomeSelectors.foundInTheEnd().test(context)
                                                        && !context.getBiomeKey().getValue()
                                                                        .equals(new Identifier("minecraft", "the_end"));
                                        return isValidBiome && EnderPest.totalEnderPests < EnderPest.MAX_ENDER_PESTS;
                                },
                                SpawnGroup.CREATURE,
                                ENDER_PEST,
                                1, 1, 1);

                // BiomeModifications.addSpawn(
                // context -> {
                // return BiomeSelectors.foundInTheNether().test(context) &&
                // (context.getBiomeKey().getValue().equals(
                // new Identifier("minecraft", "crimson_forest"))
                // || context.getBiomeKey().getValue()
                // .equals(new Identifier(
                // "betterend",
                // "crimson_glowing_woods"))
                // || context.getBiomeKey().getValue()
                // .equals(new Identifier(
                // "betterend",
                // "crimson_pinewood"))
                // || context.getBiomeKey().getValue().equals(
                // new Identifier("regions_unexplored",
                // "redstone_abyss"))
                // || context.getBiomeKey().getValue().equals(
                // new Identifier("regions_unexplored",
                // "infernal_holt")));
                // },
                // SpawnGroup.CREATURE,
                // EntitiesManager.FALLEN_KNIGHT,
                // 55,
                // 1,
                // 1);

                BiomeModifications.addSpawn(
                                context -> {
                                        Identifier biomeId = context.getBiomeKey().getValue();
                                        return biomeId.equals(new Identifier("betterend", "dragon_graveyards"))
                                                        || biomeId.equals(
                                                                        new Identifier("betterend", "dust_wastelands"))
                                                        || biomeId.equals(new Identifier("betternether",
                                                                        "upside_down_forest"))
                                                        || biomeId.equals(new Identifier("betternether",
                                                                        "wart_forest_edge"));
                                },
                                SpawnGroup.CREATURE,
                                WANDERER,
                                20,
                                1,
                                1);

                BiomeModifications.addSpawn(
                                context -> context.getBiomeKey().getValue()
                                                .equals(new Identifier("regions_unexplored", "magnolia_woodland"))
                                                || context.getBiomeKey().getValue().equals(
                                                                new Identifier("regions_unexplored", "rocky_meadow")),
                                SpawnGroup.CREATURE,
                                EntitiesManager.BLOSSOM,
                                30,
                                1,
                                1);
        }

        public static void spawnRulesRegister() {

                SpawnRestriction.register(
                                ENDER_PEST,
                                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and
                                                                          // avoids foliage
                                EnderPest::canMobSpawn // Custom spawn condition predicate
                );

                SpawnRestriction.register(
                                FALLEN_KNIGHT,
                                SpawnRestriction.Location.ON_GROUND, // The type of location where it spawns
                                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, // Ensures it spawns above the ground and
                                                                          // avoids foliage
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
                                || context.getBiomeKey().getValue()
                                                .equals(new Identifier("regions_unexplored", "muddy_river"))
                                || context.getBiomeKey().getValue()
                                                .equals(new Identifier("regions_unexplored", "frozen_tundra"))
                                || context.getBiomeKey().getValue()
                                                .equals(new Identifier("regions_unexplored", "cold_river"))
                                || context.getBiomeKey().getValue()
                                                .equals(new Identifier("terralith", "haze_mountain"))) {
                        return false;
                }

                return true;
        }
}
