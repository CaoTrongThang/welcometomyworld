package com.trongthang.welcometomyworld.entities.client.Wanderer.WandererArrow;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Wanderer;
import com.trongthang.welcometomyworld.entities.WandererArrow;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererAnimations;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class WandererArrowModel extends EntityModel<WandererArrow> {

    public static final EntityModelLayer WANDERER_ARROW = new EntityModelLayer(new Identifier(WelcomeToMyWorld.MOD_ID, "wanderer_arrow"), "main");

    private final ModelPart wanderer_arrow;

    public WandererArrowModel(ModelPart root) {
        this.wanderer_arrow = root.getChild("wanderer_arrow");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData wanderer_arrow = modelPartData.addChild("wanderer_arrow", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5F, -1.0F, -7.75F, 1.0F, 1.0F, 15.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData cube_r1 = wanderer_arrow.addChild("cube_r1", ModelPartBuilder.create().uv(2, 0).cuboid(0.8282F, -0.6363F, 5.25F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).mirrored().cuboid(0.1517F, -2.2696F, 5.25F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        ModelPartData cube_r2 = wanderer_arrow.addChild("cube_r2", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-0.8282F, -0.6363F, 5.25F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F)).mirrored(false)
                .uv(2, 0).cuboid(-0.1327F, -2.1739F, 5.25F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        ModelPartData cube_r3 = wanderer_arrow.addChild("cube_r3", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(2.6517F, -1.0F, -5.2301F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData cube_r4 = wanderer_arrow.addChild("cube_r4", ModelPartBuilder.create().uv(1, 1).cuboid(-4.6517F, -1.0F, -5.2301F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData cube_r5 = wanderer_arrow.addChild("cube_r5", ModelPartBuilder.create().uv(1, 1).cuboid(-0.5F, -4.6088F, -4.1694F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -1.25F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r6 = wanderer_arrow.addChild("cube_r6", ModelPartBuilder.create().uv(1, 1).cuboid(-0.5F, 2.4623F, -5.9372F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public void setAngles(WandererArrow entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        wanderer_arrow.render(matrices, vertices, light, overlay);
    }
}
