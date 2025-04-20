package com.trongthang.welcometomyworld.entities.client.Enderchester;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.Enderchester;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EnderchesterRenderer extends MobEntityRenderer<Enderchester, EnderchesterModel<Enderchester>> {

    public EnderchesterRenderer(EntityRendererFactory.Context context) {
        super(context, new EnderchesterModel<>(context.getPart(EnderchesterModel.A_LIVING_ENDER_CHEST)), 0.75f);
    }

    @Override
    public Identifier getTexture(Enderchester entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/enderchester.png");
    }


    @Override
    public void render(Enderchester livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
