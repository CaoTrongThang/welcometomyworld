package com.trongthang.welcometomyworld.entities.client.PurplePortal;

import com.trongthang.welcometomyworld.entities.PurplePortalEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurplePortalRenderer extends GeoEntityRenderer<PurplePortalEntity> {

    public PurplePortalRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new PurplePortalModel());
    }

    @Override
    public void render(PurplePortalEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
            VertexConsumerProvider bufferSource, int packedLight) {

        float floatingOffset = (float) Math.sin((entity.age + partialTick) * 0.05f) * 0.25f;

        poseStack.push();
        poseStack.translate(0, floatingOffset, 0);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
