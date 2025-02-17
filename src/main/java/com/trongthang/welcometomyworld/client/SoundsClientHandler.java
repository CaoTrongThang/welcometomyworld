package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class SoundsClientHandler {
    public static void register(){
        // Handle networking for sounds or other events, this PLAY_BLOCK_PORTAL_TRAVEL only happens once when player first time join the world
        ClientPlayNetworking.registerGlobalReceiver(PLAY_BLOCK_PORTAL_TRAVEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                assert client.player != null;
                client.player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.4f, 1f);

            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_WOLF_HOWL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                assert client.player != null;
                client.player.playSound(SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.BLOCKS, 0.3f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_ANVIL_USE, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                assert client.player != null;
                client.player.playSound(SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.3f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_BELL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                assert client.player != null;
                client.player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 0.4f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_EXPERIENCE_ORB_PICK_UP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_ENTITY_PLAYER_LEVELUP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.4f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_BLOCK_LEVER_CLICK, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.6f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TIRED_SOUND, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundsManager.TIRED_SOUND, SoundCategory.PLAYERS, 1f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_EATING_SOUND, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.player.playSound(SoundsManager.ENDERCHESTER_MUNCH, SoundCategory.AMBIENT, 0.12f, random.nextFloat(0.8f, 1.2f));
            });
        });
    }
}
