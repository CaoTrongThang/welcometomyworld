package com.trongthang.welcometomyworld.entities.client.Chester;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Chester;
import com.trongthang.welcometomyworld.entities.client.Enderchester.EnderchesterAnimations;
import com.trongthang.welcometomyworld.entities.client.Enderchester.EnderchesterModel;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ChesterModel extends EnderchesterModel {

    public static final EntityModelLayer CHESTER = new EntityModelLayer(Identifier.of(WelcomeToMyWorld.MOD_ID, "chester"), "main");

    public ChesterModel(ModelPart root) {
        super(root);
    }
}
