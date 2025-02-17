package com.trongthang.welcometomyworld.blocks;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.blockentities.BurningPlankBlockEntity;
import com.trongthang.welcometomyworld.managers.BlocksManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BurningPlankBlock extends BlockWithEntity implements BlockEntityProvider  {

    public static final IntProperty COUNTER = IntProperty.of("counter", 0, 240); // Range 0 to 240
    private static final int TURN_INTO_BURNING_PLANK_AFTER_TIME = 1800;

    public BurningPlankBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(COUNTER, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COUNTER);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.bypassesSteppingEffects() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity) && !entity.isOnFire()) {
            entity.damage(world.getDamageSources().generic(), 5.0f);
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    // CLIENT SIDE
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {

    }

    // SERVER SIDE
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        BurningPlankBlockEntity blockEntity = (BurningPlankBlockEntity) world.getBlockEntity(pos);

        if (world.isRaining() && world.isSkyVisible(pos)) {
            world.setBlockState(pos, BlocksManager.BURNED_PLANK.getDefaultState());
            Utils.playSound(world, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH);
        }

        if (blockEntity != null) {
            if(world.getBlockState(pos.down()).isOf(Blocks.FIRE)) return;

            blockEntity.incrementCounter(random.nextInt(20));
            if (blockEntity.getCounter() >= TURN_INTO_BURNING_PLANK_AFTER_TIME) {
                world.setBlockState(pos, BlocksManager.BURNED_PLANK.getDefaultState());
                Utils.playSound(world, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH);
            }
            blockEntity.incrementCounter(20);
        }



        world.scheduleBlockTick(pos, state.getBlock(), 20);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
        return new BurningPlankBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        world.scheduleBlockTick(pos, state.getBlock(), 20);

    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        world.scheduleBlockTick(pos, state.getBlock(), 20);

    }
}
