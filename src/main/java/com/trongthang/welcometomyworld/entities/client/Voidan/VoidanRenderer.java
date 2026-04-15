package com.trongthang.welcometomyworld.entities.client.Voidan;

import com.trongthang.welcometomyworld.entities.Voidan.Voidan;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;

public class VoidanRenderer extends GeoEntityRenderer<Voidan> {

    public VoidanRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidanModel());
        this.shadowRadius = 5f;
    }

    @Override
    public Identifier getTextureLocation(Voidan animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/voidan/voidan.png");
    }
}
