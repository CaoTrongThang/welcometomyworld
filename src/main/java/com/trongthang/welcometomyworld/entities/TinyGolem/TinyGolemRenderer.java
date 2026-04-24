package com.trongthang.welcometomyworld.entities.TinyGolem;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class TinyGolemRenderer extends GeoEntityRenderer<TinyGolem> {
    public TinyGolemRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TinyGolemModel());

        // Add custom layer for items (weapons)
        addRenderLayer(new BlockAndItemGeoLayer<>(this, (bone, animatable) -> {
            if (bone.getName().equals("fist_left")) {
                return animatable.getEquippedStack(EquipmentSlot.MAINHAND);
            }
            return null;
        }, (bone, animatable) -> null) {
            @Override
            protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack,
                    TinyGolem animatable) {
                return ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack,
                    TinyGolem animatable,
                    VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {
                // weapon
                if (bone.getName().equals("fist_left")) {
                    poseStack.scale(0.85f, 0.85f, 0.85f);
                    poseStack.translate(0, 0, -0.15f); // Lowered significantly
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85f)); // Rotate more naturally
                }
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight,
                        packedOverlay);
            }
        });

    }

    @Override
    public Identifier getTextureLocation(TinyGolem animatable) {
        return getTexture(animatable);
    }
}
