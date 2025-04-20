package com.trongthang.welcometomyworld.entities.client.Wanderer;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Wanderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WandererRenderer extends MobEntityRenderer<Wanderer, WandererModel<Wanderer>> {

    public WandererRenderer(EntityRendererFactory.Context context) {
        super(context, new WandererModel<>(context.getPart(WandererModel.WANDERER)), 0.5f);

        this.addFeature(new WandererGlowFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(Wanderer entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/wanderer.png");
    }


    @Override
    public void render(Wanderer livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        matrixStack.scale(2f, 2f, 2f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
