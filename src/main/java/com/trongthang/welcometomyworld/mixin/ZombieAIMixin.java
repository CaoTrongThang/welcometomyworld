package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.trongthang.welcometomyworld.GlobalConfig.canZombieAI;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;
import static com.trongthang.welcometomyworld.BlocksPlacedAndBrokenByMobsHandler.ZOMBIE_BLOCK_DESPAWN_TICK;

@Mixin(ZombieEntity.class)
public class ZombieAIMixin {

    private final int cooldown = 20;
    private int counter = cooldown;

    PlayerEntity lastTargetPlayer;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        if (!canZombieAI) return;

        if (counter < cooldown) {
            counter++;
        }

        ZombieEntity zombieEntity = (ZombieEntity) (Object) this;
        PlayerEntity targetPlayer = null;

        if (zombieEntity.hasVehicle() || zombieEntity.isTouchingWater()) return;

        if (zombieEntity.getTarget() instanceof PlayerEntity) {
            targetPlayer = (PlayerEntity) zombieEntity.getTarget();
        }

        if (targetPlayer == null) {
            if (lastTargetPlayer != null) {
                if (zombieEntity.getServer().getPlayerManager().getPlayer(lastTargetPlayer.getUuid()) != null) {
                    zombieEntity.setTarget(lastTargetPlayer);
                    targetPlayer = lastTargetPlayer;
                }
            }

        } else {
            if (lastTargetPlayer != targetPlayer) {
                lastTargetPlayer = targetPlayer;
            }
        }

        if (lastTargetPlayer == null) {
            return;
        }

        if (lastTargetPlayer.isDead() || lastTargetPlayer.isCreative() || lastTargetPlayer.isSpectator() || lastTargetPlayer.getWorld() != lastTargetPlayer.getWorld()) {
            lastTargetPlayer = null;
            zombieEntity.setTarget(null);
            return;
        }

        if (zombieEntity.distanceTo(lastTargetPlayer) < 48) {
            zombieEntity.setTarget(lastTargetPlayer);
        } else {
            zombieEntity.setTarget(null);
            lastTargetPlayer = null;
            return;
        }

        World world = zombieEntity.getWorld();

        checkIfPlayerTooHighThenPlaceBlocksToChase(zombieEntity, world);
    }

    private void checkIfPlayerTooHighThenPlaceBlocksToChase(ZombieEntity zombieEntity, World world) {

        if (lastTargetPlayer == null) return;
        Path path = zombieEntity.getNavigation().getCurrentPath();

        float distance = zombieEntity.distanceTo(lastTargetPlayer);
        BlockPos downPos = zombieEntity.getBlockPos().down();
        BlockPos currentPos = zombieEntity.getBlockPos();

        Vec3d directionToPlayer = lastTargetPlayer.getPos()
                .subtract(new Vec3d(downPos.getX() + 0.5, zombieEntity.getY(), downPos.getZ() + 0.5))
                .normalize();

        // Convert normalized vector into block-based direction
        int dx = (int) Math.round(directionToPlayer.x);
        int dz = (int) Math.round(directionToPlayer.z);

        // Ensure valid block positions
        BlockPos firstNextBlockPos = downPos.add(dx, 0, dz);
//        BlockPos upBlock = currentPos.add(dx, 1, dz);

        if (path == null && Math.abs(zombieEntity.getX() - lastTargetPlayer.getX()) <= 5 && counter >= cooldown) {
            counter = 0;
            boolean anyBlockUpHead = zombieEntity.getWorld().isSkyVisible(currentPos);

            if (anyBlockUpHead && ((lastTargetPlayer.getY() > zombieEntity.getY()) && Math.abs(zombieEntity.getY() - lastTargetPlayer.getY()) >= 2)) {

                if (zombieEntity.isOnGround()) {
                    tryJumpUp(zombieEntity);
                    Utils.addRunAfter(() -> {
                        placeBlock(world, currentPos);
                    }, 5);
                    return;
                }

            }

            if (Math.abs(zombieEntity.getY() - lastTargetPlayer.getY()) <= 2 && zombieEntity.getY() >= lastTargetPlayer.getY() - 1 && distance >= 2.5) {
                placeBlock(world, firstNextBlockPos);
            }
        }

//        if (!canPlaceBlockUp && canPlaceBlockSides) {
//            placeBlock(world, firstNextBlockPos);
//        } else if (canPlaceBlockUp && !canPlaceBlockSides) {
//            if (zombieEntity.isOnGround()) {
//                tryJumpUp(zombieEntity);
//
//                placeBlock(world, currentPos);
//                zombieEntity.setPose(EntityPose.SPIN_ATTACK);
//            }
//        }
    }

    private void tryJumpUp(ZombieEntity zombieEntity) {
        if (!zombieEntity.isOnGround()) return; // Jump only if on the ground

        // Adjust jump behavior
        Vec3d jumpVelocity = zombieEntity.getRotationVec(0.0F).multiply(0, 0, 0).add(0, 0.6, 0);

        zombieEntity.setVelocity(zombieEntity.getVelocity().add(jumpVelocity));
        zombieEntity.velocityDirty = true;
    }

    private void placeBlock(World world, BlockPos nextBlockPosToPlace) {

        boolean canPlace = false;
        BlockPos targetPos = nextBlockPosToPlace;

        if (!world.isClient) {
            // ! CHECK SNOW AND GRASS HERE, I WANT TO PLACE ON BLOCK CAN ALSO CAN PLACE THROUGH
            BlockState state = world.getBlockState(nextBlockPosToPlace);
            Block block = state.getBlock();
            if (block == Blocks.SNOW || block == Blocks.GRASS || block == Blocks.TALL_GRASS || world.isAir(nextBlockPosToPlace)) {
                canPlace = true;
            } else {

                if (world.isAir(nextBlockPosToPlace)) return;


                List<BlockPos> solidBlockPos = new java.util.ArrayList<>(List.of(
                        nextBlockPosToPlace.north(), nextBlockPosToPlace.south(), nextBlockPosToPlace.east(), nextBlockPosToPlace.west(), nextBlockPosToPlace.up(), nextBlockPosToPlace.down(),
                        nextBlockPosToPlace.north(1), nextBlockPosToPlace.south(1), nextBlockPosToPlace.east(1), nextBlockPosToPlace.west(1), nextBlockPosToPlace.down(1)
                ));

                solidBlockPos.sort((a, b) -> {
                    double distanceToA = lastTargetPlayer.getPos().squaredDistanceTo(Vec3d.ofCenter(a));
                    double distanceToB = lastTargetPlayer.getPos().squaredDistanceTo(Vec3d.ofCenter(b));
                    return Double.compare(distanceToA, distanceToB);
                });

                for (BlockPos targetBlock : solidBlockPos) {
                    if (world.isAir(targetBlock)) {
                        canPlace = true;
                        targetPos = targetBlock;
                    }
                    break;
                }
            }

            if (canPlace) {
                world.setBlockState(targetPos, Blocks.GRASS_BLOCK.getDefaultState());

                dataHandler.blocksPlacedByMobWillRemove.put(targetPos, ZOMBIE_BLOCK_DESPAWN_TICK);

                // Play a block placement sound
                world.playSound(
                        null,                           // Player to play sound for (null means all nearby players)
                        nextBlockPosToPlace,                            // Position of the sound
                        Blocks.GRASS_BLOCK.getDefaultState().getSoundGroup().getPlaceSound(), // Sound to play (default place sound for the block)
                        net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                        1.0F,                           // Volume (1.0 = normal)
                        1.0F                            // Pitch (1.0 = normal)
                );
            }
        }
    }


    private void closeTrapdoorOpenDoor(World world, BlockPos pos) {
        world.playSound(
                null,                           // Player to play sound for (null means all nearby players)
                pos,                            // Position of the sound
                SoundEvents.BLOCK_BAMBOO_WOOD_BREAK, // Sound to play (default place sound for the block)
                net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                1.0F,                           // Volume (1.0 = normal)
                1.0F                            // Pitch (1.0 = normal)
        );
    }
}
