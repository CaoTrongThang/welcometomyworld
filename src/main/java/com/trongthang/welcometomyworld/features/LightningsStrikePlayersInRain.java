package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.features.FallingToWaterDamage.isUmbrella;

public class LightningsStrikePlayersInRain {
    private int upDistance = 64;


    //Lightnings can strike up to 3 times;

    List<Double> strikeChances = List.of(0.8, 0.7, 0.5);

    Random rand = new Random();

    private int checkInterval = 20;
    private int counter = 0;

    public void onServerTick(MinecraftServer server) {
        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        ServerWorld world = server.getOverworld();

        for (ServerPlayerEntity player : world.getPlayers()) {

            if (player.isSpectator() || player.isCreative()) return;

            if (!player.isTouchingWater() && player.isTouchingWaterOrRain()) {
                if (!isAnyThingAbovePlayerHead(world, player)) {
                    double r = rand.nextDouble();

                    for (double d : strikeChances) {
                        if (r <= d) {
                            if (!isUmbrella(player)) {
                                summonLightning(player, world);
                            }
                        }

                    }
                }
            }
        }
    }

    public boolean isAnyThingAbovePlayerHead(ServerWorld world, ServerPlayerEntity player) {
        var checkUp = upDistance - player.getY();

        for (int x = 1; x < checkUp; x++) {
            if (!world.getBlockState(player.getBlockPos().up(x)).isAir()) {
                return true;
            }
        }

        return false;
    }

    private void summonLightning(ServerPlayerEntity mob, ServerWorld world) {
        BlockPos pos = mob.getBlockPos();
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(lightning);
    }
}
