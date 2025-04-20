package com.trongthang.welcometomyworld.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import static net.minecraft.block.BarrelBlock.FACING;

public class TrophyBlock extends Block {
    // Smaller outline shape (adjust values as needed)
    private VoxelShape OUTLINE_SHAPE = null;

    public TrophyBlock(Settings settings, VoxelShape cuboid) {
        super(settings);
        this.OUTLINE_SHAPE = cuboid;
    }

    public TrophyBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if(OUTLINE_SHAPE == null) {
            return VoxelShapes.fullCube();
        }
        return OUTLINE_SHAPE; // Use the smaller shape for shadows/outlines
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING); // Register the FACING property
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Set direction based on player's horizontal facing
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
