package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

public class PlayerDeathMobSpawner {

    private static final Random RANDOM = new Random();

    public static void registerEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) {
                // Spawn mob only if player is in survival mode
                if (!player.isCreative() && !player.isSpectator()) {
                    ServerWorld world = player.getServerWorld();

                    EntityType<?>[] mobTypes = { EntityType.ZOMBIE, EntityType.SKELETON };
                    EntityType<?> selectedType = mobTypes[RANDOM.nextInt(mobTypes.length)];

                    MobEntity spawnedMob = (MobEntity) selectedType.create(world);
                    if (spawnedMob != null) {
                        spawnedMob.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(),
                                player.getYaw(), player.getPitch());
                        world.spawnEntity(spawnedMob);
                    }
                }
            }
        });
    }
}
