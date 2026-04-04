package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class UnknownModel extends GeoModel<Unknown> {
    @Override
    public Identifier getModelResource(Unknown object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/unknown.geo.json");
    }

    @Override
    public Identifier getTextureResource(Unknown object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/unknown.png");
    }

    @Override
    public Identifier getAnimationResource(Unknown object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/unknown_anim.json");
    }

    @Override
    public void setCustomAnimations(Unknown animatable, long instanceId, AnimationState<Unknown> animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("head");

        if (head != null) {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
            head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
        }
    }
}
