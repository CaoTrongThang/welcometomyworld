package com.trongthang.welcometomyworld.mixin.fixes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.adda.enhanced_falling_trees.utils.TreeBreakingUtils", remap = false)
public class TreeBreakingUtilsMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void makeCacheThreadSafe(CallbackInfo ci) {
        try {
            Class<?> targetClass = Class.forName("me.adda.enhanced_falling_trees.utils.TreeBreakingUtils");
            java.lang.reflect.Field field = targetClass.getDeclaredField("treeCache");
            field.setAccessible(true);

            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

            Object originalMap = field.get(null);
            if (originalMap instanceof it.unimi.dsi.fastutil.longs.Long2ObjectMap) {
                @SuppressWarnings("unchecked")
                it.unimi.dsi.fastutil.longs.Long2ObjectMap<Object> castedMap = (it.unimi.dsi.fastutil.longs.Long2ObjectMap<Object>) originalMap;
                Object syncedMap = it.unimi.dsi.fastutil.longs.Long2ObjectMaps.synchronize(castedMap);
                // Atomically update the value of the final field using Unsafe
                unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), syncedMap);
            }
        } catch (Throwable e) {
            System.err.println(
                    "[WelcomeToMyWorld] Failed to make EnhancedFallingTrees cache thread-safe: " + e.getMessage());
        }
    }
}
