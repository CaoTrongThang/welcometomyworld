package com.trongthang.welcometomyworld.Utilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SpawnParticiles {
    public static void spawnExpandingParticleSquare(ServerWorld world, Entity entity, int minSize, int maxSize, int durationTicks, DefaultParticleType particle) {
        // Get initial position at the player's feet
        BlockPos playerPos = entity.getBlockPos();
        Vec3d initialPos = new Vec3d(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);


        // Run a scheduled task to handle the expansion over time
        for (int tick = 0; tick <= durationTicks; tick++) {
            int currentTick = tick;
            Utils.addRunAfter(() -> {
                // Calculate current size based on tick progression
                double progress = (double) currentTick / durationTicks;
                int currentSize = minSize + (int) (progress * (maxSize - minSize));

                // Spawn particles along the edges of the square
                spawnSquareParticles(world, initialPos, currentSize, particle);
            }, currentTick);
        }
    }

    public static void spawnParticlesAroundEntity(LivingEntity livingEntity, ParticleEffect particle, double radius, float eyeHeightDivide) {
        if(particle == null) return;

        if (livingEntity.getWorld().isClient) {

            double x = livingEntity.getX();
            double y = 0;

            if(eyeHeightDivide > 0){
                y = livingEntity.getY() + livingEntity.getHeight() / eyeHeightDivide;
            } else {
                y = livingEntity.getY() + livingEntity.getHeight();
            }

            double z = livingEntity.getZ();

            // Spawn particles in a circle around the mob
            for (int i = 0; i < 5; i++) {  // Adjust number of particles as needed
                double angle = Math.random() * 2 * Math.PI;  // Random angle in radians

                double offsetX = radius * Math.cos(angle);
                double offsetZ = radius * Math.sin(angle);

                // Add particle at random angle and radius around the mob's position
                livingEntity.getWorld().addParticle(particle, x + offsetX, y, z + offsetZ, 0, 0, 0);
            }
        }
    }

    private static void spawnSquareParticles(ServerWorld world, Vec3d center, int size, DefaultParticleType particle) {
        if (size <= 0) return;

        double y = center.y;
        double minX = center.x - size;
        double maxX = center.x + size;
        double minZ = center.z - size;
        double maxZ = center.z + size;

        // Spawn particles along the edges of the square
        for (double x = minX; x <= maxX; x += 0.5) {
            // Spawn particles on the top and bottom edges
            world.spawnParticles(particle, x, y, minZ, 1, 0, 0, 0, 0);
            world.spawnParticles(particle, x, y, maxZ, 1, 0, 0, 0, 0);
        }

        for (double z = minZ; z <= maxZ; z += 0.5) {
            // Spawn particles on the left and right edges
            world.spawnParticles(particle, minX, y, z, 1, 0, 0, 0, 0);
            world.spawnParticles(particle, maxX, y, z, 1, 0, 0, 0, 0);
        }
    }
}
