package com.trongthang.welcometomyworld.events;

import com.trongthang.welcometomyworld.entities.EnderPest;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.math.BlockPos;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

public class SpawnEvents {

    // Removed hardcoded DISABLED_MOBS, now uses ConfigLoader

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // Chặn những con đã lỡ save vào chunk từ trước, hoặc lách qua được Mixin của
            // WorldGen
            if (entity instanceof net.minecraft.entity.LivingEntity) {
                String entityId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(entity.getType()).toString();
                if (com.trongthang.welcometomyworld.Utilities.Utils.matchesPattern(entityId,
                        com.trongthang.welcometomyworld.ConfigLoader.getInstance().disabledMobs)) {
                    entity.discard();
                    return;
                }
            }

            if (entity instanceof EnderPest) {
                synchronized (EnderPest.class) {
                    EnderPest.totalEnderPests = Math.min(EnderPest.totalEnderPests + 1, EnderPest.MAX_ENDER_PESTS);
                }
            }

            if (entity instanceof PhantomEntity phantom) {
                if (entity.getCustomName() == null) {
                    if (world
                            .getBlockState(
                                    new BlockPos((int) entity.getX(), (int) entity.getY() + 30, (int) entity.getZ()))
                            .isAir()) {
                        entity.teleport((int) entity.getX(), (int) entity.getY() + 30, (int) entity.getZ());
                    }

                    phantom.setPhantomSize(random.nextInt(2, 50));
                    phantom.getNavigation().stop();
                }
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof EnderPest) {
                synchronized (EnderPest.class) {
                    EnderPest.totalEnderPests = Math.max(EnderPest.totalEnderPests - 1, 0);
                }
            }
        });
    }
}
