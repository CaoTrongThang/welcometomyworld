package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.classes.PlayerData;
import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class AwakeHandler {

    public static final List<String> notSleepyMessages = List.of(
            "I'm not tired",
            "I'm not really tired...",
            "I think i can do more...",
            "Nothing can stop me, even the nights!",
            "Sleep? Not an option right now!",
            "My energy knows no bounds!",
            "I could keep going all night!",
            "Sleep is overrated, I’m just getting started!",
            "I feel wide awake, bring it on!",
            "Tired? Never heard of it!",
            "I'm powered up and ready for more!",
            "Who needs sleep when there’s so much to do?",
            "I’m on a roll, no stopping me now!",
            "I’m at full capacity, no shutdown!",
            "Sleep? I’ll take a rain check on that!",
            "Energy levels are off the charts!",
            "I feel more alive than ever!",
            "The night is young, and so am I!",
            "Who needs sleep when there’s a world to conquer?",
            "Sleep is for the weak, I’m invincible!",
            "I’ve got endless fuel, let's keep going!",
            "I’m not slowing down anytime soon!"
    );

    public static final List<String> luckEffectMessages = List.of(
            "Here you go",
            "Good luck!",
            "Better take a snap",
            "Fortune favors you!",
            "Lady Luck smiles upon you!",
            "Destiny takes a nap too!",
            "Lucky stars aligned!",
            "Charm of Morpheus activated!",
            "Fate needs some rest too!"
    );

    private static final ConcurrentHashMap<UUID, Boolean> playerBuffTracker = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> playerBedInteractCooldownTracker = new ConcurrentHashMap<>();

    private static final int defaultBedCooldown = 200;

    private static final double WAKE_UP_CHANCE = 1.0; // Base chance of explosion

    // Variables to modify the explosion chance
    private double havingLuckEffectChanceDecrease = 1.0;
    private double rainDescreaseChance = 1.0;

    private double nearASleepingFriendChanceDecrease = 0.3;
    private double nearACampfireChanceDecrease = 0.2;
    private double holdingFlowerChanceDecrease = 0.4;

    private static final double GIVE_LUCK_EFFECT_CHANCE = 0.5f;
    private static final int LUCK_EFFECT_DURATION = 600;

    // Interval to check if player is sleeping (in ticks)
    public int checkInterval = 40;
    private int counter = 0;

    public static int lastLuckBuffDay = 0;

    public AwakeHandler() {
    }

    public static void register(){
        UseBlockCallback.EVENT.register((a,world,c,d) -> {
            BlockState state = world.getBlockState(d.getBlockPos());

            if(state.isIn(BlockTags.BEDS)){
                if(playerBedInteractCooldownTracker.containsKey(a.getUuid())){
                    if(playerBedInteractCooldownTracker.get(a.getUuid()) > 0 ){
                        return ActionResult.FAIL;
                    }
                }
            }

            return ActionResult.PASS;
        });
    }


    public static void checkAndApplyLuckEffect(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay() % 24000; // Get current in-game time (0-23999)
        int currentDay = DayAndNightCounterAnimationHandler.getCurrentDay(world);

        // Reset buff tracker at the start of a new day
        if (currentDay!= lastLuckBuffDay && !playerBuffTracker.isEmpty()) {
            playerBuffTracker.clear();
            return;
        }

        if (timeOfDay >= 21000 && timeOfDay < 21130) {
            lastLuckBuffDay = currentDay;
            for (ServerPlayerEntity player : world.getPlayers()) {
                UUID playerId = player.getUuid();

                // Check if the player has already received the buff for this night
                if (!playerBuffTracker.getOrDefault(playerId, false)) {
                    applyLuckEffectToPlayer(player);
                    playerBuffTracker.put(playerId, true); // Mark the player as buffed
                    Utils.UTILS.sendTextAfter(player, luckEffectMessages.get(random.nextInt(luckEffectMessages.size())));

                    Utils.spawnParticles(world, player.getBlockPos(), ParticleTypes.HAPPY_VILLAGER);
                }
            }
        }
    }

    private static void applyLuckEffectToPlayer(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, LUCK_EFFECT_DURATION, 0));
        ServerPlayNetworking.send(player, PLAY_EXPERIENCE_ORB_PICK_UP, PacketByteBufs.empty());
    }

    public void checkAndExplodeIfSleeping(MinecraftServer server) {

        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        for(UUID x : playerBedInteractCooldownTracker.keySet()){
            int c = playerBedInteractCooldownTracker.get(x) - checkInterval;
            if(c <= 0){
                playerBedInteractCooldownTracker.remove(x);
            } else {
                playerBedInteractCooldownTracker.put(x, c);
            }
        }

        if (random.nextDouble() < GIVE_LUCK_EFFECT_CHANCE) {
            checkAndApplyLuckEffect(server.getOverworld());
        };

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isSleeping()) {
                double awakeChance = Math.max(calculateWakeUpChance(player), 0);

                Text grayMessage = Text.literal("Awake chance: " + (awakeChance * 100) + "%");
                player.sendMessage(grayMessage, false);

                if (random.nextDouble() < awakeChance) {
                    ServerWorld world = player.getServerWorld();

                    playerBedInteractCooldownTracker.put(player.getUuid(), defaultBedCooldown);

                    BlockPos pos = player.getSleepingPosition().orElse(null);
                    Utils.spawnParticles(world, pos, ParticleTypes.SMOKE);

                    Utils.addRunAfter(() -> {
                                triggerWakeUp(player);
                            }
                            , 20);

                }
            }
        }
        // Ensure the player is still in a sleeping state
    }

    // Method to calculate the explosion chance based on various factors
    private double calculateWakeUpChance(ServerPlayerEntity player) {
        double effectiveChance = WAKE_UP_CHANCE;

        // Apply the luck effect
        if (player.hasStatusEffect(StatusEffects.LUCK)) {
            effectiveChance -= havingLuckEffectChanceDecrease;
            return Math.max(0, effectiveChance);
        }

        if (player.getWorld().isRaining()) {
            effectiveChance -= rainDescreaseChance;
            return Math.max(0, effectiveChance);
        }

        // Apply the near friends effect
        if (isNearOtherPlayers(player)) {
            effectiveChance -= nearASleepingFriendChanceDecrease;
        }

        ItemStack mainHandStack = player.getMainHandStack();

        // Apply the holding flower effect
        if (mainHandStack.isIn(ItemTags.FLOWERS)) {
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
    private void triggerWakeUp(ServerPlayerEntity player) {
        // Get the player's bed position

        player.wakeUp();

        PlayerData p = dataHandler.playerDataMap.get(player.getUuid());
        if (!p.firstWakeup) {
            Utils.grantAdvancement(player, "first_wake_up");
            p.firstWakeup = true;
            Utils.addRunAfter(() -> {
                Utils.UTILS.sendTextAfter(player, "Seems like you're not tired enough.");
            }, 60);
        } else {
            player.sendMessage(Text.literal(notSleepyMessages.get(random.nextInt(0, notSleepyMessages.size() - 1))).formatted(Formatting.WHITE));
        }
    }
}
