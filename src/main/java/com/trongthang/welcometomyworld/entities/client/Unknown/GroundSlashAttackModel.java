package com.trongthang.welcometomyworld.entities.client.Unknown;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.GroundSlashAttackEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GroundSlashAttackModel extends GeoModel<GroundSlashAttackEntity> {

    @Override
    public Identifier getModelResource(GroundSlashAttackEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/ground_slash_attack.geo.json");
    }

    @Override
    public Identifier getTextureResource(GroundSlashAttackEntity object) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/ground_slash_attack.png");
    }

    @Override
    public Identifier getAnimationResource(GroundSlashAttackEntity object) {
        // No animations — static model driven by entity yaw
        return null;
    }
}
