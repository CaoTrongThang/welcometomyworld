package com.trongthang.welcometomyworld.events;

import com.trongthang.welcometomyworld.entities.EnderPest;
import com.trongthang.welcometomyworld.entities.FallenKnight;
import com.trongthang.welcometomyworld.entities.Wanderer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.awt.*;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;
import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH;

public class SpawnEvents {
    public static void register(){
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof EnderPest) {
                synchronized (EnderPest.class) {
                    EnderPest.totalEnderPests = Math.min(EnderPest.totalEnderPests + 1, EnderPest.MAX_ENDER_PESTS);
                }
            }

            if (entity instanceof PhantomEntity phantom) {
                if(entity.getCustomName() == null){
                    entity.teleport((int) entity.getX(), (int) entity.getY() + 30, (int) entity.getZ());
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
