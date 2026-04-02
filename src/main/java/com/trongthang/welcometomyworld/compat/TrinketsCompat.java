package com.trongthang.welcometomyworld.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

public class TrinketsCompat {

    /**
     * Checks if the player has the Creeper Talisman equipped in a trinket slot.
     * This method is only called if Trinkets is loaded.
     */
    public static boolean isCreeperTalismanEquipped(PlayerEntity player) {
        if (!CompatManager.isTrinketsLoaded())
            return false;

        return dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player)
                .map(component -> component
                        .isEquipped(com.trongthang.welcometomyworld.managers.ItemsManager.CREEPER_TALISMAN))
                .orElse(false);
    }

    /**
     * Registers an item as a Trinket if the mod is present.
     * This allows us to use standard Items instead of extending TrinketItem.
     */
    public static void registerTrinket(Item item) {
        if (CompatManager.isTrinketsLoaded()) {
            dev.emi.trinkets.api.TrinketsApi.registerTrinket(item, new dev.emi.trinkets.api.Trinket() {
            });
        }
    }
}
