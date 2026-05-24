package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.CustomTameableEntity;
import com.trongthang.welcometomyworld.classes.PetData;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;

public class PetTeleportManager {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 40; // Check every 1 second

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL)
            return;
        tickCounter = 0;

        Map<UUID, PetData> petDataMap = WelcomeToMyWorld.dataHandler.petDataMap;

        for (Map.Entry<UUID, PetData> entry : petDataMap.entrySet()) {
            PetData petData = entry.getValue();
            if (!petData.isFollowing)
                continue;

            ServerPlayerEntity owner = server.getPlayerManager().getPlayer(petData.ownerUUID);
            if (owner == null)
                continue;

            ServerWorld petWorld = server
                    .getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier(petData.dimension)));
            if (petWorld == null)
                continue;

            // Check if pet is loaded
            Entity petEntity = petWorld.getEntity(petData.petUUID);

            if (petEntity == null) {
                ChunkPos chunkPos = new ChunkPos(petData.lastKnownPos);
                petWorld.getChunkManager().addTicket(ChunkTicketType.PORTAL, chunkPos, 2, petData.lastKnownPos);
                continue;
            }

            if (petEntity instanceof CustomTameableEntity mob) {
                double distance = mob.distanceTo(owner);
                boolean differentDimension = !mob.getWorld().getRegistryKey().equals(owner.getWorld().getRegistryKey());

                if (!differentDimension && distance > 128) {
                    mob.refreshPositionAndAngles(owner.getX(), owner.getY(), owner.getZ(), mob.getYaw(),
                            mob.getPitch());

                    PetData data = petDataMap.get(mob.getUuid());
                    if (data != null) {
                        data.lastKnownPos = owner.getBlockPos();
                    }
                }
            }
        }
    }

    public static void updatePetPosition(CustomTameableEntity mob, boolean following) {
        if (mob.getWorld().isClient)
            return;
        if (!mob.isTamed() || mob.getOwnerUuid() == null)
            return;

        PetData data = WelcomeToMyWorld.dataHandler.petDataMap.get(mob.getUuid());
        if (data == null) {
            data = new PetData(
                    mob.getUuid(),
                    mob.getOwnerUuid(),
                    mob.getBlockPos(),
                    mob.getWorld().getRegistryKey().getValue().toString(),
                    following);
            WelcomeToMyWorld.dataHandler.petDataMap.put(mob.getUuid(), data);
        } else {
            data.lastKnownPos = mob.getBlockPos();
            data.dimension = mob.getWorld().getRegistryKey().getValue().toString();
            data.isFollowing = following;
        }
    }
}
