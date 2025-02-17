package com.trongthang.welcometomyworld.entities.client.EnderPest;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.EnderPest;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;

public class EnderPestModel<T extends EnderPest> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer ENDER_PEST = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "ender_pest"), "main");

    private final ModelPart the_end_chest;
    private final ModelPart body;
    private final ModelPart body_inside_glow;
    private final ModelPart body_hinge;
    private final ModelPart eyes;
    private final ModelPart head;
    private final ModelPart cap;
    private final ModelPart cap_eye;
    private final ModelPart decro;
    private final ModelPart front;
    private final ModelPart keyhole;
    private final ModelPart front_dec;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart head_hinge;

    public EnderPestModel(ModelPart root) {
        this.the_end_chest = root.getChild("the_end_chest");
        this.body = the_end_chest.getChild("body");
        this.body_inside_glow = body.getChild("body_inside_glow");
        this.body_hinge = body.getChild("body_hinge");
        this.eyes = body.getChild("eyes");
        this.head = body.getChild("head");
        this.cap = head.getChild("cap");
        this.cap_eye = cap.getChild("cap_eye");
        this.decro = head.getChild("decro");
        this.front = decro.getChild("front");
        this.keyhole = front.getChild("keyhole");
        this.front_dec = front.getChild("front_dec");
        this.left = decro.getChild("left");
        this.right = decro.getChild("right");
        this.head_hinge = head.getChild("head_hinge");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData the_end_chest = modelPartData.addChild("the_end_chest", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData body = the_end_chest.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-16.0F, -13.0F, -8.0F, 16.0F, 13.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(8.0F, 0.0F, 0.0F));

        ModelPartData body_inside_glow = body.addChild("body_inside_glow", ModelPartBuilder.create().uv(0, 112).cuboid(-1.0F, -15.0F, -7.0F, 0.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(4, 124).cuboid(-15.0F, -15.0F, -7.0F, 14.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 108).cuboid(-15.0F, -15.0F, -7.0F, 0.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 120).cuboid(-15.0F, -15.0F, 7.0F, 14.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData body_hinge = body.addChild("body_hinge", ModelPartBuilder.create().uv(0, 30).cuboid(0.0F, -13.0F, 3.0F, 1.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 38).cuboid(0.0F, -13.0F, -5.0F, 1.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData eyes = body.addChild("eyes", ModelPartBuilder.create().uv(64, 0).cuboid(2.0F, -4.0F, -3.0F, 1.0F, 6.0F, 6.0F, new Dilation(0.0F))
                .uv(64, 20).cuboid(1.0F, -3.0F, -2.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-19.0F, -5.0F, 0.0F));

        ModelPartData head = body.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, -14.0F, 0.0F));

        ModelPartData cap = head.addChild("cap", ModelPartBuilder.create().uv(0, 29).cuboid(-15.0F, -4.0F, -8.0F, 16.0F, 4.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 1.0F, 0.0F));

        ModelPartData cap_eye = cap.addChild("cap_eye", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-6.0F, -4.0F, 0.0F));

        ModelPartData decro = head.addChild("decro", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData front = decro.addChild("front", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData keyhole = front.addChild("keyhole", ModelPartBuilder.create().uv(64, 12).cuboid(-1.0F, -3.0F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(64, 40).cuboid(-1.0F, 1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-16.0F, 0.0F, 0.0F));

        ModelPartData front_dec = front.addChild("front_dec", ModelPartBuilder.create(), ModelTransform.pivot(-15.25F, -2.0F, 0.0F));

        ModelPartData left = decro.addChild("left", ModelPartBuilder.create().uv(7, 66).cuboid(3.25F, 0.0F, 0.0F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F))
                .uv(16, 66).cuboid(-5.75F, 0.0F, 0.0F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-7.25F, -3.0F, -8.25F));

        ModelPartData right = decro.addChild("right", ModelPartBuilder.create().uv(7, 76).cuboid(3.0F, 0.0F, 0.25F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F))
                .uv(16, 76).cuboid(-6.0F, 0.0F, 0.25F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-7.0F, -3.0F, 8.0F));

        ModelPartData head_hinge = head.addChild("head_hinge", ModelPartBuilder.create().uv(9, 30).cuboid(0.0F, -15.0F, -5.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(9, 38).cuboid(0.0F, -15.0F, 3.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(1.0F, 14.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public ModelPart getPart() {
        return the_end_chest;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);

        this.updateAnimation(entity.disappearAnimationState, EnderPestAnimations.DISAPPEAR, animationProgress, 1f);
        this.updateAnimation(entity.idleAnimationState, EnderPestAnimations.IDLE, animationProgress, 1f);
        this.updateAnimation(entity.mouthOpenAnimationState, EnderPestAnimations.MOUTH_OPEN, animationProgress, 1f);
        this.updateAnimation(entity.scamAnimationState, EnderPestAnimations.SCAM, animationProgress, 1f);
        this.updateAnimation(entity.eatItemsAnimationState, EnderPestAnimations.EAT_ITEMS, animationProgress, 1f);

    }
}
