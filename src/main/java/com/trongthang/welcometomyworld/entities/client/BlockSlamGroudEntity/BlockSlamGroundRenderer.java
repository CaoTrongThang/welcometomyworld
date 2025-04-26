package com.trongthang.welcometomyworld.entities.client.BlockSlamGroudEntity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.entities.BlockSlamGroundEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.HashMap;

public class BlockSlamGroundRenderer extends EntityRenderer<BlockSlamGroundEntity> {


    public BlockSlamGroundRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BlockSlamGroundEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        BlockState state = entity.getBlockState();
        if (state == null || state.isAir()) return;

        matrices.push();

        double yPos = entity.getY() + entity.getYOffset();

        matrices.translate(
                entity.getX() - entity.getBlockPos().getX() - 0.5,
                yPos - entity.getBlockPos().getY(),
                entity.getZ() - entity.getBlockPos().getZ() - 0.5
        );

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getRoll()));
        matrices.translate(-0.5, -0.5, -0.5);

        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, vertexConsumers, 15728640, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(BlockSlamGroundEntity entity) {
        return null;
    }
}
