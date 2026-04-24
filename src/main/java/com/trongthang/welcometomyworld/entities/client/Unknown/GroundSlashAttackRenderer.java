package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.GroundSlashAttackEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GroundSlashAttackRenderer extends GeoEntityRenderer<GroundSlashAttackEntity> {

    // Number of ghost copies trailing behind the slash
    private static final int GHOST_COUNT = 3;
    // How far apart each ghost is spaced (in blocks) along the trail
    private static final double GHOST_SPACING = 0.7;

    public GroundSlashAttackRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new GroundSlashAttackModel());
    }

    @Override
    public void render(GroundSlashAttackEntity entity, float entityYaw, float partialTick,
            MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        RenderLayer translucentLayer = RenderLayer.getEntityTranslucentCull(getTextureLocation(entity));
        BakedGeoModel bakedModel = getGeoModel().getBakedModel(getGeoModel().getModelResource(entity, this));

        // Travel direction from last tick delta — used to position ghost trail
        // spatially
        double dx = entity.getX() - entity.prevX;
        double dz = entity.getZ() - entity.prevZ;
        double len = Math.sqrt(dx * dx + dz * dz);

        // Normalised backward unit vector (trail goes opposite to movement)
        double nx = len > 0.001 ? -dx / len : 0;
        double nz = len > 0.001 ? -dz / len : 0;

        // --- Ghost trail copies (rendered before main so main draws on top) ---
        this.animatable = entity;
        for (int i = 1; i <= GHOST_COUNT; i++) {
            // Alpha falls off quickly: 0.35 → 0.12
            float alpha = 0.38f * (1.0f - (float) i / (GHOST_COUNT + 1));
            // Age-based fade-in for the first 4 ticks and fade-out for the last 6
            if (entity.age < 4) {
                alpha *= entity.age / 4.0f;
            } else if (entity.age > 22) {
                alpha *= Math.max(0, (28 - entity.age) / 6.0f);
            }
            if (alpha <= 0)
                continue;

            double offset = i * GHOST_SPACING;

            poseStack.push();
            // Translate in world space BEFORE scale/rotation so offsets are axis-aligned
            poseStack.translate(nx * offset, 0, nz * offset);
            poseStack.scale(3f, 3f, 3f);
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));

            VertexConsumer ghostBuffer = bufferSource.getBuffer(translucentLayer);
            // Slight cyan tint to give a wind/air look
            actuallyRender(poseStack, entity, bakedModel, translucentLayer,
                    bufferSource, ghostBuffer,
                    true, partialTick, packedLight, OverlayTexture.DEFAULT_UV,
                    0.7f, 0.92f, 1.0f, alpha);

            poseStack.pop();
        }

        // --- Main body at reduced opacity (wind-like translucency) ---
        poseStack.push();
        poseStack.scale(3f, 3f, 3f);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-entity.getYaw()));

        float mainAlpha = 0.55f;
        // Fade in/out matching trail
        if (entity.age < 4) {
            mainAlpha *= entity.age / 4.0f;
        } else if (entity.age > 22) {
            mainAlpha *= Math.max(0, (28 - entity.age) / 6.0f);
        }

        VertexConsumer mainBuffer = bufferSource.getBuffer(translucentLayer);
        actuallyRender(poseStack, entity, bakedModel, translucentLayer,
                bufferSource, mainBuffer,
                true, partialTick, packedLight, OverlayTexture.DEFAULT_UV,
                1.0f, 1.0f, 1.0f, mainAlpha);

        poseStack.pop();
    }
}
