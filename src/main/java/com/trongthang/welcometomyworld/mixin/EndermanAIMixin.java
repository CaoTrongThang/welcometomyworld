package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalConfig.canEndermanAI;

import java.util.Random;

@Mixin(EndermanEntity.class)
public abstract class EndermanAIMixin extends Entity {

    private final int COOLDOWN_TIME = 1400;
    private int counter = COOLDOWN_TIME;
    private boolean canUseTeleportSkill = true;
    private static boolean endermanCanPickBlocks = true;

    private final double chanceToUseSkill = 0.4;
    private final double chanceToSwitchBetweenTwoWayOfPickingPlayers = 0.5;

    private BlockPos lastPos;
    private BlockPos playerPos;
    private BlockPos destinationPos;

    private int delay = 4;
    private int counterDelay = 0;

    private boolean isUsingSkill = false;

    private PlayerEntity liftingPlayer;

    private Random rand = new Random();

    private int blockBelowPlayer = 8;

    public EndermanAIMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if(!canEndermanAI) return;

        if (!isUsingSkill) {
            if (!canUseTeleportSkill) {
                counter++;
                if (counter > COOLDOWN_TIME) {
                    canUseTeleportSkill = true;
                }
                return;
            }
        }

        EndermanEntity enderman = (EndermanEntity) (Object) this;
        PlayerEntity targetPlayer;

        World world = enderman.getWorld();
        if(world.getRegistryKey().getValue().equals(new Identifier("minecraft:the_end"))) return;

        // Check if Enderman has a player target
        if (enderman.getTarget() instanceof PlayerEntity) {
            targetPlayer = (PlayerEntity) enderman.getTarget();
        } else {
            targetPlayer = null;
        }

        counterDelay++;
        if (counterDelay > delay) {
            if (playerPos != null) {
                enderman.teleport(playerPos.getX(), playerPos.getY(), playerPos.getZ());
            }

            if (destinationPos != null) {
                enderman.teleport(destinationPos.getX(), destinationPos.getY(), destinationPos.getZ());
            }
            counterDelay = 0;
        }
        if (liftingPlayer != null) {
            teleportPlayerInFrontOfEnderman();

            Vec3d direction = liftingPlayer.getPos().subtract(enderman.getPos()).normalize();

            double yaw = Math.atan2(direction.x, direction.z);
            double pitch = Math.asin(direction.y);

            // Set the entity's yaw and pitch
            enderman.setYaw((float) (yaw * (180 / Math.PI))); // Convert radians to degrees
            enderman.setPitch((float) (pitch * (180 / Math.PI))); // Convert radians to degrees
        }

        if (targetPlayer == null) {
            liftingPlayer = null;
            return;
        }


        if (targetPlayer != null && enderman.isAngry() && canUseTeleportSkill) {
            double chance = rand.nextDouble();

            if (chance > chanceToUseSkill) {
                canUseTeleportSkill = false;
                counter = 0;
                return;
            }

            canUseTeleportSkill = false;
            counter = 0;
            isUsingSkill = true;

            if(!targetPlayer.getEntityWorld().isSkyVisible(targetPlayer.getBlockPos())) {
                liftingPlayer = null;
                return;
            }

            enderman.setCarriedBlock(Blocks.DIRT.getDefaultState());


            if (chance <= chanceToSwitchBetweenTwoWayOfPickingPlayers && enderman.getCarriedBlock() == null) {
                enderman.setCarriedBlock(Blocks.AIR.getDefaultState());
                // Teleport to player
                teleportToPlayer(targetPlayer, enderman);

                // After teleporting, start picking up the player, then teleport them to the sky
                Utils.addRunAfter(() -> pickPlayerUp(targetPlayer, enderman), 30);
                Utils.addRunAfter(() -> teleportToSkyWithPlayer(targetPlayer, enderman), 50);
                Utils.addRunAfter(() -> teleportBackToTheLastPos(enderman), 60);
            } else {
                //Pick up a block
                checkAroundAndPickBlock(enderman);

                Utils.addRunAfter(() -> teleportToPlayer(targetPlayer, enderman), 10);

                // After teleporting, start picking up the player, then teleport them to the sky
                Utils.addRunAfter(() -> pickPlayerUp(targetPlayer, enderman), 40);

                Utils.addRunAfter(() -> teleportToSkyWithPlayer(targetPlayer, enderman), 50);

                Utils.addRunAfter(() -> placeBlockBelowPlayer(enderman), 55);

                Utils.addRunAfter(() -> teleportBackToTheLastPos(enderman), 60);
            }

        }
    }

    private void teleportToPlayer(PlayerEntity targetPlayer, EndermanEntity enderman) {
        if(enderman.getHealth() <= 0) return;

        ServerPlayerEntity player = null;
        if (targetPlayer != null) {
            player = targetPlayer.getServer().getPlayerManager().getPlayer(targetPlayer.getUuid());
        }
        if (player == null) return;

        lastPos = enderman.getBlockPos(); // Save current position
        enderman.teleport(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ()); // Teleport to player
        playerPos = targetPlayer.getBlockPos();

        playSoundAndSpawnParticles(enderman.getBlockPos(), ParticleTypes.PORTAL, SoundEvents.ENTITY_ENDERMAN_TELEPORT);
    }

    private void pickPlayerUp(PlayerEntity targetPlayer, EndermanEntity enderman) {
        if(enderman.getHealth() <= 0) return;

        ServerPlayerEntity player = null;
        if (targetPlayer != null) {
            player = targetPlayer.getServer().getPlayerManager().getPlayer(targetPlayer.getUuid());
        }
        if (player == null) return;


        if (enderman.distanceTo(targetPlayer) > 6) {
            playSoundAndSpawnParticles(enderman.getBlockPos(), ParticleTypes.ANGRY_VILLAGER, SoundEvents.ENTITY_ENDERMAN_HURT);
            enderman.setTarget(targetPlayer);
            playerPos = null;
            liftingPlayer = null;
            return;
        }
        playSoundAndSpawnParticles(enderman.getBlockPos(), ParticleTypes.CLOUD, SoundEvents.BLOCK_ANVIL_PLACE);
        playerPos = null;
        liftingPlayer = targetPlayer; // Set the player that the Enderman is lifting
    }

    private void teleportToSkyWithPlayer(PlayerEntity targetPlayer, EndermanEntity enderman) {
        if(enderman.getHealth() <= 0) return;

        ServerPlayerEntity player = null;
        if (liftingPlayer != null) {
            player = liftingPlayer.getServer().getPlayerManager().getPlayer(liftingPlayer.getUuid());
        }
        if (player == null) return;

        isUsingSkill = false;

        enderman.setTarget(null);
        enderman.setAngryAt(null);

        if (liftingPlayer == null) return;if(enderman.getHealth() <= 0) return;


        BlockPos targetPos = targetPlayer.getBlockPos().add(0, rand.nextInt(100, 140), 0); // Calculate new position high in the sky

        playSoundAndSpawnParticles(targetPos, ParticleTypes.PORTAL, SoundEvents.ENTITY_ENDERMAN_TELEPORT);

        targetPlayer.requestTeleport(targetPos.getX() - 1, targetPos.getY(), targetPos.getZ() - 1); // Teleport the player

        enderman.requestTeleport(targetPos.getX(), targetPos.getY(), targetPos.getZ()); // Teleport the Enderman

        playerPos = null;
        destinationPos = targetPos;
    }

    private void placeBlockBelowPlayer(EndermanEntity enderman) {
        if(enderman.getHealth() <= 0) return;

        ServerPlayerEntity player = null;
        if (liftingPlayer != null) {
            player = liftingPlayer.getServer().getPlayerManager().getPlayer(liftingPlayer.getUuid());
        }
        if (player == null) return;

        World world = liftingPlayer.getWorld();

        BlockPos targetPos = player.getBlockPos().down(blockBelowPlayer);
        if (!world.isAir(targetPos)) return;

        Block endermanHoldingBlock = enderman.getCarriedBlock().getBlock();

        enderman.requestTeleport(targetPos.getX() - 1, targetPos.getY() - 2, targetPos.getZ() - 1);

        playSoundAndSpawnParticles(targetPos, ParticleTypes.PORTAL, SoundEvents.ENTITY_ENDERMAN_TELEPORT);

        world.setBlockState(targetPos, endermanHoldingBlock.getDefaultState());

        // Play a block placement sound
        world.playSound(
                null,                           // Player to play sound for (null means all nearby players)
                targetPos,                            // Position of the sound
                endermanHoldingBlock.getDefaultState().getSoundGroup().getPlaceSound(), // Sound to play (default place sound for the block)
                net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                1.0F,                           // Volume (1.0 = normal)
                1.0F                            // Pitch (1.0 = normal)
        );

        playerPos = null;

        enderman.setCarriedBlock(null);
    }

    // The method that makes the Enderman check nearby blocks and pick one up
    public void checkAroundAndPickBlock(EndermanEntity enderman) {
        BlockState block = enderman.getCarriedBlock();

        if (block != null) return;

        World world = enderman.getWorld();
        BlockPos endermanPos = enderman.getBlockPos();
        int radius = 2;  // Radius around the Enderman to scan for blocks

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius - 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = endermanPos.add(x, y, z);
                    BlockState blockState = world.getBlockState(pos);

                    // Check if the block is a valid block that an Enderman can pick up
                    if (isValidEndermanPickup(blockState)) {
                        // Make Enderman pick up the block
                        world.setBlockState(pos, Blocks.AIR.getDefaultState()); // Remove the block from the world
                        Utils.playSound(enderman.getServer().getOverworld(), enderman.getBlockPos(), blockState.getBlock().getDefaultState().getSoundGroup().getBreakSound());
                        enderman.setCarriedBlock(blockState.getBlock().getDefaultState()); // Make Enderman carry the block
                        return; // Stop after picking up one block
                    }
                }
            }
        }
    }

    // The method that makes the Enderman check nearby blocks and pick one up
    public boolean checkAroundTargetBlockIfItsAir(EndermanEntity enderman, BlockPos pos) {

        int radius = 3;
        World world = enderman.getWorld();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Check if the block is a valid block that an Enderman can pick up
                    if (!world.getBlockState(pos.add(x, y, z)).isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // Check if the block is valid for Enderman to pick up (similar to vanilla rules)
    private static boolean isValidEndermanPickup(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK) ||
                state.isOf(Blocks.DIRT) ||
                state.isOf(Blocks.SAND) ||
                state.isOf(Blocks.GRAVEL) ||
                state.isOf(Blocks.CLAY) ||
                state.isOf(Blocks.TERRACOTTA) ||
                state.isOf(Blocks.FLOWER_POT);
    }

    private void teleportBackToTheLastPos(EndermanEntity enderman) {
        ServerPlayerEntity player = null;
        if (liftingPlayer != null) {
            player = liftingPlayer.getServer().getPlayerManager().getPlayer(liftingPlayer.getUuid());
        }
        if (player == null) return;

        liftingPlayer = null;
        endermanCanPickBlocks = true;
        enderman.setCarriedBlock(null);

        playSoundAndSpawnParticles(enderman.getBlockPos(), ParticleTypes.PORTAL, SoundEvents.ENTITY_ENDERMAN_TELEPORT);
        destinationPos = null;
        enderman.teleport(lastPos.getX(), lastPos.getY(), lastPos.getZ()); // Teleport the Enderman back to its last position

        enderman.setTarget(null);
        enderman.setAngryAt(null);
    }

    private void teleportPlayerInFrontOfEnderman() {
        ServerPlayerEntity player = null;
        if (liftingPlayer != null) {
            player = liftingPlayer.getServer().getPlayerManager().getPlayer(liftingPlayer.getUuid());
        }
        if (player == null) return;

        // Teleport the player slightly in front of the Enderman
        BlockPos endermanPos = this.getBlockPos(); // Get the Enderman's position
        BlockPos frontOfEnderman = endermanPos.add(1, 0, 0); // Slightly offset to the right of Enderman
        liftingPlayer.requestTeleport(frontOfEnderman.getX(), frontOfEnderman.getY(), frontOfEnderman.getZ());
    }

    private void playSoundAndSpawnParticles(BlockPos pos, DefaultParticleType type, SoundEvent soundEvent) {
        MinecraftServer server = this.getServer();
        if(server == null) return;
        ServerWorld serverWorld = server.getOverworld();

        for (int i = 0; i < 30; i++) { // Create 10 flame particles
            double offsetX = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random X offset
            double offsetY = serverWorld.getRandom().nextDouble() * 2;         // Random Y offset
            double offsetZ = (serverWorld.getRandom().nextDouble() - 0.5) * 2; // Random Z offset

            serverWorld.spawnParticles(
                    type,                      // Particle type
                    pos.getX() + offsetX,                    // X coordinate
                    pos.getY() + offsetY,                    // Y coordinate
                    pos.getZ() + offsetZ,                    // Z coordinate
                    1,                                        // Particle count
                    0.0, 0.0, 0.0,                           // No velocity
                    0.0                                      // Speed multiplier
            );

            serverWorld.playSound(
                    null,                                   // Player (null = all nearby players hear the sound)
                    pos,                  // Position of the sound
                    soundEvent,      // Sound event (Blaze ambient sound)
                    SoundCategory.HOSTILE,                 // Sound category (hostile mob sounds)
                    0.8F,                                 // Volume (1.0 = normal volume)
                    1.0F                                   // Pitch (1.0 = normal pitch)
            );
        }
    }

    @Inject(method = "setCarriedBlock", at = @At("HEAD"), cancellable = true)
    private void preventBlockPickup(BlockState state, CallbackInfo ci) {
        if (!endermanCanPickBlocks) {
            ci.cancel(); // Cancel block pickup if disabled
        }
    }
}
