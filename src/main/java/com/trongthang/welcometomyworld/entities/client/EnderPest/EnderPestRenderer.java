package com.trongthang.welcometomyworld.entities.client.EnderPest;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.EnderPest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EnderPestRenderer extends MobEntityRenderer<EnderPest, EnderPestModel<EnderPest>> {

    public EnderPestRenderer(EntityRendererFactory.Context context) {
        super(context, new EnderPestModel<>(context.getPart(EnderPestModel.ENDER_PEST)), 1f);
    }

    @Override
    public Identifier getTexture(EnderPest entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/ender_pest.png");
    }

    @Override
    public void render(EnderPest livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

}
