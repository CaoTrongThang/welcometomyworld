package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.PurplePortalActivatorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PurplePortalActivatorItemModel extends GeoModel<PurplePortalActivatorItem> {
    @Override
    public Identifier getModelResource(PurplePortalActivatorItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/purple_portal_activator.geo.json");
    }

    @Override
    public Identifier getTextureResource(PurplePortalActivatorItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/block/purple_protal_activator.png");
    }

    @Override
    public Identifier getAnimationResource(PurplePortalActivatorItem animatable) {
        return null;
    }
}
