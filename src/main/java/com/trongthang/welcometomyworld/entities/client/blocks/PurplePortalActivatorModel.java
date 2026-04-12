package com.trongthang.welcometomyworld.entities.client.blocks;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.blockentities.PurplePortalActivatorBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PurplePortalActivatorModel extends GeoModel<PurplePortalActivatorBlockEntity> {
    @Override
    public Identifier getModelResource(PurplePortalActivatorBlockEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/purple_portal_activator.geo.json");
    }

    @Override
    public Identifier getTextureResource(PurplePortalActivatorBlockEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/block/purple_protal_activator.png");
    }

    @Override
    public Identifier getAnimationResource(PurplePortalActivatorBlockEntity animatable) {
        // Return dummy or null if no animations
        return null;
    }
}
