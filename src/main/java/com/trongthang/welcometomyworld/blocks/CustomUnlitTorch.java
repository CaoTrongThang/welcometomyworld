package com.trongthang.welcometomyworld.blocks;

import com.trongthang.welcometomyworld.blockentities.JustACounterBlockEntity;
import com.trongthang.welcometomyworld.managers.BlocksManager;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import static com.trongthang.welcometomyworld.GlobalVariables.RAIN_SPEED_UP_RUSTY_TIME;

public class CustomUnlitTorch extends TorchBlock implements BlockEntityProvider {

    public int delayTime = 100;
    public static final int TIME_TO_GO_NEXT_STAGE = 120000;

    public CustomUnlitTorch(Settings settings) {
        super(settings, null);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Ensure we are on the server side
        LOGGER.info("USING TORCH");
        if (world instanceof ServerWorld serverWorld) {

        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        JustACounterBlockEntity blockEntity = (JustACounterBlockEntity) world.getBlockEntity(pos);

        if (blockEntity != null) {
        }


        world.scheduleBlockTick(pos, state.getBlock(), delayTime);

    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        world.scheduleBlockTick(pos, state.getBlock(), delayTime);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        world.scheduleBlockTick(pos, state.getBlock(), delayTime);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JustACounterBlockEntity(pos, state);
    }
}
