package com.trongthang.welcometomyworld.features;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

import static com.trongthang.welcometomyworld.Utilities.Utils.applyEffect;
import static com.trongthang.welcometomyworld.Utilities.Utils.summonLightning;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class PowerUpNearByHostileMobs {

    private static final double POWER_UP_CHANCE = 0.17; // Chance for a mob to power up
    private static final int MAX_RADIUS = 8;  // Max radius to search for mobs (10 blocks around player)

    private static final int mobEffectDuration = 1200;

    private int checkInterval = 60; // Interval in ticks to check for mobs
    private int counter = 0; // Counter for checking


    public PowerUpNearByHostileMobs() {}

    // Method to check if mobs near the player should be powered up
    public void checkAndPowerUpMobs(ServerWorld world, ServerPlayerEntity player) {
        counter++;
        if (counter > checkInterval) {
            counter = 0;
            powerUpNearbyMobs(world, player);
        }
    }

    // Method to power up nearby mobs
    private void powerUpNearbyMobs(ServerWorld world, ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        Box searchBox = createSearchBox(playerPos);

        List<HostileEntity> nearbyMobs = getNearbyMobs(world, searchBox);

        for (HostileEntity mob : nearbyMobs) {
            if (mob instanceof MobEntity) {
                MobEntity mobEntity = (MobEntity) mob;

                // If the mob is targeting the player, check if it should be powered up
                if (isAggroedToPlayer(mobEntity, player)) {
                    if (random.nextDouble() < POWER_UP_CHANCE) {
                        powerUpMob(mobEntity, world);
                        spawnFlamesParticles(mobEntity);
                    }
                }
            }
        }
    }

    // Check if the mob is aggroed (targeting) the player
    private boolean isAggroedToPlayer(MobEntity mob, ServerPlayerEntity player) {
        return mob.getTarget() != null && mob.getTarget().equals(player);
    }

    // Create a box around the player to search for mobs
    private Box createSearchBox(BlockPos playerPos) {
        return new Box(playerPos.add(-MAX_RADIUS, -MAX_RADIUS, -MAX_RADIUS), playerPos.add(MAX_RADIUS, MAX_RADIUS, MAX_RADIUS));
    }

    // Get all nearby hostile mobs in the search area
    private List<HostileEntity> getNearbyMobs(ServerWorld world, Box searchBox) {
        return world.getEntitiesByClass(HostileEntity.class, searchBox, mob -> true);
    }

    // Power up a specific mob by giving it a random effect
    private void powerUpMob(MobEntity mob, ServerWorld world) {

        summonLightning(mob.getBlockPos(), world);
        //Apply effects 3 times
        applyEffect(mob, 3, mobEffectDuration);
    }

    private void spawnFlamesParticles(Entity entity){
        ServerWorld serverWorld = entity.getServer().getOverworld();

        for (int i = 0; i < 10; i++) { // Create 10 flame particles
            double offsetX = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random X offset
            double offsetY = serverWorld.getRandom().nextDouble() * 2;         // Random Y offset
            double offsetZ = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random Z offset

            serverWorld.spawnParticles(
                    ParticleTypes.FLAME,                      // Particle type
                    entity.getX() + offsetX,                    // X coordinate
                    entity.getY() + offsetY,                    // Y coordinate
                    entity.getZ() + offsetZ,                    // Z coordinate
                    1,                                        // Particle count
                    0.0, 0.0, 0.0,                           // No velocity
                    0.0                                      // Speed multiplier
            );
        }
    }
}
