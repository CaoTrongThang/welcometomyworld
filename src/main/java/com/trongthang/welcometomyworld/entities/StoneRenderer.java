package com.trongthang.welcometomyworld.entities;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StoneRenderer extends EntityRenderer<StoneEntity> {
    private final BlockRenderManager blockRenderer;

    public StoneRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderManager();
    }

    @Override
    public void render(StoneEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // Center the block in the entity's position
        matrices.translate(-0.5F, 0F, -0.5F);
        blockRenderer.renderBlockAsEntity(
                Blocks.OAK_LOG.getDefaultState(),
                matrices,
                vertexConsumers,
                light,
                OverlayTexture.DEFAULT_UV
        );
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(StoneEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE; // Use block texture atlas
    }
}
