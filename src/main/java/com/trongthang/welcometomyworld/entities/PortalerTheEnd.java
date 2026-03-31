package com.trongthang.welcometomyworld.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

//PORTALER: This mob is a portal that can move and can switch portal randomly, players can go to the portal to go to the end or the nether
public class PortalerTheEnd extends Portaler {

    public PortalerTheEnd(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

}
