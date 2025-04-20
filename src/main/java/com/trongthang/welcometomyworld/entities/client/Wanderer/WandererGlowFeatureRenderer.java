package com.trongthang.welcometomyworld.entities.client.Wanderer;

import com.trongthang.welcometomyworld.entities.Wanderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WandererGlowFeatureRenderer<T extends Wanderer> extends EyesFeatureRenderer<T, WandererModel<T>> {
    private static final RenderLayer GLOW = RenderLayer.getEyes(new Identifier("welcometomyworld:textures/entity/wanderer/wanderer_glow.png"));

    public WandererGlowFeatureRenderer(FeatureRendererContext<T, WandererModel<T>> context) {
        super(context);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return GLOW;
    }
}
