package com.trongthang.welcometomyworld.entities.client.FallenKnight;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.FallenKnight;
import com.trongthang.welcometomyworld.entities.Portaler;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class FallenKnightModel<T extends FallenKnight> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer FALLEN_KNIGHT = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "fallen_knight"), "main");

    public final ModelPart fallen_knight;
    private final ModelPart body;
    private final ModelPart middle_body;
    private final ModelPart dress_front;
    private final ModelPart cape;
    private final ModelPart left_hand;
    private final ModelPart bone2;
    private final ModelPart right_hand_parent;
    private final ModelPart hammer;
    private final ModelPart right_hand;
    private final ModelPart bone;
    private final ModelPart head;
    private final ModelPart helmet;
    private final ModelPart top;
    private final ModelPart down;
    private final ModelPart left_leg;
    private final ModelPart left_leg_foot;
    private final ModelPart right_leg;
    private final ModelPart right_leg_foot;
    private final ModelPart portal;
    private final ModelPart frames;

    public FallenKnightModel(ModelPart root) {
        this.fallen_knight = root.getChild("fallen_knight");
        this.body = fallen_knight.getChild("body");
        this.middle_body = body.getChild("middle_body");
        this.dress_front = middle_body.getChild("dress_front");
        this.cape = middle_body.getChild("cape");
        this.left_hand = middle_body.getChild("left_hand");
        this.bone2 = left_hand.getChild("bone2");
        this.right_hand_parent = middle_body.getChild("right_hand_parent");
        this.hammer = right_hand_parent.getChild("hammer");
        this.right_hand = right_hand_parent.getChild("right_hand");
        this.bone = right_hand.getChild("bone");
        this.head = middle_body.getChild("head");
        this.helmet = head.getChild("helmet");
        this.top = helmet.getChild("top");
        this.down = helmet.getChild("down");
        this.left_leg = body.getChild("left_leg");
        this.left_leg_foot = left_leg.getChild("left_leg_foot");
        this.right_leg = body.getChild("right_leg");
        this.right_leg_foot = right_leg.getChild("right_leg_foot");
        this.portal = fallen_knight.getChild("portal");
        this.frames = portal.getChild("frames");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData fallen_knight = modelPartData.addChild("fallen_knight", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 23.0F, 0.0F));

        ModelPartData body = fallen_knight.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 1.0F, 0.0F));

        ModelPartData middle_body = body.addChild("middle_body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -4.0F, 0.0F, 8.0F, 12.0F, 5.0F, new Dilation(0.0F))
                .uv(16, 26).cuboid(-4.0F, -0.5F, 3.5F, 8.0F, 8.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -17.0F, 0.0F));

        ModelPartData cube_r1 = middle_body.addChild("cube_r1", ModelPartBuilder.create().uv(48, 0).cuboid(-4.0F, -2.0F, -1.0F, 8.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.0F, 0.75F, 0.1745F, 0.0F, 0.0F));

        ModelPartData cube_r2 = middle_body.addChild("cube_r2", ModelPartBuilder.create().uv(0, 59).cuboid(-1.0F, -2.0F, -3.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(4.75F, 5.0F, 3.0F, 0.0F, 0.0F, -0.2618F));

        ModelPartData cube_r3 = middle_body.addChild("cube_r3", ModelPartBuilder.create().uv(16, 56).cuboid(0.0F, -2.0F, -3.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(-4.75F, 5.0F, 3.0F, 0.0F, 0.0F, 0.2618F));

        ModelPartData cube_r4 = middle_body.addChild("cube_r4", ModelPartBuilder.create().uv(8, 45).cuboid(1.0F, -2.0F, -2.0F, 0.0F, 10.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-5.25F, 5.5F, 2.75F, 0.0F, 0.0F, 0.0873F));

        ModelPartData cube_r5 = middle_body.addChild("cube_r5", ModelPartBuilder.create().uv(0, 45).cuboid(-1.0F, -2.0F, -2.0F, 0.0F, 10.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(5.25F, 5.5F, 2.75F, 0.0F, 0.0F, -0.0873F));

        ModelPartData cube_r6 = middle_body.addChild("cube_r6", ModelPartBuilder.create().uv(40, 60).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 6.0F, -0.5F, 0.0F, 0.0F, 0.7854F));

        ModelPartData cube_r7 = middle_body.addChild("cube_r7", ModelPartBuilder.create().uv(25, 0).cuboid(-5.0F, -2.0F, -2.0F, 9.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, -1.0F, 0.75F, 0.3054F, 0.0F, 0.0F));

        ModelPartData dress_front = middle_body.addChild("dress_front", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 5.0F, 0.75F));

        ModelPartData cube_r8 = dress_front.addChild("cube_r8", ModelPartBuilder.create().uv(16, 47).cuboid(-3.0F, -2.0F, 0.0F, 6.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 2.0F, -1.0F, -0.1309F, 0.0F, 0.0F));

        ModelPartData cape = middle_body.addChild("cape", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -4.0F, 4.0F));

        ModelPartData cube_r9 = cape.addChild("cube_r9", ModelPartBuilder.create().uv(0, 26).cuboid(-4.0F, -2.0F, -1.0F, 8.0F, 19.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 2.0F, 2.5F, 0.0436F, 0.0F, 0.0F));

        ModelPartData left_hand = middle_body.addChild("left_hand", ModelPartBuilder.create().uv(32, 46).cuboid(0.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F))
                .uv(48, 3).cuboid(-0.25F, 2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, -4.0F, 2.5F));

        ModelPartData bone2 = left_hand.addChild("bone2", ModelPartBuilder.create().uv(32, 37).cuboid(0.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F))
                .uv(28, 21).cuboid(0.25F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-0.25F, 5.0F, 0.0F));

        ModelPartData right_hand_parent = middle_body.addChild("right_hand_parent", ModelPartBuilder.create(), ModelTransform.pivot(-4.0F, -4.0F, 2.5F));

        ModelPartData hammer = right_hand_parent.addChild("hammer", ModelPartBuilder.create().uv(25, 116).cuboid(-3.0F, -26.0F, -7.0F, 6.0F, 7.0F, 5.0F, new Dilation(0.0F))
                .uv(49, 116).cuboid(-3.0F, -26.0F, 2.0F, 6.0F, 7.0F, 5.0F, new Dilation(0.0F))
                .uv(0, 122).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(73, 119).cuboid(-2.0F, -25.0F, -2.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F))
                .uv(15, 100).cuboid(-1.0F, -26.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-13.0F, 21.0F, -2.5F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r10 = hammer.addChild("cube_r10", ModelPartBuilder.create().uv(87, 125).cuboid(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -21.25F, 1.25F, 0.1309F, 0.0F, 0.0F));

        ModelPartData cube_r11 = hammer.addChild("cube_r11", ModelPartBuilder.create().uv(88, 122).cuboid(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -23.0F, -0.75F, -0.3054F, 0.0F, 0.0F));

        ModelPartData right_hand = right_hand_parent.addChild("right_hand", ModelPartBuilder.create().uv(45, 46).cuboid(-4.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F))
                .uv(52, 32).cuboid(-2.75F, 2.0F, -1.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData bone = right_hand.addChild("bone", ModelPartBuilder.create().uv(36, 28).cuboid(-4.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F))
                .uv(28, 17).cuboid(-2.25F, 4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.25F, 5.0F, 0.0F));

        ModelPartData head = middle_body.addChild("head", ModelPartBuilder.create().uv(26, 8).cuboid(-2.5F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -4.0F, 2.0F));

        ModelPartData helmet = head.addChild("helmet", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -1.0F, 2.5F));

        ModelPartData top = helmet.addChild("top", ModelPartBuilder.create().uv(0, 17).cuboid(-3.5F, -5.0F, -5.5F, 7.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(16, 36).cuboid(-3.5F, -3.0F, -5.5F, 1.0F, 4.0F, 7.0F, new Dilation(0.0F))
                .uv(36, 17).cuboid(2.5F, -3.0F, -5.5F, 1.0F, 4.0F, 7.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData down = helmet.addChild("down", ModelPartBuilder.create().uv(46, 8).cuboid(-6.5F, -3.0F, -5.0F, 8.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(46, 12).cuboid(-6.5F, -3.0F, 1.0F, 8.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(52, 24).cuboid(-6.5F, -3.0F, -4.0F, 1.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(28, 53).cuboid(0.5F, -3.0F, -4.0F, 1.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(2.5F, 0.75F, -0.5F, 0.1745F, 0.0F, 0.0F));

        ModelPartData left_leg = body.addChild("left_leg", ModelPartBuilder.create().uv(48, 37).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(2.5F, -9.0F, 1.0F));

        ModelPartData cube_r12 = left_leg.addChild("cube_r12", ModelPartBuilder.create().uv(60, 37).cuboid(-1.0F, -2.0F, 0.0F, 3.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 5.75F, -0.25F, 0.2618F, 0.0F, 0.0F));

        ModelPartData left_leg_foot = left_leg.addChild("left_leg_foot", ModelPartBuilder.create().uv(40, 53).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 4.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 5.0F, 0.0F));

        ModelPartData right_leg = body.addChild("right_leg", ModelPartBuilder.create().uv(52, 16).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.5F, -9.0F, 1.0F));

        ModelPartData cube_r13 = right_leg.addChild("cube_r13", ModelPartBuilder.create().uv(58, 3).cuboid(-2.0F, -2.0F, 0.0F, 3.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, 5.75F, -0.25F, 0.2618F, 0.0F, 0.0F));

        ModelPartData right_leg_foot = right_leg.addChild("right_leg_foot", ModelPartBuilder.create().uv(52, 53).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 4.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 5.0F, 0.0F));

        ModelPartData portal = fallen_knight.addChild("portal", ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, -18.0F, -20.0F));

        ModelPartData frames = portal.addChild("frames", ModelPartBuilder.create().uv(44, 52).cuboid(0.0F, -22.0F, -19.0F, 21.0F, 39.0F, 21.0F, new Dilation(0.0F))
                .uv(44, 54).cuboid(-21.0F, -22.0F, -19.0F, 21.0F, 39.0F, 21.0F, new Dilation(0.0F)), ModelTransform.pivot(1.0F, 2.0F, 9.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public ModelPart getPart() {
        return fallen_knight;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);

        this.updateAnimation(entity.walkAnimationState, FallenKnightAnimations.WALK, animationProgress, 1f);
        this.updateAnimation(entity.idleAnimationState, FallenKnightAnimations.IDLE, animationProgress, 1f);
        this.updateAnimation(entity.attackAnimationState, FallenKnightAnimations.ATTACK, animationProgress, 1f);
        this.updateAnimation(entity.attack2AnimationState, FallenKnightAnimations.ATTACK2, animationProgress, 1f);
        this.updateAnimation(entity.attack3AnimationState, FallenKnightAnimations.ATTACK3, animationProgress, 1f);
        this.updateAnimation(entity.tameableAnimationState, FallenKnightAnimations.TAMEABLE, animationProgress, 1f);
        this.updateAnimation(entity.sitAnimationState, FallenKnightAnimations.SIT, animationProgress, 1f);
        this.updateAnimation(entity.teleportAnimationState, FallenKnightAnimations.TELEPORT, animationProgress, 1f);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }

}
