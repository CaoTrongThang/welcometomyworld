package com.trongthang.welcometomyworld.Utilities;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.CustomPositionedSound;
import com.trongthang.welcometomyworld.classes.RunAfter;
import com.trongthang.welcometomyworld.entities.BlockSlamGroundEntity;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.GlobalVariables.POSSIBLE_EFFECTS_FOR_MOBS;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class Utils {
    public static final Utils UTILS = new Utils();

    public static void grantAdvancement(ServerPlayerEntity player, String achievement) {
        if (player == null) return; // Prevent NPE
        if(player.getServer() == null) return;
        player.getServer().execute(() -> {
            Identifier advancementId = new Identifier("welcometomyworld", achievement);
            Advancement advancement = player.server.getAdvancementLoader().get(advancementId);

            if (advancement == null) {
                WelcomeToMyWorld.LOGGER.error("Advancement {} not found!", advancementId);
                return;
            }

            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if (!progress.isDone()) {
                for (String criterion : progress.getUnobtainedCriteria()) {
                    player.getAdvancementTracker().grantCriterion(advancement, criterion);
                }
            }
        });
    }

    public void sendTextAfter(ServerPlayerEntity player, String text) {
        Text message = Text.literal("").styled(style -> style.withColor(Formatting.WHITE))
                .append(Text.literal("? Unknown:").styled(style -> style.withColor(Formatting.YELLOW)))
                .append(Text.literal(" " + text).styled(style -> style.withColor(Formatting.WHITE)));

        player.sendMessage(message);

        ServerPlayNetworking.send(player, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
    }

    public void sendTextAfter(ServerPlayerEntity player, String text, int ticks
    ) {
        addRunAfter(() -> {
            ServerPlayerEntity currentPlayer = player.getServer().getPlayerManager().getPlayer(player.getUuid());
            if (currentPlayer == null) return;

            Text message = Text.literal("").styled(style -> style.withColor(Formatting.WHITE))
                    .append(Text.literal("? Unknown:").styled(style -> style.withColor(Formatting.YELLOW)))
                    .append(Text.literal(" " + text).styled(style -> style.withColor(Formatting.WHITE)));

            currentPlayer.sendMessage(message);
            ServerPlayNetworking.send(player, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
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
                null,
                pos,
                soundEvent,
                SoundCategory.HOSTILE,
                0.8F,
                1.0F
        );
    }

    public static void playSound(ServerWorld serverWorld, BlockPos pos, SoundEvent soundEvent, float volume, float pitch) {
        serverWorld.playSound(
                null,
                pos,
                soundEvent,
                SoundCategory.HOSTILE,
                volume,
                pitch
        );
    }

    public static void playSound(World world, BlockPos pos, SoundEvent soundEvent) {
        world.playSound(
                null,
                pos,
                soundEvent,
                SoundCategory.HOSTILE,
                1f,
                1f
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
    public static void summonLightning(BlockPos pos, ServerWorld world, boolean cosmetic) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
        lightning.setCosmetic(cosmetic); // Makes the lightning deal no damage
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

    // Spawns a mob entity at the given block position
    public static Entity spawnMob(World world, BlockPos blockPos, String mobId) {
        if (blockPos == null) return null;

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
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
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

    public static BlockPos findSafeSpawnHostileMobPositionAroundTheCenterPos(ServerWorld world, Vec3d centerPos, int searchRadius) {
        final int maxTries = 20;
        final int verticalSearchRange = 4;

        for (int i = 0; i < maxTries; i++) {
            // Generate position in circular pattern around center
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * searchRadius;
            double x = centerPos.x + Math.cos(angle) * distance;
            double z = centerPos.z + Math.sin(angle) * distance;

            // Find proper Y coordinate
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, (int) x, (int) z);
            BlockPos.Mutable testPos = new BlockPos.Mutable(x, y, z);

            // Vertical search for valid position
            for (int dy = 0; dy < verticalSearchRange; dy++) {
                testPos.setY(y + dy);
                if (isSafeSpawn(world, testPos)) {
                    return testPos.toImmutable();
                }

                testPos.setY(y - dy);
                if (isSafeSpawn(world, testPos)) {
                    return testPos.toImmutable();
                }
            }
        }
        return null;
    }

    public static boolean isSafeSpawn(ServerWorld world, BlockPos pos) {
        // Check that the block below is solid and the spawn block is air
        return world.getBlockState(pos.down()).isSolidBlock(world, pos.down()) &&
                world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir();
    }

    // Apply the effect to the mob (either increase level or apply new effect)
    public static void applyEffectForMobs(LivingEntity mob, int howManyEffects, int durationInTicks) {
        // Use a HashSet to select unique effects
        Set<StatusEffect> selectedEffects = new HashSet<>();

        for (int x = 0; x < howManyEffects; x++) {
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

    public static boolean isPlayerStandingOnBlock(LivingEntity entity) {
        // Get the world and the player's bounding box
        World world = entity.getWorld();
        Box playerBox = entity.getBoundingBox();

        // Define the area to check below the player's feet (slightly below the bounding box)
        double checkHeight = 0.1; // Small offset below the player's feet
        Box checkBox = playerBox.offset(0, -checkHeight, 0);

        // Convert bounding box coordinates to integer BlockPos range
        int minX = (int) Math.floor(checkBox.minX);
        int minY = (int) Math.floor(checkBox.minY);
        int minZ = (int) Math.floor(checkBox.minZ);
        int maxX = (int) Math.floor(checkBox.maxX);
        int maxY = (int) Math.floor(checkBox.maxY);
        int maxZ = (int) Math.floor(checkBox.maxZ);

        // Iterate through all blocks in the range
        for (BlockPos pos : BlockPos.iterate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
            BlockState blockState = world.getBlockState(pos);

            // Check if the block is not air
            if (!blockState.isAir()) {
                return true;
            }
        }

        // No blocks below the player are solid
        return false;
    }

    public static float calculateDamageWithArmor(float initialDamage, LivingEntity entity) {
        // Get the entity's armor value
        double armor = entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
        double toughness = entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

        // Calculate reduction factor
        float reductionFactor = (float) (armor / (5.0 + armor + (toughness / 2.0)));

        // Calculate final damage
        return initialDamage * (1 - reductionFactor);
    }

    public static BlockPos checkFireDirection(BlockState fireBlock, BlockPos pos) {
        if (fireBlock.isOf(Blocks.FIRE)) {
            boolean isEastBurning = fireBlock.get(Properties.EAST);
            boolean isWestBurning = fireBlock.get(Properties.WEST);
            boolean isNorthBurning = fireBlock.get(Properties.NORTH);
            boolean isSouthBurning = fireBlock.get(Properties.SOUTH);
            boolean isUpBurning = fireBlock.get(Properties.UP);

            BlockPos checkPos = null;

            if (isEastBurning) {
                checkPos = pos.east();
            } else if (isWestBurning) {
                checkPos = pos.west();
            } else if (isNorthBurning) {
                checkPos = pos.north();
            } else if (isSouthBurning) {
                checkPos = pos.south();
            } else if (isUpBurning) {
                checkPos = pos.up();
            }

            return checkPos;
        }


        return null;
    }

    public static boolean isFireBurningAtTheBlock(World world, BlockPos pos) {

        BlockState block = null;
        List<BlockState> directions = List.of(
                world.getBlockState(pos.east()),
                world.getBlockState(pos.west()),
                world.getBlockState(pos.south()),
                world.getBlockState(pos.north()),
                world.getBlockState(pos.up())

        );

        for (BlockState s : directions) {
            if (s.isOf(Blocks.FIRE)) {
                if (checkFireDirection(s, pos) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void discardEntity(ServerWorld world, Entity entity) {
        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
        if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return; // Skip if the chunk isn't loaded
        }

        entity.discard();
    }

    public static void sendAnimationPacket(World world, LivingEntity entity, AnimationName animation, int timeout){
        for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
            if (player.canSee(entity)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(entity.getId());
                buf.writeEnumConstant(animation);
                buf.writeInt(timeout);
                ServerPlayNetworking.send(player, ANIMATION_PACKET, buf);
            }
        }
    }

    public static void sendSoundPacketFromClient(SoundEvent sound, BlockPos pos){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(pos.getX())
                .writeDouble(pos.getY())
                .writeDouble(pos.getZ());
        buf.writeIdentifier(sound.getId());

        ClientPlayNetworking.send(
                SOUND_PACKET_ID,
                buf
        );
    }

    public static void sendSoundPacketToClient(SoundEvent sound, BlockPos pos){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(pos.getX())
                .writeDouble(pos.getY())
                .writeDouble(pos.getZ());
        buf.writeIdentifier(sound.getId());

        ClientPlayNetworking.send(
                SOUND_PACKET_ID,
                buf
        );
    }

    public static void playClientSound(BlockPos pos, SoundEvent sound, int maxDistance) {
        // Get the client instance
        MinecraftClient client = MinecraftClient.getInstance();

        // Ensure the client's player exists
        if (client.player == null) return;

        // Calculate the distance between the player and the sound position
        Vec3d playerPos = client.player.getPos();
        double distance = playerPos.distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

        // Check if the player is within the maximum range
        if (distance <= maxDistance) {
            // Calculate the volume based on the distance
            float volume = Math.max(0.1F, 1.0F - (float) (distance / maxDistance));

            // Create and play the custom sound instance
            SoundInstance soundInstance = new CustomPositionedSound(
                    sound,
                    pos,
                    SoundCategory.BLOCKS,
                    volume, // Dynamically calculated volume
                    1.0F    // Pitch
            );
            client.getSoundManager().play(soundInstance);
        }
    }

    public static void playClientSound(BlockPos pos, SoundEvent sound, int maxDistance, float volume, float pitch) {
        // Get the client instance
        MinecraftClient client = MinecraftClient.getInstance();

        // Ensure the client's player exists
        if (client.player == null) return;

        // Calculate the distance between the player and the sound position
        Vec3d playerPos = client.player.getPos();
        double distance = playerPos.distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

        // Check if the player is within the maximum range
        if (distance <= maxDistance) {
            // Create and play the custom sound instance
            SoundInstance soundInstance = new CustomPositionedSound(
                    sound,
                    pos,
                    SoundCategory.BLOCKS,
                    volume, // Dynamically calculated volume
                    pitch    // Pitch
            );
            client.getSoundManager().play(soundInstance);
        }
    }

    public static BlockPos findSafeSpawnPositionByPack(ServerWorld world, BlockPos center,
                                                       EntityType<?> entityType, int minRadius, int maxRadius) {
        final int MAX_ATTEMPTS = 70;
        final int HORIZONTAL_RANGE = maxRadius - minRadius;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            // Generate position in square instead of circle for better coverage
            int x = center.getX() + (minRadius + random.nextInt(HORIZONTAL_RANGE)) * (random.nextBoolean() ? 1 : -1);
            int z = center.getZ() + (minRadius + random.nextInt(HORIZONTAL_RANGE)) * (random.nextBoolean() ? 1 : -1);

            // Improved vertical search
            Heightmap.Type heightmapType = SpawnRestriction.getHeightmapType(entityType);
            int topY = world.getTopY(heightmapType, x, z);

            // Check 3 blocks below and above the surface
            for (int yOffset = -3; yOffset <= 3; yOffset++) {
                BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, topY + yOffset, z);

                // Validate spawn rules first before solid block check

                if (SpawnHelper.canSpawn(SpawnRestriction.getLocation(entityType), world, mutablePos, entityType)) {
                    return mutablePos.toImmutable();
                }
            }
        }

        return null;
    }

    public static void CreateBlockSlamGround(ServerWorld world, BlockState state, BlockPos pos){
        BlockSlamGroundEntity effectEntity = EntitiesManager.BLOCK_SLAM_GROUND.create(world);
        if (effectEntity != null) {
            effectEntity.setBlockState(state);
            effectEntity.setPosition(
                    pos.getX() + 0.5,  // Center in block
                    pos.getY() + 0.1, // Slightly above ground
                    pos.getZ() + 0.5   // Center in block
            );


            world.spawnEntity(effectEntity);
        }
    }

}
