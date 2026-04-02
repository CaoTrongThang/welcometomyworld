package com.trongthang.welcometomyworld.compat;

import net.fabricmc.loader.api.FabricLoader;

public class CompatManager {
    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    public static final boolean IMPROVED_MOBS_LOADED = FabricLoader.getInstance().isModLoaded("improvedmobs");
    public static final boolean CARRY_ON_LOADED = FabricLoader.getInstance().isModLoaded("carryon");

    public static boolean isTrinketsLoaded() {
        return TRINKETS_LOADED;
    }

    public static boolean isImprovedMobsLoaded() {
        return IMPROVED_MOBS_LOADED;
    }

    public static boolean isCarryOnLoaded() {
        return CARRY_ON_LOADED;
    }

    private static java.lang.reflect.Method getCarryDataMethod = null;
    private static java.lang.reflect.Method isCarryingMethod = null;
    private static boolean carryOnReflectionInit = false;

    public static boolean isCarrying(net.minecraft.client.network.AbstractClientPlayerEntity player) {
        if (!CARRY_ON_LOADED || player == null)
            return false;

        try {
            if (!carryOnReflectionInit) {
                Class<?> managerClass = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager");
                // Using PlayerEntity.class as the parameter type
                getCarryDataMethod = managerClass.getMethod("getCarryData",
                        net.minecraft.entity.player.PlayerEntity.class);

                Class<?> dataClass = Class.forName("tschipp.carryon.common.carry.CarryOnData");
                isCarryingMethod = dataClass.getMethod("isCarrying");

                carryOnReflectionInit = true;
            }

            if (getCarryDataMethod != null && isCarryingMethod != null) {
                Object carryData = getCarryDataMethod.invoke(null, player);
                if (carryData != null) {
                    return (boolean) isCarryingMethod.invoke(carryData);
                }
            }
        } catch (Exception e) {
            // Log only once if there's an error to avoid spam
            // No logger currently in CompatManager, so we just set init to true to stop
            // trying
            carryOnReflectionInit = true;
        }

        return false;
    }
}
