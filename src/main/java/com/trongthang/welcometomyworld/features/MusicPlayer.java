package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayer {
    public static final List<String> MUSIC_IDS = new ArrayList<>();

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Get the sound event registry
            Registry<SoundEvent> soundRegistry = server.getRegistryManager().get(RegistryKeys.SOUND_EVENT);

            // Get the "music" tag (e.g., #minecraft:music)
            TagKey<SoundEvent> musicTag = TagKey.of(RegistryKeys.SOUND_EVENT, new Identifier("minecraft", "music"));

            // Add all music track IDs to the list
            soundRegistry.getEntryList(musicTag).ifPresent(entries -> {
                for (RegistryEntry<SoundEvent> entry : entries) {
                    // Extract the ID from the registry entry
                    Identifier id = entry.getKey().get().getValue();
                    MUSIC_IDS.add(id.toString()); // Add as string (e.g., "minecraft:music_disc.stal")
                }
            });

            // Log all collected IDs
            MUSIC_IDS.forEach(id -> WelcomeToMyWorld.LOGGER.info("Found Music: " + id));
        });
    }
}