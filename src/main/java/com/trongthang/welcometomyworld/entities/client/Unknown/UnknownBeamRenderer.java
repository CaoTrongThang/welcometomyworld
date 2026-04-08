package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.UnknownBeamEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class UnknownBeamRenderer extends EntityRenderer<UnknownBeamEntity> {
    private static final Identifier TEXTURE = new Identifier(WelcomeToMyWorld.MOD_ID,
            "textures/entity/unknown_beam.png");

    public UnknownBeamRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(UnknownBeamEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        float length = entity.getLength();
        float radius = entity.getRadius();
        float age = entity.age + tickDelta;

        matrices.push();

        // Face the beam correctly. Minecraft Yaw 0 is South.
        // If it shoots behind, we add 180 to flip it forward.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - entity.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-entity.getPitch())); // Match the model's pitch
                                                                                        // inversion
                                                                                        // positive down

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(TEXTURE, true));

        // Draw 3 rotating quads for volume
        for (int i = 0; i < 3; i++) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(age * 10.0f + (i * 60)));
            renderQuad(matrices, vertexConsumer, radius, length);
            matrices.pop();
        }

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderQuad(MatrixStack matrices, VertexConsumer vertexConsumer, float radius, float length) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();

        // One vertical quad
        drawVertex(matrix4f, matrix3f, vertexConsumer, -radius, 0, 0, 0, 1);
        drawVertex(matrix4f, matrix3f, vertexConsumer, radius, 0, 0, 1, 1);
        drawVertex(matrix4f, matrix3f, vertexConsumer, radius, 0, -length, 1, 0);
        drawVertex(matrix4f, matrix3f, vertexConsumer, -radius, 0, -length, 0, 0);

        // One horizontal quad
        drawVertex(matrix4f, matrix3f, vertexConsumer, 0, -radius, 0, 0, 1);
        drawVertex(matrix4f, matrix3f, vertexConsumer, 0, radius, 0, 1, 1);
        drawVertex(matrix4f, matrix3f, vertexConsumer, 0, radius, -length, 1, 0);
        drawVertex(matrix4f, matrix3f, vertexConsumer, 0, -radius, -length, 0, 0);
    }

    private void drawVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float x, float y,
            float z, float u, float v) {
        vertexConsumer.vertex(matrix4f, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(15728880) // Full bright
                .normal(matrix3f, 0, 1, 0)
                .next();
    }

    @Override
    public boolean shouldRender(UnknownBeamEntity entity, net.minecraft.client.render.Frustum frustum, double x,
            double y, double z) {
        return true;
    }

    @Override
    public Identifier getTexture(UnknownBeamEntity entity) {
        return TEXTURE;
    }
}
