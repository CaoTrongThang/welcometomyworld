package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidWormTailRenderer extends GeoEntityRenderer<VoidWormPartEntity> {
    public VoidWormTailRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidWormTailModel());
        this.withScale(2.0f, 2.0f);
    }

    @Override
    public void preRender(MatrixStack poseStack, VoidWormPartEntity animatable, BakedGeoModel model,
            VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
                packedOverlay, red, green, blue, alpha);
    }
}
