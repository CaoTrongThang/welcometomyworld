package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

@Mixin(SpreadableBlock.class)
public class SpeardableBlockMixin {

    private int cooldown = 15;
    private int counter = 0;
    private double chanceToGrowToTallGrass = 0.35; // This controls how far the grass can spread
    private double chanceToGrowGrass = 0.5;


    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void enhanceGrassSpread(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {

        if (world.isRaining()) {
            counter++;
            if(counter < cooldown) return;
            counter = 0;

            if(world.isSkyVisible(pos.up())){
                BlockPos spreadPos = pos.up();

                if(random.nextDouble() > chanceToGrowGrass) return;

                if(!world.isAir(spreadPos)){
                    if(world.getBlockState(spreadPos).isOf(Blocks.GRASS)){
                        if(random.nextDouble() < chanceToGrowToTallGrass){

                            world.setBlockState(spreadPos, Blocks.TALL_GRASS.getDefaultState().with(TallPlantBlock.HALF, DoubleBlockHalf.LOWER), 3); // Set the lower part of the tall grass
                            world.setBlockState(spreadPos.up(), Blocks.TALL_GRASS.getDefaultState().with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), 3); // Set the upper part of the tall grass

                        }
                    }
                    return;
                } ;

                if (world.getBlockState(pos).isOf(Blocks.DIRT) || world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
                    if (canSpread(state, world, spreadPos)) {
                        // Spread grass on the block (only on the top face)
                        world.setBlockState(spreadPos, Blocks.GRASS.getDefaultState());
                    }
                }
            }
        }
    }

    // Recreate the canSpread method from the original SpreadableBlock for rain logic
    private static boolean canSpread(BlockState state, World world, BlockPos pos) {
        BlockPos blockPos = pos.up(); // Check the block directly above
        return canSurvive(state, world, pos) && !world.getFluidState(blockPos).isIn(net.minecraft.registry.tag.FluidTags.WATER);
    }

    // Recreate the canSurvive method from the original SpreadableBlock
    private static boolean canSurvive(BlockState state, World world, BlockPos pos) {
        BlockPos blockPos = pos.up(); // Look at the block above

        // Make sure the block has enough light for grass to grow
        int lightLevel = world.getLightLevel(blockPos);
        return lightLevel >= 9;  // Grass needs light level >= 9 to spread
    }
}
