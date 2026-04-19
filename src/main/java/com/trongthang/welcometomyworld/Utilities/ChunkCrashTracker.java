package com.trongthang.welcometomyworld.Utilities;

import net.minecraft.util.math.ChunkPos;

public class ChunkCrashTracker {
    // ThreadLocal ensures chunk coordinates are isolated per-thread during parallel
    // chunk loading
    public static final ThreadLocal<ChunkPos> CURRENT_LOADING_CHUNK = new ThreadLocal<>();
}
