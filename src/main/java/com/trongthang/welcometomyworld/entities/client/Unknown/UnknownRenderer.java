package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class UnknownRenderer extends GeoEntityRenderer<Unknown> {
    public UnknownRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new UnknownModel());

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

                // Adjustments to align item with hand bone
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
        // this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

}
