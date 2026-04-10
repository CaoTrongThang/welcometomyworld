package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VoidWormRenderer extends GeoEntityRenderer<VoidWormEntity> {
    public VoidWormRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidWormModel());
        this.withScale(3.0f, 3.0f);

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

}
