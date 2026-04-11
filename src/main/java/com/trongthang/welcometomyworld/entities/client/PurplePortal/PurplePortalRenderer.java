package com.trongthang.welcometomyworld.entities.client.PurplePortal;

import com.trongthang.welcometomyworld.entities.PurplePortal;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurplePortalRenderer extends GeoEntityRenderer<PurplePortal> {

    public PurplePortalRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PurplePortalModel());
    }

    @Override
    public void render(PurplePortal entity, float entityYaw, float partialTick, MatrixStack poseStack,
            VertexConsumerProvider bufferSource, int packedLight) {

        float scale = entity.getLifeScale();
        float floatingOffset = 0.75f + (float) Math.sin((entity.age + partialTick) * 0.05f) * 0.25f;

        poseStack.push();
        poseStack.translate(0, floatingOffset, 0);
        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
