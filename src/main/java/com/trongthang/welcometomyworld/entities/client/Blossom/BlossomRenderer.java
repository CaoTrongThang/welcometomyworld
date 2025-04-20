package com.trongthang.welcometomyworld.entities.client.Blossom;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Blossom;
import com.trongthang.welcometomyworld.entities.Enderchester;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererGlowFeatureRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BlossomRenderer extends MobEntityRenderer<Blossom, BlossomModel<Blossom>> {

    public BlossomRenderer(EntityRendererFactory.Context context) {
        super(context, new BlossomModel<>(context.getPart(BlossomModel.BLOSSOM)), 0.75f);

        this.addFeature(new BlossomGlowFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(Blossom entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/blossom.png");
    }


    @Override
    public void render(Blossom livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
