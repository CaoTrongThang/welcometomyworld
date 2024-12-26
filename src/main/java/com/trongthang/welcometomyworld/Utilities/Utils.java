package com.trongthang.welcometomyworld.Utilities;

import com.trongthang.welcometomyworld.classes.RunAfter;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.GlobalVariables.POSSIBLE_EFFECTS_FOR_MOBS;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

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

        for (int i = 0; i < 10; i++) {
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

    public static void SpawnItem(ServerWorld serverWorld, Vec3d pos, ItemStack itemStack) {
        serverWorld.spawnEntity(new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), itemStack));
    }

    // Summon lightning at the mob's position
    public static void summonLightning(BlockPos pos, ServerWorld world) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
        lightning.setCosmetic(true); // Makes the lightning deal no damage
        world.spawnEntity(lightning);
    }

    public static void giveEffect(LivingEntity entity, StatusEffect effect, int timeInTick) {

        StatusEffectInstance currentEffect = entity.getStatusEffect(effect);

        if (currentEffect != null) {
            // Increase effect level
            int newAmplifier = Math.min(currentEffect.getAmplifier() + 1, 24);
            entity.addStatusEffect(new StatusEffectInstance(effect, currentEffect.getDuration(), newAmplifier));
        } else {
            // Apply new effect
            entity.addStatusEffect(new StatusEffectInstance(effect, timeInTick, 0));
        }

    }

    public static void preloadHorizontalChunksAtSpawn(ServerWorld world) {
        // Get the default spawn point of the world
        if(world == null) return;

        int radius = 8;

        BlockPos spawnPos = world.getSpawnPos();
        ChunkPos spawnChunk = new ChunkPos(spawnPos);

        LOGGER.info("Preloading horizontal chunks around the world spawn point...");

        // Load a 10x10 grid of chunks horizontally around the spawn point
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                ChunkPos chunkPos = new ChunkPos(spawnChunk.x + dx, spawnChunk.z + dz);

                if(!world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)){
                    world.getChunkManager().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
                    LOGGER.info(MOD_ID + " Loaded chunk at {} (dx={}, dz={})", chunkPos, dx, dz);
                }
            }
        }
    }

    // Spawns a mob entity at the given block position
    public static Entity spawnMob(World world, BlockPos blockPos, String mobId) {
        if(blockPos == null) return null;

        Identifier mobIdentifier = new Identifier(mobId.toLowerCase());
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(mobIdentifier);
        Entity entity = null;

        if (entityType != null) {
//            entityType.spawn(player.getServer().getWorld(player.getWorld().getRegistryKey()), blockPos, null);

            // Create the entity instance
            entity = entityType.create(world);

            if (entity instanceof MobEntity mobEntity) {
                // Set the mob's position
                mobEntity.refreshPositionAndAngles(
                        blockPos.getX() + 0.5,
                        blockPos.getY(),
                        blockPos.getZ() + 0.5,
                        world.random.nextFloat() * 360F, 0
                );


                // Add the mob to the world
                world.spawnEntity(mobEntity);
            } else {
                LOGGER.info("Entity type " + mobId + " not found. Check if mod ID or entity ID is correct.");
            }
        }

        return entity;
    }

    public static BlockPos findSafeSpawnPositionAroundTheCenterPos(ServerWorld world, Vec3d centerPos, int spawnDistance) {
        int maxTries = 15;

        for (int i = 0; i < maxTries; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = spawnDistance + random.nextDouble() * 10; // Random offset beyond minimum distance
            int x = (int) (centerPos.x + Math.cos(angle) * distance);
            int z = (int) (centerPos.z + Math.sin(angle) * distance);
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z); // Get the topmost block

            BlockPos potentialPos = new BlockPos(x, y, z);

            if (isSafeSpawn(world, potentialPos)) {
                return potentialPos;
            }
        }
        return null; // No safe position found
    }

    public static boolean isSafeSpawn(ServerWorld world, BlockPos pos) {
        // Check that the block below is solid and the spawn block is air
        return world.getBlockState(pos.down()).isSolidBlock(world, pos.down()) &&
                world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir();
    }

    // Apply the effect to the mob (either increase level or apply new effect)
    public static void applyEffect(LivingEntity mob, int howManyEffects, int durationInTicks) {
        // Use a HashSet to select unique effects
        Set<StatusEffect> selectedEffects = new HashSet<>();

        while (selectedEffects.size() < howManyEffects) {
            StatusEffect randomEffect = POSSIBLE_EFFECTS_FOR_MOBS.get(random.nextInt(POSSIBLE_EFFECTS_FOR_MOBS.size()));
            selectedEffects.add(randomEffect);
        }

        for (StatusEffect effect : selectedEffects) {
            StatusEffectInstance currentEffect = mob.getStatusEffect(effect);

            if (currentEffect != null) {
                // Increase effect level
                int newAmplifier = Math.min(currentEffect.getAmplifier() + 1, 24);
                mob.addStatusEffect(new StatusEffectInstance(effect, currentEffect.getDuration(), newAmplifier));
            } else {
                // Apply new effect
                mob.addStatusEffect(new StatusEffectInstance(effect, durationInTicks, 0)); // Duration: 600 ticks (30 seconds)
            }
        }
    }
}
