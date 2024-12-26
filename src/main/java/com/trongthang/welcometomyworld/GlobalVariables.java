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
}
