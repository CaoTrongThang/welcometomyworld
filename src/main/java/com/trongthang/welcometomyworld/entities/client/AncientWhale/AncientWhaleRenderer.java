package com.trongthang.welcometomyworld.entities.client.AncientWhale;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.AncientWhale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AncientWhaleRenderer extends MobEntityRenderer<AncientWhale, AncientWhaleModel<AncientWhale>> {

    public AncientWhaleRenderer(EntityRendererFactory.Context context) {
        super(context, new AncientWhaleModel<>(context.getPart(AncientWhaleModel.ANCIENT_WHALE)), 0.75f);
    }

    @Override
    public Identifier getTexture(AncientWhale entity) {
        return Identifier.of(WelcomeToMyWorld.MOD_ID, "textures/entity/ancient_whale.png");
    }


    @Override
    public void render(AncientWhale livingEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {

        if(livingEntity.isBaby()) {
            matrixStack.scale(1, 1, 1);
        } else {
            matrixStack.scale(5, 5, 5);
        }

        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
