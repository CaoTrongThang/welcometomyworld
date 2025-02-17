package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.PlayerStatsData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.classes.PlayerStatsData.*;

public class PlayerStatsDataManager {

    private static int SOUND_COOLDOWN = 60;
    private static int SOUND_COOLDOWN_COUNTER = 0;

    private static final List<String> NEW_STATMINA_LEVEL_MESSAGES = List.of(
            "I feel I can run more now!",
            "My energy is returning!",
            "My legs feel lighter!"
    );

    public static int COOLDOWN = 10;
    public static int COUNTER = 0;

    public static void start() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) ->
        {
            if (dataHandler.playerStatsData.get(serverPlayNetworkHandler.getPlayer().getUuid()) == null) {
                dataHandler.playerStatsData.put(serverPlayNetworkHandler.getPlayer().getUuid(), new PlayerStatsData());
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(PlayerStatsDataManager::endServerTick);
    }

    public static void endServerTick(MinecraftServer server) {
        if (SOUND_COOLDOWN_COUNTER < SOUND_COOLDOWN) {
            SOUND_COOLDOWN_COUNTER++;
        }

        COUNTER++;
        if (COUNTER < COOLDOWN) return;
        COUNTER = 0;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            PlayerStatsData pStats = dataHandler.playerStatsData.get(p.getUuid());
            playerStaminaHandler(p, pStats);
        }
    }

    public static void playerStaminaHandler(ServerPlayerEntity p, PlayerStatsData pStats) {
        if (p.isCreative() || p.isSpectator()) return;
        if (p.isSprinting()) {
            if(pStats.staminaLeft >= -2000) {
                pStats.staminaLeft -= COOLDOWN;

                if (pStats.staminaLeft <= -1000) {
                    p.damage(p.getWorld().getDamageSources().generic(), (float) pStats.staminaLeft / 60);
                }
                if (pStats.staminaLeft <= 200) {
                    playSoundClient(p, TIRED_SOUND);
                }

                if (pStats.staminaLeft <= 0) {
                    giveEffect(p, StatusEffects.SLOWNESS, Math.abs((int) (pStats.staminaLeft / 100)), 80);
                } else {
                    if (pStats.currentStaminaExp <= pStats.staminaExpRequiredToLevelUp) {
                        pStats.currentStaminaExp += COOLDOWN;
                    } else {
                        if (pStats.stamilaLevel < STAMINA_MAX_LEVEL && pStats.staminaExpRequiredToLevelUp < MAX_EXPERIENCE){
                            pStats.currentStaminaExp = 0;
                            pStats.stamilaLevel += 1;
                            pStats.staminaExpRequiredToLevelUp *= EXPERIENCE_MULTIPLY_BY_EACH_LEVEL;
                            pStats.staminaMax = DEFAULT_PLAYER_STAMINA + (pStats.stamilaLevel * 10);

                            pStats.staminaLeft = pStats.staminaMax;

                            p.sendMessage(Text.literal(NEW_STATMINA_LEVEL_MESSAGES.get(random.nextInt(NEW_STATMINA_LEVEL_MESSAGES.size()))));
                            ServerPlayNetworking.send(p, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
                        }
                    }
                }
            }



        } else {
            if (pStats.staminaLeft < pStats.staminaMax) {
                pStats.staminaLeft += COOLDOWN + (pStats.stamilaLevel / 5);

                if (pStats.staminaLeft > pStats.staminaMax) {
                    pStats.staminaLeft = pStats.staminaMax;
                }
            }
        }
    }

    public static void giveEffect(LivingEntity entity, StatusEffect effect, int level, int timeInTick) {
        // Apply new effect
        StatusEffectInstance currentEffect = entity.getStatusEffect(effect);

        if (currentEffect != null) {
            // Increase effect level
            int newAmplifier = Math.min(currentEffect.getAmplifier() + 1, 24);
            entity.addStatusEffect(new StatusEffectInstance(effect, timeInTick, newAmplifier));
        } else {
            // Apply new effect
            entity.addStatusEffect(new StatusEffectInstance(effect, timeInTick, level));
        }
    }

    private static void playSoundClient(ServerPlayerEntity p, Identifier sound) {

        if (SOUND_COOLDOWN_COUNTER < SOUND_COOLDOWN) return;
        SOUND_COOLDOWN_COUNTER = 0;

        spawnBreathingAtThePlayerMouth(p, p.getServerWorld());
        ServerPlayNetworking.send(p, sound, PacketByteBufs.empty());

    }

    private static void spawnBreathingAtThePlayerMouth(ServerPlayerEntity p, ServerWorld serverWorld) {
        // Get the player's head position (eye position)
        Vec3d eyePos = p.getEyePos();
        Vec3d mouthPos = eyePos.subtract(0, 0.1, 0); // Offset by 0.3 blocks down

        // Get the player's look direction based on yaw and pitch
        float yaw = p.getYaw();
        float pitch = p.getPitch();

        // Convert yaw and pitch to direction vector (look vector)
        Vec3d lookDirection = new Vec3d(
                -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
                -Math.sin(Math.toRadians(pitch)),
                Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
        );

        // Get a position a bit in front of the player (can adjust the factor as needed)
        double lookOffsetX = eyePos.x + lookDirection.x;
        double lookOffsetY = eyePos.y + lookDirection.y;
        double lookOffsetZ = eyePos.z + lookDirection.z;

        // Spawn particles 3 times at the player's head position (mouth)
        for (int i = 0; i < 3; i++) {
            serverWorld.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,                      // Particle type
                    mouthPos.x,                                // X coordinate (head)
                    mouthPos.y,                                // Y coordinate (head)
                    mouthPos.z,                                // Z coordinate (head)
                    1,                                        // Particle count
                    0.0, 0.0, 0.0,                           // No velocity
                    0.0                                      // Speed multiplier
            );
        }

        // Spawn particles 3 times at the point where the player is looking
        for (int i = 0; i < 3; i++) {
            serverWorld.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,                      // Particle type
                    lookOffsetX,                             // X coordinate (look position)
                    lookOffsetY,                             // Y coordinate (look position)
                    lookOffsetZ,                             // Z coordinate (look position)
                    1,                                        // Particle count
                    0.0, 0.0, 0.0,                           // No velocity
                    0.0                                      // Speed multiplier
            );
        }
    }
}
