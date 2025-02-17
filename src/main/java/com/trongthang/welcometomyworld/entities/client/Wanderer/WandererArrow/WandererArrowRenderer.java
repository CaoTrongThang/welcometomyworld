package com.trongthang.welcometomyworld.entities.client.Wanderer.WandererArrow;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.WandererArrow;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class WandererArrowRenderer extends EntityRenderer<WandererArrow> {

    private static final Identifier TEXTURE = new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/wanderer_arrow.png");
    private final WandererArrowModel model;

    public WandererArrowRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new WandererArrowModel(context.getPart(WandererArrowModel.WANDERER_ARROW));
    }


    @Override
    public void render(WandererArrow entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Calculate interpolated yaw and pitch for smooth rotation
        float interpolatedYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F;
        float interpolatedPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        // Apply rotations to align the arrow with its direction
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(interpolatedYaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(interpolatedPitch));

        // Adjust scale and position of the model
        matrices.scale(4F, -1.0F, -1.0F);
        matrices.translate(-0.3F, -1.5F, 0.0F);

        // Render the model
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }


    @Override
    public Identifier getTexture(WandererArrow entity) {
        return TEXTURE; // Path to your texture file
    }
}
