package com.trongthang.welcometomyworld.entities.client.Blossom;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Blossom;
import com.trongthang.welcometomyworld.entities.Enderchester;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class BlossomModel<T extends Blossom> extends SinglePartEntityModel<T> {

    public static final EntityModelLayer BLOSSOM = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "blossom"), "main");

    private final ModelPart blossom;
    private final ModelPart whole_body;
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart hair;
    private final ModelPart hat;
    private final ModelPart hat_top_left;
    private final ModelPart hat_top_left_leaf;
    private final ModelPart hat_top_right;
    private final ModelPart hat_top_right_branch;
    private final ModelPart hat_top_right_leaf;
    private final ModelPart hat_circle;
    private final ModelPart hat_down_left;
    private final ModelPart hat_down_left_leaf;
    private final ModelPart hat_down_right;
    private final ModelPart hat_down_right_leaf;
    private final ModelPart body;
    private final ModelPart body_dress;
    private final ModelPart body_dress_left;
    private final ModelPart body_dress_right;
    private final ModelPart body_dress_back;
    private final ModelPart body_dress_front;
    private final ModelPart body_legs;
    private final ModelPart body_right_hand;
    private final ModelPart body_right_hand_staff;
    private final ModelPart hand_staff_orb;
    private final ModelPart body_left_hand;
    private final ModelPart body_wings;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    public BlossomModel(ModelPart root) {
        this.blossom = root.getChild("blossom");
        this.whole_body = blossom.getChild("whole_body");
        this.head = whole_body.getChild("head");
        this.eye = head.getChild("eye");
        this.hair = head.getChild("hair");
        this.hat = head.getChild("hat");
        this.hat_top_left = hat.getChild("hat_top_left");
        this.hat_top_left_leaf = hat_top_left.getChild("hat_top_left_leaf");
        this.hat_top_right = hat.getChild("hat_top_right");
        this.hat_top_right_branch = hat_top_right.getChild("hat_top_right_branch");
        this.hat_top_right_leaf = hat_top_right_branch.getChild("hat_top_right_leaf");
        this.hat_circle = hat.getChild("hat_circle");
        this.hat_down_left = hat.getChild("hat_down_left");
        this.hat_down_left_leaf = hat_down_left.getChild("hat_down_left_leaf");
        this.hat_down_right = hat.getChild("hat_down_right");
        this.hat_down_right_leaf = hat_down_right.getChild("hat_down_right_leaf");
        this.body = whole_body.getChild("body");
        this.body_dress = body.getChild("body_dress");
        this.body_dress_left = body_dress.getChild("body_dress_left");
        this.body_dress_right = body_dress.getChild("body_dress_right");
        this.body_dress_back = body_dress.getChild("body_dress_back");
        this.body_dress_front = body_dress.getChild("body_dress_front");
        this.body_legs = body.getChild("body_legs");
        this.body_right_hand = body.getChild("body_right_hand");
        this.body_right_hand_staff = body_right_hand.getChild("body_right_hand_staff");
        this.hand_staff_orb = body_right_hand_staff.getChild("hand_staff_orb");
        this.body_left_hand = body.getChild("body_left_hand");
        this.body_wings = body.getChild("body_wings");
        this.right_wing = body_wings.getChild("right_wing");
        this.left_wing = body_wings.getChild("left_wing");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData blossom = modelPartData.addChild("blossom", ModelPartBuilder.create(), ModelTransform.pivot(-0.0295F, 16.0056F, 0.25F));

        ModelPartData whole_body = blossom.addChild("whole_body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData head = whole_body.addChild("head", ModelPartBuilder.create().uv(0, 14).cuboid(-3.0F, -4.0F, -1.0F, 6.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0295F, -3.0056F, -2.25F));

        ModelPartData eye = head.addChild("eye", ModelPartBuilder.create().uv(2, 62).cuboid(-1.0F, -1.0F, -0.25F, 2.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(1.25F, -2.0F, -1.0F));

        ModelPartData hair = head.addChild("hair", ModelPartBuilder.create().uv(0, 24).cuboid(-3.25F, 0.0F, -3.5F, 4.0F, 0.0F, 7.0F, new Dilation(0.0F))
                .uv(24, 0).cuboid(0.25F, 0.0F, -3.5F, 3.0F, 0.0F, 7.0F, new Dilation(0.0F))
                .uv(24, 7).cuboid(-3.25F, 0.0F, -3.5F, 0.0F, 6.0F, 7.0F, new Dilation(0.0F))
                .uv(24, 20).cuboid(3.25F, 0.0F, -3.5F, 0.0F, 6.0F, 7.0F, new Dilation(0.0F))
                .uv(38, 7).cuboid(-3.5F, 0.0F, -3.5F, 7.0F, 6.0F, 0.0F, new Dilation(0.0F))
                .uv(24, 33).cuboid(-3.5F, 0.0F, 3.5F, 7.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -4.5F, 2.0F));

        ModelPartData hat = head.addChild("hat", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -4.25F, 2.0F));

        ModelPartData hat_top_left = hat.addChild("hat_top_left", ModelPartBuilder.create(), ModelTransform.pivot(-1.0F, -1.0F, -2.0F));

        ModelPartData cube_r1 = hat_top_left.addChild("cube_r1", ModelPartBuilder.create().uv(22, 48).cuboid(-1.3147F, -3.0604F, -1.0115F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.5166F, -4.1314F, 0.6082F, -0.1455F, -0.0965F, -0.6908F));

        ModelPartData cube_r2 = hat_top_left.addChild("cube_r2", ModelPartBuilder.create().uv(48, 23).cuboid(-1.3147F, -3.0604F, -1.0115F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.2157F, -4.4097F, 0.6365F, -0.2177F, 0.0114F, -0.0444F));

        ModelPartData cube_r3 = hat_top_left.addChild("cube_r3", ModelPartBuilder.create().uv(48, 19).cuboid(-1.3147F, -3.0604F, -1.0115F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.0931F, -1.4831F, -0.0114F, -0.2177F, 0.0114F, -0.0444F));

        ModelPartData cube_r4 = hat_top_left.addChild("cube_r4", ModelPartBuilder.create().uv(50, 29).cuboid(-1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

        ModelPartData hat_top_left_leaf = hat_top_left.addChild("hat_top_left_leaf", ModelPartBuilder.create(), ModelTransform.pivot(-2.0543F, -5.1966F, 0.0223F));

        ModelPartData cube_r5 = hat_top_left_leaf.addChild("cube_r5", ModelPartBuilder.create().uv(44, 0).cuboid(-1.6176F, -0.4845F, -0.0935F, 3.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-0.25F, 0.25F, -0.25F, 0.0F, 0.1745F, 0.9599F));

        ModelPartData hat_top_right = hat.addChild("hat_top_right", ModelPartBuilder.create(), ModelTransform.pivot(1.4329F, -2.9526F, -3.0F));

        ModelPartData cube_r6 = hat_top_right.addChild("cube_r6", ModelPartBuilder.create().uv(50, 43).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.2182F));

        ModelPartData cube_r7 = hat_top_right.addChild("cube_r7", ModelPartBuilder.create().uv(50, 32).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.4329F, 1.9526F, 1.0F, 0.0F, 0.0F, 0.2182F));

        ModelPartData cube_r8 = hat_top_right.addChild("cube_r8", ModelPartBuilder.create().uv(0, 51).cuboid(-0.2164F, -2.071F, -0.185F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.867F, -2.9106F, 2.4396F, -0.7418F, 0.0F, 0.2182F));

        ModelPartData cube_r9 = hat_top_right.addChild("cube_r9", ModelPartBuilder.create().uv(50, 49).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.3749F, -1.691F, 1.0F, -0.7418F, 0.0F, 0.2182F));

        ModelPartData hat_top_right_branch = hat_top_right.addChild("hat_top_right_branch", ModelPartBuilder.create(), ModelTransform.of(3.5006F, -2.1216F, 1.6898F, 0.0F, 0.0F, 0.2618F));

        ModelPartData cube_r10 = hat_top_right_branch.addChild("cube_r10", ModelPartBuilder.create().uv(48, 15).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.2F, -0.2324F, -2.0742F));

        ModelPartData hat_top_right_leaf = hat_top_right_branch.addChild("hat_top_right_leaf", ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, -0.75F, 0.25F));

        ModelPartData cube_r11 = hat_top_right_leaf.addChild("cube_r11", ModelPartBuilder.create().uv(38, 43).cuboid(-1.0F, -2.0F, -1.0F, 3.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.75F, 0.25F, -0.3007F, -0.2032F, -0.3782F));

        ModelPartData hat_circle = hat.addChild("hat_circle", ModelPartBuilder.create().uv(48, 13).cuboid(-1.0F, -1.0F, 3.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(44, 4).cuboid(-1.0F, -2.0F, -3.25F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r12 = hat_circle.addChild("cube_r12", ModelPartBuilder.create().uv(12, 48).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(3.3202F, 0.0F, 2.3335F, 0.0F, -1.9635F, 0.0F));

        ModelPartData cube_r13 = hat_circle.addChild("cube_r13", ModelPartBuilder.create().uv(46, 47).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(3.5702F, 0.0F, 0.3335F, 0.0F, -1.5272F, 0.0F));

        ModelPartData cube_r14 = hat_circle.addChild("cube_r14", ModelPartBuilder.create().uv(34, 47).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(3.3202F, 0.0F, -1.6665F, 0.0F, -1.4399F, 0.0F));

        ModelPartData cube_r15 = hat_circle.addChild("cube_r15", ModelPartBuilder.create().uv(0, 47).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(2.25F, 0.0F, -3.0F, 0.0F, -0.3491F, 0.0F));

        ModelPartData cube_r16 = hat_circle.addChild("cube_r16", ModelPartBuilder.create().uv(46, 39).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.8202F, 0.0F, 3.5835F, 0.0F, -2.7925F, 0.0F));

        ModelPartData cube_r17 = hat_circle.addChild("cube_r17", ModelPartBuilder.create().uv(6, 48).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.8202F, 0.0F, 3.5835F, 0.0F, 2.7925F, 0.0F));

        ModelPartData cube_r18 = hat_circle.addChild("cube_r18", ModelPartBuilder.create().uv(40, 47).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.3202F, 0.0F, 2.3335F, 0.0F, 1.9635F, 0.0F));

        ModelPartData cube_r19 = hat_circle.addChild("cube_r19", ModelPartBuilder.create().uv(28, 47).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.5702F, 0.0F, 0.3335F, 0.0F, 1.5272F, 0.0F));

        ModelPartData cube_r20 = hat_circle.addChild("cube_r20", ModelPartBuilder.create().uv(46, 41).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.3202F, 0.0F, -1.6665F, 0.0F, 1.4399F, 0.0F));

        ModelPartData cube_r21 = hat_circle.addChild("cube_r21", ModelPartBuilder.create().uv(46, 37).cuboid(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-2.25F, 0.0F, -3.0F, 0.0F, 0.3491F, 0.0F));

        ModelPartData hat_down_left = hat.addChild("hat_down_left", ModelPartBuilder.create(), ModelTransform.pivot(-4.5F, 0.0F, -1.0F));

        ModelPartData cube_r22 = hat_down_left.addChild("cube_r22", ModelPartBuilder.create().uv(48, 27).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

        ModelPartData cube_r23 = hat_down_left.addChild("cube_r23", ModelPartBuilder.create().uv(26, 49).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.3575F, 0.089F, 0.0F, 0.0F, 0.0F, -0.3491F));

        ModelPartData cube_r24 = hat_down_left.addChild("cube_r24", ModelPartBuilder.create().uv(38, 49).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.2977F, 0.3025F, 0.0F, 0.0F, 0.0F, 0.1745F));

        ModelPartData cube_r25 = hat_down_left.addChild("cube_r25", ModelPartBuilder.create().uv(50, 0).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-5.0759F, -0.48F, 0.0F, 0.0F, 0.0F, 0.6545F));

        ModelPartData hat_down_left_leaf = hat_down_left.addChild("hat_down_left_leaf", ModelPartBuilder.create().uv(8, 44).cuboid(-1.0F, -0.25F, -0.25F, 3.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.0325F, -0.6212F, -1.0F));

        ModelPartData hat_down_right = hat.addChild("hat_down_right", ModelPartBuilder.create(), ModelTransform.pivot(7.7977F, 0.3025F, -1.0F));

        ModelPartData cube_r26 = hat_down_right.addChild("cube_r26", ModelPartBuilder.create().uv(50, 2).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        ModelPartData cube_r27 = hat_down_right.addChild("cube_r27", ModelPartBuilder.create().uv(44, 49).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.9402F, -0.2135F, 0.0F, 0.0F, 0.0F, 0.3491F));

        ModelPartData cube_r28 = hat_down_right.addChild("cube_r28", ModelPartBuilder.create().uv(32, 49).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.2977F, -0.3025F, 0.0F, 0.0F, 0.0F, -0.2182F));

        ModelPartData cube_r29 = hat_down_right.addChild("cube_r29", ModelPartBuilder.create().uv(0, 49).cuboid(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.7782F, -0.7824F, 0.0F, 0.0F, 0.0F, -0.6545F));

        ModelPartData hat_down_right_leaf = hat_down_right.addChild("hat_down_right_leaf", ModelPartBuilder.create(), ModelTransform.pivot(2.5437F, -1.4345F, -0.7066F));

        ModelPartData cube_r30 = hat_down_right_leaf.addChild("cube_r30", ModelPartBuilder.create().uv(44, 43).cuboid(-1.0F, -2.0F, -1.0F, 3.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, 2.0F, 0.25F, 0.0F, -0.9599F, 0.0F));

        ModelPartData body = whole_body.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0295F, -3.0056F, -5.25F, 6.0F, 8.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(-1.9409F, 0.0F, 2.0F));

        ModelPartData body_dress = body.addChild("body_dress", ModelPartBuilder.create(), ModelTransform.pivot(1.9705F, -0.0056F, -2.25F));

        ModelPartData body_dress_left = body_dress.addChild("body_dress_left", ModelPartBuilder.create(), ModelTransform.pivot(-3.0167F, 2.9321F, 0.0F));

        ModelPartData cube_r31 = body_dress_left.addChild("cube_r31", ModelPartBuilder.create().uv(0, 31).cuboid(0.0F, -2.0F, -3.0F, 0.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        ModelPartData body_dress_right = body_dress.addChild("body_dress_right", ModelPartBuilder.create(), ModelTransform.pivot(2.9833F, 2.9321F, 0.0F));

        ModelPartData cube_r32 = body_dress_right.addChild("cube_r32", ModelPartBuilder.create().uv(12, 31).cuboid(0.0F, -2.0F, -3.0F, 0.0F, 4.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.5333F, 2.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        ModelPartData body_dress_back = body_dress.addChild("body_dress_back", ModelPartBuilder.create(), ModelTransform.pivot(-0.0167F, 2.9321F, 3.0F));

        ModelPartData cube_r33 = body_dress_back.addChild("cube_r33", ModelPartBuilder.create().uv(38, 29).cuboid(-3.0F, -2.0F, -1.0F, 6.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0167F, 1.7412F, 1.4826F, 0.2618F, 0.0F, 0.0F));

        ModelPartData body_dress_front = body_dress.addChild("body_dress_front", ModelPartBuilder.create(), ModelTransform.pivot(-0.0167F, 2.9321F, -3.0F));

        ModelPartData cube_r34 = body_dress_front.addChild("cube_r34", ModelPartBuilder.create().uv(38, 33).cuboid(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0167F, 1.9997F, -0.5176F, -0.2618F, 0.0F, 0.0F));

        ModelPartData body_legs = body.addChild("body_legs", ModelPartBuilder.create().uv(50, 4).cuboid(-2.0F, 4.75F, -0.75F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(18, 50).cuboid(1.0F, 4.75F, -0.75F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(1.9705F, -0.0056F, -2.25F));

        ModelPartData body_right_hand = body.addChild("body_right_hand", ModelPartBuilder.create(), ModelTransform.of(4.4705F, -2.0056F, -2.25F, -1.5272F, 0.0F, 0.0F));

        ModelPartData cube_r35 = body_right_hand.addChild("cube_r35", ModelPartBuilder.create().uv(34, 41).cuboid(0.0F, -2.0F, 0.0F, 1.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

        ModelPartData cube_r36 = body_right_hand.addChild("cube_r36", ModelPartBuilder.create().uv(0, 41).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(1.5F, 1.5F, 0.5F, 0.0F, 0.0F, -0.4363F));

        ModelPartData body_right_hand_staff = body_right_hand.addChild("body_right_hand_staff", ModelPartBuilder.create().uv(18, 41).cuboid(-1.0F, -3.0F, -1.0F, 1.0F, 8.0F, 1.0F, new Dilation(0.0F))
                .uv(22, 45).cuboid(-1.5F, 5.0F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(3.25F, 4.25F, 0.5F, 1.5236F, 0.0112F, -0.43F));

        ModelPartData cube_r37 = body_right_hand_staff.addChild("cube_r37", ModelPartBuilder.create().uv(30, 51).cuboid(-1.3991F, -2.0313F, -1.8452F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(2.5872F, -3.4445F, 1.0233F, -0.3431F, 0.2751F, 0.6502F));

        ModelPartData cube_r38 = body_right_hand_staff.addChild("cube_r38", ModelPartBuilder.create().uv(34, 51).cuboid(0.3991F, -2.0313F, -1.8452F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.5872F, -3.4445F, 1.0233F, -0.3431F, -0.2751F, -0.6502F));

        ModelPartData cube_r39 = body_right_hand_staff.addChild("cube_r39", ModelPartBuilder.create().uv(26, 51).cuboid(-1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-1.25F, -3.25F, 0.0F, -0.3431F, -0.2751F, -0.6502F));

        ModelPartData cube_r40 = body_right_hand_staff.addChild("cube_r40", ModelPartBuilder.create().uv(14, 50).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.1844F, -8.0274F, 1.117F, 0.0F, 0.0F, -1.2217F));

        ModelPartData cube_r41 = body_right_hand_staff.addChild("cube_r41", ModelPartBuilder.create().uv(10, 50).cuboid(-1.0F, -2.0F, -1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-2.1844F, -8.0274F, 1.117F, 0.0F, 0.0F, 1.2217F));

        ModelPartData cube_r42 = body_right_hand_staff.addChild("cube_r42", ModelPartBuilder.create().uv(14, 44).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(2.3372F, -6.3671F, 1.117F, 0.0F, 0.0F, -0.3491F));

        ModelPartData cube_r43 = body_right_hand_staff.addChild("cube_r43", ModelPartBuilder.create().uv(6, 50).cuboid(-1.0F, -2.0F, -1.0F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-3.3372F, -6.3671F, 1.117F, 0.0F, 0.0F, 0.3491F));

        ModelPartData cube_r44 = body_right_hand_staff.addChild("cube_r44", ModelPartBuilder.create().uv(38, 51).cuboid(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.25F, -3.25F, 0.0F, -0.3431F, 0.2751F, 0.6502F));

        ModelPartData cube_r45 = body_right_hand_staff.addChild("cube_r45", ModelPartBuilder.create().uv(8, 41).cuboid(-2.0F, -1.0F, -1.0F, 3.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.75F, -0.5F, -0.2182F, 0.0F, 0.0F));

        ModelPartData hand_staff_orb = body_right_hand_staff.addChild("hand_staff_orb", ModelPartBuilder.create(), ModelTransform.pivot(-0.5F, -5.25F, 0.0F));

        ModelPartData cube_r46 = hand_staff_orb.addChild("cube_r46", ModelPartBuilder.create().uv(22, 41).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3568F, 0.2048F, -0.0757F));

        ModelPartData body_left_hand = body.addChild("body_left_hand", ModelPartBuilder.create(), ModelTransform.pivot(-1.5295F, -2.0056F, -2.25F));

        ModelPartData cube_r47 = body_left_hand.addChild("cube_r47", ModelPartBuilder.create().uv(38, 37).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-0.5F, 1.5F, 0.5F, 0.0F, 0.0F, 0.4363F));

        ModelPartData cube_r48 = body_left_hand.addChild("cube_r48", ModelPartBuilder.create().uv(30, 41).cuboid(-1.0F, -2.0F, 0.0F, 1.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

        ModelPartData body_wings = body.addChild("body_wings", ModelPartBuilder.create(), ModelTransform.pivot(1.9409F, 0.0F, 0.0F));

        ModelPartData right_wing = body_wings.addChild("right_wing", ModelPartBuilder.create().uv(39, 56).cuboid(-0.25F, -4.0F, -0.25F, 6.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, 0.0F, 1.0F));

        ModelPartData cube_r49 = right_wing.addChild("cube_r49", ModelPartBuilder.create().uv(6, 60).mirrored().cuboid(-2.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(0.6705F, -1.4453F, -0.5F, 0.0F, 0.0F, -0.9599F));

        ModelPartData cube_r50 = right_wing.addChild("cube_r50", ModelPartBuilder.create().uv(6, 62).mirrored().cuboid(-2.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(4.0295F, -2.7556F, -0.5F, 0.0F, 0.0F, 0.6981F));

        ModelPartData left_wing = body_wings.addChild("left_wing", ModelPartBuilder.create().uv(52, 56).cuboid(-5.75F, -4.0F, 0.0F, 6.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.9409F, 0.0F, 0.75F));

        ModelPartData cube_r51 = left_wing.addChild("cube_r51", ModelPartBuilder.create().uv(6, 60).cuboid(-3.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-4.0295F, -2.7556F, -0.25F, 0.0F, 0.0F, -0.6981F));

        ModelPartData cube_r52 = left_wing.addChild("cube_r52", ModelPartBuilder.create().uv(6, 60).cuboid(-3.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.6705F, -1.4453F, -0.25F, 0.0F, 0.0F, 0.9599F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() {
        return blossom;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.getPart().traverse().forEach(ModelPart::resetTransform);
        this.setHeadAngles(headYaw, headPitch);


        this.updateAnimation(entity.walkAnimationState, BlossomAnimations.WALK, animationProgress, 1f);
        this.updateAnimation(entity.greetingAnimationState, BlossomAnimations.GREETING, animationProgress, 1f);

        this.updateAnimation(entity.healAnimationState, BlossomAnimations.HEAL, animationProgress, 1f);
        this.updateAnimation(entity.selfHealAnimationState, BlossomAnimations.SELF_HEAL, animationProgress, 1f);
        this.updateAnimation(entity.sitAnimationState, BlossomAnimations.SIT, animationProgress, 1f);

        this.updateAnimation(entity.attackAnimationState, BlossomAnimations.ATTACK1, animationProgress, 1f);
        this.updateAnimation(entity.attack2AnimationState, BlossomAnimations.ATTACK2, animationProgress, 1f);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = MathHelper.clamp(headYaw, -30.0F, 30.0F);
        headPitch = MathHelper.clamp(headPitch, -25.0F, 45.0F);

        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
    }

}
