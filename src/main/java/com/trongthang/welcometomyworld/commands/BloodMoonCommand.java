package com.trongthang.welcometomyworld.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.BLOOD_MOON_SYNC;

public class BloodMoonCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("bloodmoon")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerWorld world = source.getWorld();
                            WelcomeToMyWorld.dataHandler.worldData.isBloodMoon = true;
                            WelcomeToMyWorld.dataHandler.saveWorldData();
                            sendBloodMoonPacket(world, true);
                            source.sendFeedback(() -> Text.literal("§c[Welcome To My World] Blood Moon forced on!"),
                                    true);
                            return 1;
                        }))
                .then(CommandManager.literal("end")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerWorld world = source.getWorld();
                            WelcomeToMyWorld.dataHandler.worldData.isBloodMoon = false;
                            WelcomeToMyWorld.dataHandler.saveWorldData();
                            sendBloodMoonPacket(world, false);
                            source.sendFeedback(() -> Text.literal("§a[Welcome To My World] Blood Moon forced off!"),
                                    true);
                            return 1;
                        }));
    }

    private static void sendBloodMoonPacket(ServerWorld world, boolean active) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(active);
        buf.writeBoolean(false);
        for (ServerPlayerEntity p : world.getPlayers()) {
            ServerPlayNetworking.send(p, BLOOD_MOON_SYNC, buf);
        }
    }
}
