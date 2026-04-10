package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.PurpleCrystalEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurpleCrystalRenderer extends GeoEntityRenderer<PurpleCrystalEntity> {
    public PurpleCrystalRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PurpleCrystalModel());
    }
}
