package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import java.util.List;

public class UnknownRenderer extends GeoEntityRenderer<Unknown> {

    // --- Premium Afterimage Config ---
    private static final int GHOST_COUNT = 4; // How many ghosts to render
    private int ghostLingerTimer = 0;

    public UnknownRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new UnknownModel());
        this.shadowRadius = 0.7f;

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, Unknown animatable) {
                if (bone.getName().equals("right_arm_hand")) {
                    return animatable.getMainHandStack();
                } else if (bone.getName().equals("left_arm_hand")) {
                    return animatable.getOffHandStack();
                }
                return null;
            }

            @Override
            protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack,
                    Unknown animatable) {
                if (bone.getName().equals("right_arm_hand")) {
                    return ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
                } else if (bone.getName().equals("left_arm_hand")) {
                    return ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
                }
                return ModelTransformationMode.NONE;
            }

            @Override
            protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, Unknown animatable,
                    VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {

                poseStack.push();
                if (bone.getName().equals("right_arm_hand")) {
                    poseStack.translate(0, -0.06, -0.2);
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                } else if (bone.getName().equals("left_arm_hand")) {
                    poseStack.translate(0, -0.06, -0.2);
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
                }
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight,
                        packedOverlay);
                poseStack.pop();
            }
        });
    }

    @Override
    public void render(Unknown entity, float entityYaw, float partialTick, MatrixStack poseStack,
            VertexConsumerProvider bufferSource, int packedLight) {

        boolean isDashing = entity.getDashDir() != 0 || entity.getSkillId() == Unknown.DASH_FORWARD.id;

        // 1. Manage Shadow Life Cycle
        if (isDashing) {
            ghostLingerTimer = 20; // Linger for 20 ticks (1s) after dash
        } else if (ghostLingerTimer > 0) {
            ghostLingerTimer--;
        }

        if (ghostLingerTimer > 0) {
            this.animatable = entity;

            RenderLayer ghostLayer = RenderLayer.getEntityTranslucentCull(getTextureLocation(entity));
            BakedGeoModel bakedModel = getGeoModel().getBakedModel(getGeoModel().getModelResource(entity, this));

            // Use the movement delta to offset ghosts backward
            // This makes them "stick" to the boss with a slight trail
            double dx = entity.getX() - entity.prevX;
            double dy = entity.getY() - entity.prevY;
            double dz = entity.getZ() - entity.prevZ;

            for (int i = 1; i <= GHOST_COUNT; i++) {
                float alpha = 0.45f * (1.0f - (float) i / (GHOST_COUNT + 1));
                if (ghostLingerTimer < 8)
                    alpha *= (ghostLingerTimer / 8.0f);

                poseStack.push();

                // Space ghosts out backward along the movement vector
                double factor = i * 0.6;
                poseStack.translate(-dx * factor, -dy * factor, -dz * factor);

                VertexConsumer ghostBuffer = bufferSource.getBuffer(ghostLayer);

                // Render with normal color (1.0, 1.0, 1.0) instead of purple
                actuallyRender(poseStack, entity, bakedModel, ghostLayer, bufferSource, ghostBuffer,
                        true, partialTick, packedLight, OverlayTexture.DEFAULT_UV,
                        1.0f, 1.0f, 1.0f, alpha);

                poseStack.pop();
            }
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
