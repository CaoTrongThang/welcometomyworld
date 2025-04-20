package com.trongthang.welcometomyworld.entities.client.Blossom;

import com.trongthang.welcometomyworld.entities.Blossom;
import com.trongthang.welcometomyworld.entities.Wanderer;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BlossomGlowFeatureRenderer<T extends Blossom> extends EyesFeatureRenderer<T, BlossomModel<T>> {
    private static final RenderLayer GLOW = RenderLayer.getEyes(new Identifier("welcometomyworld:textures/entity/blossom/blossom_glow.png"));

    public BlossomGlowFeatureRenderer(FeatureRendererContext<T, BlossomModel<T>> context) {
        super(context);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return GLOW;
    }
}
