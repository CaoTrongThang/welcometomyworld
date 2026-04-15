package com.trongthang.welcometomyworld.entities.client.Voidan;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Voidan.Voidan;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidanModel extends GeoModel<Voidan> {

    @Override
    public Identifier getModelResource(Voidan object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/voidan.geo.json");
    }

    @Override
    public Identifier getTextureResource(Voidan object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/voidan/voidan.png");
    }

    @Override
    public Identifier getAnimationResource(Voidan animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/voidan_anim.json");
    }

    @Override
    public void setCustomAnimations(Voidan animatable, long instanceId,
            software.bernie.geckolib.core.animation.AnimationState<Voidan> animationState) {
        software.bernie.geckolib.core.animatable.model.CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            software.bernie.geckolib.model.data.EntityModelData entityData = animationState
                    .getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}
