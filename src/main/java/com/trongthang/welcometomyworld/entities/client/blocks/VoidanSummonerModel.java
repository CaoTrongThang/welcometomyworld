package com.trongthang.welcometomyworld.entities.client.blocks;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.blockentities.VoidanSummonerBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VoidanSummonerModel extends GeoModel<VoidanSummonerBlockEntity> {
    @Override
    public Identifier getModelResource(VoidanSummonerBlockEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "geo/voidan_summoner.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidanSummonerBlockEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "textures/block/voidan_summoner.png");
    }

    @Override
    public Identifier getAnimationResource(VoidanSummonerBlockEntity animatable) {
        return new Identifier(WelcomeToMyWorld.MOD_ID, "animations/void_summoner_anim.json");
    }
}
