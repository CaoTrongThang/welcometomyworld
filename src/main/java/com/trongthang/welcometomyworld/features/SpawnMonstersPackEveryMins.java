package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.Utilities.Utils.addRunAfter;
import static com.trongthang.welcometomyworld.Utilities.Utils.spawnMob;

public class SpawnMonstersPackEveryMins {

    private static final double CHANCE_TO_HAPPEN = 69;

    private static final int COOLDOWN = 6000;
    private static int counter = 0;

    private static final int MONSTER_DESPAWN_AFTER_TICK = 400;
    private static final int PACK_MIN_SIZE = 7;
    private static final int PACK_MAX_SIZE = 15;

    private static final int MIN_SPAWN_DISTANCE = 24; // Minimum distance from the player
    private static final int MAX_SPAWN_DISTANCE = 64; // Maximum distance from the player

    public static final int stopSpawningDay = 500;

    private static final List<MonsterSpawn> MONSTERS = List.of(
            new MonsterSpawn("minecraft:zombie", 50),
            new MonsterSpawn("minecraft:skeleton", 30),
            new MonsterSpawn("minecraft:creeper", 30)
    );

    private static final Random RANDOM = new Random();

    public static void spawnMonsters(MinecraftServer server) {
        counter++;
        if (counter <= COOLDOWN) return;
        counter = 0;

        if(WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay >= stopSpawningDay) return;

        ServerWorld world = server.getOverworld();
        if (world == null) {
            return;
        }

        if (!world.isNight()) return;
        if (RANDOM.nextInt(100) >= CHANCE_TO_HAPPEN) return;

        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;

        ServerPlayerEntity player = players.get(RANDOM.nextInt(players.size()));

        if(player.isCreative() || player.isSpectator()) return;

        int packSize = RANDOM.nextInt(PACK_MAX_SIZE - PACK_MIN_SIZE + 1) + PACK_MIN_SIZE;
        for (int i = 0; i < packSize; i++) {
            BlockPos spawnPos = findSafeSpawnPosition(world, player.getBlockPos());
            if (spawnPos == null) {
                continue;
            }

            MonsterSpawn selectedMonster = getRandomMonster();
            if (selectedMonster == null) {
                continue;
            }

            Entity entity = spawnMob(world, spawnPos, selectedMonster.getId());
            if (entity == null) {
                continue;
            }

            if (entity instanceof MobEntity mob) {
                mob.setTarget(player);
            }

            addRunAfter(entity::discard, MONSTER_DESPAWN_AFTER_TICK);
        }
    }

    private static MonsterSpawn getRandomMonster() {
        int totalWeight = MONSTERS.stream().mapToInt(MonsterSpawn::getData).sum();
        if (totalWeight <= 0) {
            return null;
        }

        int randomValue = RANDOM.nextInt(totalWeight);
        int currentWeight = 0;

        for (MonsterSpawn monster : MONSTERS) {
            currentWeight += monster.getData();
            if (randomValue < currentWeight) {
                return monster;
            }
        }

        return null; // Fallback, though this should never happen.
    }

    public static BlockPos findSafeSpawnPosition(ServerWorld world, BlockPos playerPos) {
        final int MAX_ATTEMPTS = 40; // Maximum attempts to find a valid position
        final int VERTICAL_SEARCH_RANGE = 10; // Range to search vertically if needed

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            double distance = MIN_SPAWN_DISTANCE + RANDOM.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            double angle = RANDOM.nextDouble() * 2 * Math.PI;

            int offsetX = (int) (Math.cos(angle) * distance);
            int offsetZ = (int) (Math.sin(angle) * distance);

            int baseY = playerPos.getY();
            BlockPos candidatePos = playerPos.add(offsetX, 0, offsetZ);

            // Start vertical search
            for (int offsetY = -VERTICAL_SEARCH_RANGE; offsetY <= VERTICAL_SEARCH_RANGE; offsetY++) {
                BlockPos potentialPos = candidatePos.add(0, offsetY, 0);

                if (!world.isInBuildLimit(potentialPos)) continue; // Ensure position is within world bounds

                BlockPos blockBelow = potentialPos.down();
                if (world.getBlockState(blockBelow).isSolid() && isAirSpaceClear(world, potentialPos)) {
                    return potentialPos;
                }
            }
        }

        return null; // Return null if no valid position is found
    }

    private static boolean isAirSpaceClear(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 3; i++) { // Check three blocks above the position
            if (!world.isAir(pos.up(i))) {
                return false;
            }
        }
        return true;
    }
}
