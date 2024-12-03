package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class FallingToWaterDamage {
    // Now falling to water still applies damages
    public void handleFallingToWaterDamage() {
        ServerPlayNetworking.registerGlobalReceiver(FALLING_TO_WATER, (server, p, handler, buf, responseSender) -> {

            float damageAmount = buf.readFloat();
            UUID playerUuid = buf.readUuid();

            server.execute(() -> {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);

                if (player != null) {
                    if((player.getHealth() - damageAmount) <= 0){
                        Utils.grantAdvancement(player,"first_falling_to_water_die");
                    }
                    player.damage(player.getWorld().getDamageSources().fall(), damageAmount);
                }
            });
        });
    }
}
