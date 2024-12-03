package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class SwitchPerspectiveFirstJoin {
    public static void swichPerspective(ServerPlayerEntity player){
        PacketByteBuf perspectiveBuf = PacketByteBufs.create();
        perspectiveBuf.writeInt(0); // 1 represents third-person perspective
        ServerPlayNetworking.send(player, CHANGE_PERSPECTIVE, perspectiveBuf);
    }
}
