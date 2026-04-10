package com.trongthang.welcometomyworld.entities.client.VoidWorm;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.VoidWorm.PurpleCrystalEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PurpleCrystalModel extends GeoModel<PurpleCrystalEntity> {
    @Override
    public Identifier getModelResource(PurpleCrystalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/purple_crystal.geo.json");
    }

    @Override
    public Identifier getTextureResource(PurpleCrystalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/purple_crystal.png");
    }

    @Override
    public Identifier getAnimationResource(PurpleCrystalEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/purple_crystal_anim.json");
    }
}
