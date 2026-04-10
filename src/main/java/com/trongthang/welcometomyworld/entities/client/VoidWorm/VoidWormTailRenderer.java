package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidWormTailRenderer extends GeoEntityRenderer<VoidWormPartEntity> {
    public VoidWormTailRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidWormTailModel());
        this.withScale(2.5f, 2.5f);
    }
}
