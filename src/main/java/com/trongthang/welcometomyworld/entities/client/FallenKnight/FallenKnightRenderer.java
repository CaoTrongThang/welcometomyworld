package com.trongthang.welcometomyworld.entities.client.FallenKnight;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.FallenKnight;
import com.trongthang.welcometomyworld.entities.client.Portaler.PortalerModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class FallenKnightRenderer extends MobEntityRenderer<FallenKnight, FallenKnightModel<FallenKnight>> {

    public FallenKnightRenderer(EntityRendererFactory.Context context) {
        super(context, new FallenKnightModel<>(context.getPart(FallenKnightModel.FALLEN_KNIGHT)), 0.5f);
    }

    @Override
    public Identifier getTexture(FallenKnight entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/fallen_knight.png");
    }


    @Override
    public void render(FallenKnight livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        matrixStack.scale(3f, 3f, 3f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

}
