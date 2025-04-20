package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;
import static com.trongthang.welcometomyworld.features.FallingToWaterDamage.isHoldingUmbrella;

public class LightningsStrikePlayersInRain {
    float strikeChances = 0.5f;

    private int checkInterval = 80;
    private int counter = 0;

    public void onServerTick(MinecraftServer server) {

        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        ServerWorld world = server.getOverworld();

        if (!world.isRaining() && !world.isThundering()) return;

        if (world.getPlayers().isEmpty()) return;

        ServerPlayerEntity player = world.getPlayers().get(random.nextInt(world.getPlayers().size()));

        if (player.isSpectator() || player.isCreative() || player.hasStatusEffect(StatusEffects.LUCK)) {
            return;
        }


        BlockPos pos = player.getBlockPos();
        if (world.isSkyVisible(pos) && world.getBiome(pos).value().getPrecipitation(pos) != Biome.Precipitation.NONE) {
            double r = random.nextDouble();

            if (r <= strikeChances) {
                if (!isHoldingUmbrella(player)) {
                    summonLightning(player, world);
                }
            }
        }
    }

    private void summonLightning(ServerPlayerEntity player, ServerWorld world) {
        boolean directStrike = random.nextDouble() < 0.10;

        double strikeX, strikeZ;
        double distance;

        if (directStrike) {
            strikeX = player.getX();
            strikeZ = player.getZ();
        } else {
            distance = 7.0 + (random.nextDouble() * 60.0);
            double yaw = Math.toRadians(player.getYaw());
            double angleOffset = random.nextGaussian() * Math.toRadians(30.0); // 30° spread
            double strikeAngle = yaw + angleOffset;

            strikeX = player.getX() + (-Math.sin(strikeAngle)) * distance;
            strikeZ = player.getZ() + Math.cos(strikeAngle) * distance;
        }

        // Get top block at strike position
        BlockPos strikePos = new BlockPos((int) strikeX, 0, (int) strikeZ);
        strikePos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, strikePos);

        // Spawn lightning
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(strikePos.getX(), strikePos.getY(), strikePos.getZ());
        world.spawnEntity(lightning);
    }
}