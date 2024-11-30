package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class SwitchPerspectiveFirstJoin {
    public void swichPerspective(ServerPlayerEntity player){
        PacketByteBuf perspectiveBuf = PacketByteBufs.create();
        perspectiveBuf.writeInt(0); // 1 represents third-person perspective
        ServerPlayNetworking.send(player, CHANGE_PERSPECTIVE, perspectiveBuf);

        // Send a packet to play sound for the player only
//        player.sendMessage(
//                Text.literal("<Unknown> If you want to know more about the")
//                        .append(Text.literal(" Easycraft's World").styled(style -> style.withBold(false).withColor(Formatting.WHITE)))
//                        .append(Text.literal(" then read the ").styled(style -> style.withBold(false).withColor(Formatting.WHITE)))
//                        .append(Text.literal("secret wiki.")
//                                .styled(style -> style.withUnderline(true).withColor(Formatting.RED)
//                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/modpack/easycraft"))))
//        );
    }
}
