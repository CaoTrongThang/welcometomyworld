package com.trongthang.welcometomyworld.blocks;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import com.trongthang.welcometomyworld.managers.EffectsManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.FernBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GlowingPlantVoid extends FernBlock {
    private final int duration;
    private final StatusEffect[] effects;

    public GlowingPlantVoid(Settings settings, int duration, StatusEffect[] effects) {
        super(settings);
        this.duration = duration;
        this.effects = effects;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);

        if (!world.isClient && entity instanceof LivingEntity livingEntity) {
            for (StatusEffect effect : effects) {
                if (effect != null) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(effect, duration, 0));
                }
            }
        }
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOpaqueFullCube(world, pos);
    }
}
