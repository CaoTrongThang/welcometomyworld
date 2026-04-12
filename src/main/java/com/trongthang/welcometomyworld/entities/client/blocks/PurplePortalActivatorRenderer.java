package com.trongthang.welcometomyworld.entities.client.blocks;

import com.trongthang.welcometomyworld.blockentities.PurplePortalActivatorBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PurplePortalActivatorRenderer extends GeoBlockRenderer<PurplePortalActivatorBlockEntity> {
    public PurplePortalActivatorRenderer(BlockEntityRendererFactory.Context context) {
        super(new PurplePortalActivatorModel());
        this.withScale(1f);
    }

    // GeckoLib's GeoBlockRenderer already handles rotation automatically
    // for blocks that have a HorizontalFacingBlock.FACING property.
}
