package com.trongthang.welcometomyworld.client.renderer.armor;

import com.trongthang.welcometomyworld.entities.client.armor.VoidWormBoneArmorModel;
import com.trongthang.welcometomyworld.items.VoidWormBoneArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class VoidWormBoneArmorRenderer extends GeoArmorRenderer<VoidWormBoneArmorItem> {
    public VoidWormBoneArmorRenderer() {
        super(new VoidWormBoneArmorModel());
    }
}
