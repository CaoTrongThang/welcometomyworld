package com.trongthang.welcometomyworld.entities.client.PortalerTheEnd;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Portaler;
import com.trongthang.welcometomyworld.entities.client.Portaler.PortalerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class PortalerTheEndRenderer extends PortalerRenderer {

    public static final Identifier[] TEXTURES = {
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler_the_end.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler_nether.png")
    };

    public PortalerTheEndRenderer(EntityRendererFactory.Context context) {
        super(context);
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
