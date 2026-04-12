package com.trongthang.welcometomyworld.entities.client.RiftPortal;

import com.trongthang.welcometomyworld.entities.RiftPortalEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.util.math.random.Random;

public class RiftPortalRenderer extends GeoEntityRenderer<RiftPortalEntity> {

        public RiftPortalRenderer(EntityRendererFactory.Context renderManager) {
                super(renderManager, new RiftPortalModel());
        }

        @Override
        public void render(RiftPortalEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                        VertexConsumerProvider bufferSource, int packedLight) {

                poseStack.push();
                poseStack.scale(3.0F, 3.0F, 3.0F);

                super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

                // Render "dimension cut" cracks

                // Re-roll seed every few ticks for a chaotic "rapid slash" effect
                // Or keep constant if you want them to just grow. Let's make it chaotic but
                // somewhat stable.
                // We'll update the random seed every 2 ticks to make slashes flicker in and out
                // fast.
                Random random = Random.create(entity.getUuid().getLeastSignificantBits());

                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderLayer.getLightning());

                poseStack.push();

                // Lowering the center of the cracks so it's more aligned with the portal
                // visuals
                poseStack.translate(0.0D, 0.5D, 0.0D);

                int beamCount = 40; // Static number of cracks

                for (int i = 0; i < beamCount; ++i) {
                        poseStack.push();

                        poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
                        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
                        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F));

                        Matrix4f matrix4f = poseStack.peek().getPositionMatrix();

                        // Very thin lines to simulate cracks in "glass" space
                        float beamLength = random.nextFloat() * 15.0F + 5.0F;
                        float beamWidth = random.nextFloat() * 0.05F + 0.01F; // Extremely thin!

                        // Colorless faint white/grey for the "glass edge" reflection
                        int r = 255;
                        int g = 255;
                        int b = 255;
                        // The alpha is represented by fading the additive color or by passing the
                        // transparency
                        int alphaOuter = 0;
                        int alphaInner = random.nextInt(40) + 10; // Very faint

                        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                        .color(r, g, b, alphaInner).next();
                        vertexConsumer.vertex(matrix4f, -0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                        .color(r, g, b, alphaOuter).next();
                        vertexConsumer.vertex(matrix4f, 0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                        .color(r, g, b, alphaOuter).next();

                        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                        .color(r, g, b, alphaInner).next();
                        vertexConsumer.vertex(matrix4f, 0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                        .color(r, g, b, alphaOuter).next();
                        vertexConsumer.vertex(matrix4f, 0.0F, beamLength, 1.0F * beamWidth).color(r, g, b, alphaOuter)
                                        .next();

                        vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                        .color(r, g, b, alphaInner).next();
                        vertexConsumer.vertex(matrix4f, 0.0F, beamLength, 1.0F * beamWidth).color(r, g, b, alphaOuter)
                                        .next();
                        vertexConsumer.vertex(matrix4f, -0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                        .color(r, g, b, alphaOuter).next();

                        // Sometime render slightly wider but barely visible shards
                        if (random.nextFloat() < 0.2f) {
                                float shardWidth = beamWidth * 10f;
                                int shardAlpha = 15;
                                vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                                .color(200, 200, 200, shardAlpha).next();
                                vertexConsumer.vertex(matrix4f, -0.866F * shardWidth, beamLength * 0.5f,
                                                -0.5F * shardWidth).color(r, g, b, alphaOuter).next();
                                vertexConsumer.vertex(matrix4f, 0.866F * shardWidth, beamLength * 0.5f,
                                                -0.5F * shardWidth).color(r, g, b, alphaOuter).next();
                        }

                        poseStack.pop();
                }

                poseStack.pop();
                poseStack.pop();
        }
}
