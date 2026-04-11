package com.trongthang.welcometomyworld.entities.client.armor;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.items.VoidWormBoneArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidWormBoneArmorModel extends GeoModel<VoidWormBoneArmorItem> {
    @Override
    public Identifier getModelResource(VoidWormBoneArmorItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/void_worm_bone_armor.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidWormBoneArmorItem animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/models/armor/void_worm_bone_armor.png");
    }

    @Override
    public Identifier getAnimationResource(VoidWormBoneArmorItem animatable) {
        return null;
    }
}
