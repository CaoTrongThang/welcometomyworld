package com.trongthang.welcometomyworld.entities.client.PurplePortal;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.PurplePortal;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PurplePortalModel extends GeoModel<PurplePortal> {
    @Override
    public Identifier getModelResource(PurplePortal object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/purple_portal.geo.json");
    }

    @Override
    public Identifier getTextureResource(PurplePortal object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID,
                "textures/entity/purple_portal/purple_portal" + object.currentFrame + ".png");
    }

    @Override
    public Identifier getAnimationResource(PurplePortal object) {
        // Return dummy animation resource to avoid crash or just return empty
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/purple_portal.animation.json");
    }
}
