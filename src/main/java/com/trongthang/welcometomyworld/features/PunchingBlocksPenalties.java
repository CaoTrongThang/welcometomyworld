package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.classes.PlayerData;
import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class PunchingBlocksPenalties {

    List<Block> nonDamagingBlocks = List.of(
            Blocks.SNOW_BLOCK
    );

    public static class BreakBlockProtection {
        public BlockPos blockPos;
        public int cooldown = 8;

        public BreakBlockProtection(BlockPos blockPos){
            this.blockPos = blockPos;
        }

        public void decreaseCooldown(int num){
            this.cooldown -= num;
        }
    }

    //Immune damage from breaking block for a while after broke 1
    private static ConcurrentHashMap<ServerPlayerEntity, BreakBlockProtection> immunePunchPenalties = new ConcurrentHashMap<>();

    public void handlePunchingBlock() {
        ServerTickEvents.END_SERVER_TICK.register((t) -> {
            if(immunePunchPenalties.isEmpty()) return;

            for(ServerPlayerEntity p : immunePunchPenalties.keySet()){
                if(immunePunchPenalties.get(p).cooldown > 0){
                    immunePunchPenalties.get(p).decreaseCooldown(1);
                } else {
                    immunePunchPenalties.remove(p);
                }
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((a,b,c,d,e) -> {
            if(a.isClient) return true;

            double armor = b.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            if(armor < 10){
                immunePunchPenalties.putIfAbsent((ServerPlayerEntity) b, new BreakBlockProtection(c));
            }

            return true;
        });


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

                    if(immunePunchPenalties.containsKey(player)){
                        if(blockPos.equals(immunePunchPenalties.get(player).blockPos)){
                            applyPunchingBlockDamage(server, player, blockPos);
                        }
                    } else {
                        applyPunchingBlockDamage(server, player, blockPos);
                    }
                }
            });
        });
    }


    private void applyPunchingBlockDamage(MinecraftServer server, ServerPlayerEntity player, BlockPos blockPos) {
        World world = player.getWorld();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isAir() || blockState.getBlock() == Blocks.WATER) return;

        if (!nonDamagingBlocks.contains(blockState.getBlock())){
            float hardness = blockState.getHardness(world, blockPos);

            if (hardness > 0) {
                float damage = Utils.calculateDamageWithArmor(calculateDamage(hardness), player);

                float finalDamageIfHasSomethingOnHand = player.getMainHandStack().isEmpty()
                        ? damage
                        : (!player.getMainHandStack().isSuitableFor(blockState) ? damage / 2 : 0);

                if(finalDamageIfHasSomethingOnHand > 20){
                    finalDamageIfHasSomethingOnHand = 20;
                }
                if (finalDamageIfHasSomethingOnHand > 1.1) {
                    player.damage(player.getWorld().getDamageSources().generic(), finalDamageIfHasSomethingOnHand);

                    PlayerData p = dataHandler.playerDataMap.get(player.getUuid());
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
    }

    public float calculateDamage(float hardness) {
        // Avoid division by zero or negative hardness issues
        double damage = 1 + 2.5 * Math.log(hardness + 1);
        return (float) Math.min(damage, 10); // Cap damage at 10
    }
}
