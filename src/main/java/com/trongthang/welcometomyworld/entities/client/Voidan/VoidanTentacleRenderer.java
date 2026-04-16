package com.trongthang.welcometomyworld.entities.client.Voidan;

import com.trongthang.welcometomyworld.entities.Voidan.VoidanTentacle;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidanTentacleRenderer extends GeoEntityRenderer<VoidanTentacle> {
    public VoidanTentacleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidanTentacleModel());
    }
}
