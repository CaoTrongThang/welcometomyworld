package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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

                    if (isUmbrella(player)) {
                        return;
                    }

                    if ((player.getHealth() - damageAmount) <= 0) {
                        Utils.grantAdvancement(player, "first_falling_to_water_die");
                    }
                    player.damage(player.getWorld().getDamageSources().fall(), damageAmount);
                }
            });
        });
    }

    public static boolean isUmbrella(ServerPlayerEntity player) {
        // Get the "artifacts:umbrella" item
        Item umbrellaItem = Registries.ITEM.get(new Identifier("artifacts", "umbrella"));
        if (umbrellaItem == null) {
            System.out.println("Umbrella item not found in registry!");
            return false;
        }

        // Check main hand
        ItemStack mainHandItem = player.getMainHandStack();
        if (mainHandItem.getItem() == umbrellaItem) {
            return true;
        }

        // Check offhand
        ItemStack offHandItem = player.getOffHandStack();
        if (offHandItem.getItem() == umbrellaItem) {
            return true;
        }

        return false; // Not holding in either hand
    }
}
