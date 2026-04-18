package com.trongthang.welcometomyworld.entities.client.blocks;

import com.trongthang.welcometomyworld.blockentities.VoidanSummonerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidanSummonerRenderer extends GeoBlockRenderer<VoidanSummonerBlockEntity> {
    public VoidanSummonerRenderer(BlockEntityRendererFactory.Context context) {
        super(new VoidanSummonerModel());
    }
}
