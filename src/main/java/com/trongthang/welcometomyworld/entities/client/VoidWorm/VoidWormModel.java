package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidWormModel extends GeoModel<VoidWormEntity> {
    @Override
    public Identifier getModelResource(VoidWormEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/void_worm_head.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidWormEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/voidworm/void_worm_head.png");
    }

    @Override
    public Identifier getAnimationResource(VoidWormEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/void_worm.animation.json");
    }
}
