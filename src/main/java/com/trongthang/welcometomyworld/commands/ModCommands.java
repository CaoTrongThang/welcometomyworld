package com.trongthang.welcometomyworld.commands;

import com.trongthang.welcometomyworld.ConfigLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("welcometomyworld")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.literal("reloadconfig")
                            .executes(context -> {
                                ConfigLoader.loadConfig();
                                com.trongthang.welcometomyworld.features.MobsGearsUp.reload();
                                context.getSource()
                                        .sendFeedback(
                                                () -> Text.literal(
                                                        "§a[Welcome To My World] Configuration reloaded successfully!"),
                                                false);
                                return 1;
                            }))
                    .then(BloodMoonCommand.build()));
        });
    }
}
