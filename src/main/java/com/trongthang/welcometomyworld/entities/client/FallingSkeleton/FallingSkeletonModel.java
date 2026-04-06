package com.trongthang.welcometomyworld.entities.client.FallingSkeleton;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.FallingSkeleton.FallingSkeleton;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FallingSkeletonModel extends GeoModel<FallingSkeleton> {
    @Override
    public Identifier getModelResource(FallingSkeleton object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/falling_skeleton.geo.json");
    }

    @Override
    public Identifier getTextureResource(FallingSkeleton object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/falling_skeleton.png");
    }

    @Override
    public Identifier getAnimationResource(FallingSkeleton object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/falling_skeleton_anim.json");
    }
}
