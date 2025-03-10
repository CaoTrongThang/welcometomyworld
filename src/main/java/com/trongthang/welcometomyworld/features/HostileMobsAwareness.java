package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.NoiseSource;
import it.unimi.dsi.fastutil.longs.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HostileMobsAwareness {
    private static class Config {
        public static final double DETECTION_DISTANCE_SQ = 64.0 * 64.0;
        public static final int CHECK_INTERVAL = 40;
        public static final double NOISE_DECREASE_OVERTIME = 10.0;
        public static final double MAX_NOISE = 100.0;

        public static final double DISTANCE_FROM_NOISE_SOURCE = 25.0;

        public static final int ALERT_COOLDOWN = 800;
        public static final int MAX_MOBS_PER_CHECK = 24;

        public static final double MIN_MOVE_THRESHOLD_SQ = 0.0001;

        public static final double ATTACK_NOISE = 13.0;
        public static final double BLOCK_BREAK_NOISE = 13.0;
        public static final double BLOCK_PLACE_NOISE = 7.0;
    }

    // Message List
    private static final List<String> ALERT_MESSAGES = List.of(
            "Did something hear me?", "I think they noticed me!", "Was that movement over there?",
            "I need to be quieter...", "Something's following!", "Not alone anymore...",
            "They know I'm here!", "Need to find cover!", "Too much noise!",
            "Can't stay in one place!", "Movement in the dark...", "Need to move carefully...",
            "They're homing in!", "Should've been quieter..."
    );

    public static final Set<Identifier> TRACKED_MOBS = new HashSet<>(Set.of(
            new Identifier("minecraft", "zombie"),
            new Identifier("minecraft", "skeleton"),
            new Identifier("minecraft", "creeper"),
            new Identifier("minecraft", "pillager"),
            new Identifier("minecraft", "vindicator"),

            new Identifier("wandering_orc", "troll"),
            new Identifier("wandering_orc", "orc_archer"),
            new Identifier("wandering_orc", "minotaur"),
            new Identifier("wandering_orc", "orc_champion"),
            new Identifier("wandering_orc", "orc_warrior")
    ));

    private static final Map<UUID, HostileEntity> activeMobs = new ConcurrentHashMap<>();
    public static final Long2ObjectMap<Vec3d> lastPositions = new Long2ObjectOpenHashMap<>();

    public static final ConcurrentHashMap<BlockPos, NoiseSource> noiseSourceMap = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    public static void onServerTick(MinecraftServer server) {

        if (server.getOverworld().isClient) return;

        if (++tickCounter >= Config.CHECK_INTERVAL) {
            tickCounter = 0;

            noiseSourceMap.replaceAll((pos, existing) ->
                    new NoiseSource(
                            Math.max(existing.noise - Config.NOISE_DECREASE_OVERTIME, 0),
                            Math.max(existing.cooldown - Config.CHECK_INTERVAL, 0)
                    )
            );

            for (BlockPos pos : noiseSourceMap.keySet()) {
                WelcomeToMyWorld.LOGGER.info("POS: " + pos);
                WelcomeToMyWorld.LOGGER.info("NOISE: " + noiseSourceMap.get(pos).noise);
                WelcomeToMyWorld.LOGGER.info("COOLDOWN: " + noiseSourceMap.get(pos).cooldown);
            }

            noiseSourceMap.keySet().removeIf(pos -> noiseSourceMap.get(pos).noise <= 0);
            checkPlayerMovement(server);
        }
    }


    private static void checkPlayerMovement(MinecraftServer server) {
        if (activeMobs.isEmpty()) return;
        if (server.getOverworld().isDay()) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!(player.getWorld().getRegistryKey() == World.OVERWORLD)) continue;

            long uuidLSB = player.getUuid().getLeastSignificantBits();
            Vec3d currentPos = player.getPos();
            Vec3d lastPos = lastPositions.get(uuidLSB);

            if (lastPos == null) {
                lastPositions.put(uuidLSB, currentPos);
                continue;
            }

            // Calculate squared horizontal distance
            double dx = currentPos.x - lastPos.x;
            double dz = currentPos.z - lastPos.z;
            double distanceSq = dx * dx + dz * dz;

            lastPositions.put(uuidLSB, currentPos);

            if (distanceSq > Config.MIN_MOVE_THRESHOLD_SQ) {
                float movementNoise = calculateMovementNoise(player);
                handleNoise(player, movementNoise);
            }
        }
    }

    private static float calculateMovementNoise(ServerPlayerEntity player) {
        if (player.isSneaking()) return 4;
        if (player.isSprinting()) return 15;
        return 7;
    }

    public static void registerEvents() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) ->
                handleNoise(player, Config.BLOCK_BREAK_NOISE));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !(entity instanceof HostileEntity)) return ActionResult.PASS;
            handleNoise(player, Config.ATTACK_NOISE);

            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
                handleNoise(player, Config.BLOCK_PLACE_NOISE);
            }
            return ActionResult.PASS;
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HostileEntity hostile) {
                if (shouldTrackPlayer(hostile)) {
                    activeMobs.put(entity.getUuid(), hostile);
                }
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof HostileEntity) {
                activeMobs.remove(entity.getUuid());
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerEntity player = handler.getPlayer();
            if (player != null) {
                long uuidLong = player.getUuid().getLeastSignificantBits();
                lastPositions.remove(uuidLong);
            }
        });
    }

    private static boolean shouldTrackPlayer(HostileEntity mob) {
        Identifier mobId = EntityType.getId(mob.getType());
        return TRACKED_MOBS.contains(mobId);
    }

    private static void handleNoise(PlayerEntity player, double noiseAmount) {
        if (!validateConditions(player)) return;
        if (player.isSneaking()) noiseAmount *= 0.5;

        boolean foundNoiseSource = false;

        for (BlockPos pos : noiseSourceMap.keySet()) {
            NoiseSource noise = noiseSourceMap.get(pos);

            if (Math.abs(pos.getX() - player.getX()) <= Config.DISTANCE_FROM_NOISE_SOURCE
                    && Math.abs(pos.getY() - player.getY()) <= Config.DISTANCE_FROM_NOISE_SOURCE
                    && Math.abs(pos.getZ() - player.getZ()) <= Config.DISTANCE_FROM_NOISE_SOURCE) {

                double newNoise = Math.min(Config.MAX_NOISE, noise.noise + noiseAmount);
                NoiseSource newNoiseSource = new NoiseSource();

                if (noise.noise >= Config.MAX_NOISE && noise.cooldown <= 0) {
                    alertMobs(pos.toCenterPos(), player);

                    newNoiseSource.cooldown = Config.ALERT_COOLDOWN;
                    newNoiseSource.noise = noise.noise;

                    noiseSourceMap.put(pos, newNoiseSource);
                } else {
                    // Preserve existing cooldown when updating noise
                    newNoiseSource.noise = newNoise;
                    newNoiseSource.cooldown = noise.cooldown; // Add this line
                    noiseSourceMap.put(pos, newNoiseSource);
                }

                foundNoiseSource = true;
            }
        }

        if (!foundNoiseSource) {
            NoiseSource noise = new NoiseSource(Math.min(Config.MAX_NOISE, noiseAmount), 0);
            noiseSourceMap.put(player.getBlockPos(), noise);
        }
    }

    private static boolean alertMobs(Vec3d noisePos, PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld world)) return false;

        List<ServerPlayerEntity> targets = world.getPlayers()
                .stream()
                .filter(p -> {
                    Vec3d playerPos = p.getPos();
                    return Math.abs(noisePos.x - playerPos.x) <= 30 &&
                            Math.abs(noisePos.y - playerPos.y) <= 30 &&
                            Math.abs(noisePos.z - playerPos.z) <= 30;
                })
                .collect(Collectors.toList());

        if (targets.isEmpty()) return false;

        List<HostileEntity> availableMobs = activeMobs.values()
                .stream()
                .filter(mob -> mob.squaredDistanceTo(noisePos) <= Config.DETECTION_DISTANCE_SQ)
                .limit(Config.MAX_MOBS_PER_CHECK)
                .collect(Collectors.toList());

        // 3. Distribute mobs evenly
        int mobsPerPlayer = availableMobs.size() / targets.size();
        int remainder = availableMobs.size() % targets.size();
        int index = 0;

        for (HostileEntity mob : availableMobs) {
            ServerPlayerEntity target = targets.get(index % targets.size());
            mob.setTarget(target);

            if (index < targets.size() * mobsPerPlayer + remainder) {
                index++;
            } else {
                index = 0;
            }
        }

        targets.forEach(p -> {
            p.sendMessage(Text.literal(ALERT_MESSAGES.get(p.getRandom().nextInt(ALERT_MESSAGES.size()))));
            Utils.spawnParticles(world, p.getBlockPos(), ParticleTypes.FLAME);
            Utils.sendSoundPacketToClient(SoundEvents.ENTITY_FOX_SNIFF, p.getBlockPos());
        });

        return !availableMobs.isEmpty();
    }


    private static boolean validateConditions(PlayerEntity player) {
        return !player.getWorld().isClient()
                && player.getWorld().isNight()
                && player.getWorld().getRegistryKey() == World.OVERWORLD
                && player.getWorld().getDifficulty() != Difficulty.PEACEFUL
                && !player.isSpectator()
                && !player.isCreative();
    }
}