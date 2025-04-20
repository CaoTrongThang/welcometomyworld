package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;
import static com.trongthang.welcometomyworld.features.SpawnMonstersPackEveryMins.*;

public class SpawnMonstersAtNight {

    private static int MAX_MONSTERS_FOR_EACH_PLAYER = 15;
    private static int MAX_MONSTERS = 40;

    private static double chanceToHappen = 50;
    private static int monsterWillIncreasePerDay = 10;
    private static int increaseMonsterByDay = 1;
    private static int eachPlayerIncreaseMonster = 10;
    private static int monsterDespawnAfterTick = 2000;

    public static void spawnMonsters(ServerWorld world, int currentDay) {
        if (currentDay <= 1 || currentDay >= ConfigLoader.getInstance().hostileMobsEventsStopSpawningDay) return;

        if (random.nextInt(0, 100) > chanceToHappen) return;

        List<ServerPlayerEntity> players = world.getPlayers();

        int counter = 0;
        int maxMonsterCounter = 0;

        int totalMonstersWillSpawn = ((currentDay / monsterWillIncreasePerDay) * increaseMonsterByDay) + (players.size() * eachPlayerIncreaseMonster);

        int monsterWillSpawnForEachPlayer = totalMonstersWillSpawn / players.size();

//        LOGGER.info("SPAWN AT NIGHT");

        for (ServerPlayerEntity player : players) {
            if(player.isCreative() || player.isSpectator() || player.isDead()) continue;

            World w = player.getWorld();
            if (!(w.getRegistryKey() == World.OVERWORLD)) continue;

            for (int y = 0; y <= monsterWillSpawnForEachPlayer; y++) {

                List<MonsterSpawn> availableMonsters = monsters.stream()
                        .filter(m -> m.spawnDay <= currentDay)
                        .toList();

                if (availableMonsters.isEmpty()) return;

                MonsterSpawn mon = availableMonsters.get(random.nextInt(availableMonsters.size()));

                EntityType<?> entityType = EntityType.get(mon.getId()).orElse(null);
                if (entityType == null) continue;

                BlockPos spawnPos = findSafeSpawnPositionByPack(world, player.getBlockPos(), entityType);
                if (spawnPos == null) {
                    continue;
                }

                Entity entity;

                if (spawnPos != null) {
                    entity = spawnMob(world, spawnPos, mon.id);
                } else {
                    continue;
                }

                MobEntity mobEntity = (MobEntity) entity;

                mobEntity.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null, null);

                    if (mobEntity.canSee(player)) {
                        mobEntity.setTarget(player);
                    }

                    tryDiscardSpawnedMobAfterTime(world, mobEntity, monsterDespawnAfterTick);

                counter++;
                maxMonsterCounter++;
                if (counter >= MAX_MONSTERS_FOR_EACH_PLAYER) break;
            }

            if(maxMonsterCounter > MAX_MONSTERS){
                break;
            }
            counter = 0;
        }
    }
}
