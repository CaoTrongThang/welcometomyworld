package com.trongthang.welcometomyworld.fluids;

import com.trongthang.welcometomyworld.managers.FluidsManager;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.fluid.FlowableFluid;

public abstract class PurpleWaterFluid extends FlowableFluid {
    @Override
    public boolean canBeReplacedWith(FluidState state, BlockView world, net.minecraft.util.math.BlockPos pos,
            Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.isIn(net.minecraft.registry.tag.FluidTags.WATER);
    }

    @Override
    public Fluid getFlowing() {
        return FluidsManager.FLOWING_DEATH_WATER;
    }

    @Override
    public Fluid getStill() {
        return FluidsManager.STILL_DEATH_WATER;
    }

    @Override
    public Item getBucketItem() {
        return FluidsManager.DEATH_WATER_BUCKET;
    }

    @Override
    protected boolean isInfinite(net.minecraft.world.World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(net.minecraft.world.WorldAccess world, net.minecraft.util.math.BlockPos pos,
            BlockState state) {
        final net.minecraft.block.entity.BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos)
                : null;
        net.minecraft.block.Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    public int getTickRate(WorldView world) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return FluidsManager.DEATH_WATER_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    @Override
    public boolean isStill(FluidState state) {
        return false;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    public static class Flowing extends PurpleWaterFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends PurpleWaterFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
