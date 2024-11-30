package com.trongthang.welcometomyworld.features;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dayAndNightCounterAnimationHandler;

public class WorldDifficultyBasedOnInGameDay {
    public boolean firstTimeCheck = false;
    public boolean stopChangingDifficultyBasedOnDay = false;

    public int checkInterval = 60;
    public int counter;

    public void onServerTick(MinecraftServer server){
        counter++;
        if(counter < checkInterval) return;
        counter = 0;
        if(stopChangingDifficultyBasedOnDay) return;

        if(firstTimeCheck == false && dayAndNightCounterAnimationHandler.currentDay >= 10){
            firstTimeCheck = true;
            stopChangingDifficultyBasedOnDay = true;
        }
        Difficulty currentDifficulty = server.getOverworld().getDifficulty();

        if(dayAndNightCounterAnimationHandler.currentDay >= 0 &&  dayAndNightCounterAnimationHandler.currentDay < 5 && currentDifficulty != Difficulty.EASY){
            server.setDifficulty(Difficulty.EASY, true);
        }

        if(dayAndNightCounterAnimationHandler.currentDay >= 3 &&  dayAndNightCounterAnimationHandler.currentDay < 15 && currentDifficulty != Difficulty.NORMAL){
            server.setDifficulty(Difficulty.NORMAL, true);
        }

        if(dayAndNightCounterAnimationHandler.currentDay >= 15 && currentDifficulty != Difficulty.HARD){
            server.setDifficulty(Difficulty.HARD, true);
            stopChangingDifficultyBasedOnDay = true;
        }
    }
}
