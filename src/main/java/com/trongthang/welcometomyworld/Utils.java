package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.saveData.BlockProgress;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.PLAY_EXPERIENCE_ORB_PICK_UP;

public class Utils {
    public static final Utils UTILS = new Utils();

    public static void grantAdvancement(ServerPlayerEntity player, String achievement) {
        String advancementId = "welcometomyworld:" + achievement;
        Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier(advancementId));
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getUnobtainedCriteria()) {
                    player.getAdvancementTracker().grantCriterion(advancement, criterion);
                }
            }
        }
    }

    public void sendTextAfter(ServerPlayerEntity player, String text) {
        Text message = Text.literal("<").styled(style -> style.withColor(Formatting.WHITE))
                .append(Text.literal("Unknown").styled(style -> style.withColor(Formatting.YELLOW)))
                .append(Text.literal("> " + text).styled(style -> style.withColor(Formatting.WHITE)));

        player.sendMessage(message);

        ServerPlayNetworking.send(player, PLAY_EXPERIENCE_ORB_PICK_UP, PacketByteBufs.empty());
    }

    public void sendTextAfter(ServerPlayerEntity player, String text, int ticks
    ) {
        addRunAfter(() -> {
            ServerPlayerEntity currentPlayer = player.getServer().getPlayerManager().getPlayer(player.getUuid());
            if (currentPlayer == null) return;

            Text message = Text.literal("<").styled(style -> style.withColor(Formatting.WHITE))
                    .append(Text.literal("Unknown").styled(style -> style.withColor(Formatting.YELLOW)))
                    .append(Text.literal("> " + text).styled(style -> style.withColor(Formatting.WHITE)));

            currentPlayer.sendMessage(message);
            ServerPlayNetworking.send(currentPlayer, PLAY_EXPERIENCE_ORB_PICK_UP, PacketByteBufs.empty());
        }, ticks);
    }

    public static void spawnCircleParticles(ServerPlayerEntity player) {
        int particleCount = 30; // Number of particles in the circle
        double radius = 2; // Radius of the circle
        double yOffset = -2; // Slightly above the feet level

        ServerWorld world = player.getServerWorld();

        Vec3d playerPos = player.getPos();
        double centerX = playerPos.x;
        double centerY = playerPos.y + yOffset;
        double centerZ = playerPos.z;

        for (int i = 0; i < particleCount; i++) {
            int finalI = i;
            addRunAfter(() -> {
                // Calculate the angle for each particle
                double angle = 2 * Math.PI * finalI / particleCount;

                // Calculate particle positions based on the angle
                double x = centerX + radius * Math.cos(angle);
                double z = centerZ + radius * Math.sin(angle);

                // Spawn the particle (using purple portal particles)
                world.spawnParticles(
                        ParticleTypes.PORTAL, // Purple portal particles
                        x, centerY, z,        // Position of the particle
                        1,                    // Number of particles (spawned in one call)
                        0, 0, 0,              // Spread (no random motion here)
                        0.0                   // Extra (speed multiplier for particle motion)
                );
            }, 2);
        }
    }

    static ConcurrentHashMap<UUID, RunAfter> runnableList = new ConcurrentHashMap();

    public static void addRunAfter(Runnable runFunction, int afterTicks) {
        UUID taskId = UUID.randomUUID();
        runnableList.put(taskId, new RunAfter(runFunction, afterTicks));
    }

    public static void onServerTick(MinecraftServer server) {
        for (UUID key : runnableList.keySet()) {
            RunAfter runTask = runnableList.get(key);

            runTask.runAfterInTick--;
            if (runTask.runAfterInTick <= 0) {
                runTask.functionToRun.run();
                runnableList.remove(key);
            }
        }
    }

    public static void playSound(ServerWorld serverWorld, BlockPos pos, SoundEvent soundEvent) {
        serverWorld.playSound(
                null,                                   // Player (null = all nearby players hear the sound)
                pos,                  // Position of the sound
                soundEvent,      // Sound event (Blaze ambient sound)
                SoundCategory.HOSTILE,                 // Sound category (hostile mob sounds)
                0.8F,                                 // Volume (1.0 = normal volume)
                1.0F                                   // Pitch (1.0 = normal pitch)
        );
    }

    public static void spawnParticles(ServerWorld serverWorld, BlockPos pos, DefaultParticleType particle) {

        for (int i = 0; i < 10; i++) { // Create 10 flame particles
            double offsetX = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random X offset
            double offsetY = serverWorld.getRandom().nextDouble() * 2;         // Random Y offset
            double offsetZ = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random Z offset

            serverWorld.spawnParticles(
                    particle,                      // Particle type
                    pos.getX() + offsetX,                    // X coordinate
                    pos.getY() + offsetY,                    // Y coordinate
                    pos.getZ() + offsetZ,                    // Z coordinate
                    1,                                        // Particle count
                    0.0, 0.0, 0.0,                           // No velocity
                    0.0                                      // Speed multiplier
            );
        }
    }

    public static void spawnBlockBreakParticles(ServerWorld world, BlockPos pos, BlockStateParticleEffect particle) {
        world.spawnParticles(
                particle, // Block particle
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, // Position (centered)
                20,  // Number of particles
                0.5, 0.5, 0.5, // Spread in X, Y, Z
                0.1  // Speed multiplier
        );
    }

    public static boolean anyBlockUpHead(World world, BlockPos pos, int upDistance) {

        var checkUp = upDistance - pos.getY();

        for (int x = 1; x < checkUp; x++) {
            if (!world.getBlockState(pos.up(x)).isAir()) {
                return true;
            }
        }

        return false;
    }

    public static void SpawnItem(ServerWorld serverWorld, Vec3d pos, ItemStack itemStack) {
        serverWorld.spawnEntity(new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), itemStack));
    }
}
