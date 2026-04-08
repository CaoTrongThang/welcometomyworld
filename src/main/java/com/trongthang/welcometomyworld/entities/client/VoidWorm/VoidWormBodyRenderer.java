package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidWormBodyRenderer extends GeoEntityRenderer<VoidWormPartEntity> {
    public VoidWormBodyRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidWormBodyModel());
    }
}
