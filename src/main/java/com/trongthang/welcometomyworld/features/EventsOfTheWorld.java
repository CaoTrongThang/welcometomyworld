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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class EventsOfTheWorld {

    public static final int EVENT_COOLDOWN_IN_TICKS = 24000;
    public static final double HAPPEN_CHANCE = 0.4;

    private static final Map<String, Consumer<ServerWorld>> EVENT_MAP = new HashMap<>();
    private static int ticksSinceLastEvent = 0;

    public static final int stopEventsDay = 300;

    // Static block for default event registration
    static {
        registerEvent("CIRCLE_OF_DEATHS", EventsOfTheWorld::circleOfDeaths);
        registerEvent("CIRCLE_OF_PHANTOMS", EventsOfTheWorld::circleOfPhantom);
        registerEvent("RAIN_OF_FOOD", EventsOfTheWorld::rainOfFood);
        registerEvent("ANIMALS_RISING", EventsOfTheWorld::animalRaising);
        registerEvent("ILLAGER_RAID", EventsOfTheWorld::illagerRaid);
        registerEvent("FIELD_OF_GOLDEN_MOTHS", EventsOfTheWorld::fieldOfGoldenMoths); //aquamirae:golden_moth
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

            if(WelcomeToMyWorld.dayAndNightCounterAnimationHandler.currentDay >= stopEventsDay){
                return;
            }

            server.getWorlds().forEach(world -> {
                if (random.nextDouble() < HAPPEN_CHANCE) {
                    triggerRandomEvent(world);
                }
            });
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
            LOGGER.info("Triggered event: " + randomEventName);
        }
    }

    public static void circleOfDeaths(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));


        targetPlayer.sendMessage(
                Text.literal("They're around you...").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
        );


        int numberOfMobs = 20; // Number of zombies in the circle
        double radius = 48.0;    // Radius of the circle

        for (int i = 0; i < numberOfMobs; i++) {
            double angle = 2 * Math.PI * i / numberOfMobs; // Evenly distribute zombies around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);

            BlockPos spawnPos = new BlockPos((int) x, (int) targetPlayer.getY(), (int) z);
            Entity zombie = spawnMob(world, spawnPos, "minecraft:zombie"); // Custom spawnMob utility

            if (zombie != null && zombie instanceof MobEntity mob) {
                mob.setTarget(targetPlayer); // Make the zombie target the player

                addRunAfter(mob::discard, 3000);
            }
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

        int numberOfPhantoms = 6; // Number of phantoms in the circle
        double radius = 64.0;     // Radius of the circle
        double heightOffset = 128; // Height above the player

        targetPlayer.sendMessage(
                Text.literal("Something is coming from the sky...").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
        );

        for (int i = 0; i < numberOfPhantoms; i++) {
            double angle = 2 * Math.PI * i / numberOfPhantoms; // Evenly distribute phantoms around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);
            double y = targetPlayer.getY() + heightOffset; // Spawn above the player

            BlockPos spawnPos =  new BlockPos((int) x, (int) y, (int) z) ;
            if(!Utils.isSafeSpawn(world, spawnPos)) continue;

            Entity phantom = spawnMob(world, spawnPos, "minecraft:phantom"); // Custom spawnMob utility

            if (phantom != null && phantom instanceof MobEntity mob) {
                mob.setTarget(targetPlayer); // Make the phantom target the player

                addRunAfter(mob::discard, 4000);
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

        int numberOfItems = 20; // Total number of food items
        double spawnRadius = 10.0; // Radius within which food will "rain"
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

        System.out.println("Rain of Food event triggered for player: " + targetPlayer.getName().getString());
    }

    public static void animalRaising(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            System.out.println("No players available for Animal Raising event.");
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        int numberOfAnimals = 12; // Total number of animals to spawn
        int spawnRadius = 64;    // Spawn radius around the player
        int packs = 3;           // Number of packs
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
            for (int j = 0; j < numberOfAnimals / packs; j++) {
                double angle = 2 * Math.PI * random.nextDouble(); // Random angle for distribution
                double distance = random.nextDouble() * spawnRadius; // Random distance within radius

                double x = targetPlayer.getX() + distance * Math.cos(angle);
                double z = targetPlayer.getZ() + distance * Math.sin(angle);
                double y = world.getTopY() + 1; // Spawn just above the ground

                BlockPos spawnPos = findSafeSpawnPositionAroundTheCenterPos(world, new Vec3d((int) x, (int) targetPlayer.getY(), (int) z), 4) ;

                if(spawnPos == null) continue;

                String chosenAnimal = animalTypes[random.nextInt(animalTypes.length)]; // Random animal type

                Entity animal = spawnMob(world, spawnPos, chosenAnimal); // Custom spawnMob utility
                if (animal != null && animal instanceof MobEntity mob) {
                    mob.setTarget(targetPlayer); // Optional: Animals target the player

                    //Mob will despawn after 2000 ticks
                    addRunAfter(mob::discard, 2000);
                }
            }
        }

        System.out.println("Animal Raising event triggered for player: " + targetPlayer.getName().getString());
    }

    public static void illagerRaid(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            System.out.println("No players available for Illager Raid event.");
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        int numberOfIllagers = 10; // Total number of illagers
        int spawnRadius = 20;     // Spawn radius around the player
        String[] illagerTypes = {
                "minecraft:evoker",
                "minecraft:vindicator",
                "minecraft:pillager",
                "minecraft:illusioner" // For fun, even if not vanilla accessible
        };

        for (int i = 0; i < numberOfIllagers; i++) {
            double angle = 2 * Math.PI * random.nextDouble(); // Random angle for distribution
            double distance = random.nextDouble() * spawnRadius; // Random distance within radius

            double x = targetPlayer.getX() + distance * Math.cos(angle);
            double z = targetPlayer.getZ() + distance * Math.sin(angle);
            double y = world.getTopY() + 1; // Spawn just above the ground

            BlockPos spawnPos = findSafeSpawnPositionAroundTheCenterPos(world, new Vec3d((int) x, (int) targetPlayer.getY(), (int) z), 4) ;

            if(spawnPos == null) continue;

            String chosenIllager = illagerTypes[random.nextInt(illagerTypes.length)]; // Random illager type

            Entity illager = spawnMob(world, spawnPos, chosenIllager); // Custom spawnMob utility
            if (illager != null && illager instanceof MobEntity mob) {
                mob.setTarget(targetPlayer); // Make illagers target the player

                addRunAfter(mob::discard, 2000);
            }
        }

        LOGGER.info("Illager Raid event triggered for player: " + targetPlayer.getName().getString());
    }

    public static void fieldOfGoldenMoths(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Pick a random player
        ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

        UTILS.sendTextAfter(targetPlayer, "It seems like the golden butterflies are welcome you.", 120);

        int numberOfGoldenMoths = 30; // Number of mob
        double radius = 15.0;    // Radius of the circle

        int counter = 0;

        for (int i = 0; i < numberOfGoldenMoths; i++) {
            counter = i * 40;

            double angle = 2 * Math.PI * i / numberOfGoldenMoths; // Evenly distribute zombies around the circle
            double x = targetPlayer.getX() + radius * Math.cos(angle);
            double z = targetPlayer.getZ() + radius * Math.sin(angle);

            BlockPos spawnPos = findSafeSpawnPositionAroundTheCenterPos(world, new Vec3d((int) x, (int) targetPlayer.getY(), (int) z), 4) ;

            if(spawnPos == null) continue;

            addRunAfter(() -> {
                Entity goldenMoth = spawnMob(world, spawnPos, "aquamirae:golden_moth"); // Custom spawnMob utility

                if (goldenMoth != null && goldenMoth instanceof MobEntity mob) {
                    mob.setTarget(targetPlayer); // Make the mob target the player
                    addRunAfter(mob::discard, 3000);
                }
            }, counter);

        }

        LOGGER.info("Field Of Golden Moths triggered for player: " + targetPlayer.getName().getString());
    }
}
