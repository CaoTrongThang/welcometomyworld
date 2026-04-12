package com.trongthang.welcometomyworld.entities.client.blocks;

import com.trongthang.welcometomyworld.blockentities.VoidBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Block;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VoidBlockRenderer implements BlockEntityRenderer<VoidBlockEntity> {

    public VoidBlockRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(VoidBlockEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        this.renderSides(entity, matrix4f, vertexConsumers.getBuffer(RenderLayer.getEndPortal()));
    }

    private void renderSides(VoidBlockEntity entity, Matrix4f matrix, VertexConsumer vertexConsumer) {
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F,
                Direction.SOUTH);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                Direction.NORTH);
        this.renderSide(entity, matrix, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        this.renderSide(entity, matrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
    }

    private void renderSide(VoidBlockEntity entity, Matrix4f matrix, VertexConsumer vertexConsumer, float x1, float x2,
            float y1, float y2, float z1, float z2, float z3, float z4, Direction side) {
        if (entity.getWorld() != null && Block.shouldDrawSide(entity.getWorld().getBlockState(entity.getPos()),
                entity.getWorld(), entity.getPos(), side, entity.getPos().offset(side))) {
            vertexConsumer.vertex(matrix, x1, y1, z1).next();
            vertexConsumer.vertex(matrix, x2, y1, z2).next();
            vertexConsumer.vertex(matrix, x2, y2, z3).next();
            vertexConsumer.vertex(matrix, x1, y2, z4).next();
        }
    }
}
