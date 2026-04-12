package com.trongthang.welcometomyworld.entities.client.RiftPortal;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.RiftPortalEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RiftPortalModel extends GeoModel<RiftPortalEntity> {
    @Override
    public Identifier getModelResource(RiftPortalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/rift_portal_entity.geo.json");
    }

    @Override
    public Identifier getTextureResource(RiftPortalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID,
                "textures/entity/portaler/portaler" + object.currentFrame + ".png");
    }

    @Override
    public Identifier getAnimationResource(RiftPortalEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/rift_portal_anim.json");
    }
}
