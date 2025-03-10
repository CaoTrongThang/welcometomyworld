package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.IServerPlayerEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class MinecellsDimensionSarcastic {
    public static final List<String> minecellsNightTimeMessages = List.of(
            "The night here hungers for intruders...",
            "This realm's shadows devour the unwary after dark.",
            "The veil between worlds grows thin at night - you don't belong here.",

            "Can you feel the air itself rejecting you? Night has fallen.",
            "The stones whisper warnings - this place is not yours to walk tonight.",
            "Moonlight here cuts like knives. Flee before it finds you.",

            "What walks these paths after dark should not be witnessed.",
            "The guardians stir. Your presence disturbs their nocturnal vigil.",
            "Nightbreath coats the walls - your time here is ended.",

            "The gateways seal at sundown. Your exit is now.",
            "Return when the twin moons wane. You cannot stay tonight.",

            // Poetic Evictions
            "Stars here sing a song of banishment. Heed their chorus.",
            "The clockwork night grinds forward - your gears don't mesh here.",

            // Ominous Threats
            "They smell your daylight warmth. Go, before they wake fully.",
            "Every shadow just blinked. You shouldn't stay to see why.",

            // Mysterious
            "Can't you hear the locks turning? The night claims its territory.",
            "Your shadow grows teeth. Time to leave."
    );

    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if(world.isClient) return ActionResult.PASS;
            if(world.getRegistryKey() != World.OVERWORLD) return ActionResult.PASS;

            if (Registries.BLOCK.getId(world.getBlockState(hitResult.getBlockPos()).getBlock()).getNamespace().toLowerCase().contains("waystone")) {
                IServerPlayerEntity p = (IServerPlayerEntity) (ServerPlayerEntity) player;

                p.setLastDimensionMinecells(player.getWorld().getRegistryKey().toString());
                p.setLastPosMinecells(player.getPos());
            }

            return ActionResult.PASS;
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (isRestrictedDimension(destination)) {
                IServerPlayerEntity p = (IServerPlayerEntity) player;

                String lastDim = p.getLastDimensionMinecells();
                Vec3d lasPos = p.getLastPosMinecells();

                if (lastDim == null || lasPos == null) return;

                TeleportToPreviousWorld(player, lastDim, lasPos);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 5));
            }
        });
    }

    public static void kickIfPlayerInMinecell(MinecraftServer server) {
        if (server.getOverworld().isClient) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isRestrictedDimension(player.getWorld())) {
                IServerPlayerEntity p = (IServerPlayerEntity) player;

                String lastDim = p.getLastDimensionMinecells();
                Vec3d lasPos = p.getLastPosMinecells();

                if (lastDim == null || lasPos == null) return;

                player.sendMessage(Text.literal(minecellsNightTimeMessages.get(random.nextInt(minecellsNightTimeMessages.size()))).formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
                TeleportToPreviousWorld(player, lastDim, lasPos);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 5));
            }
        }
    }

    public static void TeleportToPreviousWorld(ServerPlayerEntity player, String lastDimension, Vec3d lastPos) {
        if (isRestrictedDimension(player.getWorld()) && player.getServer().getOverworld().isNight()) {
            lastDimension = extractDimensionId(lastDimension);
            Identifier lastDim = new Identifier(lastDimension);


            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, lastDim);
            ServerWorld targetWorld = player.getServer().getWorld(worldKey);

            if (targetWorld != null) {
                player.getServer().execute(() -> {
                    player.teleport(
                            targetWorld,
                            lastPos.x,
                            lastPos.y + 6,
                            lastPos.z,
                            EnumSet.noneOf(PositionFlag.class),
                            player.getYaw(),
                            player.getPitch()
                    );
                });

            }
        }
    }

    public static String extractDimensionId(String rawKey) {
        // Convert "ResourceKey[minecraft:dimension / minecraft:overworld]" → "minecraft:overworld"
        if (rawKey.contains("ResourceKey[")) {
            int start = rawKey.lastIndexOf("/") + 1;
            int end = rawKey.lastIndexOf("]");
            return rawKey.substring(start, end).trim();
        }
        return rawKey;
    }

    public static boolean isRestrictedDimension(World world) {
        String s = world.getRegistryKey().getValue().toString();
        return s.equals("minecells:black_bridge") ||
                s.equals("minecells:insufferable_crypt") ||
                s.equals("minecells:prison") ||
                s.equals("minecells:promenade") ||
                s.equals("minecells:ramparts");
    }
}
