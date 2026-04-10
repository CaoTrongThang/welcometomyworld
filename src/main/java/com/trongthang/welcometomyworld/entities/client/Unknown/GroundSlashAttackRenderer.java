package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.GroundSlashAttackEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GroundSlashAttackRenderer extends GeoEntityRenderer<GroundSlashAttackEntity> {

    public GroundSlashAttackRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new GroundSlashAttackModel());
    }

    @Override
    public void render(GroundSlashAttackEntity entity, float entityYaw, float partialTick,
            MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();
        poseStack.scale(6.0f, 6.0f, 6.0f);
        // Rotate model so it faces the direction of travel (yaw)
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.pop();
    }
}
