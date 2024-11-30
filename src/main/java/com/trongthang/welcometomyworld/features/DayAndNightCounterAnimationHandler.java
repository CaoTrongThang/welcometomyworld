package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

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

    private boolean canAnimateNight = true;

    private int tickRandomBound = 20;
    private int tickRandomMin = 10;
    private int delayTick = 0;
    private int currentTick = 0;

    Random rand = new Random();

    public void resetFields() {
        delayTick = 0;
        currentTick = 0;
        counter = 0;
        currentDay = 0;
        currentCharIndex = 0;
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
        if (counter < checkInterval) return;
        counter = 0;

        long currentTime = world.getTimeOfDay();  // Get the current time in the world (in ticks)



        long currentTimeInDay = currentTime % 24000;

        // Check if it's the start of a new day
        if (currentTimeInDay < 13000 && currentTime / TICKS_IN_DAY > currentDay && !isAnimatingDay) {
            currentDay = (int) (currentTime / TICKS_IN_DAY);
            startDayAnimation(world, currentDay);
        }

        if(currentTime / TICKS_IN_DAY > currentDay){
            canAnimateNight = true;
        }

        if (canAnimateNight && currentTimeInDay >= 13000 && currentTimeInDay < 23999 && !isAnimatingNight) {
            startNightAnimation(world);
            currentDay = (int) (currentTime / TICKS_IN_DAY);
            canAnimateNight = false;
        }
    }

    private void startDayAnimation(ServerWorld world, int day) {
        animationText = "DAY " + day;  // Prepare the text to be animated for day
        currentCharIndex = 0;  // Reset the index
        delayTick = 0;
        isAnimatingDay = true;  // Start the animation
    }

    private void startNightAnimation(ServerWorld world) {
        // Prepare the "Nighttime is coming..." message split into words.
        currentCharIndex = 0;
        delayTick = 0;
        isAnimatingNight = true;  // Start the animation

    }

    private void sendAnimationUpdate(ServerWorld world, String textToSend, boolean nighttime) {
        // Send the current part of the text to all players
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (nighttime) {
                player.sendMessage(Text.literal(textToSend).formatted(Formatting.DARK_RED), true);
            } else {
                textToSend = "- " + textToSend + " -";
                player.sendMessage(Text.literal(textToSend).formatted(Formatting.WHITE), true);
            }
            ServerPlayNetworking.send(player, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
        }

        if (nighttime) {
            delayTick = rand.nextInt(tickRandomMin * 2, tickRandomBound * 2); // Random delay for animation
        } else {
            delayTick = rand.nextInt(tickRandomMin, tickRandomBound); // Random delay for animation
        }

    }
}
