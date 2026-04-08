package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.Unknown.SummoningCircleEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SummoningCircleRenderer extends GeoEntityRenderer<SummoningCircleEntity> {
    public SummoningCircleRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new SummoningCircleModel());
    }

    @Override
    public void render(SummoningCircleEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
            VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        poseStack.scale(3.0f, 3.0f, 3.0f); // Scale up the circle
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
