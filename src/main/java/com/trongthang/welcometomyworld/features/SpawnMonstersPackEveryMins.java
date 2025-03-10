package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static com.trongthang.welcometomyworld.Utilities.Utils.addRunAfter;
import static com.trongthang.welcometomyworld.Utilities.Utils.spawnMob;

public class SpawnMonstersPackEveryMins {

    private static double CHANCE_TO_HAPPEN = 50;

    private static int COOLDOWN = 2400;
    private static int counter = 0;

    private static final int PACK_MIN_SIZE = 7;
    private static final int PACK_MAX_SIZE = 25;

    private static final int MIN_SPAWN_DISTANCE = 32;
    private static final int MAX_SPAWN_DISTANCE = 64;

    public static final int stopSpawningDay = 2000;

    private static final List<MonsterSpawn> MONSTERS = List.of(
            new MonsterSpawn("minecraft:zombie", 50),
            new MonsterSpawn("minecraft:skeleton", 30),
            new MonsterSpawn("minecraft:creeper", 30),
            new MonsterSpawn("minecraft:pillager", 15),
            new MonsterSpawn("minecraft:vindicator", 10),
            new MonsterSpawn("derelict:spiny_spider", 2)
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

        if(CHANCE_TO_HAPPEN <= 100){
            CHANCE_TO_HAPPEN += (double) DayAndNightCounterAnimationHandler.getCurrentDay(server.getOverworld());
            if (RANDOM.nextInt(100) >= (CHANCE_TO_HAPPEN)) return;
        }


        List<ServerPlayerEntity> players = world.getPlayers();

        if (players.isEmpty()) return;

        ServerPlayerEntity player = players.get(RANDOM.nextInt(players.size()));

        World w = player.getWorld();
        if(!(w.getRegistryKey() == World.OVERWORLD)) return;

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
                mob.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null, null);
            }

            if (entity instanceof MobEntity mob) {
                if(mob.canSee(player)) {
                    mob.setTarget(player);
                };
            }

            addRunAfter(() -> Utils.discardEntity(world, entity), COOLDOWN);
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
        final int MAX_ATTEMPTS = 30; // Maximum attempts to find a valid position
        final int VERTICAL_SEARCH_RANGE = 10; // Range to search vertically if needed

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            double distance = MIN_SPAWN_DISTANCE + RANDOM.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            double angle = RANDOM.nextDouble() * 2 * Math.PI;

            int offsetX = (int) (Math.cos(angle) * distance);
            int offsetZ = (int) (Math.sin(angle) * distance);

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
