package com.trongthang.welcometomyworld.entities.client.Portaler;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Portaler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

//the portal can turn into the end portal
@Environment(EnvType.CLIENT)
public class PortalerRenderer extends MobEntityRenderer<Portaler, PortalerModel<Portaler>> {

    public static final Identifier[] END_PORTAL_PNG = {
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler_end.png")
    };

    public static final Identifier[] NETHER_PORTAL_PNGS = {
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler1.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler2.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler3.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler4.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler5.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler6.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler7.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler8.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler9.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler10.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler11.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler12.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler13.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler14.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler15.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler16.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler17.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler18.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler19.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler20.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler21.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler22.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler23.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler24.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler25.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler26.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler27.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler28.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler29.png"),
            new Identifier(WelcomeToMyWorld.MOD_ID, "textures/entity/portaler/portaler30.png")
    };

    public PortalerRenderer(EntityRendererFactory.Context context) {
        super(context, new PortalerModel<>(context.getPart(PortalerModel.PORTALER)), 1f);
    }

    @Override
    public Identifier getTexture(Portaler entity) {
        int variant = entity.getTextureVariant();
        if (variant == 0) {
            return NETHER_PORTAL_PNGS[entity.getCurrentFrame()];
        } else if (variant == 1) {
            return END_PORTAL_PNG[0];
        }
        return NETHER_PORTAL_PNGS[0];
    }


    @Override
    public void render(Portaler livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        matrixStack.scale(1.5f, 1.5f, 1.5f);

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

}
