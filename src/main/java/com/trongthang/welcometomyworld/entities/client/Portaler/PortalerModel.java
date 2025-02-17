package com.trongthang.welcometomyworld.entities.client.Portaler;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Portaler;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class PortalerModel<T extends Portaler> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer PORTALER = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "portaler"), "main");
    private final ModelPart portaler;
    private final ModelPart leg_left;
    private final ModelPart leg_right;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart right_hand;
    private final ModelPart left_hand;
    private final ModelPart left_side;
    private final ModelPart middle_top;
    private final ModelPart right_side;
    private final ModelPart middle_down;
    private final ModelPart portal_fame;
    public PortalerModel(ModelPart root) {
        this.portaler = root.getChild("portaler");
        this.leg_left = portaler.getChild("leg_left");
        this.leg_right = portaler.getChild("leg_right");
        this.body = portaler.getChild("body");
        this.head = body.getChild("head");
        this.right_hand = body.getChild("right_hand");
        this.left_hand = body.getChild("left_hand");
        this.left_side = body.getChild("left_side");
        this.middle_top = body.getChild("middle_top");
        this.right_side = body.getChild("right_side");
        this.middle_down = body.getChild("middle_down");
        this.portal_fame = body.getChild("portal_fame");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData portaler = modelPartData.addChild("portaler", ModelPartBuilder.create(), ModelTransform.pivot(2.1129F, 22.0F, -0.5F));

        ModelPartData leg_left = portaler.addChild("leg_left", ModelPartBuilder.create().uv(104, 96).cuboid(0.3629F, -1.0F, -4.5F, 8.0F, 16.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(4.8871F, -13.0F, 1.5F));

        ModelPartData leg_right = portaler.addChild("leg_right", ModelPartBuilder.create().uv(104, 120).cuboid(-8.0F, -1.0F, 0.0F, 8.0F, 16.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(-8.25F, -13.0F, -3.0F));

        ModelPartData body = portaler.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(-2.0F, -35.0F, 0.0F));

        ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(104, 74).cuboid(-11.8871F, -12.0F, -3.5F, 25.0F, 12.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(-0.1129F, -39.0F, 0.0F));

        ModelPartData right_hand = body.addChild("right_hand", ModelPartBuilder.create().uv(29, 138).cuboid(-6.0F, 0.0F, -3.0F, 6.0F, 15.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-24.0F, -28.0F, 2.0F, 0.0F, 0.0F, 0.3927F));

        ModelPartData left_hand = body.addChild("left_hand", ModelPartBuilder.create().uv(0, 122).cuboid(1.0F, 1.0F, -1.0F, 6.0F, 15.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(22.75F, -29.0F, -1.0F, 0.0F, 0.0F, -0.3491F));

        ModelPartData left_side = body.addChild("left_side", ModelPartBuilder.create().uv(52, 53).cuboid(13.0F, -30.0F, -8.0F, 8.0F, 51.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, 0.0F, 0.0F));

        ModelPartData middle_top = body.addChild("middle_top", ModelPartBuilder.create().uv(0, 0).cuboid(-27.0F, -35.0F, -8.0F, 48.0F, 9.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, -4.0F, 0.0F));

        ModelPartData right_side = body.addChild("right_side", ModelPartBuilder.create().uv(0, 187).cuboid(-21.0F, -30.0F, -8.0F, 8.0F, 51.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 0.0F, 0.0F));

        ModelPartData middle_down = body.addChild("middle_down", ModelPartBuilder.create().uv(0, 27).cuboid(-13.0F, 9.0F, -8.0F, 32.0F, 8.0F, 18.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 4.0F, 0.0F));

        ModelPartData portal_fame = body.addChild("portal_fame", ModelPartBuilder.create().uv(105, 209).cuboid(-18.0F, -34.0F, -3.0F, 32.0F, 43.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, 4.0F, 0.0F));
        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public ModelPart getPart() {
        return portaler;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);


        this.updateAnimation(entity.walkAnimationState, PortalerAnimations.WALK, animationProgress, 1f);
        this.updateAnimation(entity.idleAnimationState, PortalerAnimations.IDLE, animationProgress, 1f);
        this.updateAnimation(entity.switchingPortalAnimationState, PortalerAnimations.TURN_TO_ANOTHER_PORTAL, animationProgress, 1f);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }

}
