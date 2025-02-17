package com.trongthang.welcometomyworld.entities.client.Portaler;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Enderchester;
import com.trongthang.welcometomyworld.entities.Portaler;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.Identifier;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class PortalerRenderer extends MobEntityRenderer<Portaler, PortalerModel<Portaler>> {

    public static final Identifier[] TEXTURES = {
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler_end.png")
    };


    public PortalerRenderer(EntityRendererFactory.Context context) {
        super(context, new PortalerModel<>(context.getPart(PortalerModel.PORTALER)), 1f);
    }

    @Override
    public Identifier getTexture(Portaler entity) {
        int variant = entity.getTextureVariant();
        return TEXTURES[variant]; // Prevents out-of-bounds
    }


    @Override
    public void render(Portaler livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        matrixStack.scale(1.5f, 1.5f, 1.5f);

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

}
