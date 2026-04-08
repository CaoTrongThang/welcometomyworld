package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Unknown.SummoningCircleEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SummoningCircleModel extends GeoModel<SummoningCircleEntity> {
    @Override
    public Identifier getModelResource(SummoningCircleEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/summoning_circle.geo.json");
    }

    @Override
    public Identifier getTextureResource(SummoningCircleEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/summon_circle.png");
    }

    @Override
    public Identifier getAnimationResource(SummoningCircleEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/summoning_circle_anim.json");
    }

    @Override
    public void setCustomAnimations(SummoningCircleEntity animatable, long instanceId,
            AnimationState<SummoningCircleEntity> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            // Rotate the head based on the entity's pitch and yaw
            // Radian = Degree * PI / 180
            head.setRotX(animatable.getPitch() * ((float) Math.PI / 180F));
            head.setRotY(animatable.getYaw() * ((float) Math.PI / 180F));

            if (animatable.age % 20 == 0) {
                System.out
                        .println("[Model] (pitch, " + animatable.getPitch() + "), (yaw, " + animatable.getYaw() + ")");
                System.out.println("[Model] (headRotX, " + head.getRotX() + "), (headRotY, " + head.getRotY() + ")");
            }
        }
    }
}
