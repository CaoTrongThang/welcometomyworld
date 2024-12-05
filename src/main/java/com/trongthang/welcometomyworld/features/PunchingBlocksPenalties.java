package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.saveData.PlayerClass;
import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class PunchingBlocksPenalties {

    List<Block> nonDamagingBlocks = List.of(
            Blocks.SNOW_BLOCK
    );
    public void handlePunchingBlock() {
        ServerPlayNetworking.registerGlobalReceiver(PLAYER_BREAKING_BLOCK, (server, p, handler, buf, responseSender) -> {
            // Read all data from the buffer immediately
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            UUID playerUuid = buf.readUuid();

            // Defer execution to the server thread
            server.execute(() -> {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                if (player != null) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    applyBlockDamage(server, player, blockPos);
                }
            });
        });
    }


    private void applyBlockDamage(MinecraftServer server, ServerPlayerEntity player, BlockPos blockPos) {
        World world = player.getWorld();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isAir()) return;

        if (!nonDamagingBlocks.contains(blockState.getBlock())){
            float hardness = blockState.getHardness(world, blockPos);

            if (hardness > 0) {
                float damage = calculateDamage(hardness);

                float finalDamageIfHasSomethingOnHand = player.getMainHandStack().isEmpty()
                        ? damage
                        : (!player.getMainHandStack().isSuitableFor(blockState) ? damage / 2 : 0);

                if(finalDamageIfHasSomethingOnHand > 20){
                    finalDamageIfHasSomethingOnHand = 20;
                }

                if (finalDamageIfHasSomethingOnHand > 1.1) {
                    LOGGER.info("Final damage: " + finalDamageIfHasSomethingOnHand);
                    player.damage(player.getWorld().getDamageSources().generic(), finalDamageIfHasSomethingOnHand);
                }

                PlayerClass p = dataHandler.playerDataMap.get(player.getUuid());
                if (!p.firstPunchingBlocksDamage) {
                    Utils.grantAdvancement(player, "first_punching_blocks_damage");
                    p.firstPunchingBlocksDamage = true;
                }

                if (player.getHealth() <= 0 && !p.firstPunchingBlocksDie) {
                    Utils.grantAdvancement(player, "first_punching_blocks_die");
                    p.firstPunchingBlocksDie = true;
                }
            }
        }
    }

    public float calculateDamage(float hardness) {
        // Avoid division by zero or negative hardness issues
        double damage = 1 + 2.5 * Math.log(hardness + 1);
        return (float) Math.min(damage, 10); // Cap damage at 10
    }


    //Old Method

    //    class BlockDamage {
//        String blockId;
//        float damage;
//
//        public BlockDamage(String blockId, float damage) {
//            this.blockId = blockId;
//            this.damage = damage;
//        }
//    }

//    public List<BlockDamage> blockDamageList = List.of(
//            // Wood logs
//            new BlockDamage("minecraft:oak_log", 3),
//            new BlockDamage("minecraft:spruce_log", 3),
//            new BlockDamage("minecraft:birch_log", 3),
//            new BlockDamage("minecraft:jungle_log", 3),
//            new BlockDamage("minecraft:acacia_log", 3),
//            new BlockDamage("minecraft:dark_oak_log", 3),
//            new BlockDamage("croptopia:cinnamon_log", 3),
//
//            // Soft block
//            new BlockDamage("minecraft:grass_block", 2),
//            new BlockDamage("minecraft:dirt", 2),
//            new BlockDamage("minecraft:gravel", 2),
//            new BlockDamage("minecraft:sand", 2),
//
//            // Common hard blocks
//            new BlockDamage("minecraft:cobblestone", 5),
//            new BlockDamage("minecraft:stone", 8),
//            new BlockDamage("minecraft:andesite", 4),
//            new BlockDamage("minecraft:granite", 4),
//            new BlockDamage("minecraft:diorite", 4),
//
//            // Miscellaneous
//            new BlockDamage("minecraft:glass", 3),
//            new BlockDamage("minecraft:sandstone", 5),
//            new BlockDamage("minecraft:terracotta", 5)
//    );

//    private void applyBlockDamage(MinecraftServer server, ServerPlayerEntity player, BlockPos blockPos) {
//        World world = player.getWorld();
//        BlockState blockState = world.getBlockState(blockPos);
//
//        if (blockState.isAir()) return;

//        for (BlockDamage b : blockDamageList) {
//            if (b.blockId.equals(Registries.BLOCK.getId(blockState.getBlock()).toString())) {
//                float finalDamage = player.getMainHandStack().isEmpty()
//                        ? b.damage
//                        : (!player.getMainHandStack().isSuitableFor(blockState) ? b.damage / 2 : 0);
//
//                if (finalDamage > 1) {
//                    player.damage(player.getWorld().getDamageSources().generic(), finalDamage);
//                }
//
//                PlayerClass p = dataHandler.playerDataMap.get(player.getUuid());
//                if (!p.firstPunchingBlocksDamage) {
//                    Utils.grantAdvancement(player, "first_punching_blocks_damage");
//                    p.firstPunchingBlocksDamage = true;
//                }
//
//                if (player.getHealth() <= 0 && !p.firstPunchingBlocksDie) {
//                    Utils.grantAdvancement(player, "first_punching_blocks_die");
//                    p.firstPunchingBlocksDie = true;
//                }
//                return;
//            }
//        }
//    }
}
