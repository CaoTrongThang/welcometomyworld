package com.trongthang.welcometomyworld.entities.client.Enderchester;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Enderchester;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class EnderchesterModel<T extends Enderchester> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer A_LIVING_ENDER_CHEST = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "a_living_ender_chest"), "main");

    private final ModelPart a_living_chest;
    private final ModelPart all_body;
    private final ModelPart body_fur;
    private final ModelPart back2;
    private final ModelPart front2;
    private final ModelPart left2;
    private final ModelPart right2;
    private final ModelPart tongue;
    private final ModelPart teeths;
    private final ModelPart head;
    private final ModelPart right_horn;
    private final ModelPart left_horn;
    private final ModelPart teeths_head;
    private final ModelPart head_fur;
    private final ModelPart back;
    private final ModelPart front;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart leg_front_right;
    private final ModelPart leg_front_left;
    private final ModelPart leg_back_right;
    private final ModelPart leg_back_left;

    public EnderchesterModel(ModelPart root) {
        this.a_living_chest = root.getChild("a_living_chest");
        this.all_body = a_living_chest.getChild("all_body");
        this.tongue = all_body.getChild("tongue");
        this.teeths = all_body.getChild("teeths");
        this.head = all_body.getChild("head");
        this.right_horn = head.getChild("right_horn");
        this.left_horn = head.getChild("left_horn");
        this.leg_front_right = a_living_chest.getChild("leg_front_right");
        this.leg_front_left = a_living_chest.getChild("leg_front_left");
        this.leg_back_right = a_living_chest.getChild("leg_back_right");
        this.leg_back_left = a_living_chest.getChild("leg_back_left");
        this.teeths_head = head.getChild("teeths_head");

        this.body_fur = all_body.getChild("body_fur");
        this.back2 = body_fur.getChild("back2");
        this.front2 = body_fur.getChild("front2");
        this.left2 = body_fur.getChild("left2");
        this.right2 = body_fur.getChild("right2");
        this.head_fur = head.getChild("head_fur");
        this.back = head_fur.getChild("back");
        this.front = head_fur.getChild("front");
        this.left = head_fur.getChild("left");
        this.right = head_fur.getChild("right");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData a_living_chest = modelPartData.addChild("a_living_chest", ModelPartBuilder.create(), ModelTransform.of(0.0F, 22.75F, 0.0F, 0.0F, -3.098F, 0.0F));

        ModelPartData all_body = a_living_chest.addChild("all_body", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -8.0F, -6.0F, 12.0F, 7.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData body_fur = all_body.addChild("body_fur", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -4.0F, -6.0F));

        ModelPartData back2 = body_fur.addChild("back2", ModelPartBuilder.create(), ModelTransform.pivot(6.0F, -2.25F, -0.75F));

        ModelPartData cube_r1 = back2.addChild("cube_r1", ModelPartBuilder.create().uv(46, 54).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(46, 52).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(36, 52).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(44, 53).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(38, 52).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 52).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(42, 53).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(40, 54).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 53).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(36, 54).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData left2 = body_fur.addChild("left2", ModelPartBuilder.create(), ModelTransform.of(-6.75F, -2.25F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r2 = left2.addChild("cube_r2", ModelPartBuilder.create().uv(40, 59).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(46, 62).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(36, 60).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(44, 61).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(38, 60).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 60).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(42, 61).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(40, 62).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(32, 61).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(36, 62).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData right2 = body_fur.addChild("right2", ModelPartBuilder.create(), ModelTransform.of(6.75F, -2.25F, 12.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData cube_r3 = right2.addChild("cube_r3", ModelPartBuilder.create().uv(54, 59).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(62, 62).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(52, 60).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(60, 61).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(56, 60).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 60).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(58, 61).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(56, 62).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 61).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(52, 62).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData front2 = body_fur.addChild("front2", ModelPartBuilder.create(), ModelTransform.of(-6.0F, -2.25F, 12.75F, 0.0F, 3.1416F, 0.0F));

        ModelPartData cube_r4 = front2.addChild("cube_r4", ModelPartBuilder.create().uv(62, 54).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(62, 52).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(52, 52).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(60, 53).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 56).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 52).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(58, 53).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(56, 54).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 53).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(52, 54).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData tongue = all_body.addChild("tongue", ModelPartBuilder.create().uv(54, 28).cuboid(-2.0F, 0.25F, 0.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(41, 28).cuboid(-2.0F, 1.25F, -2.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(54, 23).cuboid(-2.0F, 0.25F, -1.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(41, 22).cuboid(-2.0F, 0.75F, 1.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(44, 25).cuboid(-2.0F, 1.25F, 2.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -10.0F, 6.0F));

        ModelPartData teeths = all_body.addChild("teeths", ModelPartBuilder.create().uv(48, 4).cuboid(5.0F, -1.0F, -3.0F, 0.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(32, 47).cuboid(5.0F, -2.0F, 0.0F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(36, 47).cuboid(5.0F, -2.0F, 3.0F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(48, 7).cuboid(-5.0F, -1.0F, -3.0F, 0.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(40, 47).cuboid(-5.0F, -2.0F, 0.0F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(48, 0).cuboid(-5.0F, -2.0F, 3.0F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(12, 48).cuboid(-4.0F, -2.0F, 5.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 12).cuboid(2.0F, -2.0F, 5.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -8.0F, 0.0F));

        ModelPartData head = all_body.addChild("head", ModelPartBuilder.create().uv(0, 19).cuboid(-6.0F, -4.0F, 0.0F, 12.0F, 4.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -8.0F, -6.0F, 0.5236F, 0.0F, 0.0F));

        ModelPartData right_horn = head.addChild("right_horn", ModelPartBuilder.create().uv(12, 43).cuboid(-5.0F, -3.0F, 9.0F, 3.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(7.1434F, -2.865F, -3.5497F));

        ModelPartData cube_r5 = right_horn.addChild("cube_r5", ModelPartBuilder.create().uv(48, 14).cuboid(0.5F, -1.5F, -1.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -3.75F, 11.5F, -0.0177F, -0.0192F, -0.2601F));

        ModelPartData cube_r6 = right_horn.addChild("cube_r6", ModelPartBuilder.create().uv(24, 47).cuboid(-0.5F, -2.5F, -1.5F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-4.0F, -2.0F, 11.0F, -0.0185F, -0.0185F, -0.2164F));

        ModelPartData left_horn = head.addChild("left_horn", ModelPartBuilder.create().uv(0, 43).cuboid(-5.0F, -4.0F, 9.0F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.1434F, -2.865F, -3.5497F));

        ModelPartData cube_r7 = left_horn.addChild("cube_r7", ModelPartBuilder.create().uv(44, 45).cuboid(-2.0F, -1.5F, 0.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-2.5F, -4.0F, 9.5F, 0.0174F, -0.0184F, 0.1725F));

        ModelPartData cube_r8 = left_horn.addChild("cube_r8", ModelPartBuilder.create().uv(48, 10).cuboid(-1.0F, -0.5F, 1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-2.75F, -5.75F, 9.0F, 0.0149F, -0.0205F, 0.3034F));

        ModelPartData teeths_head = head.addChild("teeths_head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData head_fur = head.addChild("head_fur", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData back = head_fur.addChild("back", ModelPartBuilder.create(), ModelTransform.pivot(6.0F, -2.25F, -0.75F));

        ModelPartData cube_r9 = back.addChild("cube_r9", ModelPartBuilder.create().uv(12, 52).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(10, 54).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(16, 52).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 52).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(10, 52).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(2, 54).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(8, 52).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 54).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(4, 52).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 52).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData right = head_fur.addChild("right", ModelPartBuilder.create(), ModelTransform.of(6.75F, -2.25F, 12.0F, 0.0F, -1.5708F, 0.0F));

        ModelPartData cube_r10 = right.addChild("cube_r10", ModelPartBuilder.create().uv(30, 54).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 53).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(28, 55).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(26, 54).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 54).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 56).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 52).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 55).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(18, 52).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(18, 55).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData left = head_fur.addChild("left", ModelPartBuilder.create(), ModelTransform.of(-6.75F, -2.25F, 0.0F, 0.0F, 1.5708F, 0.0F));

        ModelPartData cube_r11 = left.addChild("cube_r11", ModelPartBuilder.create().uv(26, 61).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 60).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 62).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(22, 61).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(20, 61).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(20, 63).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(18, 59).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(18, 62).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 59).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(14, 62).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData front = head_fur.addChild("front", ModelPartBuilder.create(), ModelTransform.of(-6.0F, -2.25F, 12.75F, 0.0F, 3.1416F, 0.0F));

        ModelPartData cube_r12 = front.addChild("cube_r12", ModelPartBuilder.create().uv(12, 61).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(10, 60).cuboid(-2.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(10, 62).cuboid(-3.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(8, 61).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(6, 61).cuboid(-5.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(6, 63).cuboid(-6.0F, -2.0F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.0F))
                .uv(4, 59).cuboid(-7.0F, -2.0F, 0.0F, 1.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(4, 62).cuboid(-8.0F, -2.0F, 0.0F, 1.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 59).cuboid(-10.0F, -2.0F, 0.0F, 2.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(0, 62).cuboid(-12.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        ModelPartData leg_front_right = a_living_chest.addChild("leg_front_right", ModelPartBuilder.create(), ModelTransform.pivot(6.0F, -2.0F, 4.0F));

        ModelPartData cube_r13 = leg_front_right.addChild("cube_r13", ModelPartBuilder.create().uv(0, 35).cuboid(0.0F, -3.0F, -2.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-0.75F, 1.5F, 0.0F, 0.0658F, -0.0218F, -0.0813F));

        ModelPartData leg_front_left = a_living_chest.addChild("leg_front_left", ModelPartBuilder.create(), ModelTransform.pivot(-6.0F, -2.0F, 4.0F));

        ModelPartData cube_r14 = leg_front_left.addChild("cube_r14", ModelPartBuilder.create().uv(12, 35).cuboid(-2.0F, -2.0F, -1.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-0.25F, 0.25F, 0.0F, 0.0344F, 0.0317F, 0.1205F));

        ModelPartData leg_back_right = a_living_chest.addChild("leg_back_right", ModelPartBuilder.create(), ModelTransform.pivot(6.0F, -2.0F, -4.0F));

        ModelPartData cube_r15 = leg_back_right.addChild("cube_r15", ModelPartBuilder.create().uv(24, 35).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.25F, 0.0F, -0.0855F, -0.0173F, -0.17F));

        ModelPartData leg_back_left = a_living_chest.addChild("leg_back_left", ModelPartBuilder.create(), ModelTransform.pivot(-6.0F, -2.0F, -4.0F));

        ModelPartData cube_r16 = leg_back_left.addChild("cube_r16", ModelPartBuilder.create().uv(36, 35).cuboid(-3.0F, -2.0F, -1.0F, 3.0F, 5.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, 0.25F, 0.0F, -0.0399F, 0.0089F, 0.146F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return a_living_chest;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);


        this.updateAnimation(entity.walkAnimationState, EnderchesterAnimations.WALK, animationProgress, 1f);
        this.updateAnimation(entity.idleAnimationState, EnderchesterAnimations.IDLE, animationProgress, 1f);
        this.updateAnimation(entity.sitAnimationState, EnderchesterAnimations.SIT, animationProgress, 1f);
        this.updateAnimation(entity.mouthOpenAnimationState, EnderchesterAnimations.MOUTH_OPEN, animationProgress, 1f);
        this.updateAnimation(entity.mouthCloseAnimationState, EnderchesterAnimations.MOUTH_CLOSE, animationProgress, 1f);
        this.updateAnimation(entity.eatAnimationState, EnderchesterAnimations.EAT, animationProgress, 1f);
        this.updateAnimation(entity.jumpAnimationState, EnderchesterAnimations.JUMP, animationProgress, 1f);
        this.updateAnimation(entity.sleepAnimationState, EnderchesterAnimations.SLEEP, animationProgress, 1f);
        this.updateAnimation(entity.attackAnimationState, EnderchesterAnimations.ATTACK, animationProgress, 1f);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.all_body.yaw = headYaw * 0.017453292F;
        this.all_body.pitch = headPitch * 0.017453292F;
    }

}
