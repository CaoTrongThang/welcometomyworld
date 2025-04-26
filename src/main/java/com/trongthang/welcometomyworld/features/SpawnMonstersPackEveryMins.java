package com.trongthang.welcometomyworld.features;

import com.ibm.icu.impl.Pair;
import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.trongthang.welcometomyworld.Utilities.Utils.addRunAfter;
import static com.trongthang.welcometomyworld.Utilities.Utils.spawnMob;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;


public class SpawnMonstersPackEveryMins {

    private static double CHANCE_TO_HAPPEN = 50;

    private static int COOLDOWN = 3500;
    private static final int MIN_COOLDOWN = 2500;

    private static final int MOBS_THRESHOLD = 250;

    private static int counter = 0;

    private static final int PACK_MIN_SIZE = 7;
    private static final int PACK_MAX_SIZE = 25;

    private static final int MIN_SPAWN_DISTANCE = 42;
    private static final int MAX_SPAWN_DISTANCE = 88;

    private static final int CAN_SPAWN_PACK_DAY = 35;
    private static final double PACK_SPAWN_CHANCE = 0.7;
    private static boolean CAN_SPAWN_BY_PACK = false;

    public static List<MonsterSpawn> monsters = List.of(
            new MonsterSpawn("minecraft:zombie", 0, 25),
            new MonsterSpawn("minecraft:skeleton", 2, 12),
            new MonsterSpawn("minecraft:witch", 6, 10)
    );

    public static List<Pair<String, Integer>> vanillaComposition = List.of(
            Pair.of("minecraft:zombie", 10),
            Pair.of("minecraft:skeleton", 5),
            Pair.of("minecraft:witch", 2)
    );

    public static List<Pair<String, Integer>> orcPackComposition = List.of(
            Pair.of("wandering_orc:orc_warrior", 5),
            Pair.of("wandering_orc:orc_archer", 5),
            Pair.of("wandering_orc:troll", 2),
            Pair.of("wandering_orc:troll_doctor", 2),
            Pair.of("wandering_orc:orc_champion", 1),
            Pair.of("wandering_orc:minotaur", 1),
            Pair.of("wandering_orc:orc_warlock", 2)
    );

    public static List<Pair<String, Integer>> mutantMonsterPackComposition = List.of(
            Pair.of("mutantmonsters:mutant_zombie", 4),
            Pair.of("mutantmonsters:mutant_skeleton", 3),
            Pair.of("mutantmonsters:mutant_enderman", 1)
    );

    public static List<Pair<String, Integer>> pillagerPackComposition = List.of(
            Pair.of("minecraft:evoker", 1),
            Pair.of("minecraft:pillager", 4),
            Pair.of("minecraft:vindicator", 5),
            Pair.of("minecraft:ravager", 1),
            Pair.of("minecraft:witch", 2)
    );


    public static void spawnMonsters(MinecraftServer server) {
        counter++;
        if (counter <= COOLDOWN) return;
        counter = 0;

        if (WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay <= 1 || WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay >= ConfigLoader.getInstance().hostileMobsEventsStopSpawningDay) return;

        ServerWorld world = server.getOverworld();

        if (world == null) {
            return;
        }

        if(world.getDifficulty() == Difficulty.PEACEFUL) return;

        if (!world.isNight()) return;
        int totalHostileMobs = world.getEntitiesByType(
                TypeFilter.instanceOf(HostileEntity.class),
                entity -> !entity.isPersistent() && entity.isAlive()
        ).size();

        if (totalHostileMobs > MOBS_THRESHOLD) {
            return;
        }

        int currentDay = dayAndNightCounterAnimationHandler.currentDay;

        if (COOLDOWN > MIN_COOLDOWN) {
            COOLDOWN -= currentDay * 3;
        } else {
            COOLDOWN = MIN_COOLDOWN;
        }

        if (CHANCE_TO_HAPPEN <= 100) {
            CHANCE_TO_HAPPEN += currentDay;
            if (random.nextInt(100) >= (CHANCE_TO_HAPPEN)) return;
        }

        List<ServerPlayerEntity> players = world.getPlayers();

        if (players.isEmpty()) return;

        ServerPlayerEntity player = players.get(random.nextInt(players.size()));

        World w = player.getWorld();


        int maxTries = 10;
        int counter = 0;

        while (w.getRegistryKey() != World.OVERWORLD && counter < maxTries) {
            player = players.get(random.nextInt(players.size()));
            counter++;
        }


        if (w.getRegistryKey() != World.OVERWORLD) return;

        if (player.isCreative() || player.isSpectator()) return;


//        LOGGER.info("SPAWN BY MINS");

        if (CAN_SPAWN_BY_PACK) {
            if (random.nextDouble() < PACK_SPAWN_CHANCE) {
                spawnByPack(world, player);
            } else {
                spawnRandomly(world, player);
            }
        } else {
            if (currentDay > CAN_SPAWN_PACK_DAY) {
                CAN_SPAWN_BY_PACK = true;
            }
            if (CAN_SPAWN_BY_PACK) {
                if (random.nextDouble() < PACK_SPAWN_CHANCE) {
                    spawnByPack(world, player);
                }
            } else {
                spawnRandomly(world, player);
            }
        }
    }


    // Spawn Monsters by packs, like a pack of mutant monsters, a pack of orcs, a pack of vanilla mobs,...

    public enum MonstersPackTypes {
        VANILLA,
        PILLAGER,
        ORCS,
        MUTANTS
    }

    private static void spawnByPack(ServerWorld world, ServerPlayerEntity player) {

        MonstersPackTypes packType = getRandomPackType();
        List<Pair<String, Integer>> packMonsters = null;

        if (packType == MonstersPackTypes.ORCS) {
            packMonsters = orcPackComposition;
        } else if (packType == MonstersPackTypes.MUTANTS) {
            packMonsters = mutantMonsterPackComposition;
        } else if (packType == MonstersPackTypes.VANILLA) {
            packMonsters = vanillaComposition;
        } else if (packType == MonstersPackTypes.PILLAGER) {
            packMonsters = pillagerPackComposition;
        }

        if (packMonsters == null || packMonsters.isEmpty()) return;

        // Find pack center first
        BlockPos packCenter = findPackCenter(world, player.getBlockPos(), packMonsters);

        if (packCenter == null) return;

        // Spawn each mob type in specified quantities
        for (Pair<String, Integer> entry : packMonsters) {
            int totalMonster = entry.second;
            if (random.nextInt(100) < 10) {
                totalMonster *= 2;
            }
            for (int i = 0; i < totalMonster; i++) {
                EntityType<?> entityType = EntityType.get(entry.first).orElse(null);
                if (entityType == null) continue;

                BlockPos spawnPos = findSafeSpawnPositionByPack(world, packCenter, entityType, 0, 24);
                if (spawnPos == null) continue;

                Entity entity = spawnMob(world, spawnPos.add(0, 2, 0), entry.first);

                if (entity instanceof MobEntity mob) {
                    mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null, null);
                    if (mob.canSee(player)) mob.setTarget(player);

                    tryDiscardSpawnedMobAfterTime(world, (MobEntity) entity, COOLDOWN);
                    mob.setHealth(mob.getMaxHealth());
                }
            }
        }
    }

    public static void tryDiscardSpawnedMobAfterTime(ServerWorld world, MobEntity entity, int discardTime){
        addRunAfter(() -> {
            if (entity.getTarget() == null) {
                Utils.discardEntity(world, entity);
            } else {
                tryDiscardSpawnedMobAfterTime(world, entity, discardTime);
            }
        }, discardTime);
    }

    private static BlockPos findPackCenter(ServerWorld world, BlockPos playerPos, List<Pair<String, Integer>> mobList) {
        final int MAX_ATTEMPTS = 30;
        List<String> mobIds = mobList.stream().map(p -> p.first).collect(Collectors.toList());

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String mobId = mobIds.get(random.nextInt(mobIds.size()));
            EntityType<?> entityType = EntityType.get(mobId).orElse(null);
            if (entityType == null) continue;

            BlockPos center = findSafeSpawnPositionByPack(world, playerPos, entityType, MIN_SPAWN_DISTANCE, MAX_SPAWN_DISTANCE);
            if (center != null) return center;
        }
        return null;
    }

    private static MonstersPackTypes getRandomPackType() {
        int roll = random.nextInt(100);
        if (roll < 30) return MonstersPackTypes.VANILLA;
        if (roll < 50) return MonstersPackTypes.ORCS;
        if (roll < 80) return MonstersPackTypes.PILLAGER;
        return MonstersPackTypes.MUTANTS;
    }

    private static void spawnRandomly(ServerWorld world, ServerPlayerEntity player) {
        int packSize = random.nextInt(PACK_MAX_SIZE - PACK_MIN_SIZE + 1) + PACK_MIN_SIZE;
        List<MonsterSpawn> avaiMonsters = monsters.stream().filter((m) -> m.getSpawnDay() <= WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay).toList();
        for (int i = 0; i < packSize; i++) {

            MonsterSpawn selectedMonster = getRandomMonster(avaiMonsters);
            if (selectedMonster == null) {
                continue;
            }

            EntityType<?> entityType = EntityType.get(selectedMonster.getId()).orElse(null);
            if (entityType == null) continue;

            BlockPos spawnPos = findSafeSpawnPositionByPack(world, player.getBlockPos(), entityType);
            if (spawnPos == null) {
                continue;
            }

            Entity entity = spawnMob(world, spawnPos, selectedMonster.getId());
            if (entity == null) {
                continue;
            }

            if (entity instanceof MobEntity mob) {
                mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null, null);
            }

            if (entity instanceof MobEntity mob) {
                if (mob.canSee(player)) {
                    mob.setTarget(player);
                }

                mob.setHealth(mob.getMaxHealth());
                tryDiscardSpawnedMobAfterTime(world, mob, 2500);
            }
        }
    }

    private static MonsterSpawn getRandomMonster(List<MonsterSpawn> avail) {
        int totalWeight = monsters.stream().mapToInt(m -> m.spawnWeight).sum();
        int randomWeight = random.nextInt(totalWeight);
        int cumulative = 0;

        for (MonsterSpawn m : monsters) {
            cumulative += m.spawnWeight;
            if (randomWeight < cumulative) return m;
        }
        return monsters.get(0);
    }


    public static BlockPos findSafeSpawnPositionByPack(ServerWorld world, BlockPos center,
                                                       EntityType<?> entityType, int minRadius, int maxRadius) {
        final int MAX_ATTEMPTS = 70;
        final int HORIZONTAL_RANGE = maxRadius - minRadius;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            // Generate position in square instead of circle for better coverage
            int x = center.getX() + (minRadius + random.nextInt(HORIZONTAL_RANGE)) * (random.nextBoolean() ? 1 : -1);
            int z = center.getZ() + (minRadius + random.nextInt(HORIZONTAL_RANGE)) * (random.nextBoolean() ? 1 : -1);

            // Improved vertical search
            Heightmap.Type heightmapType = SpawnRestriction.getHeightmapType(entityType);
            int topY = world.getTopY(heightmapType, x, z);

            // Check 3 blocks below and above the surface
            for (int yOffset = -3; yOffset <= 3; yOffset++) {
                BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, topY + yOffset, z);

                // Validate spawn rules first before solid block check

                if (SpawnHelper.canSpawn(SpawnRestriction.getLocation(entityType), world, mutablePos, entityType)) {
                    return mutablePos.toImmutable();
                }
            }
        }

        return null;
    }

    public static BlockPos findSafeSpawnPositionByPack(ServerWorld world, BlockPos playerPos, EntityType<?> entityType) {
        final int MAX_ATTEMPTS = 30;
        final int HORIZONTAL_RANGE = MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            // Generate random position around the player
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * HORIZONTAL_RANGE;
            int x = playerPos.getX() + (int) (Math.cos(angle) * distance);
            int z = playerPos.getZ() + (int) (Math.sin(angle) * distance);

            // Get the correct heightmap type for the entity
            Heightmap.Type heightmapType = SpawnRestriction.getHeightmapType(entityType);
            int topY = world.getTopY(heightmapType, x, z);
            BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, topY, z);

            // Adjust Y for dimensions with ceilings (e.g., Nether)
            if (world.getDimension().hasCeiling()) {
                while (mutablePos.getY() > world.getBottomY() && !world.getBlockState(mutablePos).isAir()) {
                    mutablePos.move(Direction.DOWN);
                }
                while (mutablePos.getY() > world.getBottomY() && world.getBlockState(mutablePos).isAir()) {
                    mutablePos.move(Direction.DOWN);
                }
                mutablePos.move(Direction.UP);
            }

            if ((SpawnHelper.canSpawn(SpawnRestriction.getLocation(entityType), world, mutablePos, entityType))) {
                return mutablePos.toImmutable();
            }
        }

        return null;
    }
}
