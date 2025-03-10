package com.trongthang.welcometomyworld.entities.client.Wanderer;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Wanderer;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class WandererModel<T extends Wanderer> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer WANDERER = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "wanderer"), "main");

    private final ModelPart wanderer;
    private final ModelPart body_parent;
    private final ModelPart shirt;
    private final ModelPart neckless;
    private final ModelPart head;
    private final ModelPart hair;
    private final ModelPart scarf;
    private final ModelPart scarf_head;
    private final ModelPart belt;
    private final ModelPart belt_small_bag;
    private final ModelPart belt_small_bag_eye;
    private final ModelPart belt_water_bottle;
    private final ModelPart cloak_top;
    private final ModelPart right_hand;
    private final ModelPart right_hand_2;
    private final ModelPart right_hand_2_gaunt;
    private final ModelPart bow;
    private final ModelPart bowstring_2;
    private final ModelPart bowstring_1;
    private final ModelPart left_hand;
    private final ModelPart left_hand_2;
    private final ModelPart left_hand_2_gaunt;
    private final ModelPart hip_down;
    private final ModelPart cloak_down;
    private final ModelPart cloak_down_left;
    private final ModelPart cloak_down_right;
    private final ModelPart cloak_down_back;
    private final ModelPart cloak_down_front_right;
    private final ModelPart cloak_down_front_left;
    private final ModelPart right_leg;
    private final ModelPart right_leg_down;
    private final ModelPart left_leg;
    private final ModelPart left_leg_down;
    private final ModelPart sword;
    public WandererModel(ModelPart root) {
        this.wanderer = root.getChild("wanderer");
        this.body_parent = wanderer.getChild("body_parent");
        this.shirt = body_parent.getChild("shirt");
        this.neckless = body_parent.getChild("neckless");
        this.head = body_parent.getChild("head");
        this.hair = head.getChild("hair");
        this.scarf = head.getChild("scarf");
        this.scarf_head = scarf.getChild("scarf_head");
        this.belt = body_parent.getChild("belt");
        this.belt_small_bag = belt.getChild("belt_small_bag");
        this.belt_small_bag_eye = belt_small_bag.getChild("belt_small_bag_eye");
        this.belt_water_bottle = belt.getChild("belt_water_bottle");
        this.cloak_top = body_parent.getChild("cloak_top");
        this.right_hand = body_parent.getChild("right_hand");
        this.right_hand_2 = right_hand.getChild("right_hand_2");
        this.right_hand_2_gaunt = right_hand_2.getChild("right_hand_2_gaunt");
        this.bow = body_parent.getChild("bow");
        this.bowstring_2 = bow.getChild("bowstring_2");
        this.bowstring_1 = bow.getChild("bowstring_1");
        this.left_hand = body_parent.getChild("left_hand");
        this.left_hand_2 = left_hand.getChild("left_hand_2");
        this.left_hand_2_gaunt = left_hand_2.getChild("left_hand_2_gaunt");
        this.hip_down = wanderer.getChild("hip_down");
        this.cloak_down = hip_down.getChild("cloak_down");
        this.cloak_down_left = cloak_down.getChild("cloak_down_left");
        this.cloak_down_right = cloak_down.getChild("cloak_down_right");
        this.cloak_down_back = cloak_down.getChild("cloak_down_back");
        this.cloak_down_front_right = cloak_down.getChild("cloak_down_front_right");
        this.cloak_down_front_left = cloak_down.getChild("cloak_down_front_left");
        this.right_leg = hip_down.getChild("right_leg");
        this.right_leg_down = right_leg.getChild("right_leg_down");
        this.left_leg = hip_down.getChild("left_leg");
        this.left_leg_down = left_leg.getChild("left_leg_down");
        this.sword = hip_down.getChild("sword");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData wanderer = modelPartData.addChild("wanderer", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData body_parent = wanderer.addChild("body_parent", ModelPartBuilder.create().uv(0, 20).cuboid(-5.0F, -2.0F, -2.0F, 10.0F, 13.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -26.0F, 0.0F));

        ModelPartData shirt = body_parent.addChild("shirt", ModelPartBuilder.create().uv(1, 2).cuboid(-5.25F, -2.0F, -2.75F, 4.0F, 13.0F, 0.0F, new Dilation(0.0F))
                .uv(1, 2).cuboid(1.25F, -2.0F, -2.75F, 4.0F, 13.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData neckless = body_parent.addChild("neckless", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 2.0F, -1.25F));

        ModelPartData cube_r1 = neckless.addChild("cube_r1", ModelPartBuilder.create().uv(1, 103).cuboid(-1.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 1.75F, 0.0F, 0.0F, 0.0F, 0.5672F));

        ModelPartData head = body_parent.addChild("head", ModelPartBuilder.create().uv(32, 37).cuboid(-3.0F, -3.0F, -3.0F, 7.0F, 6.0F, 7.0F, new Dilation(0.0F)), ModelTransform.pivot(-0.5F, -5.0F, 0.0F));

        ModelPartData hair = head.addChild("hair", ModelPartBuilder.create().uv(49, 117).cuboid(-0.5F, -4.25F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(41, 113).cuboid(-2.5F, -3.5F, -3.0F, 6.0F, 1.0F, 6.0F, new Dilation(0.0F))
                .uv(24, 112).cuboid(-3.5F, -2.5F, -4.0F, 8.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(84, 28).cuboid(-4.5F, -1.5F, -6.0F, 10.0F, 1.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.5F, 0.0F, 0.0873F, 0.0F, 0.0F));

        ModelPartData cube_r2 = hair.addChild("cube_r2", ModelPartBuilder.create().uv(36, 118).cuboid(-5.0F, -2.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, 1.5F, 7.0F, 0.0436F, 0.0F, 0.0F));

        ModelPartData cube_r3 = hair.addChild("cube_r3", ModelPartBuilder.create().uv(56, 111).cuboid(-5.0F, -2.0F, -1.0F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, 1.4829F, -5.7389F, 0.1309F, 0.0F, 0.0F));

        ModelPartData cube_r4 = hair.addChild("cube_r4", ModelPartBuilder.create().uv(28, 113).mirrored().cuboid(0.0F, -2.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(5.0F, 1.5F, 0.0F, 0.0F, 0.0F, 0.1309F));

        ModelPartData cube_r5 = hair.addChild("cube_r5", ModelPartBuilder.create().uv(29, 113).cuboid(-1.0F, -2.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(-4.25F, 1.5F, 0.0F, 0.0F, 0.0F, -0.0873F));

        ModelPartData scarf = head.addChild("scarf", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 3.0F, 0.0F));

        ModelPartData cube_r6 = scarf.addChild("cube_r6", ModelPartBuilder.create().uv(0, 125).cuboid(-1.0F, -2.0F, -1.0F, 8.0F, 3.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.25F, -0.5F, -2.25F, 0.0873F, 0.0F, 0.0F));

        ModelPartData scarf_head = scarf.addChild("scarf_head", ModelPartBuilder.create().uv(-8, 120).cuboid(-5.5F, 5.5F, -7.25F, 7.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(2.5F, -5.25F, 3.75F));

        ModelPartData cube_r7 = scarf_head.addChild("cube_r7", ModelPartBuilder.create().uv(0, 119).cuboid(-6.0F, -8.0F, -1.0F, 8.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, -7.0F, -1.5272F, 0.0F, 0.0F));

        ModelPartData cube_r8 = scarf_head.addChild("cube_r8", ModelPartBuilder.create().uv(0, 121).cuboid(-3.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-3.0F, 5.5F, 1.75F, 0.0436F, 0.0F, 0.0F));

        ModelPartData cube_r9 = scarf_head.addChild("cube_r9", ModelPartBuilder.create().uv(0, 121).cuboid(-6.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.75F, 5.75F, -1.25F, 2.2046F, -1.5378F, -2.2024F));

        ModelPartData cube_r10 = scarf_head.addChild("cube_r10", ModelPartBuilder.create().uv(0, 121).cuboid(-2.0F, -7.0F, -1.0F, 8.0F, 7.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-4.75F, 5.5F, -1.0F, 1.2519F, 1.4891F, 1.2517F));

        ModelPartData belt = body_parent.addChild("belt", ModelPartBuilder.create().uv(32, 20).cuboid(-5.0F, 11.0F, -3.0F, 11.0F, 2.0F, 8.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData belt_small_bag = belt.addChild("belt_small_bag", ModelPartBuilder.create().uv(82, 108).cuboid(-2.0F, -2.0F, -1.0F, 5.0F, 6.0F, 3.0F, new Dilation(0.0F))
                .uv(82, 118).cuboid(-2.5F, -2.25F, -0.5F, 6.0F, 2.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-2.75F, 13.0F, 6.0F, 0.0873F, 0.0F, 0.0F));

        ModelPartData belt_small_bag_eye = belt_small_bag.addChild("belt_small_bag_eye", ModelPartBuilder.create().uv(94, 101).cuboid(-1.0F, 0.25F, 1.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData belt_water_bottle = belt.addChild("belt_water_bottle", ModelPartBuilder.create().uv(28, 48).cuboid(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(2, 78).cuboid(-1.5F, -1.5F, 0.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(68, 31).cuboid(-2.0F, -2.0F, 2.0F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(-6.0F, 12.0F, -2.0F, -1.0894F, 0.1146F, 0.158F));

        ModelPartData cloak_top = body_parent.addChild("cloak_top", ModelPartBuilder.create().uv(32, 30).cuboid(-5.5F, 0.0F, -2.75F, 11.0F, 0.0F, 7.0F, new Dilation(0.0F))
                .uv(0, 39).cuboid(-5.25F, 0.0F, -2.75F, 0.0F, 13.0F, 7.0F, new Dilation(0.0F))
                .uv(14, 39).cuboid(5.25F, 0.0F, -2.75F, 0.0F, 13.0F, 7.0F, new Dilation(0.0F))
                .uv(38, 0).cuboid(-5.5F, 0.0F, 4.25F, 11.0F, 13.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -2.0F, 0.0F));

        ModelPartData right_hand = body_parent.addChild("right_hand", ModelPartBuilder.create().uv(16, 68).cuboid(-2.0F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F, new Dilation(0.0F))
                .uv(58, 69).cuboid(3.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(102, 0).cuboid(-2.0F, -1.75F, -2.0F, 9.0F, 0.0F, 4.0F, new Dilation(0.0F))
                .uv(114, 21).cuboid(-2.0F, -1.75F, -1.75F, 9.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(113, 6).cuboid(-2.0F, -1.75F, 1.75F, 9.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(109, 14).cuboid(-2.0F, 2.25F, -2.0F, 9.0F, 0.0F, 4.0F, new Dilation(0.0F))
                .uv(112, 96).cuboid(-2.0F, -1.75F, -2.0F, 0.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, 0.5F, 0.75F));

        ModelPartData right_hand_2 = right_hand.addChild("right_hand_2", ModelPartBuilder.create().uv(60, 59).cuboid(0.0F, -2.0F, -1.0F, 8.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(6.0F, 1.0F, 0.0F));

        ModelPartData right_hand_2_gaunt = right_hand_2.addChild("right_hand_2_gaunt", ModelPartBuilder.create().uv(95, 80).cuboid(-2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -1.0F, 0.0F));

        ModelPartData bow = body_parent.addChild("bow", ModelPartBuilder.create().uv(77, 37).cuboid(0.0F, -4.0F, -1.0F, 1.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(2.0F, 4.0F, 5.0F, 0.0F, -1.5708F, -0.48F));

        ModelPartData cube_r11 = bow.addChild("cube_r11", ModelPartBuilder.create().uv(36, 77).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 10.5F, 4.0F, 0.9163F, 0.0F, 0.0F));

        ModelPartData cube_r12 = bow.addChild("cube_r12", ModelPartBuilder.create().uv(28, 77).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -11.0F, 2.0F, -0.9163F, 0.0F, 0.0F));

        ModelPartData cube_r13 = bow.addChild("cube_r13", ModelPartBuilder.create().uv(76, 45).cuboid(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 9.25F, 2.0F, 0.3054F, 0.0F, 0.0F));

        ModelPartData cube_r14 = bow.addChild("cube_r14", ModelPartBuilder.create().uv(44, 76).cuboid(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -5.75F, 0.5F, -0.3054F, 0.0F, 0.0F));

        ModelPartData cube_r15 = bow.addChild("cube_r15", ModelPartBuilder.create().uv(76, 63).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -4.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

        ModelPartData cube_r16 = bow.addChild("cube_r16", ModelPartBuilder.create().uv(76, 53).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 4.0F, 0.25F, 0.1745F, 0.0F, 0.0F));

        ModelPartData bowstring_2 = bow.addChild("bowstring_2", ModelPartBuilder.create().uv(26, 74).cuboid(0.0F, -13.0F, -0.5F, 0.0F, 13.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 10.0F, 3.75F));

        ModelPartData bowstring_1 = bow.addChild("bowstring_1", ModelPartBuilder.create().uv(0, 73).cuboid(0.0F, -2.0F, -0.75F, 0.0F, 15.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -11.0F, 4.0F));

        ModelPartData left_hand = body_parent.addChild("left_hand", ModelPartBuilder.create().uv(104, 68).cuboid(-6.25F, 2.25F, -2.0F, 9.0F, 0.0F, 4.0F, new Dilation(0.0F))
                .uv(102, 47).cuboid(-6.25F, -1.75F, -2.0F, 9.0F, 0.0F, 4.0F, new Dilation(0.0F))
                .uv(111, 27).cuboid(-6.25F, -1.75F, 1.75F, 9.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(110, 95).cuboid(-6.25F, -1.75F, -1.75F, 9.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(44, 68).cuboid(-7.5F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 67).cuboid(-2.5F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F, new Dilation(0.0F))
                .uv(120, 91).cuboid(2.75F, -1.75F, -2.0F, 0.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.5F, 0.5F, 0.75F));

        ModelPartData left_hand_2 = left_hand.addChild("left_hand_2", ModelPartBuilder.create().uv(68, 18).cuboid(-7.0F, -2.0F, -1.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-6.5F, 1.0F, 0.0F));

        ModelPartData left_hand_2_gaunt = left_hand_2.addChild("left_hand_2_gaunt", ModelPartBuilder.create().uv(94, 63).cuboid(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, -1.0F, 0.0F));

        ModelPartData hip_down = wanderer.addChild("hip_down", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -14.0F, 0.0F));

        ModelPartData cloak_down = hip_down.addChild("cloak_down", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cloak_down_left = cloak_down.addChild("cloak_down_left", ModelPartBuilder.create(), ModelTransform.pivot(-4.75F, 3.0F, 0.0F));

        ModelPartData cube_r17 = cloak_down_left.addChild("cube_r17", ModelPartBuilder.create().uv(28, 50).cuboid(-0.6084F, -1.0257F, -4.25F, 0.0F, 10.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0F, 1.25F, 0.0F, 0.0F, 0.1309F));

        ModelPartData cloak_down_right = cloak_down.addChild("cloak_down_right", ModelPartBuilder.create(), ModelTransform.pivot(4.75F, 3.0F, 0.0F));

        ModelPartData cube_r18 = cloak_down_right.addChild("cube_r18", ModelPartBuilder.create().uv(44, 50).cuboid(0.7063F, -1.7692F, -4.25F, 0.0F, 10.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.25F, 1.25F, 0.0F, 0.0F, -0.1309F));

        ModelPartData cloak_down_back = cloak_down.addChild("cloak_down_back", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.5F, 5.0F));

        ModelPartData cube_r19 = cloak_down_back.addChild("cube_r19", ModelPartBuilder.create().uv(0, 59).cuboid(-6.0F, -2.0F, 1.0F, 12.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 2.5F, -1.0F, 0.0436F, 0.0F, 0.0F));

        ModelPartData cloak_down_front_right = cloak_down.addChild("cloak_down_front_right", ModelPartBuilder.create(), ModelTransform.pivot(9.5F, 2.0F, -1.75F));

        ModelPartData cube_r20 = cloak_down_front_right.addChild("cube_r20", ModelPartBuilder.create().uv(62, 97).cuboid(-2.25F, -1.0086F, -0.8695F, 5.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-6.75F, -1.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

        ModelPartData cloak_down_front_left = cloak_down.addChild("cloak_down_front_left", ModelPartBuilder.create(), ModelTransform.pivot(-3.0F, 0.75F, -2.0F));

        ModelPartData cube_r21 = cloak_down_front_left.addChild("cube_r21", ModelPartBuilder.create().uv(62, 97).cuboid(-9.0F, -2.0F, -1.0F, 5.0F, 9.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(6.5F, 1.25F, 0.25F, -0.1309F, 0.0F, 0.0F));

        ModelPartData right_leg = hip_down.addChild("right_leg", ModelPartBuilder.create().uv(62, 116).cuboid(-2.0F, -2.0F, 0.0F, 4.0F, 8.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, 1.0F, -2.0F));

        ModelPartData right_leg_down = right_leg.addChild("right_leg_down", ModelPartBuilder.create().uv(32, 68).cuboid(-1.5F, 0.0F, -0.25F, 3.0F, 6.0F, 3.0F, new Dilation(0.0F))
                .uv(64, 12).cuboid(-1.5F, 6.0F, -1.5F, 3.0F, 1.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 6.0F, 0.75F));

        ModelPartData left_leg = hip_down.addChild("left_leg", ModelPartBuilder.create().uv(60, 37).cuboid(-2.0F, -2.0F, 0.25F, 4.0F, 8.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 1.0F, -2.25F));

        ModelPartData left_leg_down = left_leg.addChild("left_leg_down", ModelPartBuilder.create().uv(60, 63).cuboid(-1.5F, 6.0F, -1.5F, 3.0F, 1.0F, 5.0F, new Dilation(0.0F))
                .uv(116, 119).cuboid(-1.5F, 0.0F, -0.25F, 3.0F, 6.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 6.0F, 1.0F));

        ModelPartData sword = hip_down.addChild("sword", ModelPartBuilder.create().uv(1, 0).cuboid(0.5F, -2.0F, 0.0F, 0.0F, 1.0F, 18.0F, new Dilation(0.0F))
                .uv(17, 86).cuboid(0.5F, -1.0F, 0.0F, 0.0F, 1.0F, 19.0F, new Dilation(0.0F))
                .uv(23, 58).cuboid(0.0F, -4.0F, -2.0F, 1.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(70, 75).cuboid(0.0F, -2.0F, -6.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, 0.0F, 0.0F, -0.3011F, -0.0522F, -0.1666F));

        ModelPartData cube_r22 = sword.addChild("cube_r22", ModelPartBuilder.create().uv(0, 98).cuboid(-1.0F, -1.0F, -1.25F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.5F, -1.0F, -0.75F, -0.7854F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public ModelPart getPart() {
        return wanderer;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);

        this.updateAnimation(entity.walkAnimationState, WandererAnimations.WALK, animationProgress, 1f);
        this.updateAnimation(entity.idleAnimationState, WandererAnimations.IDLE, animationProgress, 1f);
        this.updateAnimation(entity.blockAnimationState, WandererAnimations.PARRY1, animationProgress, 1f);
        this.updateAnimation(entity.healAnimationState, WandererAnimations.DRINK_HEALTH, animationProgress, 1f);

        this.updateAnimation(entity.bowSkillAnimationState, WandererAnimations.BOW_SKILL, animationProgress, 1f);
        this.updateAnimation(entity.bowSkill2AnimationState, WandererAnimations.BOW_SKILL2, animationProgress, 1f);

        this.updateAnimation(entity.swordSlashAnimationState, WandererAnimations.ATTACK_SWORD, animationProgress, 1f);

        this.updateAnimation(entity.attack3AnimationState, WandererAnimations.PARRY1, animationProgress, 1f);

        this.updateAnimation(entity.backflipAnimationState, WandererAnimations.BACKFLIP, animationProgress, 1f);

        this.updateAnimation(entity.tameableAnimationState, WandererAnimations.TAMEABLE, animationProgress, 1f);
        this.updateAnimation(entity.sitAnimationState, WandererAnimations.SIT, animationProgress, 1f);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }
}
