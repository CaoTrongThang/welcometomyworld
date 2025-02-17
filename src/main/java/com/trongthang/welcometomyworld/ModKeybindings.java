package com.trongthang.welcometomyworld;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {
    public static KeyBinding openMobStats;

    public static void registerKeybindings() {
        openMobStats = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.welcometomyworld.open_mob_stats", // Translation key
                InputUtil.Type.KEYSYM, // Keyboard input
                GLFW.GLFW_KEY_M, // Default key (M)
                "category.welcometomyworld.keys" // Category in controls menu
        ));
    }
}
