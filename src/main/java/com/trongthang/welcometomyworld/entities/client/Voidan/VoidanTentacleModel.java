package com.trongthang.welcometomyworld.entities.client.Voidan;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Voidan.VoidanTentacle;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidanTentacleModel extends GeoModel<VoidanTentacle> {
    @Override
    public Identifier getModelResource(VoidanTentacle object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/voidan_tentacle.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidanTentacle object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/voidan_tentacle.png");
    }

    @Override
    public Identifier getAnimationResource(VoidanTentacle animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/voidan_tentacle_anim.json");
    }
}
