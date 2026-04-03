package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.GlobalConfig;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

// DON'T READ THIS CODE, I DON'T KNOW WHAT I'M DOING, AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA, but it works lol
public class DayAndNightCounterAnimationHandler {

    private static final String NIGHT_PREFIX = "Nighttime is coming... ";
    public int currentDay = 0;
    private static final long TICKS_IN_DAY = 24000L;

    private int checkInterval = 40;
    private int counter = 0;

    // Animation tracking variables
    private String animationText = "";
    private int currentCharIndex = 0;

    private boolean isAnimatingDay = false;
    private boolean isAnimatingNight = false;

    private boolean dayAnimationComplete = false;
    private boolean nightAnimationComplete = false;

    private int tickRandomBound = 25;
    private int tickRandomMin = 15;
    private int delayTick = 0;
    private int currentTick = 0;

    Random rand = new Random();

    public void resetFields() {
        delayTick = 0;
        currentTick = 0;
        counter = 0;
        currentDay = 0;
        currentCharIndex = 0;
        dayAnimationComplete = false;
        nightAnimationComplete = false;

    }

    // Main method that checks for day changes every tick
    public void onServerTick(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        if (isAnimatingDay || isAnimatingNight) {
            currentTick++;
            if (currentTick > delayTick) {
                if (isAnimatingDay) {
                    if (currentCharIndex < animationText.length()) {
                        String textToSend = animationText.substring(0, currentCharIndex + 1);
                        sendAnimationUpdate(world, textToSend, false);
                        currentCharIndex++;
                    } else {
                        isAnimatingDay = false; // Stop once the animation finishes
                        ServerPlayNetworking.send(player, PLAY_BELL, PacketByteBufs.empty());
                    }
                }

                if (isAnimatingNight) {
                    String[] words = NIGHT_PREFIX.split(" ");
                    if (currentCharIndex < words.length) {
                        sendAnimationUpdate(world, words[currentCharIndex], true);
                        currentCharIndex++;

                    } else {
                        isAnimatingNight = false; // Stop once the animation finishes
                        ServerPlayNetworking.send(player, PLAY_WOLF_HOWL, PacketByteBufs.empty());
                    }
                }
                currentTick = 0;
            }
        }

        counter++;
        if (counter < checkInterval)
            return;
        counter = 0;

        long currentTime = world.getTimeOfDay(); // Get the current time in the world (in ticks)
        long currentTimeInDay = currentTime % 24000;

        // Safety check: Blood moon should only be active at night
        if (WelcomeToMyWorld.dataHandler.worldData.isBloodMoon && currentTimeInDay < 13000) {
            WelcomeToMyWorld.dataHandler.worldData.isBloodMoon = false;
            WelcomeToMyWorld.dataHandler.saveWorldData();
            sendBloodMoonPacket(world, false);
        }

        // Check if it's the start of a new day
        if (currentTimeInDay < 13000 && !isAnimatingDay
                && (currentTime / TICKS_IN_DAY > currentDay || !dayAnimationComplete)) {
            currentDay = (int) (currentTime / TICKS_IN_DAY);
            dayAnimationComplete = true;
            nightAnimationComplete = false;

            // Blood moon ends at dawn — notify all players
            if (WelcomeToMyWorld.dataHandler.worldData.isBloodMoon) {
                WelcomeToMyWorld.dataHandler.worldData.isBloodMoon = false;
                WelcomeToMyWorld.dataHandler.saveWorldData();
                sendBloodMoonPacket(world, false);
            }

            if (currentDay == ConfigLoader.getInstance().hostileMobsEventsStopSpawningDay) {
                for (ServerPlayerEntity p : world.getPlayers()) {
                    Utils.UTILS.sendTextAfter(p, "You did a great job, The nighttime will be easier for you.", 20);
                }
            }

            if (currentDay > 0) {
                startDayAnimation(world, currentDay);
            }
        }

        if ((currentTimeInDay >= 13000 && currentTimeInDay < 23999) && !isAnimatingNight
                && (currentTime / TICKS_IN_DAY > currentDay || !nightAnimationComplete)) {
            startNightAnimation(world);

            // Roll blood moon chance at the start of each night
            ConfigLoader.BloodMoonConfig bmCfg = ConfigLoader.getInstance().bloodMoon;

            // Whitelist check
            String worldId = world.getRegistryKey().getValue().toString();
            boolean isWhitelisted = bmCfg.bloodMoonWorldWhitelist.contains(worldId);

            if (WelcomeToMyWorld.dataHandler.worldData.isBloodMoon) {
                // If it's already true (e.g. forced via command or loaded from previous session
                // during night)
                sendBloodMoonPacket(world, true);
            } else if (isWhitelisted && !bmCfg.disableBloodMoon && rand.nextFloat() < bmCfg.bloodMoonChance) {
                WelcomeToMyWorld.dataHandler.worldData.isBloodMoon = true;
                WelcomeToMyWorld.dataHandler.saveWorldData();
                sendBloodMoonPacket(world, true);
            }

            if (GlobalConfig.canSpawnMonstersAtNight) {
                SpawnMonstersAtNight.spawnMonsters(world, currentDay);
            }

            currentDay = (int) (currentTime / TICKS_IN_DAY);
            dayAnimationComplete = false;
            nightAnimationComplete = true;

            MinecellsDimensionSarcastic.kickIfPlayerInMinecell(player.getServer());
        }
    }

    public static int getCurrentDay(World world) {
        long currentTime = world.getTimeOfDay(); // Get the current time in the world (in ticks)
        return (int) (currentTime / TICKS_IN_DAY);
    }

    private void startDayAnimation(ServerWorld world, int day) {
        animationText = "- DAY " + day + " -"; // Hyphens included here
        currentCharIndex = 0;
        delayTick = 0;
        isAnimatingDay = true;
    }

    private void startNightAnimation(ServerWorld world) {
        // Prepare the "Nighttime is coming..." message split into words.
        currentCharIndex = 0;
        delayTick = 0;
        isAnimatingNight = true; // Start the animation
    }

    private void sendBloodMoonPacket(ServerWorld world, boolean active) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(active);
        buf.writeBoolean(false);
        for (ServerPlayerEntity p : world.getPlayers()) {
            ServerPlayNetworking.send(p, WelcomeToMyWorld.BLOOD_MOON_SYNC, buf);
        }
    }

    private void sendAnimationUpdate(ServerWorld world, String textToSend, boolean nighttime) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (nighttime) {
                player.sendMessage(Text.literal(textToSend).formatted(Formatting.DARK_RED), true);
            } else {
                // Remove hyphen addition here
                player.sendMessage(Text.literal(textToSend).formatted(Formatting.WHITE), true);
            }
            ServerPlayNetworking.send(player, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
        }

        if (nighttime) {
            delayTick = rand.nextInt(tickRandomMin * 2, tickRandomBound * 2);
        } else {
            delayTick = rand.nextInt(tickRandomMin, tickRandomBound);
        }
    }
}
