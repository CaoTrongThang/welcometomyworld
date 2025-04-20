package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.MonsterSpawn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;

import java.util.List;

import static com.trongthang.welcometomyworld.Utilities.Utils.addRunAfter;
import static com.trongthang.welcometomyworld.Utilities.Utils.spawnMob;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;
import static com.trongthang.welcometomyworld.features.SpawnMonstersPackEveryMins.*;

public class SpawnSingleMonsterEverySeconds {
    private static int cooldown = 1000;
    private static int counter = 0;
    private static double spawnChance = 0.5;

    public static void spawnMonsters(ServerWorld world, int currentDay) {
        if (currentDay <= 1 || currentDay >= ConfigLoader.getInstance().hostileMobsEventsStopSpawningDay) return;

        counter++;
        if (counter < cooldown) return;
        counter = 0;

        if (world.isDay()) return;
        if (world.getPlayers().isEmpty()) return;

        ServerPlayerEntity player = world.getPlayers().get(random.nextInt(world.getPlayers().size()));

        if (player.isCreative() || player.isSpectator() || player.isDead()) return;

        MonsterSpawn mon = monsters.get(random.nextInt(monsters.size()));

        EntityType<?> entityType = EntityType.get(mon.getId()).orElse(null);
        if (entityType == null) return;

        if(random.nextDouble() > spawnChance) return;

        Entity entity;
        BlockPos spawnPos = findSafeSpawnPositionByPack(world, player.getBlockPos(), entityType);

        if (spawnPos == null) return;

        entity = spawnMob(world, spawnPos, mon.id);

        MobEntity mobEntity = (MobEntity) entity;

        mobEntity.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.NATURAL, null, null);

        if (mobEntity.canSee(player)) {
            mobEntity.setTarget(player);
        }

        tryDiscardSpawnedMobAfterTime(world, mobEntity, 2500);
    }
}
