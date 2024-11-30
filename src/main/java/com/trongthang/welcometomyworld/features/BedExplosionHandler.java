package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.saveData.PlayerClass;
import com.trongthang.welcometomyworld.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.item.Items;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

public class BedExplosionHandler {

    private static final Random randomGenerator = new Random();
    private static final double BASE_EXPLOSION_CHANCE = 0.8; // Base chance of explosion

    // Variables to modify the explosion chance
    private double havingLuckEffectChanceDecrease = 1.0;
    private double nearASleepingFriendChanceDecrease = 0.4;
    private double nearACampfireChanceDecrease = 0.2;
    private double holdingFlowerChanceDecrease = 0.4;

    // Interval to check if player is sleeping (in ticks)
    public int checkInterval = 60;
    private int counter = 0;

    public BedExplosionHandler() {}

    public void checkAndExplodeIfSleeping(ServerPlayerEntity player) {
        counter++;
        if (counter < checkInterval) return;
            counter = 0;

            // Ensure the player is still in a sleeping state
            if (player.isSleeping()) {
                // Calculate the effective explosion chance
                double effectiveExplosionChance = calculateExplosionChance(player);

                // Check if explosion happens based on the effective chance
                if (randomGenerator.nextDouble() < effectiveExplosionChance) {
                    triggerExplosion(player);
                }
            }

    }

    // Method to calculate the explosion chance based on various factors
    private double calculateExplosionChance(ServerPlayerEntity player) {
        double effectiveChance = BASE_EXPLOSION_CHANCE;

        // Apply the luck effect
        if (player.hasStatusEffect(StatusEffects.LUCK)) {
            effectiveChance -= havingLuckEffectChanceDecrease;
        }

        // Apply the near friends effect
        if (isNearOtherPlayers(player)) {
            effectiveChance -= nearASleepingFriendChanceDecrease;
        }

        // Apply the holding flower effect
        if (player.getMainHandStack().getItem() == Items.POPPY
                || player.getMainHandStack().getItem() == Items.DANDELION
                || player.getMainHandStack().getItem() == Items.OXEYE_DAISY
                || player.getMainHandStack().getItem() == Items.CORNFLOWER
                || player.getMainHandStack().getItem() == Items.CHORUS_FLOWER
                || player.getMainHandStack().getItem() == Items.SUNFLOWER
                || player.getMainHandStack().getItem() == Items.TORCHFLOWER) {
            effectiveChance -= holdingFlowerChanceDecrease;
        }

        // Apply the near campfire effect
        if (isNearCampfire(player)) {
            effectiveChance -= nearACampfireChanceDecrease;
        }

        // Ensure the chance doesn't go below 0
        return Math.max(0, effectiveChance);
    }

    // Method to check if the player is near another player (friend)
    private boolean isNearOtherPlayers(ServerPlayerEntity player) {
        World world = player.getWorld();
        for (ServerPlayerEntity otherPlayer : world.getServer().getPlayerManager().getPlayerList()) {
            if (!otherPlayer.equals(player)) {
                // Check if the player is within a 5-block radius of another player
                if (player.squaredDistanceTo(otherPlayer) < 100 && player.isSleeping()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Method to check if the player is near a campfire
    private boolean isNearCampfire(ServerPlayerEntity player) {
        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();
        int radius = 5;  // Search radius around the player (you can adjust this value)

        // Check the blocks in the area around the player
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos checkPos = playerPos.add(dx, dy, dz);
                    if (world.getBlockState(checkPos).getBlock() == Blocks.CAMPFIRE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Trigger the explosion if the conditions are met
    private void triggerExplosion(ServerPlayerEntity player) {
        // Get the player's bed position
        BlockPos bedPos = player.getSleepingPosition().orElse(null);
        if (bedPos != null) {

            World world = player.getWorld();
            world.createExplosion(null, bedPos.getX(), bedPos.getY(), bedPos.getZ(), 4.0F, World.ExplosionSourceType.TNT);

            PlayerClass p = dataHandler.playerDataMap.get(player.getUuid());
            if(!p.firstBedExplosion){
                Utils.grantAdvancement(player, "first_bed_explosion");
                p.firstBedExplosion = true;
                Utils.UTILS.sendTextAfter(player,"Seems like the God of Bed doesn't like to let people sleep on beds, might need to follow some rules. Well, better luck next time.");
            }
        }
    }
}
