package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.CaptureCageItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class CaptureCageModel extends GeoModel<CaptureCageItem> {
    @Override
    public Identifier getModelResource(CaptureCageItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/capture_cage.geo.json");
    }

    @Override
    public Identifier getTextureResource(CaptureCageItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/item/capture_cage.png");
    }

    @Override
    public Identifier getAnimationResource(CaptureCageItem animatable) {
        return null;
    }
}
