package com.trongthang.welcometomyworld.compat;

import net.fabricmc.loader.api.FabricLoader;

public class CompatManager {
    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    public static final boolean IMPROVED_MOBS_LOADED = FabricLoader.getInstance().isModLoaded("improvedmobs");

    public static boolean isTrinketsLoaded() {
        return TRINKETS_LOADED;
    }

    public static boolean isImprovedMobsLoaded() {
        return IMPROVED_MOBS_LOADED;
    }
}
