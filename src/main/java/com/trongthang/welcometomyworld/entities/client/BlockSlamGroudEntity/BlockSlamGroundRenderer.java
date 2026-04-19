package com.trongthang.welcometomyworld.entities.client.BlockSlamGroudEntity;

import com.trongthang.welcometomyworld.entities.BlockSlamGroundEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class BlockSlamGroundRenderer extends EntityRenderer<BlockSlamGroundEntity> {

    public BlockSlamGroundRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlockSlamGroundEntity entity, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        BlockState state = entity.getBlockState();
        if (state == null || state.isAir())
            return;

        matrices.push();

        // Rotate around centered origin
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getRoll()));

        // Offset the model so its center corresponds to the entity position
        // (half-buried)
        matrices.translate(-0.5, -0.5, -0.5);

        // When sinking into ground, getBlockPos() ends up inside a solid block
        // (light=0).
        // Sample from above as well and take the brighter of the two.
        int blockLight = Math.max(
                WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getBlockPos()),
                WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getBlockPos().up()));
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, vertexConsumers,
                blockLight, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(BlockSlamGroundEntity entity) {
        return null;
    }
}
