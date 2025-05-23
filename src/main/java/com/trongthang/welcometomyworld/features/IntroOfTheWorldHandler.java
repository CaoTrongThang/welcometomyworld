package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.SpawnParticiles;
import com.trongthang.welcometomyworld.classes.PlayerData;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.Blossom;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class IntroOfTheWorldHandler {

    Random rand = new Random();

    public static double playersDeathChanceInTheIntro = 0.15;

    byte phantomSpawnAmount = 8;
    public boolean alreadySpawnedPhantom = false;

    private int slownessTimeInTickAfterLand = 280;

    public static boolean firstTimeLoadChunkIntro = false;

    public void teleportPlayersToSkyFirstJoin(ServerPlayerEntity player) {
        //teleport player to the sky
        Vec3d skyPosition = new Vec3d(player.getX(), 400, player.getZ());
        player.teleport(skyPosition.x, skyPosition.y, skyPosition.z);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 160, 128));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 160, 4));
    }

    //THIS IS A FUCKING MESS, DON'T READ
    public void handlePlayerFirstJoin(ServerPlayerEntity player) {
        if (player.getWorld().isClient) return;
        PlayerData playerData = dataHandler.playerDataMap.get(player.getUuid());
        ServerWorld world = player.getServerWorld();

        if (!firstTimeLoadChunkIntro) {
            firstTimeLoadChunkIntro = true;
        }

        if (!playerData.firstTouchGround || !playerData.firstTeleportedToSky || !playerData.completeOriginSelectingScreen || !playerData.completeLoadingTerrainScreen ) {
            if (player.isCreative()) {
                dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
                return;
            }
            playerData.introTimeLimit--;
            if (playerData.introTimeLimit <= 0) {
                dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
                return;
            }
            ;
        }

        if (playerData.playerFirstIntroDeathChance == 0) {
            playerData.playerFirstIntroDeathChance = rand.nextDouble(0, 1);
        }

        if (compatityChecker.originMod) {
            if (!playerData.completeOriginSelectingScreen) {
                teleportPlayersToSkyFirstJoin(player);

                if (playerData.firstTouchGround) {
                    playerData.completeOriginSelectingScreen = true;
                }

                return;
            }
        }
        if (!playerData.completeLoadingTerrainScreen) {
            teleportPlayersToSkyFirstJoin(player);

            if (playerData.firstTouchGround) {
                playerData.completeLoadingTerrainScreen = true;
            }

            return;
        }

        if (!playerData.firstTeleportedToSky) {
            if (player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
                teleportPlayersToSkyFirstJoin(player);

                if(!playerData.completeSpawningParticles){
                    for (int x = 1; x < 40; x++) {
                        Utils.addRunAfter(() -> {
                            Utils.spawnCircleParticles(player);
                        }, x);
                    }
                    playerData.completeSpawningParticles = true;
                }

                Utils.UTILS.sendTextAfter(player, "Finally!", 20);

                for (int x = 0; x < rand.nextInt(10, 20); x++) {
                    Utils.addRunAfter(() -> {
                        Utils.summonLightning(player.getBlockPos(), player.getServerWorld(), true);
                    }, rand.nextInt(5, 20));
                }

                if (!alreadySpawnedPhantom) {
                    spawnPhantom(player.getServerWorld(), player);
                    alreadySpawnedPhantom = true;
                }
            } else {
                playerData.firstTouchGround = true;
                dataHandler.playerDataMap.put(player.getUuid(), PlayerData.CreateExistPlayer());
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }
        };

        if (!playerData.firstTeleportedToSky && player.getY() >= 350) {
            playerData.firstTeleportedToSky = true;
            ServerPlayNetworking.send(player, PLAY_BLOCK_PORTAL_TRAVEL, PacketByteBufs.empty());
        }

        if (playerData.firstTouchGround || !playerData.firstTeleportedToSky) return;

        if (playerData.completeOriginSelectingScreen && !playerData.completeSpawningParticles) {
            Utils.spawnCircleParticles(player);
            for (int x = 1; x < 40; x++) {
                Utils.addRunAfter(() -> {
                    Utils.spawnCircleParticles(player);
                }, x);
            }

            playerData.completeSpawningParticles = true;

            Utils.UTILS.sendTextAfter(player, "Finally!");
        }

        if ((player.getHealth() <= 0 || player.isDead()) && !playerData.introDeathByGod) {
            Utils.UTILS.sendTextAfter(player, "That was... bad.", 20);
            playerData.firstTouchGround = true;
            playerData.firstTeleportedToSky = true;
            Utils.grantAdvancement(player, "a_rough_start");
            playerData.completeLoadingTerrainScreen = true;
            return;
        }

        if (playerData.playerFirstIntroDeathChance > playersDeathChanceInTheIntro) {
            if (!world.getBlockState(player.getBlockPos().down(40)).isAir()) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 255));
            }
            if (Utils.isPlayerStandingOnBlock(player)) {
                if ((player.getHealth() <= 0 || player.isDead()) && !playerData.introDeathByGod) {
                    Utils.UTILS.sendTextAfter(player, "That was... bad.", 20);
                    playerData.firstTouchGround = true;
                    playerData.firstTeleportedToSky = true;
                    Utils.grantAdvancement(player, "a_rough_start");
                    playerData.completeLoadingTerrainScreen = true;
                    return;
                }

                // Create an explosion at the player's landing position
                BlockPos landingPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
                world.createExplosion(player, landingPos.getX(), landingPos.getY(), landingPos.getZ(), 6F, World.ExplosionSourceType.TNT);

                player.setVelocity(player.getVelocity().x, 1.2, player.getVelocity().z); // Small upward bounce
                player.velocityModified = true; // Ensure the velocity is synced to the client
                Utils.addRunAfter(() -> {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 4));
                }, 20);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slownessTimeInTickAfterLand, 2));

                spawnLandEffect(player);
                SpawnParticiles.spawnExpandingParticleSquare(world, player, 2, 5, 20, ParticleTypes.END_ROD);
                // Set the player’s "firstLandFromSky" to true
                playerData.firstTouchGround = true;
                playerData.firstTeleportedToSky = true;
                playerData.introMessageAfterDeath = true;

                Utils.grantAdvancement(player, "successfully_landed");
                introMessages(player, false);
                playerData.completeLoadingTerrainScreen = true;
            }
        } else {
            if (Utils.isPlayerStandingOnBlock(player) && player.getHealth() > 0) {
                BlockPos landingPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
                world.createExplosion(player, landingPos.getX(), landingPos.getY(), landingPos.getZ(), 6F, World.ExplosionSourceType.TNT);
                playerData.completeLoadingTerrainScreen = true;
                player.setVelocity(player.getVelocity().x, 1.2, player.getVelocity().z); // Small upward bounce
                player.velocityModified = true; // Ensure the velocity is synced to the client
                Utils.addRunAfter(() -> {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 4));
                }, 20);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, slownessTimeInTickAfterLand, 2));

                spawnLandEffect(player);
                SpawnParticiles.spawnExpandingParticleSquare(world, player, 2, 5, 20, ParticleTypes.END_ROD);
                // Set the player’s "firstLandFromSky" to true
                playerData.firstTouchGround = true;
                playerData.firstTeleportedToSky = true;
                playerData.introMessageAfterDeath = true;

                Utils.grantAdvancement(player, "successfully_landed");
                introMessages(player, false);
            }
        }
    }

    private void spawnLandEffect(ServerPlayerEntity player) {
        // Create a particle effect around the zombie to simulate a glowing effect
        ServerWorld world = player.getServerWorld();
        for (int i = 0; i < 20; i++) {
            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    player.getX() + (world.random.nextDouble() - 0.5) * 0.5,
                    player.getY() + world.random.nextDouble() * 1.0,
                    player.getZ() + (world.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0, 0, 0.05
            );
        }
    }

    public void registerIntroEvents() {
        if (ConfigLoader.getInstance().introOfTheWorld) {
            ServerPlayerEvents.AFTER_RESPAWN.register((serverPlayerEntity1, serverPlayerEntity, c) -> {
                PlayerData playerData = dataHandler.playerDataMap.get(serverPlayerEntity.getUuid());
                if (playerData == null) return;

                if (!playerData.introMessageAfterDeath) {
                    playerData.introMessageAfterDeath = true;
                    introMessages(serverPlayerEntity1, true);
                }
            });

            ServerPlayNetworking.registerGlobalReceiver(FIRST_ORIGIN_CHOOSING_SCREEN, (server, p, handler, buf, responseSender) -> {
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

                    if (playerData.completeOriginSelectingScreen) return;

                    if (state == 1) {
                        playerData.firstOriginSelectingScreen = true;
                    }

                    if (state == 0 && playerData.firstOriginSelectingScreen) {
                        playerData.completeOriginSelectingScreen = true;
                        ServerPlayNetworking.send(p, STOP_SENDING_ORIGINS_SCREEN, PacketByteBufs.empty());
                    }
                });
            });

            ServerPlayNetworking.registerGlobalReceiver(FIRST_LOADING_TERRAIN_SCREEN, (server, p, handler, buf, responseSender) -> {
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

                    if (playerData.completeLoadingTerrainScreen) return;

                    playerData.completeLoadingTerrainScreen = true;
                });
            });
        }
    }


    public void introMessages(ServerPlayerEntity player, boolean isDeath) {
        //There're 2 cases will happen, one is the npc couldn't protect the player and let them fall to the ground lead to their death and respawn, second is
        //the NPC successfully protected the player from falling from the summon ritual, both will have different messages from the NPC

        if (!isDeath) {
            Utils.UTILS.sendTextAfter(player, "Whew, that was close!", 20);
            Utils.UTILS.sendTextAfter(player, "That was a hard land, try pulling your self together.", 4 * 20);
            Utils.UTILS.sendTextAfter(player, "Feeling less dizzy now? It looks like you're done with your old world... so welcome to this new one!", 13 * 20);
            Utils.UTILS.sendTextAfter(player, "Oh, and here's a small gift for you. Sorry about the botched summoning ritual.", 18 * 20);

            Utils.addRunAfter(() -> {
                giveStartingItemsHandler.giveMoreItems(player);
            }, 21 * 20);

            Utils.UTILS.sendTextAfter(player, "That Golem will be a great help. Consider it your new best friend.", 24 * 20);

            Utils.UTILS.sendTextAfter(player, "I think I have a book that might help you progress in this magical world...", 28 * 20);

            Utils.addRunAfter(() -> {
                ItemStack book = GiveStartingItemsHandler.getModdedItems("ftbquests:book", 1); // Change to mod's item ID and quantity
                if (book != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, book);
                }
            }, 32 * 20);

            Utils.UTILS.sendTextAfter(player, "Well, that's it for now. Enjoy Your New World!", 36 * 20);

            Utils.addRunAfter(() -> {
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }, 40 * 20);

            Utils.addRunAfter(() -> {
                spawnBlossom((ServerWorld) player.getWorld(), player);
            }, 46 * 20);

        } else {
            if (player.getWorld().getLevelProperties().isHardcore()) {
                Utils.UTILS.sendTextAfter(player, "Oops, my protection spell came a bit late... Looks like it's time to create a new world!");
                Utils.grantAdvancement(player, "welcome_to_easycraft");
                return;
            }

            Utils.UTILS.sendTextAfter(player, "Well, that was a disaster. I'm getting too old for this...", 2 * 20);
            Utils.UTILS.sendTextAfter(player, "But let's not dwell on the past. Welcome to this new world!", 6 * 20);
            Utils.UTILS.sendTextAfter(player, "I can tell you're looking for adventure. That's why I summoned you here. Now, hold on a moment...", 10 * 20);


            Utils.addRunAfter(() -> {
                giveStartingItemsHandler.giveMoreItems(player);
            }, 14 * 20);

            Utils.UTILS.sendTextAfter(player, "Here you go—a, we've a 'friend' to keep you company, and a controller so you can guide it.", 15 * 20);
            Utils.UTILS.sendTextAfter(player, "Well, maybe 'friendship' isn't the healthiest way to describe this...", 18 * 20);

            Utils.UTILS.sendTextAfter(player, "There's more, i think there's something for you to eat...", 21 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("expandeddelight:cheese_wheel", 1); // Change to mod's item ID and quantity
                if (food != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, food);
                }
            }, 23 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("expandeddelight:chocolate_cooke", 1); // Change to mod's item ID and quantity
                if (food != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, food);
                }
            }, 25 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("croptopia:steamed_rice", 1); // Change to mod's item ID and quantity
                if (food != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, food);
                }
            }, 27 * 20);

            Utils.addRunAfter(() -> {
                ItemStack food = GiveStartingItemsHandler.getModdedItems("croptopia:cooked_bacon", 1); // Change to mod's item ID and quantity
                if (food != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, food);
                }
            }, 29 * 20);

            Utils.UTILS.sendTextAfter(player, "I don't really into cooking much, so that's all from me, but the most important is... Enjoy Your New World!", 31 * 20);

            Utils.addRunAfter(() -> {
                Utils.grantAdvancement(player, "welcome_to_easycraft");
            }, 35 * 20);

            Utils.UTILS.sendTextAfter(player, "Wait, I forgot a really useful book, here you go", 42 * 20);

            Utils.addRunAfter(() -> {
                ItemStack book = GiveStartingItemsHandler.getModdedItems("ftbquests:book", 1); // Change to mod's item ID and quantity
                if (book != null) {
                    GiveStartingItemsHandler.dropItemToPlayer(player, book);
                }
            }, 44 * 20);
        }
    }

    public void spawnBlossom(ServerWorld world, PlayerEntity player) {
        if (world.isClient) return;

        if (player == null) return;

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
        if (!world.isInBuildLimit(pos)) return false;

        // Check block states only if chunk is loaded
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir();
    }

    private static BlockPos findSafeBelow(ServerWorld world, BlockPos startPos, ChunkPos centerChunk) {
        // Limit search to original chunk
        if (!isSameChunk(startPos, centerChunk)) return null;

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
                (int) Math.floor(player.getZ() + zOffset)
        );
    }

    private static boolean isPositionSafe(World world, BlockPos pos) {
        // Check if position is within world bounds
        if (!world.isInBuildLimit(pos)) return false;

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

        if (player == null) return;

        int addDistance = 20;
        int distance = 80;

        Vec3d playerPos = player.getPos();

        for (int x = 0; x < phantomSpawnAmount; x++) {
            double offsetX = (world.getRandom().nextDouble() - 0.5) * 2 * distance;
            double offsetY = world.getRandom().nextDouble() * 5 - rand.nextInt(0, 20);
            double offsetZ = (world.getRandom().nextDouble() - 0.5) * 2 * distance;

            // Calculate the spawn position
            BlockPos spawnPos = new BlockPos((int) (playerPos.x + offsetX), (int) (playerPos.y + offsetY), (int) (playerPos.z + offsetZ));
            // Create and spawn the Phantom entity
            PhantomEntity phantom = EntityType.PHANTOM.create(world.toServerWorld());

            if (!world.getBlockState(spawnPos).isAir()) continue;

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
        BlockPos spawnPos = new BlockPos((int) (playerPos.x + offsetX), (int) (playerPos.y + offsetY), (int) (playerPos.z + offsetZ));
        if (!world.getBlockState(spawnPos).isAir()) return;

        // Create and spawn the Phantom entity
        PhantomEntity phantom = EntityType.PHANTOM.create(world.toServerWorld());

        if (phantom != null) {
            phantom.setPhantomSize(200);
            phantom.refreshPositionAndAngles(spawnPos, world.getRandom().nextFloat() * 360F, 0);
            world.spawnEntity(phantom);
        }
    }
}
