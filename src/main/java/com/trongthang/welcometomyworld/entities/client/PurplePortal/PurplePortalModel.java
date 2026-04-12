package com.trongthang.welcometomyworld.entities.client.PurplePortal;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.PurplePortalEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PurplePortalModel extends GeoModel<PurplePortalEntity> {
    @Override
    public Identifier getModelResource(PurplePortalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/purple_portal.geo.json");
    }

    @Override
    public Identifier getTextureResource(PurplePortalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID,
                "textures/entity/purple_portal/purple_portal" + object.currentFrame + ".png");
    }

    @Override
    public Identifier getAnimationResource(PurplePortalEntity object) {
        // Return dummy animation resource to avoid crash or just return empty
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/purple_portal_anim.json");
    }
}
