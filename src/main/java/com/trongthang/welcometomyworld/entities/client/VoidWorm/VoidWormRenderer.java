package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidWormRenderer extends GeoEntityRenderer<VoidWormEntity> {
    public VoidWormRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidWormModel());
    }
}
