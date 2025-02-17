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
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.trongthang.welcometomyworld.GlobalVariables.RAIN_SPEED_UP_RUSTY_TIME;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class CustomIronBlock extends BlockWithEntity implements BlockEntityProvider {


    public int delayTime = 100;
    public static final int TIME_TO_SPAWN_A_VINE_AROUND_THE_BLOCK = 69000;
    public static final int TIME_TO_GO_NEXT_STAGE = 72000;


    public CustomIronBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Ensure we are on the server side
        if (world instanceof ServerWorld serverWorld) {
            JustACounterBlockEntity blockEntity = (JustACounterBlockEntity) world.getBlockEntity(pos);

            if (blockEntity != null) {
                ItemStack heldItem = player.getStackInHand(hand);
                if (heldItem.getItem() instanceof AxeItem) {
                    // Play the sound directly on the server
                    Utils.playSound((ServerWorld) world, pos, SoundEvents.BLOCK_COPPER_HIT);

                    // Damage the item


                    // Change the block state
                    if(state.isOf(BlocksManager.RUSTED_IRON_BLOCK)){
                        world.setBlockState(pos, BlocksManager.TOUGHER_IRON_BLOCK.getDefaultState());
                        heldItem.damage(50, player, (p) -> p.sendToolBreakStatus(hand));
                    } else if(state.isOf(BlocksManager.RUSTED_IRON_BLOCK_STAGE2)){
                        world.setBlockState(pos, BlocksManager.RUSTED_IRON_BLOCK.getDefaultState());
                        heldItem.damage(50, player, (p) -> p.sendToolBreakStatus(hand));
                    } else if(state.isOf(BlocksManager.RUSTED_IRON_BLOCK_STAGE3)){
                        world.setBlockState(pos, BlocksManager.RUSTED_IRON_BLOCK_STAGE2.getDefaultState());
                        heldItem.damage(50, player, (p) -> p.sendToolBreakStatus(hand));
                    }

                    // Spawn particles
                    Utils.spawnBlockBreakParticles(serverWorld, pos, new BlockStateParticleEffect(ParticleTypes.BLOCK, state));

                    return ActionResult.SUCCESS;

                }
            }
        }
        return ActionResult.FAIL;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {


            JustACounterBlockEntity blockEntity = (JustACounterBlockEntity) world.getBlockEntity(pos);

            if (blockEntity != null) {
                blockEntity.incrementCounter(world.isRaining() ? delayTime + RAIN_SPEED_UP_RUSTY_TIME : delayTime);
                if (blockEntity.getCounter() >= TIME_TO_SPAWN_A_VINE_AROUND_THE_BLOCK) {
                    BlockState vineState = BlocksManager.CUSTOM_VINE.getDefaultState();
                    spawnVine(pos, vineState, world);

                }

                if(blockEntity.getCounter() >= TIME_TO_GO_NEXT_STAGE){
                    if(state.isOf(BlocksManager.TOUGHER_IRON_BLOCK)){
                        world.setBlockState(pos, BlocksManager.RUSTED_IRON_BLOCK.getDefaultState());
                    } else if(state.isOf(BlocksManager.RUSTED_IRON_BLOCK)){
                        world.setBlockState(pos, BlocksManager.RUSTED_IRON_BLOCK_STAGE2.getDefaultState());
                    } else if(state.isOf(BlocksManager.RUSTED_IRON_BLOCK_STAGE2)){
                        world.setBlockState(pos, BlocksManager.RUSTED_IRON_BLOCK_STAGE3.getDefaultState());
                    }
                }
            }


        if(!state.isOf(BlocksManager.RUSTED_IRON_BLOCK_STAGE3)) {
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


    public static void spawnVine(BlockPos pos, BlockState vineState, ServerWorld world) {

        BlockPos airPos = null;

        List<BlockPos> p = List.of(
                pos.east(),
                pos.south(),
                pos.north(),
                pos.west()
        );

        List<BlockPos> airPoses = new java.util.ArrayList<>(List.of());

        for (BlockPos po : p) {
            if (world.isAir(po)) {
                airPoses.add(po);
            }
        }

        airPos = !airPoses.isEmpty() ? airPoses.get(random.nextInt(airPoses.size())) : null;


        if (airPos != null) {
            // Check relative positions of the air block to determine the facing direction
            if (airPos.equals(pos.east())) {
                vineState = vineState.with(Properties.WEST, true); // Faces toward the block on the west
            } else if (airPos.equals(pos.west())) {
                vineState = vineState.with(Properties.EAST, true); // Faces toward the block on the east
            } else if (airPos.equals(pos.south())) {
                vineState = vineState.with(Properties.NORTH, true); // Faces toward the block on the north
            } else if (airPos.equals(pos.north())) {
                vineState = vineState.with(Properties.SOUTH, true); // Faces toward the block on the south
            }

            // Place the vine at the determined position with the correct facing
            world.setBlockState(airPos, vineState);

            Utils.playSound(world, pos, SoundEvents.BLOCK_GRASS_PLACE); // Optional: play a sound for feedback
        }
    }


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JustACounterBlockEntity(pos, state);
    }
}
