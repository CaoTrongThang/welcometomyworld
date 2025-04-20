package com.trongthang.welcometomyworld;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;

public class GlobalVariables {
    public static final List<StatusEffect> POSSIBLE_EFFECTS_FOR_MOBS = List.of(
            StatusEffects.SPEED,
            StatusEffects.STRENGTH,
            StatusEffects.HASTE,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE
    );

    public static final int RAIN_SPEED_UP_RUSTY_TIME = 240;

    public static final float DEFAULT_XP_TAMEABLE_MOB = 50f;
    public static final float EXP_MULTIPLIER_EACH_LEVEL_MOB = 1.15f;

    public static final float MIN_HEALTH_MOB = 6f;
    public static final float MAX_HEALTH_MOB = 120f;
    public static final float HEALTH_ADD_PER_LEVEL_PERCENT = 2f;

    public static final float MIN_DAMAGE_MOB = 0.5f;
    public static final float MAX_DAMAGE_MOB = 2;
    public static final float DAMAGE_ADD_PER_LEVEL_MOB_PERCENT = 2f;

    public static final float MIN_ARMOR_MOB = 0.1f;
    public static final float MAX_ARMOR_MOB = 1f;
    public static final float ARMOR_ADD_PER_LEVEL_MOB_PERCENT = 1f;

    public static final float SPEED_ADD_PER_LEVEL = 0.01f;

    public static final int MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS = 60;
    public static final int MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS = 20;
}
