package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.interfaces.IScaleEntity;
import com.trongthang.welcometomyworld.Utilities.SpawnParticles;
import com.trongthang.welcometomyworld.classes.PlayerData;
import com.trongthang.welcometomyworld.entities.Blossom.Blossom;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class IntroOfTheWorldHandler {

    Random rand = new Random();

    public static double playersDeathChanceInTheIntro = 0.15;

    byte phantomSpawnAmount = 8;
    public boolean alreadySpawnedPhantom = false;

    public static boolean firstTimeLoadChunkIntro = false;

    public void teleportPlayersToSkyFirstJoin(ServerPlayerEntity player, PlayerData playerData) {
        // Teleport player to the sky
        Vec3d skyPosition = new Vec3d(player.getX(), 400, player.getZ());
        player.teleport(skyPosition.x, skyPosition.y, skyPosition.z);

        // Determine their fate right at the start if not already determined
        if (playerData.playerFirstIntroDeathChance == 0) {
            playerData.playerFirstIntroDeathChance = rand.nextDouble();
        }

        // Give them effects that last LONG enough to survive the whole fall (1200 ticks
        // = 60 seconds)
        // If they are in the 85% that survives, give them absolute protection.
        if (playerData.playerFirstIntroDeathChance > playersDeathChanceInTheIntro) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 255, false, false));
        }

        // Give everyone slow falling initially
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 120, 4, false, false));

        // Set player scale to small
        if (player instanceof IScaleEntity scaleEntity) {
            scaleEntity.setScale(0.00f);

            // Sync to client
            net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            buf.writeInt(player.getId());
            buf.writeFloat(0.01f);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.SYNC_SCALE_PACKET, buf);
        }
    }

    // THIS IS A FUCKING MESS, DON'T READ
    public void handlePlayerFirstJoin(ServerPlayerEntity player) {
        if (player.getWorld().isClient)
            return;

        PlayerData playerData = dataHandler.playerDataMap.get(player.getUuid());

        // Handle wrong dimension or creative mode escapes
        if (!player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
            if (playerData != null && !playerData.firstTouchGround) {
                dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
            }
            return;
        }

        if (player.isCreative() && (!playerData.firstTouchGround || !playerData.firstTeleportedToSky)) {
            dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
            return;
        }

        if (!firstTimeLoadChunkIntro) {
            firstTimeLoadChunkIntro = true;
        }

        ServerWorld world = player.getServerWorld();

        // Screen completion checks (Origin / Terrain Loading)
        if (compatityChecker.originMod && !playerData.completeOriginSelectingScreen) {
            teleportPlayersToSkyFirstJoin(player, playerData);
            if (playerData.firstTouchGround)
                playerData.completeOriginSelectingScreen = true;
            return;
        }

        if (!playerData.completeLoadingTerrainScreen) {
            teleportPlayersToSkyFirstJoin(player, playerData);
            if (playerData.firstTouchGround)
                playerData.completeLoadingTerrainScreen = true;
            return;
        }

        // Time limit check
        if (!playerData.firstTouchGround) {
            playerData.introTimeLimit--;
            if (playerData.introTimeLimit <= 0) {
                dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
                return;
            }
        }

        // ---------------------------------------------------------
        // PHASE 1: Initialization in the Sky
        // ---------------------------------------------------------
        if (!playerData.firstTeleportedToSky) {
            teleportPlayersToSkyFirstJoin(player, playerData);

            if (!playerData.completeSpawningParticles) {
                for (int x = 1; x < 40; x++) {
                    Utils.addRunAfter(() -> Utils.spawnCircleParticles(player), x);
                }
                playerData.completeSpawningParticles = true;
                if (ConfigLoader.getInstance().enableDialogAfterIntro) {
                    Utils.UTILS.sendTextAfter(player, "!!!", 20);
                }
            }

            for (int x = 0; x < rand.nextInt(10, 20); x++) {
                Utils.addRunAfter(() -> Utils.summonLightning(player.getBlockPos(), world, true), rand.nextInt(5, 20));
            }

            if (!alreadySpawnedPhantom) {
                spawnPhantom(world, player);
                alreadySpawnedPhantom = true;
            }

            // Spawn the rift portal once when they are high up
            if (player.getY() >= 350) {
                playerData.firstTeleportedToSky = true;
                ServerPlayNetworking.send(player, PLAY_BLOCK_PORTAL_TRAVEL, PacketByteBufs.empty());
                com.trongthang.welcometomyworld.entities.RiftPortalEntity portal = EntitiesManager.RIFT_PORTAL_ENTITY
                        .create(world);
                if (portal != null) {
                    portal.setPosition(player.getX(), player.getY() - 4f, player.getZ());
                    world.spawnEntity(portal);
                }
            }
        }

        // If intro is already done, do nothing
        if (playerData.firstTouchGround)
            return;

        // ---------------------------------------------------------
        // PHASE 2: Mid-Air Falling Logic
        // ---------------------------------------------------------

        boolean meantToSurvive = playerData.playerFirstIntroDeathChance > playersDeathChanceInTheIntro;

        // Ground Radar: If surviving, inject hidden resistance just before impact
        if (meantToSurvive && isGroundNearby(player, 15, 20)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 220, 255, false, false));
        }

        // Check if player died mid-air (or upon impact if meant to die)
        if (player.getHealth() <= 0 || player.isDead()) {
            handleIntroDeath(player, playerData);
            return;
        }

        // ---------------------------------------------------------
        // PHASE 3: Touchdown!
        // ---------------------------------------------------------
        if (Utils.isPlayerStandingOnBlock(player) || player.isOnGround()) {

            // Clean up sky effects
            // player.removeStatusEffect(StatusEffects.RESISTANCE); // Let it last through
            // the bounce and dizzy phase
            player.removeStatusEffect(StatusEffects.SLOW_FALLING);

            // Double check safety: Reset fall distance exactly on the landing tick
            if (meantToSurvive)
                player.fallDistance = 0.0f;

            // Create explosion
            BlockPos landingPos = player.getBlockPos();
            world.createExplosion(player, landingPos.getX(), landingPos.getY(), landingPos.getZ(), 6F,
                    World.ExplosionSourceType.TNT);

            // Small bounce
            player.setVelocity(player.getVelocity().x, 1.2, player.getVelocity().z);
            player.velocityModified = true;

            // Apply post-landing effects
            Utils.addRunAfter(
                    () -> player
                            .addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 4, false, false)),
                    20);

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 3, false, false));

            // Cosmetics
            spawnLandEffect(player);
            SpawnParticles.spawnExpandingParticleSquare(world, player, 2, 5, 20, ParticleTypes.END_ROD);

            // Mark completed
            playerData.firstTouchGround = true;
            playerData.firstTeleportedToSky = true;
            playerData.completeLoadingTerrainScreen = true;
            playerData.introMessageAfterDeath = true;

            Utils.grantAdvancement(player, "successfully_landed");
            if (ConfigLoader.getInstance().enableDialogAfterIntro) {
                introMessages(player, false);
            } else {
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }
        }
    }

    private void handleIntroDeath(ServerPlayerEntity player, PlayerData playerData) {
        if (!playerData.introDeathByGod) {
            Utils.UTILS.sendTextAfter(player, "That was... bad.", 20);
            playerData.firstTouchGround = true;
            playerData.firstTeleportedToSky = true;
            playerData.completeLoadingTerrainScreen = true;
            Utils.grantAdvancement(player, "a_rough_start");
        }
    }

    private void spawnLandEffect(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        for (int i = 0; i < 20; i++) {
            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    player.getX() + (world.random.nextDouble() - 0.5) * 0.5,
                    player.getY() + world.random.nextDouble() * 1.0,
                    player.getZ() + (world.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0, 0, 0.05);
        }
    }

    public void registerIntroEvents() {
        if (ConfigLoader.getInstance().enableIntroOfTheWorld) {
            ServerPlayerEvents.AFTER_RESPAWN.register((serverPlayerEntity1, serverPlayerEntity, c) -> {
                PlayerData playerData = dataHandler.playerDataMap.get(serverPlayerEntity.getUuid());
                if (playerData == null)
                    return;

                if (!playerData.introMessageAfterDeath) {
                    playerData.introMessageAfterDeath = true;
                    if (ConfigLoader.getInstance().enableDialogAfterIntro) {
                        introMessages(serverPlayerEntity1, true);
                    } else {
                        Utils.grantAdvancement(serverPlayerEntity1, "a_rough_start");
                        Utils.grantAdvancement(serverPlayerEntity1, "welcome_to_easycraft");
                    }
                }
            });

            ServerPlayNetworking.registerGlobalReceiver(FIRST_ORIGIN_CHOOSING_SCREEN,
                    (server, p, handler, buf, responseSender) -> {
                        // Read all data from the buffer immediately
                        int state = buf.readInt();

                        server.execute(() -> {
                            // Validate that the player exists
                            if (p == null) {
                                return;
                            }

                            // Access player data
                            PlayerData playerData = dataHandler.playerDataMap.get(p.getUuid());

                            if (playerData == null) {
                                return;
                            }

                            if (playerData.completeOriginSelectingScreen)
                                return;

                            if (state == 1) {
                                playerData.firstOriginSelectingScreen = true;
                            }

                            if (state == 0 && playerData.firstOriginSelectingScreen) {
                                playerData.completeOriginSelectingScreen = true;
                                ServerPlayNetworking.send(p, STOP_SENDING_ORIGINS_SCREEN, PacketByteBufs.empty());
                            }
                        });
                    });

            ServerPlayNetworking.registerGlobalReceiver(FIRST_LOADING_TERRAIN_SCREEN,
                    (server, p, handler, buf, responseSender) -> {
                        // Read all data from the buffer immediately
                        buf.readInt(); // state variable removed as it was unused

                        server.execute(() -> {
                            // Validate that the player exists
                            if (p == null) {
                                return;
                            }

                            // Access player data
                            PlayerData playerData = dataHandler.playerDataMap.get(p.getUuid());

                            if (playerData == null) {
                                return;
                            }

                            if (playerData.completeLoadingTerrainScreen)
                                return;

                            playerData.completeLoadingTerrainScreen = true;
                        });
                    });
        }
    }

    public void introMessages(ServerPlayerEntity player, boolean isDeath) {
        // There're 2 cases will happen, one is the npc couldn't protect the player and
        // let them fall to the ground lead to their death and respawn, second is
        // the NPC successfully protected the player from falling from the summon
        // ritual, both will have different messages from the NPC

        if (!isDeath) {
            Utils.UTILS.sendTextAfter(player, "Whew, that was close!", 20);
            Utils.UTILS.sendTextAfter(player, "That was a hard land, try pulling your self together.", 4 * 20);
            Utils.UTILS.sendTextAfter(player,
                    "Feeling less dizzy now? It looks like you're done with your old world... so welcome to this new one!",
                    13 * 20);
            Utils.UTILS.sendTextAfter(player,
                    "Oh, and here's a small gift for you. Sorry about the botched summoning ritual.", 18 * 20);

            Utils.addRunAfter(() -> {
                if (player == null)
                    return;

                ItemStack summonGolem = GiveStartingItemsHandler.getModdedItems("advancedgolems:golem_spawner", 1);
                ItemStack golemController = GiveStartingItemsHandler.getModdedItems("advancedgolems:golem_control", 1);

                if (summonGolem != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, summonGolem, 2);
                }

                if (golemController != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, golemController, 3);
                }
            }, 21 * 20);

            Utils.UTILS.sendTextAfter(player, "That Golem will be a great help. Consider it your new best friend.",
                    24 * 20);

            Utils.UTILS.sendTextAfter(player,
                    "I think I have a book that might help you progress in this magical world...", 28 * 20);

            Utils.addRunAfter(() -> {
                ItemStack book = GiveStartingItemsHandler.getModdedItems("ftbquests:book", 1);
                if (book != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, book, 4);
                }
            }, 32 * 20);

            Utils.UTILS.sendTextAfter(player,
                    "Well, that's it for now, can't stay here for long, I've got to get back to my work. Enjoy Your New World!",
                    36 * 20);

            Utils.addRunAfter(() -> {
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }, 40 * 20);

            Utils.addRunAfter(() -> {
                spawnBlossom((ServerWorld) player.getWorld(), player);
            }, 46 * 20);

        } else {
            if (player.getWorld().getLevelProperties().isHardcore()) {
                Utils.UTILS.sendTextAfter(player,
                        "I'm late... again.");
                Utils.grantAdvancement(player, "welcome_to_easycraft");
                return;
            }

            Utils.UTILS.sendTextAfter(player, "I was too late. My spell didn’t hold. I’m sorry… truly.", 2 * 20);
            Utils.UTILS.sendTextAfter(player, "But let's not dwell on the past. Welcome to this new world!", 6 * 20);
            Utils.UTILS.sendTextAfter(player,
                    "I can tell you're looking for adventure. That's why you're summoned here by the rift. Now, hold on a moment...",
                    10 * 20);

            Utils.addRunAfter(() -> {
                if (player == null)
                    return;

                ItemStack summonGolem = GiveStartingItemsHandler.getModdedItems("advancedgolems:golem_spawner", 1);
                ItemStack golemController = GiveStartingItemsHandler.getModdedItems("advancedgolems:golem_control", 1);

                if (summonGolem != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, summonGolem, 2);
                }

                if (golemController != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, golemController, 3);
                }
            }, 14 * 20);

            Utils.UTILS.sendTextAfter(player,
                    "Here you go—a, we've a 'friend' to keep you company, and a controller so you can guide it.",
                    15 * 20);
            Utils.UTILS.sendTextAfter(player, "Well, maybe 'friendship' isn't the healthiest way to describe this...",
                    18 * 20);

            Utils.UTILS.sendTextAfter(player, "There's more, i think there's something for you to eat...", 21 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("expandeddelight:cheese_wheel", 1);
                if (food != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, food, 4);
                }
            }, 23 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("expandeddelight:chocolate_cooke", 1);
                if (food != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, food, 1);
                }
            }, 25 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("croptopia:steamed_rice", 5);
                if (food != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, food, 5);
                }
            }, 27 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("croptopia:cooked_bacon", 1);
                if (food != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, food, 6);
                }
            }, 29 * 20);

            Utils.UTILS.sendTextAfter(player,
                    "I don't really into cooking much, so that's all from me, but the most important is... Enjoy Your New World!, now I'll get back to work...",
                    31 * 20);

            Utils.addRunAfter(() -> {
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }, 35 * 20);

            Utils.UTILS.sendTextAfter(player, "Wait, I forgot a really useful book, here you go", 42 * 20);

            Utils.addRunAfter(() -> {
                ItemStack book = GiveStartingItemsHandler.getModdedItems("ftbquests:book", 1);
                if (book != null) {
                    GiveStartingItemsHandler.giveItemToPlayerSlot(player, book, 7);
                }
            }, 44 * 20);
        }
    }

    public void spawnBlossom(ServerWorld world, PlayerEntity player) {
        if (world.isClient)
            return;

        if (player == null)
            return;

        Blossom blossom = EntitiesManager.BLOSSOM.create(world.toServerWorld());

        if (blossom != null) {
            BlockPos safePos = findSpawnPosition((ServerPlayerEntity) player, 3);
            ;

            if (safePos == null) {
                LOGGER.info("No safe pos to spawn Blossom");
                return;
            }

            blossom.setPosition(safePos.getX(), safePos.getY(), safePos.getZ());

            Utils.addRunAfter(() -> {
                blossom.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, player.getPos());
            }, 5);

            blossom.setIsGreeting(true);
            blossom.greetingTarget = player;

            world.spawnEntity(blossom);
        }
    }

    public static BlockPos findSpawnPosition(ServerPlayerEntity player, int scanRadius) {
        ServerWorld world = (ServerWorld) player.getWorld();

        // Get chunk coordinates once
        ChunkPos playerChunkPos = new ChunkPos(player.getBlockPos());

        // 1. Highest priority - 2 blocks in front at head height
        BlockPos frontPos = calculateFrontPosition(player);
        if (isPositionSafe(world, frontPos, playerChunkPos)) {
            return frontPos;
        }

        // 2. Check below front position (with chunk check)
        BlockPos belowPos = findSafeBelow(world, frontPos, playerChunkPos);
        if (belowPos != null) {
            return belowPos;
        }

        // 3. Scan around player with chunk awareness
        BlockPos aroundPos = scanAroundPosition(world, frontPos, scanRadius, playerChunkPos);
        return aroundPos;
    }

    private static boolean isPositionSafe(ServerWorld world, BlockPos pos, ChunkPos centerChunk) {
        // Check chunk loading first
        if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }

        // Check if position is within world bounds
        if (!world.isInBuildLimit(pos))
            return false;

        // Check block states only if chunk is loaded
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir();
    }

    private static BlockPos findSafeBelow(ServerWorld world, BlockPos startPos, ChunkPos centerChunk) {
        // Limit search to original chunk
        if (!isSameChunk(startPos, centerChunk))
            return null;

        for (int i = 0; i < 5; i++) {
            BlockPos checkPos = startPos.down(i);
            if (isPositionSafe(world, checkPos, centerChunk)) {
                return checkPos;
            }
        }
        return null;
    }

    private static BlockPos scanAroundPosition(ServerWorld world, BlockPos center, int radius, ChunkPos centerChunk) {
        // Search only in initially loaded chunks around player
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Only check chunks that were loaded when we started
                if (!world.getChunkManager().isChunkLoaded(centerChunk.x + dx, centerChunk.z + dz)) {
                    continue;
                }

                // Check vertical column in loaded chunk
                for (int dy = -radius; dy <= radius; dy++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (isPositionSafe(world, pos, centerChunk)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSameChunk(BlockPos pos, ChunkPos chunk) {
        return pos.getX() >> 4 == chunk.x && pos.getZ() >> 4 == chunk.z;
    }

    private static BlockPos calculateFrontPosition(ServerPlayerEntity player) {
        // Convert yaw to direction vector
        float yaw = player.getHeadYaw();
        double radianYaw = Math.toRadians(yaw);

        double xOffset = -Math.sin(radianYaw) * 2;
        double zOffset = Math.cos(radianYaw) * 2;

        // Calculate position 2 blocks in front at head height
        return new BlockPos(
                (int) Math.floor(player.getX() + xOffset),
                (int) Math.floor(player.getY() + 1.5), // Head height
                (int) Math.floor(player.getZ() + zOffset));
    }

    private static boolean isPositionSafe(World world, BlockPos pos) {
        // Check if position is within world bounds
        if (!world.isInBuildLimit(pos))
            return false;

        // Check if block is passable and has space
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir();
    }

    private static BlockPos findSafeBelow(World world, BlockPos startPos) {
        // Check up to 5 blocks below
        for (int i = 0; i < 5; i++) {
            BlockPos checkPos = startPos.down(i);
            if (isPositionSafe(world, checkPos)) {
                return checkPos;
            }
        }

        return null;
    }

    public void spawnPhantom(ServerWorld world, PlayerEntity player) {
        // Get player's current position

        if (player == null)
            return;

        int addDistance = 20;
        int distance = 80;

        Vec3d playerPos = player.getPos();

        for (int x = 0; x < phantomSpawnAmount; x++) {
            double offsetX = (world.getRandom().nextDouble() - 0.5) * 2 * distance;
            double offsetY = world.getRandom().nextDouble() * 5 - rand.nextInt(0, 20);
            double offsetZ = (world.getRandom().nextDouble() - 0.5) * 2 * distance;

            // Calculate the spawn position
            BlockPos spawnPos = new BlockPos((int) (playerPos.x + offsetX), (int) (playerPos.y + offsetY),
                    (int) (playerPos.z + offsetZ));
            // Create and spawn the Phantom entity
            PhantomEntity phantom = EntityType.PHANTOM.create(world.toServerWorld());

            if (!world.getBlockState(spawnPos).isAir())
                continue;

            if (phantom != null) {
                phantom.setPhantomSize(rand.nextInt(50, 200));
                phantom.refreshPositionAndAngles(spawnPos, world.getRandom().nextFloat() * 360F, 0);
                world.spawnEntity(phantom);
            }

            distance += rand.nextInt(10, addDistance);
        }

        double offsetX = (world.getRandom().nextDouble() - 0.5) * 2 * 80;
        double offsetY = world.getRandom().nextDouble() * 5 - rand.nextInt(100, 180);
        double offsetZ = (world.getRandom().nextDouble() - 0.5) * 2 * 80;

        // Calculate the spawn position
        BlockPos spawnPos = new BlockPos((int) (playerPos.x + offsetX), (int) (playerPos.y + offsetY),
                (int) (playerPos.z + offsetZ));
        if (!world.getBlockState(spawnPos).isAir())
            return;

        // Create and spawn the Phantom entity
        PhantomEntity phantom = EntityType.PHANTOM.create(world.toServerWorld());

        if (phantom != null) {
            phantom.setPhantomSize(200);
            phantom.refreshPositionAndAngles(spawnPos, world.getRandom().nextFloat() * 360F, 0);
            world.spawnEntity(phantom);
        }
    }

    private boolean isGroundNearby(ServerPlayerEntity player, int minDistance, int maxDistance) {
        World world = player.getWorld();
        BlockPos.Mutable mutablePos = player.getBlockPos().mutableCopy();

        for (int i = minDistance; i <= maxDistance; i++) {
            if (!world.getBlockState(mutablePos.set(player.getX(), player.getY() - i, player.getZ())).isAir()) {
                return true;
            }
        }
        return false;
    }
}
