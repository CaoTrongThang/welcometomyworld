package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class SpawnMonstersAtNight {

    private static int MAX_MONSTERS_FOR_EACH_PLAYER = 15;

    private static double chanceToHappen = 50;

    private static int monsterWillIncreasePerDay = 10;

    private static int increaseMonsterByDay = 1;

    private static int eachPlayerIncreaseMonster = 10;

    private static int monsterDespawnAfterTick = 2000;

    public static final int stopSpawningDay = 1000;

    private static List<MonsterSpawn> monsters = List.of(
            new MonsterSpawn("minecraft:zombie", 0),
            new MonsterSpawn("minecraft:skeleton", 2),
            new MonsterSpawn("minecraft:creeper", 4)
    );

    public static void spawnMonsters(ServerWorld world, int currentDay) {
        if (currentDay <= 0 || currentDay >= stopSpawningDay) return;

        if (random.nextInt(0, 100) > chanceToHappen) return;

        List<ServerPlayerEntity> players = world.getPlayers();

        int counter = 0;

        int totalMonstersWillSpawn = ((currentDay / monsterWillIncreasePerDay) * increaseMonsterByDay) + (players.size() * eachPlayerIncreaseMonster);

        int monsterWillSpawnForEachPlayer = totalMonstersWillSpawn / players.size();

        for (ServerPlayerEntity player : players) {
            World w = player.getWorld();
            if (!(w.getRegistryKey() == World.OVERWORLD)) continue;
            for (int y = 0; y <= monsterWillSpawnForEachPlayer; y++) {

                while (true) {
                    MonsterSpawn mon = monsters.get(random.nextInt(0, monsters.size()));
                    if (mon.data <= currentDay) {
                        BlockPos safePos = SpawnMonstersPackEveryMins.findSafeSpawnPosition(world, player.getBlockPos());

                        Entity entity;

                        if (safePos != null) {
                            entity = spawnMob(world, safePos, mon.id);
                        } else {
                            entity = null;
                        }

                        if (entity == null) return;
                        if (entity instanceof MobEntity mobEntity) {

                            mobEntity.initialize(world, world.getLocalDifficulty(safePos), SpawnReason.NATURAL, null, null);

                            if (mobEntity.canSee(player)) {
                                mobEntity.setTarget(player);
                            }
                            ;
                        }


                        addRunAfter(() -> Utils.discardEntity(world, entity), monsterDespawnAfterTick);

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
