package com.trongthang.welcometomyworld.entities.TinyGolem;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;

public class TinyGolemModel extends GeoModel<TinyGolem> {

    @Override
    public Identifier getModelResource(TinyGolem object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/tiny_golem.geo.json");
    }

    @Override
    public Identifier getTextureResource(TinyGolem object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/tiny_golem.png");
    }

    @Override
    public Identifier getAnimationResource(TinyGolem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/tiny_golem_anim.json");
    }
}
