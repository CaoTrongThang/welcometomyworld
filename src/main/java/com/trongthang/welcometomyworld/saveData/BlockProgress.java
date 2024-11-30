package com.trongthang.welcometomyworld.saveData;

import net.minecraft.entity.Entity;

public class BlockProgress {

    public Entity entity;
    public int progress;

    public BlockProgress(Entity entity, int progress){
        this.entity = entity;
        this.progress = progress;
    }
}
