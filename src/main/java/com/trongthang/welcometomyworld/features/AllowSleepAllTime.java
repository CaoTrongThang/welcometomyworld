package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AllowSleepAllTime {
    public static final int nightTime = 13000;
    public static final int dawnTime = 24000;
    private static final Logger LOGGER = LogManager.getLogger("AllowSleepAllTime");

    private static int totalSleepPlayers = 0;
    private static boolean canCheck = false;

    // Track sleep state PER DIMENSION
    private static final Map<Identifier, Boolean> dimensionSleepChecks = new HashMap<>();

    public static void registerEvents() {
        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPos, vanillaResult) -> {
            Identifier dimensionId = player.getWorld().getRegistryKey().getValue();

            dimensionSleepChecks.put(dimensionId, true);
            return ActionResult.SUCCESS;
        });


        EntitySleepEvents.START_SLEEPING.register((livingEntity, blockPos) -> {
                canCheck = true;
        });

        EntitySleepEvents.STOP_SLEEPING.register((livingEntity, blockPos) -> {
            Identifier dimensionId = livingEntity.getWorld().getRegistryKey().getValue();

            dimensionSleepChecks.remove(dimensionId); // Disable checks for this dimension
            canCheck = false;
        });

        ServerTickEvents.START_SERVER_TICK.register(AllowSleepAllTime::onServerTick);
    }

    public static void onServerTick(MinecraftServer server) {
        // Process ALL loaded worlds
        if(!canCheck) return;

        for (ServerWorld world : server.getWorlds()) {
            Identifier dimensionId = world.getRegistryKey().getValue();

            if (!dimensionSleepChecks.getOrDefault(dimensionId, false)) {
                continue;
            }

            // Check players IN THIS DIMENSION
            boolean allPlayersSleeping = false;

            for(ServerPlayerEntity p : world.getPlayers()){
                boolean isSleeping = p.isSleeping();
                boolean canResetTime = p.canResetTimeBySleeping();

                if(isSleeping && canResetTime){
                    allPlayersSleeping = true;
                } else {
                    allPlayersSleeping = false;
                }
            }

            if (!allPlayersSleeping) {
                continue;
            }

            long currentTime = world.getTimeOfDay() % 24000;
            long newTime = world.getTimeOfDay();

            if (world.isNight()) {
                newTime = world.getTimeOfDay() + (24000 - currentTime);
            } else {
                if (currentTime < nightTime) {
                    newTime = world.getTimeOfDay() + (nightTime - currentTime);
                } else {
                    newTime = world.getTimeOfDay() + ((24000 - currentTime) + nightTime);
                }
            }

            world.setTimeOfDay(newTime);
            world.setWeather(0, 0, false, false);

            // Wake up players IN THIS DIMENSION only
            wakeUpPlayersInDimension(world);
            dimensionSleepChecks.remove(dimensionId);
        }
    }

    private static void wakeUpPlayersInDimension(ServerWorld world) {
        world.getPlayers().forEach(player -> {
            player.wakeUp();
        });
    }
}