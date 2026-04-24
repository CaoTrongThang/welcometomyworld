package com.trongthang.welcometomyworld.features;

import java.text.NumberFormat;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class ItemPurgeHandler {
    // 10 seconds for testing (200 ticks)
    // 30 minutes for production (36000 ticks)
    private static final int PURGE_INTERVAL = 24000;

    private int tickCounter = PURGE_INTERVAL;

    public void onServerTick(MinecraftServer server) {
        if (tickCounter == 1200) {
            Utils.UTILS.sendMessageToAllPlayers(server,
                    "There're so many trashes across dimensions, I'll crash a powerful spell to purge it in 1 minute");
        }

        tickCounter--;

        if (tickCounter <= 0) {
            purgeItems(server);
            Utils.UTILS.sendMessageToAllPlayers(server, "Purge!!!");
            tickCounter = PURGE_INTERVAL;
        }
    }

    public void onPlayerJoin(net.minecraft.server.network.ServerPlayerEntity player) {
        if (tickCounter > 0 && tickCounter <= 1200) {
            Utils.UTILS.sendTextAfter(player,
                    "There're so many trashes across dimensions, I'll crash a powerful spell to purge it in "
                            + NumberFormat.getInstance().format(tickCounter / 20) + " seconds");
        }
    }

    private void purgeItems(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (ItemEntity item : world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, entity -> true)) {
                item.discard();
            }
        }
    }
}
