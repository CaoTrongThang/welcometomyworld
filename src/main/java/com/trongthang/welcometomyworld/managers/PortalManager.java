package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class PortalManager {
    public static void initialize() {
        // // Test portal: Diamond Block frame, lit by Diamond item, leads to The Nether
        // CustomPortalBuilder.beginPortal()
        // .frameBlock(Blocks.DIAMOND_BLOCK)
        // .lightWithItem(Items.DIAMOND)
        // .destDimID(new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim"))
        // .tintColor(45, 65, 101)
        // .registerPortal();
    }
}
