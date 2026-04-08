package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.effects.VoidSightEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class EffectsManager {

    public static final StatusEffect VOID_SIGHT = new VoidSightEffect(StatusEffectCategory.BENEFICIAL, 0x000000); // 0x000000
                                                                                                                  // can
                                                                                                                  // be
                                                                                                                  // black
                                                                                                                  // or
                                                                                                                  // 0xFFFFFF
                                                                                                                  // for
                                                                                                                  // white

    public static void initialize() {
        registerEffect("void_sight", VOID_SIGHT);
        WelcomeToMyWorld.LOGGER.info("Registering Custom Effects for " + WelcomeToMyWorld.MOD_ID);
    }

    private static void registerEffect(String name, StatusEffect effect) {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(WelcomeToMyWorld.MOD_ID, name), effect);
    }
}
