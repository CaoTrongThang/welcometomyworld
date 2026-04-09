package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import net.minecraft.util.math.MathHelper;

public class VoidWormBodyModel extends GeoModel<VoidWormPartEntity> {
    @Override
    public Identifier getModelResource(VoidWormPartEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/void_worm_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidWormPartEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/voidworm/void_worm_body.png");
    }

    @Override
    public Identifier getAnimationResource(VoidWormPartEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/void_worm_head_anim.json");
    }

    @Override
    public void setCustomAnimations(VoidWormPartEntity animatable, long instanceId,
            AnimationState<VoidWormPartEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            // Apply pitch directly to the model bone, bypassing global Gimbal Lock!
            head.setRotX(animatable.visualPitch * ((float) Math.PI / 180F));

            // Apply roll based on turning difference
            float yawDelta = MathHelper.wrapDegrees(animatable.visualYaw - animatable.prevVisualYaw);
            float roll = MathHelper.clamp(yawDelta * 3.5f, -60.0f, 60.0f);
            head.setRotZ(roll * ((float) Math.PI / 180F));
        }
    }
}
