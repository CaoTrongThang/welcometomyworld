package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.items.VoidanHornItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class VoidanHornItemRenderer extends GeoItemRenderer<VoidanHornItem> {
    public VoidanHornItemRenderer() {
        super(new VoidanHornItemModel());
    }
}
