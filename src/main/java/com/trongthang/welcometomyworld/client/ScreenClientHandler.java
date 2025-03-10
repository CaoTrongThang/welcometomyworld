package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.entities.Enderchester;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.client.ClientData.LAST_INTERACTED_MOB_ID;

public class ScreenClientHandler {
    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if ((screen.getTitle().equals(Text.translatable("container.enderchest")) || screen.getTitle().equals(Text.translatable("entity.welcometomyworld.chesterstomach"))) && LAST_INTERACTED_MOB_ID != -1) {
                ScreenEvents.remove(screen).register(removedScreen -> {

                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(LAST_INTERACTED_MOB_ID);
                    ClientPlayNetworking.send(A_LIVING_CHEST_MOUTH_CLOSE, buf);
                    LAST_INTERACTED_MOB_ID = -1;
                });
            }
        });
    }
}
