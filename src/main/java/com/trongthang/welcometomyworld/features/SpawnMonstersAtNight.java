package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.classes.MonsterSpawnAtDay;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class SpawnMonstersAtNight {

    private static final int SPAWN_DISTANCE = 48;

    private static int MAX_MONSTERS_FOR_EACH_PLAYER = 10;

    private static double chanceToHappen = 50;

    private static int monsterWillIncreasePerDay = 10;
    private static int increaseMonsterByDay = 1;

    private static int eachPlayerIncreaseMonster = 10;

    private static int monsterDespawnAfterTick = 2000;

    private static List<MonsterSpawnAtDay> monsters = List.of(
            new MonsterSpawnAtDay("minecraft:zombie", 0),
            new MonsterSpawnAtDay("minecraft:skeleton", 2),
            new MonsterSpawnAtDay("minecraft:creeper", 4)
    );

    public static void spawnMonsters(ServerWorld world, int currentDay) {
        if(random.nextInt(0, 100) > chanceToHappen) return;

        List<ServerPlayerEntity> players = world.getPlayers();

        int counter = 0;

        int totalMonstersWillSpawn = ((currentDay / monsterWillIncreasePerDay) * increaseMonsterByDay) + (players.size() * eachPlayerIncreaseMonster);

        int monsterWillSpawnForEachPlayer = totalMonstersWillSpawn / players.size();

        for (ServerPlayerEntity player : players) {
            for (int y = 0; y <= monsterWillSpawnForEachPlayer; y++) {

                while (true){
                    MonsterSpawnAtDay mon = monsters.get(random.nextInt(0, monsters.size()));
                    if(mon.dayToSpawn <= currentDay){
                        BlockPos safePos = findSafeSpawnPositionAroundTheCenterPos(world, player.getPos(), SPAWN_DISTANCE);
                        Entity entity = spawnMob(world, safePos, mon.id);

                        if(entity == null) return;

                        ((MobEntity) entity).setTarget(player);

                        addRunAfter(entity::discard, monsterDespawnAfterTick);

                        break;
                    }
                }

                counter++;
                if (counter >= MAX_MONSTERS_FOR_EACH_PLAYER) break;
            }

            counter = 0;
        }
    }
}
