package com.trongthang.welcometomyworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.BlocksPlacedAndBrokenByMobsHandler.SPIDER_COBWEB_DESPAWN_TICK;
import static com.trongthang.welcometomyworld.GlobalConfig.canSpiderAI;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

@Mixin(SpiderEntity.class)
public abstract class SpiderAIMixin extends Entity {

    public int attackSpiderWebCooldown = 140;
    public int counter = attackSpiderWebCooldown;

    public int minShootDistance = 3;
    public int maxShootDistance = 8;

    public boolean isShooting = false;
    public int shootingSpeedTick = 1;
    public int shootSpeedCounter = 0;

    public BlockPos playerPosSaver = null;
    public BlockPos currentCobwebPos = null;

    public SpiderAIMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (!canSpiderAI) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;

        PlayerEntity targetPlayer = null;

        if (spider.getTarget() instanceof PlayerEntity) {
            targetPlayer = (PlayerEntity) spider.getTarget();
        } else {
            targetPlayer = null;
        }

        if (targetPlayer == null) return;

        if (isShooting) {
            shootSpeedCounter++;
            if(shootSpeedCounter < shootingSpeedTick) return;
            shootSpeedCounter = 0;

            World world = spider.getWorld();

            BlockPos taretPos = getNextBlockInDirection(currentCobwebPos, playerPosSaver);

            BlockState state = world.getBlockState(taretPos);
            if(state.isOf(Blocks.AIR) || state.isOf(Blocks.GRASS) || state.isOf(Blocks.TALL_GRASS)){
                currentCobwebPos = taretPos;
                placeCobwebBlock(world, taretPos);
                return;
            } else {}

            isShooting = false;
            playerPosSaver = null;
            currentCobwebPos = null;
            return;
        }


        counter++;
        if (counter < attackSpiderWebCooldown) {
            return;
        }
        if ((spider.distanceTo(targetPlayer) < maxShootDistance && spider.distanceTo(targetPlayer) > minShootDistance)) {
            isShooting = true;
            playerPosSaver = targetPlayer.getBlockPos();
            currentCobwebPos = getNextBlockInDirection(spider.getBlockPos(), targetPlayer.getBlockPos());
            counter = 0;
        }

    }

    private boolean placeCobwebBlock(World world, BlockPos pos) {
        if (!world.isAir(pos)) return false;

        dataHandler.blocksPlacedByMobWillRemove.put(pos, SPIDER_COBWEB_DESPAWN_TICK);

        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());


        // Play a block placement sound
        world.playSound(
                null,                           // Player to play sound for (null means all nearby players)
                pos,                            // Position of the sound
                Blocks.COBWEB.getDefaultState().getSoundGroup().getPlaceSound(), // Sound to play (default place sound for the block)
                net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                1.0F,                           // Volume (1.0 = normal)
                1.0F                            // Pitch (1.0 = normal)
        );
        return true;
    }

    private static BlockPos getNextBlockInDirection(BlockPos startPos, BlockPos targetPos) {
        // Step 1: Calculate the vector from the start position to the target position
        Vector3d direction = new Vector3d(targetPos.getX() - startPos.getX(),
                targetPos.getY() - startPos.getY(),
                targetPos.getZ() - startPos.getZ());

        // Step 2: Normalize the direction vector to ensure it's a unit vector (length = 1)
        direction = direction.normalize();

        // Step 3: Get the next block in the direction of the target (moving one unit in that direction)
        // You can round or cast the normalized vector to integers to move by one block
        int nextX = (int) Math.round(startPos.getX() + direction.x);
        int nextY = (int) Math.round(startPos.getY() + direction.y);
        int nextZ = (int) Math.round(startPos.getZ() + direction.z);

        // Step 4: Return the new position
        return new BlockPos(nextX, nextY, nextZ);
    }


    private static BlockPos getDirectionBlockPos(PlayerEntity player) {
        if (player == null) {
            return null;
        }

        // Get the player's current position and velocity
        Vec3d playerPos = player.getPos();
        Vec3d velocity = player.getVelocity();

        // Calculate the next position the player will be at
        Vec3d nextPos = playerPos.add(velocity.x, 0, velocity.z).normalize().multiply(2); // Adjust the multiplier as needed

        return new BlockPos((int) nextPos.x, player.getBlockPos().getY(), (int) nextPos.z);
    }
}
