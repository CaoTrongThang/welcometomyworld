package com.trongthang.welcometomyworld.entities.client.AncientWhale;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.AncientWhale;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AncientWhaleModel<T extends AncientWhale> extends SinglePartEntityModel<T> {
    public static final EntityModelLayer ANCIENT_WHALE = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "ancient_whale"), "main");


    private final ModelPart ancient_whale;
    private final ModelPart wing_right;
    private final ModelPart wing_left;
    private final ModelPart head;
    private final ModelPart jaw2;
    private final ModelPart lower_jaw;
    private final ModelPart fill_right;
    private final ModelPart fill_left;
    private final ModelPart jaw;
    private final ModelPart middle_body_near_tail;
    private final ModelPart middle_body_near_head;
    private final ModelPart body_near_tail;
    private final ModelPart tail_joint1;
    private final ModelPart tail_joint2;
    private final ModelPart tail;
    public AncientWhaleModel(ModelPart root) {
        this.ancient_whale = root.getChild("ancient_whale");
        this.wing_right = this.ancient_whale.getChild("wing_right");
        this.wing_left = this.ancient_whale.getChild("wing_left");
        this.head = this.ancient_whale.getChild("head");
        this.jaw2 = this.head.getChild("jaw2");
        this.lower_jaw = this.head.getChild("lower_jaw");
        this.fill_right = this.lower_jaw.getChild("fill_right");
        this.fill_left = this.lower_jaw.getChild("fill_left");
        this.jaw = this.lower_jaw.getChild("jaw");
        this.middle_body_near_tail = this.ancient_whale.getChild("middle_body_near_tail");
        this.middle_body_near_head = this.ancient_whale.getChild("middle_body_near_head");
        this.body_near_tail = this.ancient_whale.getChild("body_near_tail");
        this.tail_joint1 = this.ancient_whale.getChild("tail_joint1");
        this.tail_joint2 = this.ancient_whale.getChild("tail_joint2");
        this.tail = this.ancient_whale.getChild("tail");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData ancient_whale = modelPartData.addChild("ancient_whale", ModelPartBuilder.create(), ModelTransform.pivot(-6.0F, 24.0F, -2.0F));

        ModelPartData wing_right = ancient_whale.addChild("wing_right", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData wing_right_r1 = wing_right.addChild("wing_right_r1", ModelPartBuilder.create().uv(28, 3).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        ModelPartData wing_left = ancient_whale.addChild("wing_left", ModelPartBuilder.create(), ModelTransform.pivot(11.0F, 0.0F, -1.0F));

        ModelPartData wing_left_r1 = wing_left.addChild("wing_left_r1", ModelPartBuilder.create().uv(28, 11).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        ModelPartData head = ancient_whale.addChild("head", ModelPartBuilder.create().uv(26, 26).cuboid(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 0.0F, -10.0F));

        ModelPartData jaw2 = head.addChild("jaw2", ModelPartBuilder.create().uv(42, 38).cuboid(-3.0F, -3.0F, 5.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(42, 41).cuboid(-3.0F, -3.0F, 3.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(44, 19).cuboid(-3.0F, -3.0F, 1.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(44, 22).cuboid(-3.0F, -2.0F, -1.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -2.0F, -8.0F));

        ModelPartData lower_jaw = head.addChild("lower_jaw", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -1.0F, -8.0F));

        ModelPartData fill_right = lower_jaw.addChild("fill_right", ModelPartBuilder.create().uv(16, 43).cuboid(1.0F, -2.0F, 1.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(4, 45).cuboid(1.0F, -3.0F, 3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(20, 43).cuboid(1.0F, -2.0F, 3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(8, 45).cuboid(1.0F, -3.0F, 1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(12, 45).cuboid(1.0F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(24, 46).cuboid(1.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.0F, 0.0F, 2.0F));

        ModelPartData fill_left = lower_jaw.addChild("fill_left", ModelPartBuilder.create().uv(-1, 45).cuboid(0.0F, -2.0F, 1.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(45, 44).cuboid(0.0F, -3.0F, 3.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(41, 44).cuboid(0.0F, -2.0F, 3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(35, 46).cuboid(0.0F, -3.0F, 1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(27, 46).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(31, 46).cuboid(0.0F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, 0.0F, 2.0F));

        ModelPartData jaw = lower_jaw.addChild("jaw", ModelPartBuilder.create().uv(28, 19).cuboid(-3.0F, 0.0F, 5.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(28, 22).cuboid(-3.0F, 0.0F, 3.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 39).cuboid(-3.0F, -1.0F, 1.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 42).cuboid(-3.0F, -2.0F, -1.0F, 6.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData middle_body_near_tail = ancient_whale.addChild("middle_body_near_tail", ModelPartBuilder.create().uv(0, 2).cuboid(-4.0F, -7.0F, -1.0F, 8.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 0.0F, 2.0F));

        ModelPartData middle_body_near_head = ancient_whale.addChild("middle_body_near_head", ModelPartBuilder.create().uv(0, 13).cuboid(-4.0F, -7.0F, -1.0F, 8.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 0.0F, -4.0F));

        ModelPartData body_near_tail = ancient_whale.addChild("body_near_tail", ModelPartBuilder.create().uv(0, 27).cuboid(-3.0F, -6.0F, -4.0F, 6.0F, 6.0F, 7.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 0.0F, 11.0F));

        ModelPartData tail_joint1 = ancient_whale.addChild("tail_joint1", ModelPartBuilder.create().uv(26, 38).cuboid(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, -1.0F, 15.0F));

        ModelPartData tail_joint2 = ancient_whale.addChild("tail_joint2", ModelPartBuilder.create().uv(16, 39).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, -1.0F, 19.0F));

        ModelPartData tail = ancient_whale.addChild("tail", ModelPartBuilder.create().uv(28, 0).cuboid(-6.0F, -2.0F, -1.0F, 11.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 0.0F, 21.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        ancient_whale.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return ancient_whale;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);


        if(limbAngle >= 0.1){
            this.updateAnimation(entity.idleAnimationState, AncientWhaleAnimations.ANCIENT_WHALE_WALK, animationProgress, 1f);
        }
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }

}
