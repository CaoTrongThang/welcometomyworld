package com.trongthang.welcometomyworld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

public class BlocksPlacedAndBrokenByMobsHandler {
    public static final int SPIDER_COBWEB_DESPAWN_TICK = 150;
    public static final int ZOMBIE_BLOCK_DESPAWN_TICK = 300;
    public static final int BLOCK_BROKEN_RESPAWN_TICK = 300;

    public void onSererTick(MinecraftServer server) {

        //Despawn the cobwebs placed by Spider mobs
        for (BlockPos key : dataHandler.blocksPlacedByMobWillRemove.keySet()) {
            int timeLeft = dataHandler.blocksPlacedByMobWillRemove.get(key) - 1;
            dataHandler.blocksPlacedByMobWillRemove.replace(key, timeLeft);

            if (timeLeft > 0) continue;

            ServerWorld world = server.getOverworld();
            BlockState state = world.getBlockState(key);

            if (state != null && !state.isAir()) {
                if (state.getBlock() == Blocks.COBWEB || state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {

                    Utils.spawnBlockBreakParticles(server.getOverworld(), key, new BlockStateParticleEffect(ParticleTypes.BLOCK, state.getBlock().getDefaultState()));
                    Utils.playSound(server.getOverworld(), key, state.getSoundGroup().getBreakSound());

                    world.setBlockState(key, Blocks.AIR.getDefaultState());
                }
            }

            dataHandler.blocksPlacedByMobWillRemove.remove(key);
        }
    }
}
