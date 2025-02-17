package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.managers.BlocksManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.random.Random;

import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.Utilities.Utils.checkFireDirection;
import static com.trongthang.welcometomyworld.Utilities.Utils.isFireBurningAtTheBlock;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixinTick {

    private static final int MIN_ADD_TIME = 1;
    private static final int SCHEDULE_DELAY_TIME = 20;


    private static final int RUSTY_IRON_BLOCK_TIME = 240;
    private static final int PLANKS_LOGS_TURN_TO_BURNING_BLOCK_TIME = 80;

    private static final ConcurrentHashMap<BlockPos, Integer> blockTurnIntoCounter = new ConcurrentHashMap<>();

    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        scheduledTickMethod(state, world, pos);
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void onStateReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {

        stateAddAndRemove(BlockTags.LOGS, newState, oldState, world, pos);
        stateAddAndRemove(BlockTags.PLANKS, newState, oldState, world, pos);

        if (newState.isOf(Blocks.FIRE)) {
            BlockPos burningPos = checkFireDirection(newState, pos);

            if (burningPos == null) return;

            BlockState currentBurningState = world.getBlockState(burningPos);
            if (!blockTurnIntoCounter.containsKey(burningPos)) ;
            {
                if (currentBurningState.isIn(BlockTags.PLANKS) || currentBurningState.isIn(BlockTags.LOGS)) {
                    world.scheduleBlockTick(burningPos, currentBurningState.getBlock(), 20);
                }
            }
        }
    }

    @Deprecated
    public void onBlockAdded(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean notify) {

    }

    private void scheduledTickMethod(BlockState state, ServerWorld world, BlockPos pos) {
        if (state.isIn(BlockTags.PLANKS) || state.isIn(BlockTags.LOGS)) {
            boolean isBurning = isFireBurningAtTheBlock(world, pos);
            if (!isBurning) {
                blockTurnIntoCounter.remove(pos);
            } else {
                blockTurnIntoCounter.putIfAbsent(pos, 0);
                int counter = blockTurnIntoCounter.get(pos);
                counter += random.nextInt(MIN_ADD_TIME, SCHEDULE_DELAY_TIME);

                if (counter > PLANKS_LOGS_TURN_TO_BURNING_BLOCK_TIME) {
                    if (world.getServer() != null) {
                        Utils.spawnBlockBreakParticles(world.getServer().getOverworld(), pos, new BlockStateParticleEffect(ParticleTypes.BLOCK, state.getBlock().getDefaultState()));
                    }
                    world.setBlockState(pos, BlocksManager.BURNING_PLANK.getDefaultState());
                    blockTurnIntoCounter.remove(pos);

                }

                blockTurnIntoCounter.put(pos, counter);

                world.scheduleBlockTick(pos, state.getBlock(), SCHEDULE_DELAY_TIME);
            };
        }
    }

    private void stateAddAndRemove(TagKey tag, BlockState newState, BlockState oldState, World world, BlockPos pos) {
        if (newState.isIn(tag)) {
            world.scheduleBlockTick(pos, newState.getBlock(), 20);
        }

        if (oldState.isIn(tag)) {
            blockTurnIntoCounter.remove(pos);
        }
    }

    private void stateAddAndRemove(Block checkBlock, BlockState newState, BlockState oldState, World world, BlockPos pos) {
        if (newState.isOf(checkBlock)) {
            world.scheduleBlockTick(pos, newState.getBlock(), 20);
        }

        if (oldState.isOf(checkBlock)) {
            blockTurnIntoCounter.remove(pos);
        }
    }
}
