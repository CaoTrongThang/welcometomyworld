package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class EventsOfTheWorld {

    public static final int EVENT_COOLDOWN_IN_TICKS = 48000;
    public static final double HAPPEN_CHANCE = 0.2;

    private static final Map<String, Consumer<ServerWorld>> EVENT_MAP = new HashMap<>();
    private static int ticksSinceLastEvent = 0;

    public static final int stopEventsDay = 100;

    // Static block for default event registration
    static {
        registerEvent("FIELD_OF_GOLDEN_MOTHS", EventsOfTheWorld::fieldOfGoldenMoths);
        registerEvent("CIRCLE_OF_DEATHS", EventsOfTheWorld::circleOfDeaths);
        registerEvent("RAIN_OF_FOOD", EventsOfTheWorld::rainOfFood);
        registerEvent("ANIMALS_RISING", EventsOfTheWorld::animalRaising);

    }

    // Method to register events dynamically
    public static void registerEvent(String name, Consumer<ServerWorld> eventLogic) {
        EVENT_MAP.put(name, eventLogic);
    }

    // Server tick method
    public static void onServerTick(MinecraftServer server) {
        ticksSinceLastEvent++;

        if (ticksSinceLastEvent >= EVENT_COOLDOWN_IN_TICKS) {
            ticksSinceLastEvent = 0;

            if (WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay >= stopEventsDay) {
                return;
            }


            if (random.nextDouble() < HAPPEN_CHANCE) {
                triggerRandomEvent(server.getOverworld());
            }

        }
    }

    // Trigger a random event
    private static void triggerRandomEvent(ServerWorld world) {
        if (EVENT_MAP.isEmpty()) {
            return;
        }
        Object[] eventNames = EVENT_MAP.keySet().toArray();
        String randomEventName = (String) eventNames[random.nextInt(eventNames.length)];
        Consumer<ServerWorld> eventLogic = EVENT_MAP.get(randomEventName);

        if (eventLogic != null) {
            eventLogic.accept(world);
        }
    }

    public static void circleOfDeaths(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        boolean mobSpawned = false;

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return;

        World w = targetPlayer.getWorld();
        if (!(w.getRegistryKey() == World.OVERWORLD)) return;

        int numberOfMobs = 20; // Number of zombies in the circle
        double radius = 36.0;    // Radius of the circle

        for (int i = 0; i < numberOfMobs; i++) {
            double angle = 2 * Math.PI * i / numberOfMobs; // Evenly distribute zombies around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);

            BlockPos spawnPos = Utils.findSafeSpawnHostileMobPositionAroundTheCenterPos(world, new Vec3d(x, targetPlayer.getY(), z), 5);
            Entity zombie = spawnMob(world, spawnPos, "minecraft:zombie"); // Custom spawnMob utility

            if (zombie != null && zombie instanceof MobEntity mob) {
                mob.getNavigation().startMovingTo(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), 1f);
                addRunAfter(() -> Utils.discardEntity(world, mob), 3000);
                mobSpawned = true;
            }
        }

        if(mobSpawned){
            targetPlayer.sendMessage(
                    Text.literal("They're around you...").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
            );
        }
    }

    public static void circleOfPhantom(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            System.out.println("No players available for Circle of Phantoms event.");
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return;

        int numberOfPhantoms = 3; // Number of phantoms in the circle
        double radius = 48.0;     // Radius of the circle
        double heightOffset = 0; // Height above the player

        targetPlayer.sendMessage(
                Text.literal("Something is on the sky...").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
        );

        for (int i = 0; i < numberOfPhantoms; i++) {
            double angle = 2 * Math.PI * i / numberOfPhantoms; // Evenly distribute phantoms around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);
            double y = targetPlayer.getY() + heightOffset; // Spawn above the player

            BlockPos spawnPos = new BlockPos((int) x, (int) y, (int) z);
            if (!Utils.isSafeSpawn(world, spawnPos)) continue;

            Entity phantom = spawnMob(world, spawnPos, "minecraft:phantom"); // Custom spawnMob utility

            if (phantom != null && phantom instanceof MobEntity mob) {
                addRunAfter(() -> Utils.discardEntity(world, mob), 4000);
            }
        }
    }

    public static void rainOfFood(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            System.out.println("No players available for Rain of Food event.");
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return;

        int numberOfItems = 30; // Total number of food items
        double spawnRadius = 48.0; // Radius within which food will "rain"
        double heightOffset = 64.0; // Height from which the food falls

        // List of food items to rain
        ItemStack[] foodItems = {
                new ItemStack(Items.APPLE, 1),
                new ItemStack(Items.BREAD, 1),
                new ItemStack(Items.COOKED_BEEF, 1),
                new ItemStack(Items.CARROT, 1),
                new ItemStack(Items.POTATO, 1),
                new ItemStack(Items.COOKED_CHICKEN, 1)
        };

        for (int i = 0; i < numberOfItems; i++) {
            // Random position within the radius
            Utils.addRunAfter(() -> {
                        double angle = 2 * Math.PI * random.nextDouble();
                        double distance = spawnRadius * random.nextDouble();
                        double x = targetPlayer.getX() + distance * Math.cos(angle);
                        double z = targetPlayer.getZ() + distance * Math.sin(angle);
                        double y = targetPlayer.getY() + heightOffset;

                        Vec3d spawnPos = new Vec3d(x, y, z);

                        // Random food item
                        ItemStack chosenFood = foodItems[random.nextInt(foodItems.length)];

                        // Use SpawnItem to spawn the food
                        SpawnItem(world, spawnPos, chosenFood);
                    }
                    , i * 10);
        }
    }

    public static void animalRaising(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            System.out.println("No players available for Animal Raising event.");
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));
        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return;

        int numberOfAnimals = 12;
        int packs = 3;
        double minRadius = 50.0;  // Minimum distance from player
        double maxRadius = 70.0;  // Maximum distance from player
        String[] animalTypes = {
                "minecraft:cow",
                "minecraft:sheep",
                "minecraft:chicken",
                "minecraft:pig"
        };

        targetPlayer.sendMessage(
                Text.literal("Even the kindest soul must rise up...").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
        );

        for (int i = 0; i < packs; i++) {
            // Calculate pack center position
            double angle = 2 * Math.PI * random.nextDouble();
            double distance = minRadius + (maxRadius - minRadius) * random.nextDouble();
            double centerX = targetPlayer.getX() + distance * Math.cos(angle);
            double centerZ = targetPlayer.getZ() + distance * Math.sin(angle);

            for (int j = 0; j < numberOfAnimals / packs; j++) {
                // Generate position within pack cluster
                double packSpread = 8.0;  // Max spread from pack center
                double x = centerX + (random.nextDouble() - 0.5) * packSpread * 2;
                double z = centerZ + (random.nextDouble() - 0.5) * packSpread * 2;

                // Find safe spawn position with proper Y coordinate
                BlockPos spawnPos = findSafeSpawnHostileMobPositionAroundTheCenterPos(
                        world,
                        new Vec3d(x, targetPlayer.getY(), z),
                        10  // Increased search radius for safe spot
                );

                if (spawnPos == null) continue;

                String chosenAnimal = animalTypes[random.nextInt(animalTypes.length)];
                Entity animal = spawnMob(world, spawnPos, chosenAnimal);

                if (animal instanceof MobEntity mob) {
                    mob.setTarget(targetPlayer);
                    addRunAfter(() -> Utils.discardEntity(world, mob), 2000);
                }
            }
        }
    }

    public static void fieldOfGoldenMoths(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));
        if (targetPlayer.isCreative() || targetPlayer.isSpectator()) return;

        UTILS.sendTextAfter(targetPlayer, "It seems like the golden butterflies are welcome you.", 120);

        int numberOfGoldenMoths = 30; // Number of mob
        double radius = 15.0;    // Radius of the circle

        int counter = 0;

        for (int i = 0; i < numberOfGoldenMoths; i++) {
            counter = i * 40;

            double angle = 2 * Math.PI * i / numberOfGoldenMoths; // Evenly distribute zombies around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);

            BlockPos spawnPos = findSafeSpawnHostileMobPositionAroundTheCenterPos(world, new Vec3d((int) x, (int) targetPlayer.getY(), (int) z), 4);

            if (spawnPos == null) continue;

            addRunAfter(() -> {
                Entity goldenMoth = spawnMob(world, spawnPos, "aquamirae:golden_moth"); // Custom spawnMob utility

                if (goldenMoth != null && goldenMoth instanceof MobEntity mob) {
                    mob.setTarget(targetPlayer); // Make the mob target the player
                    addRunAfter(() -> Utils.discardEntity(world, mob), 3000);
                }
            }, counter);
        }
    }
}
