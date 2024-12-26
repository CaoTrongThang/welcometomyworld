package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.classes.PlayerData;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class GiveStartingItemsHandler {
    public static void giveItemHandler(ServerPlayerEntity player, boolean clearItem) {
        PlayerData p = dataHandler.playerDataMap.get(player.getUuid());
        if (!p.firstRemoveStartingItems) {
            if (hasAnyItemInInventory(player)) {
                if(clearItem){
                    player.getInventory().clear();
                }
                p.firstRemoveStartingItems = true;
                giveItems(player);
            }
        }
    }

    private static void giveItems(ServerPlayerEntity player) {
        // Modded items - using Identifiers for mod items
        if (player == null) return;
        PlayerData p = dataHandler.playerDataMap.get(player.getUuid());

        if(p.firstGivingStartingItems) return;
        p.firstGivingStartingItems = true;

        MinecraftServer server = player.getServer();
        ItemStack sandwich = getModdedItems("croptopia:blt", 1); // Change to mod's item ID and quantity
        ItemStack purrifiedWater = new ItemStack(Items.POTION);
        purrifiedWater.getOrCreateNbt().putString("Potion", "minecraft:purified_water");
        ItemStack gamingConsole = getModdedItems("gamediscs:gaming_console", 1);
        ItemStack gameDisc = getModdedItems("gamediscs:game_disc_flappy_bird", 1);

        if (sandwich != null) {
            server.execute(() -> {
                player.getInventory().insertStack(sandwich);
            });
        }
        if (purrifiedWater != null) {
            server.execute(() -> {
                player.getInventory().insertStack(purrifiedWater);
            });
        }

        if (gamingConsole != null) {
            server.execute(() -> {
                player.getInventory().insertStack(9, gamingConsole);
            });
        }

        if (gameDisc != null) {
            server.execute(() -> {
                player.getInventory().insertStack(10, gameDisc);
            });

        }

        LOGGER.info("Gave starting items to player {}", player.getEntityName());
    }

    public void giveMoreItems(ServerPlayerEntity player) {
        if (player == null) return;

        ItemStack summonGolem = getModdedItems("advancedgolems:golem_spawner", 1);
        ItemStack golemController = getModdedItems("advancedgolems:golem_control", 1);

        if (summonGolem != null) {
            dropItemToPlayer(player, summonGolem);
        }

        if (golemController != null) {
            dropItemToPlayer(player, golemController);
        }

    }

    public static void dropItemToPlayer(ServerPlayerEntity player, ItemStack item) {
        player.getServer().execute(() -> {
            ServerPlayerEntity currentPlayer = player.getServer().getPlayerManager().getPlayer(player.getUuid());
            if (currentPlayer == null) return;

            ServerWorld serverWorld = player.getServerWorld();
            ItemEntity itemEntity = new ItemEntity(serverWorld, currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(), item);
            serverWorld.spawnEntity(itemEntity);
        });
    }

    public static ItemStack getModdedItems(String itemId, int count) {
        Identifier identifier = new Identifier(itemId);
        {
            if (Registries.ITEM.containsId(identifier)) {
                LOGGER.info("Found: " + itemId);
                return new ItemStack(Registries.ITEM.get(identifier), count);
            } else {
                LOGGER.warn("Item with ID '{}' not found", itemId);
                return null; // Item ID does not exist
            }
        }
    }

    private static boolean hasAnyItemInInventory(ServerPlayerEntity player) {
        // Check the player's main inventory (including hotbar)
        for (ItemStack itemStack : player.getInventory().main) {
            if (!itemStack.isEmpty()) {
                return true;  // Found an item
            }
        }

        // Check the player's armor inventory
        for (ItemStack itemStack : player.getInventory().armor) {
            if (!itemStack.isEmpty()) {
                return true;  // Found an item
            }
        }

        // Check the player's offhand inventory
        if (!player.getInventory().offHand.get(0).isEmpty()) {
            return true;  // Found an item in offhand
        }

        // No items found in any inventory slot
        return false;
    }
}
