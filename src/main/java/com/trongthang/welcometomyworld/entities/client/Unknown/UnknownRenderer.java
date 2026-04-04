package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UnknownRenderer extends GeoEntityRenderer<Unknown> {
    public UnknownRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new UnknownModel());
    }
}
