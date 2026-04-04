package com.trongthang.welcometomyworld.entities.Unknown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class Unknown extends MobEntity {
    public Unknown(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
}
