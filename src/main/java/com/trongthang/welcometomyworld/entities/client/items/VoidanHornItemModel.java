package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.VoidanHornItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidanHornItemModel extends GeoModel<VoidanHornItem> {
    @Override
    public Identifier getModelResource(VoidanHornItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/voidan_horn.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidanHornItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/item/voidan_horn.png");
    }

    @Override
    public Identifier getAnimationResource(VoidanHornItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/voidan_horn_anim.json");
    }
}
