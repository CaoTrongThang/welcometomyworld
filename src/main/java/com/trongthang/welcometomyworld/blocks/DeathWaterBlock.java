package com.trongthang.welcometomyworld.blocks;

import com.trongthang.welcometomyworld.managers.EffectsManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeathWaterBlock extends FluidBlock {

    // 30 seconds in ticks
    private static final int EFFECT_DURATION = 600;

    public DeathWaterBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof PlayerEntity player) {
            // Wither II (amplifier 1 = level 2)
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WITHER, EFFECT_DURATION, 1, false, true, true));
            // Night Vision 255 (amplifier 254 = level 255)
            player.addStatusEffect(new StatusEffectInstance(
                    EffectsManager.VOID_SIGHT, EFFECT_DURATION, 1, false, false, true));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NAUSEA, EFFECT_DURATION, 1, false, false, true));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.MINING_FATIGUE, EFFECT_DURATION, 1, false, false, true));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS, EFFECT_DURATION, 1, false, false, true));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS, EFFECT_DURATION, 1, false, false, true));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HUNGER, EFFECT_DURATION, 1, false, false, true));
        }
    }
}
