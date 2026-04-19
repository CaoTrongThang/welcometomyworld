package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import net.minecraft.util.math.random.Random;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;

public class VoidWormTailRenderer extends GeoEntityRenderer<VoidWormPartEntity> {

        @Override
        public boolean shouldRender(VoidWormPartEntity entity, Frustum frustum, double x, double y, double z) {
                return true;
        }

        public VoidWormTailRenderer(EntityRendererFactory.Context renderManager) {
                super(renderManager, new VoidWormTailModel());
                this.withScale(3f, 3f);
                this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }

        @Override
        public void render(VoidWormPartEntity entity, float entityYaw, float partialTick, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
                super.render(entity, entityYaw, partialTick, matrices, vertexConsumers, light);

                VoidWormEntity head = entity.getHead();
                if (head != null && head.ticksSinceDeath > 0) {
                        float deathProgress = ((float) head.ticksSinceDeath + partialTick) / 200.0F;
                        float beamIntensity = Math.min(deathProgress > 0.8F ? (deathProgress - 0.8F) / 0.2F : 0.0F,
                                        1.0F);
                        Random random = Random.create(432L + entity.getId());
                        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLightning());

                        matrices.push();
                        matrices.translate(0.0D, entity.getHeight() / 2.0f, 0.0D);

                        int beamCount = (int) ((deathProgress + deathProgress * deathProgress) / 2.0F * 50.0F);

                        for (int i = 0; i < beamCount; ++i) {
                                matrices.push();
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(
                                                RotationAxis.POSITIVE_Z.rotationDegrees(
                                                                random.nextFloat() * 360.0F + deathProgress * 90.0F));

                                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                                float beamLength = random.nextFloat() * 15.0F + 4.0F + beamIntensity * 8.0F;
                                float beamWidth = random.nextFloat() * 1.5F + 0.8F + beamIntensity * 1.5F;

                                int r = 255;
                                int g = 0;
                                int b = 255;

                                vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                                .color(r, g, b, (int) (255.0F * (1.0F - beamIntensity))).next();
                                vertexConsumer.vertex(matrix4f, -0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                                .color(r, g, b, 0)
                                                .next();
                                vertexConsumer.vertex(matrix4f, 0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                                .color(r, g, b, 0)
                                                .next();

                                vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                                .color(r, g, b, (int) (255.0F * (1.0F - beamIntensity))).next();
                                vertexConsumer.vertex(matrix4f, 0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                                .color(r, g, b, 0)
                                                .next();
                                vertexConsumer.vertex(matrix4f, 0.0F, beamLength, 1.0F * beamWidth).color(r, g, b, 0)
                                                .next();

                                vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F)
                                                .color(r, g, b, (int) (255.0F * (1.0F - beamIntensity))).next();
                                vertexConsumer.vertex(matrix4f, 0.0F, beamLength, 1.0F * beamWidth).color(r, g, b, 0)
                                                .next();
                                vertexConsumer.vertex(matrix4f, -0.866F * beamWidth, beamLength, -0.5F * beamWidth)
                                                .color(r, g, b, 0)
                                                .next();
                                matrices.pop();
                        }
                        matrices.pop();
                }
        }
}
