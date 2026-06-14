package com.trongthang.welcometomyworld.client.commands;

import com.trongthang.welcometomyworld.mixin.accessor.SoundManagerAccessor;
import com.trongthang.welcometomyworld.mixin.accessor.SoundSystemAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.text.Text;

import java.util.Map;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class ModClientCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("wtmw-client")
                    .then(ClientCommandManager.literal("logsounds")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client == null)
                                    return 0;

                                SoundSystem soundSystem = ((SoundManagerAccessor) client.getSoundManager())
                                        .getSoundSystem();
                                Map<SoundInstance, ?> sources = ((SoundSystemAccessor) soundSystem).getSources();

                                if (sources.isEmpty()) {
                                    context.getSource()
                                            .sendFeedback(Text.literal("§e[WTMW] No sounds currently playing."));
                                    return 1;
                                }

                                LOGGER.info("--- CURRENTLY PLAYING SOUNDS (" + sources.size() + ") ---");
                                for (SoundInstance sound : sources.keySet()) {
                                    LOGGER.info(String.format(
                                            "ID: %s | Category: %s | Volume: %.2f | Pitch: %.2f | Repeat: %b",
                                            sound.getId(),
                                            sound.getCategory(),
                                            sound.getVolume(),
                                            sound.getPitch(),
                                            sound.isRepeatable()));
                                }
                                LOGGER.info("------------------------------------------");

                                context.getSource().sendFeedback(Text
                                        .literal("§a[WTMW] Logged " + sources.size() + " active sounds to console."));
                                return 1;
                            })));
        });
    }
}
