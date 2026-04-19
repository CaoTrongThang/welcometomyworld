package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import net.minecraft.util.math.random.Random;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VoidWormRenderer extends GeoEntityRenderer<VoidWormEntity> {

        @Override
        public boolean shouldRender(VoidWormEntity entity, Frustum frustum, double x, double y, double z) {
                return true;
        }

        public VoidWormRenderer(EntityRendererFactory.Context renderManager) {
                super(renderManager, new VoidWormModel());
                this.withScale(5.0f, 5.0f);

                this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }

        @Override
        public void render(VoidWormEntity entity, float entityYaw, float partialTick, MatrixStack matrices,
                        VertexConsumerProvider vertexConsumers, int light) {
                super.render(entity, entityYaw, partialTick, matrices, vertexConsumers, light);

                // If the mob is dying, draw the purple beams!
                if (entity.ticksSinceDeath > 0) {
                        float deathProgress = ((float) entity.ticksSinceDeath + partialTick) / 200.0F;

                        // This makes the beams grow thicker and brighter over time
                        float beamIntensity = Math.min(deathProgress > 0.8F ? (deathProgress - 0.8F) / 0.2F : 0.0F,
                                        1.0F);

                        // Seed a random number generator so beams don't jitter wildly
                        Random random = Random.create(432L);

                        // Get the "Lightning" render layer which glows in the dark and is
                        // semi-transparent
                        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLightning());

                        matrices.push();

                        // Move the beams to the center of your mob's body
                        matrices.translate(0.0D, entity.getHeight() / 2.0f, 0.0D);

                        // How many beams to draw. Starts small, grows as the animation plays.
                        int beamCount = (int) ((deathProgress + deathProgress * deathProgress) / 2.0F * 60.0F);

                        for (int i = 0; i < beamCount; ++i) {
                                matrices.push();

                                // Randomly rotate each beam to face different directions
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(random.nextFloat() * 360.0F));

                                // Randomly rotate them around their own axis
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(random.nextFloat() * 360.0F));
                                matrices.multiply(
                                                RotationAxis.POSITIVE_Z.rotationDegrees(
                                                                random.nextFloat() * 360.0F + deathProgress * 90.0F));

                                // Create the vertices for the triangle
                                Matrix4f matrix4f = matrices.peek().getPositionMatrix();

                                // Beam length and width
                                float beamLength = random.nextFloat() * 20.0F + 5.0F + beamIntensity * 10.0F;
                                float beamWidth = random.nextFloat() * 2.0F + 1.0F + beamIntensity * 2.0F;

                                // Purple/Pink color values (Red, Green, Blue, Alpha)
                                int r = 255;
                                int g = 0;
                                int b = 255;

                                // Draw the beam as a triangle
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
