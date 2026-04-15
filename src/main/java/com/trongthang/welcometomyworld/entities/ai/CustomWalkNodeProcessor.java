package com.trongthang.welcometomyworld.entities.ai;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;

import java.util.Set;

/**
 * Custom PathNodeMaker that better handles larger hitboxes.
 * Inspired by Mowzie's Mobs.
 */
public class CustomWalkNodeProcessor extends LandPathNodeMaker {
    @Override
    public PathNode getStart() {
        int y;
        Box bounds = this.entity.getBoundingBox();
        if (this.canSwim() && this.entity.isSubmergedInWater()) {
            y = (int) bounds.minY;
            BlockPos.Mutable pos = new BlockPos.Mutable(MathHelper.floor(this.entity.getX()), y,
                    MathHelper.floor(this.entity.getZ()));
            for (Block block = this.cachedWorld.getBlockState(pos)
                    .getBlock(); block == Blocks.WATER; block = this.cachedWorld.getBlockState(pos).getBlock()) {
                pos.setY(++y);
            }
        } else if (this.entity.isOnGround()) {
            y = MathHelper.floor(bounds.minY + 0.5D);
        } else {
            y = MathHelper.floor(this.entity.getY());
            BlockPos.Mutable pos = new BlockPos.Mutable(MathHelper.floor(this.entity.getX()), y,
                    MathHelper.floor(this.entity.getZ()));
            while (y > 0 && (this.cachedWorld.getBlockState(pos).isAir() || this.cachedWorld.getBlockState(pos)
                    .getBlock().getCollisionShape(this.cachedWorld.getBlockState(pos), this.cachedWorld, pos,
                            ShapeContext.absent()) != VoxelShapes.empty())) {
                pos.setY(y--);
            }
            y++;
        }

        // Account for node size based on entity width
        float r = this.entity.getWidth() * 0.5F;
        int x = MathHelper.floor(this.entity.getX() - r);
        int z = MathHelper.floor(this.entity.getZ() - r);

        if (this.entity.getPathfindingPenalty(this.getNodeType(this.entity, x, y, z)) < 0.0F) {
            Set<BlockPos> diagonals = Sets.newHashSet();
            diagonals.add(new BlockPos((int) (bounds.minX - r), y, (int) (bounds.minZ - r)));
            diagonals.add(new BlockPos((int) (bounds.minX - r), y, (int) (bounds.maxZ - r)));
            diagonals.add(new BlockPos((int) (bounds.maxX - r), y, (int) (bounds.minZ - r)));
            diagonals.add(new BlockPos((int) (bounds.maxX - r), y, (int) (bounds.maxZ - r)));
            for (BlockPos p : diagonals) {
                PathNodeType pathnodetype = this.getNodeType(this.entity, p.getX(), p.getY(), p.getZ());
                if (this.entity.getPathfindingPenalty(pathnodetype) >= 0.0F) {
                    return this.getNode(p.getX(), p.getY(), p.getZ());
                }
            }
        }
        return this.getNode(x, y, z);
    }
}
