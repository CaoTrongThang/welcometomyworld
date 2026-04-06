package com.trongthang.welcometomyworld.entities.client.FallingSkeleton;

import com.trongthang.welcometomyworld.entities.FallingSkeleton.FallingSkeleton;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FallingSkeletonRenderer extends GeoEntityRenderer<FallingSkeleton> {
    public FallingSkeletonRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new FallingSkeletonModel());
    }
}
