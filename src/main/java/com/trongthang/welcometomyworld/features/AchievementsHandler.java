package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dayAndNightCounterAnimationHandler;

public class AchievementsHandler {

    private int checkInterval = 120;
    private int counter = 0;

    public void onServerTick(MinecraftServer server) {
        counter++;
        if(counter < checkInterval) return;
        counter = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkDayMilestone(player);
        }
    }

    private void checkDayMilestone(ServerPlayerEntity player) {
        if (dayAndNightCounterAnimationHandler.currentDay >= 1 && dayAndNightCounterAnimationHandler.currentDay < 50) {
            Utils.grantAdvancement(player, "day_" + 1);
        } else if (dayAndNightCounterAnimationHandler.currentDay >= 50 && dayAndNightCounterAnimationHandler.currentDay < 100) {
            Utils.grantAdvancement(player, "day_" + 50);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 100 && dayAndNightCounterAnimationHandler.currentDay < 200) {
            Utils.grantAdvancement(player, "day_" + 100);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 200 && dayAndNightCounterAnimationHandler.currentDay < 300) {
            Utils.grantAdvancement(player, "day_" + 200);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 300 && dayAndNightCounterAnimationHandler.currentDay < 400) {
            Utils.grantAdvancement(player, "day_" + 300);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 400 && dayAndNightCounterAnimationHandler.currentDay < 500) {
            Utils.grantAdvancement(player, "day_" + 400);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 500 && dayAndNightCounterAnimationHandler.currentDay < 600) {
            Utils.grantAdvancement(player, "day_" + 500);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 600 && dayAndNightCounterAnimationHandler.currentDay < 700) {
            Utils.grantAdvancement(player, "day_" + 600);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 700 && dayAndNightCounterAnimationHandler.currentDay < 800) {
            Utils.grantAdvancement(player, "day_" + 700);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 800 && dayAndNightCounterAnimationHandler.currentDay < 900) {
            Utils.grantAdvancement(player, "day_" + 800);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 900 && dayAndNightCounterAnimationHandler.currentDay < 1000) {
            Utils.grantAdvancement(player, "day_" + 900);
        }else if (dayAndNightCounterAnimationHandler.currentDay >= 1000) {
            Utils.grantAdvancement(player, "day_" + 1000);
        }
    }
}
