package com.trongthang.welcometomyworld.blocks;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.blockentities.JustACounterBlockEntity;
import com.trongthang.welcometomyworld.managers.BlocksManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.trongthang.welcometomyworld.GlobalVariables.RAIN_SPEED_UP_RUSTY_TIME;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class CustomIronBars extends PaneBlock implements BlockEntityProvider {

    public int delayTime = 100;
    public static final int TIME_TO_GO_NEXT_STAGE = 120000;

    public CustomIronBars(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Ensure we are on the server side
        if (world instanceof ServerWorld serverWorld) {
            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.getItem() instanceof AxeItem) {
                // Play the sound directly on the server
                Utils.playSound((ServerWorld) world, pos, SoundEvents.BLOCK_COPPER_HIT);

                // Damage the item


                // Change the block state
                if (state.isOf(BlocksManager.RUSTED_IRON_BARS)) {
                    world.setBlockState(pos, BlocksManager.TOUGHER_IRON_BARS.getDefaultState());
                    heldItem.damage(30, player, (p) -> p.sendToolBreakStatus(hand));
                }

                // Spawn particles
                Utils.spawnBlockBreakParticles(serverWorld, pos, new BlockStateParticleEffect(ParticleTypes.BLOCK, state));

                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {


        JustACounterBlockEntity blockEntity = (JustACounterBlockEntity) world.getBlockEntity(pos);

        if (blockEntity != null) {

            blockEntity.incrementCounter(world.isRaining() ? delayTime + RAIN_SPEED_UP_RUSTY_TIME : delayTime);

            if (blockEntity.getCounter() >= TIME_TO_GO_NEXT_STAGE) {

                if (state.isOf(BlocksManager.TOUGHER_IRON_BARS)) {
                    world.setBlockState(pos, BlocksManager.RUSTED_IRON_BARS.getDefaultState());
                }

            }
        }

        if (state.isOf(BlocksManager.TOUGHER_IRON_BARS)) {
            world.scheduleBlockTick(pos, state.getBlock(), delayTime);
        }
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
