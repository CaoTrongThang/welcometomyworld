package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

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
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/void_worm.animation.json");
    }
}
