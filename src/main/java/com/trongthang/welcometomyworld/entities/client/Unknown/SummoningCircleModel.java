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
        CoreGeoBone root = getAnimationProcessor().getBone("body");
        if (root != null) {
            float pitch = animatable.getPitch();
            float yaw = animatable.getYaw();
            root.setRotX(-pitch * ((float) Math.PI / 180F));
            root.setRotY(-yaw * ((float) Math.PI / 180F));
        }
    }
}
