package com.trongthang.welcometomyworld.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class DebugMusicCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("debugmusic")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("--- SOUNDS EXPLICITLY CATEGORIZED AS HOSTILE OR CONTAINING 'MUSIC' ---\n");

                    try {
                        ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
                        for (String namespace : manager.getAllNamespaces()) {
                            if (namespace.equals("minecraft"))
                                continue;
                            Identifier soundsId = new Identifier(namespace, "sounds.json");
                            try {
                                List<Resource> resources = manager.getAllResources(soundsId);
                                for (Resource resource : resources) {
                                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
                                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                                        for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                                            if (entry.getValue().isJsonObject()) {
                                                JsonObject soundObj = entry.getValue().getAsJsonObject();
                                                String key = entry.getKey();
                                                String category = soundObj.has("category")
                                                        ? soundObj.get("category").getAsString()
                                                        : "master";

                                                if (category.equalsIgnoreCase("hostile")) {
                                                    sb.append("Mod/Namespace: ").append(namespace)
                                                            .append(" | Event: ").append(namespace).append(":")
                                                            .append(key)
                                                            .append(" | Category: ").append(category)
                                                            .append("\n");
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        // Ignore parse errors for a specific sounds.json
                                    }
                                }
                            } catch (Exception e) {
                                // sounds.json not found for this namespace
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sb.append("Error reading resource manager: ").append(e.getMessage());
                    }

                    try {
                        File file = new File("debug_hostile_sounds.txt");
                        FileWriter writer = new FileWriter(file);
                        writer.write(sb.toString());
                        writer.close();
                        context.getSource().sendFeedback(
                                () -> Text.literal("§aSounds dumped to " + file.getAbsolutePath()), false);
                        LOGGER.info("Sounds dumped to " + file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        context.getSource().sendFeedback(() -> Text.literal("§cFailed to dump sounds! Check console."),
                                false);
                    }

                    return 1;
                });
    }
}
