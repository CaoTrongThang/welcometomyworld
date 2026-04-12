package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.items.PurplePortalActivatorItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PurplePortalActivatorItemRenderer extends GeoItemRenderer<PurplePortalActivatorItem> {
    public PurplePortalActivatorItemRenderer() {
        super(new PurplePortalActivatorItemModel());
    }
}
