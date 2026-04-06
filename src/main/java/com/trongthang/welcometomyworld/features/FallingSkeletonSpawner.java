package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.entities.FallingSkeleton.FallingSkeleton;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class FallingSkeletonSpawner {
    private int counter = 0;
    private final int CHECK_INTERVAL = 100; // Check every 5 seconds
    private final int MAX_SKELETONS = 5;
    private final double SPAWN_CHANCE = 0.3; // 30% chance for a player to trigger a spawn every interval
    private final Random random = new Random();

    public void tick(MinecraftServer server) {
        counter++;
        if (counter < CHECK_INTERVAL)
            return;
        counter = 0;

        ServerWorld voidWorld = server.getWorld(VoidDimension.VOID_DIM_LEVEL_KEY);
        if (voidWorld == null)
            return;

        long count = voidWorld.getEntitiesByType(EntitiesManager.FALLING_SKELETON, e -> true).size();
        if (count >= MAX_SKELETONS)
            return;

        for (ServerPlayerEntity player : voidWorld.getPlayers()) {
            if (random.nextDouble() < SPAWN_CHANCE) {
                spawnAbovePlayer(voidWorld, player);
                count++;
                if (count >= MAX_SKELETONS)
                    break;
            }
        }
    }

    private void spawnAbovePlayer(ServerWorld world, ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();

        int offsetX = random.nextInt(31) - 15; // -15 to 15
        int offsetZ = random.nextInt(31) - 15; // -15 to 15
        int offsetY = random.nextInt(21) + 30; // 30 to 50

        BlockPos spawnPos = playerPos.add(offsetX, offsetY, offsetZ);

        if (world.isAir(spawnPos)) {
            FallingSkeleton skeleton = EntitiesManager.FALLING_SKELETON.create(world);
            if (skeleton != null) {
                skeleton.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                world.spawnEntity(skeleton);
            }
        }
    }
}
