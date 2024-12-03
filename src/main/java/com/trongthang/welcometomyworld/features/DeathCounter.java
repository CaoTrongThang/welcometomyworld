package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.saveData.PlayerClass;
import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

public class DeathCounter {

    public void startCountingDeaths(){

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof PlayerEntity) {
                ServerPlayerEntity player = entity.getServer().getPlayerManager().getPlayer(entity.getUuid());
                PlayerClass p = dataHandler.playerDataMap.get(entity.getUuid());

                p.deaths++;  // Increment death counter

                // First death - Give apple and message
                if(p.deaths == 1){
                    if(player.getWorld().getLevelProperties().isHardcore()){
                        Utils.UTILS.sendTextAfter(player, "Congrats! Your first death and also your last...");
                    } else {
                        Utils.UTILS.sendTextAfter(player, "Congrats! Your first death!");
                    }
                }

                if(p.deaths == 100){
                    Utils.UTILS.sendTextAfter(player, "Wow, 100 deaths already? You've truly mastered the art of dying.");
                }
                if(p.deaths == 200){
                    Utils.UTILS.sendTextAfter(player, "Still here after 200 deaths? I thought you'd give up by now!");
                }
                if(p.deaths == 300){
                    ItemStack lovedItem = null;
                    // Check player's inventory for loved items
                    for (ItemStack itemStack : player.getInventory().main) {
                        if (!itemStack.isEmpty()) {
                            lovedItem = itemStack;
                            break;
                        }
                    }
                    if(lovedItem != null){
                        Utils.UTILS.sendTextAfter(player, "300 deaths! You’ve been through so much, yet still carry that " + lovedItem.getName() + ". Impressive!");
                    } else {
                        Utils.UTILS.sendTextAfter(player, "300 deaths... and you still seem to find a way to survive. Incredible!");
                    }
                }

                if(p.deaths == 1000){
                    Utils.UTILS.sendTextAfter(player,"A thousand deaths, my friend. If this were a competition, you’d be *winning*. Have you tried not dying?", 4);
                    Utils.UTILS.sendTextAfter(player,"The mobs are starting a fan club for you. I hear they’re planning a parade... you’re a legend.",  3);
                    checkSurroundings(player);
                    Utils.UTILS.sendTextAfter(player,"I think, I'll be off for now, just have fun on your journey.",  6);
                }

                // Death-based interactions with difficulty
                Difficulty worldDifficulty = player.getWorld().getDifficulty();
                if (worldDifficulty == Difficulty.HARD) {
                    Utils.UTILS.sendTextAfter(player, "This is Hard mode! Your struggles are real.");
                } else if (worldDifficulty == Difficulty.EASY) {
                    Utils.UTILS.sendTextAfter(player, "Aww, you're in Easy mode. Deaths don't count as much, right?");
                }

                // Special message if the player died near a specific block (e.g., bed)
                if (player.getBlockPos().getY() <= 0) {
                    Utils.UTILS.sendTextAfter(player, "Did you really think dying in the void would be a good idea?");
                }
            }
        });
    }
    private void checkSurroundings(ServerPlayerEntity player) {
        String biome = player.getWorld().getRegistryKey().getValue().getPath();
        if(biome.contains("desert")) {
            Utils.UTILS.sendTextAfter(player, "A thousand deaths in the desert? You’re either a masochist or *really* bad at navigating.");
        } else if(biome.contains("forest")) {
            Utils.UTILS.sendTextAfter(player, "A thousand deaths in the forest? Is the wood too comfortable, or are the mobs just really good at hiding?");
        } else if(biome.contains("ocean")) {
            Utils.UTILS.sendTextAfter(player, "1000 deaths at sea... Is it the endless blue that’s drawing you in, or just your *drowning* instincts?");
        } else {
            Utils.UTILS.sendTextAfter(player, "I’ve got to hand it to you, surviving this long in a [biome] is almost an art form. Keep it up!");
        }
    }
}
