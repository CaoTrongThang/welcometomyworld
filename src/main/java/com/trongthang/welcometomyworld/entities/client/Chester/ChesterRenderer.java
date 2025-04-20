package com.trongthang.welcometomyworld.entities.client.Chester;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Chester;
import com.trongthang.welcometomyworld.entities.Enderchester;
import com.trongthang.welcometomyworld.entities.client.Enderchester.EnderchesterRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class ChesterRenderer extends EnderchesterRenderer {

    public ChesterRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(Enderchester entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/chester.png");
    }
}
