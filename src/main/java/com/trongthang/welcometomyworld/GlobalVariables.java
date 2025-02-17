package com.trongthang.welcometomyworld;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVariables {
    public static final List<StatusEffect> POSSIBLE_EFFECTS_FOR_MOBS = List.of(
            StatusEffects.SPEED,
            StatusEffects.STRENGTH,
            StatusEffects.HASTE,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE
    );

    public static final int RAIN_SPEED_UP_RUSTY_TIME = 240;

    public static final float EXP_MULTIPLIER_EACH_LEVEL_MOB = 1.5f;
    public static final float DAMAGE_ADD_PER_LEVEL_MOB = 0.7f;
    public static final float ARMOR_ADD_PER_LEVEL_MOB = 0.6f;
    public static final float SPEED_ADD_PER_LEVEL = 0.01f;
    public static final float HEALTH_ADD_PER_LEVEL = 30f;

    public static final int MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS = 500;
    public static final int MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS = 20;
}
