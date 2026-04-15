package com.trongthang.welcometomyworld.entities.ai;

import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * Custom MobNavigation that improves path following for larger hitboxes.
 * Includes shortcutting logic to skip unnecessary nodes.
 * Inspired by Mowzie's Mobs.
 */
public class CustomPathNavigateGround extends MobNavigation {
    public CustomPathNavigateGround(PathAwareEntity entity, World world) {
        super(entity, world);
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int maxVisitedNodes) {
        this.nodeMaker = new CustomWalkNodeProcessor();
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new CustomPathFinder(this.nodeMaker, maxVisitedNodes);
    }

    @Override
    protected void continueFollowingPath() {
        if (this.currentPath == null)
            return;

        Path path = Objects.requireNonNull(this.currentPath);
        Vec3d entityPos = this.getPos();
        int pathLength = path.getLength();

        // Check for elevation changes or obstacles that require following the exact
        // path
        for (int i = path.getCurrentNodeIndex(); i < path.getLength(); i++) {
            if (path.getNode(i).y != Math.floor(entityPos.y)) {
                pathLength = i;
                break;
            }
        }

        final Vec3d base = entityPos.add(-this.entity.getWidth() * 0.5F, 0.0F, -this.entity.getWidth() * 0.5F);
        final Vec3d max = base.add(this.entity.getWidth(), this.entity.getHeight(), this.entity.getWidth());

        // Try to shortcut the path if a direct line is clear
        if (this.tryShortcut(path, new Vec3d(this.entity.getX(), this.entity.getY(), this.entity.getZ()), pathLength,
                base, max)) {
            if (this.isAt(path, 0.5F)
                    || this.atElevationChange(path) && this.isAt(path, this.entity.getWidth() * 0.5F)) {
                path.setCurrentNodeIndex(path.getCurrentNodeIndex() + 1);
            }
        }
        this.checkTimeouts(entityPos);
    }

    private boolean isAt(Path path, float threshold) {
        final Vec3d pathPos = path.getNodePosition(this.entity);
        return MathHelper.abs((float) (this.entity.getX() - pathPos.x)) < threshold &&
                MathHelper.abs((float) (this.entity.getZ() - pathPos.z)) < threshold &&
                Math.abs(this.entity.getY() - pathPos.y) < 1.0D;
    }

    private boolean atElevationChange(Path path) {
        final int curr = path.getCurrentNodeIndex();
        final int end = Math.min(path.getLength(), curr + MathHelper.ceil(this.entity.getWidth() * 0.5F) + 1);
        final int currY = path.getNode(curr).y;
        for (int i = curr + 1; i < end; i++) {
            if (path.getNode(i).y != currY) {
                return true;
            }
        }
        return false;
    }

    private boolean tryShortcut(Path path, Vec3d entityPos, int pathLength, Vec3d base, Vec3d max) {
        for (int i = pathLength; --i > path.getCurrentNodeIndex();) {
            final Vec3d vec = path.getNodePosition(this.entity, i).subtract(entityPos);
            if (this.sweep(vec, base, max)) {
                path.setCurrentNodeIndex(i);
                return false;
            }
        }
        return true;
    }

    private static final float EPSILON = 1.0E-8F;

    /**
     * AABB sweep check to see if the entity can move directly to a path node.
     */
    private boolean sweep(Vec3d vec, Vec3d base, Vec3d max) {
        float t = 0.0F;
        float max_t = (float) vec.length();
        if (max_t < EPSILON)
            return true;

        final float[] tr = new float[3];
        final int[] ldi = new int[3];
        final int[] tri = new int[3];
        final int[] step = new int[3];
        final float[] tDelta = new float[3];
        final float[] tNext = new float[3];
        final float[] normed = new float[3];

        for (int i = 0; i < 3; i++) {
            float value = element(vec, i);
            boolean dir = value >= 0.0F;
            step[i] = dir ? 1 : -1;
            float lead = element(dir ? max : base, i);
            tr[i] = element(dir ? base : max, i);
            ldi[i] = leadEdgeToInt(lead, step[i]);
            tri[i] = trailEdgeToInt(tr[i], step[i]);
            normed[i] = value / max_t;
            tDelta[i] = MathHelper.abs(max_t / value);
            float dist = dir ? (ldi[i] + 1 - lead) : (lead - ldi[i]);
            tNext[i] = tDelta[i] < Float.POSITIVE_INFINITY ? tDelta[i] * dist : Float.POSITIVE_INFINITY;
        }

        while (t < max_t) {
            int axis = 0;
            if (tNext[0] < tNext[1]) {
                if (tNext[0] < tNext[2])
                    axis = 0;
                else
                    axis = 2;
            } else {
                if (tNext[1] < tNext[2])
                    axis = 1;
                else
                    axis = 2;
            }

            float tCurrent = tNext[axis];
            if (tCurrent > max_t)
                break;

            ldi[axis] += step[axis];
            tNext[axis] += tDelta[axis];
            t = tCurrent;

            // Check collision
            for (int i = 0; i < 3; i++) {
                tri[i] = trailEdgeToInt(tr[i] + t * normed[i], step[i]);
            }

            int xStart = Math.min(ldi[0], tri[0]);
            int xEnd = Math.max(ldi[0], tri[0]);
            int yStart = Math.min(ldi[1], tri[1]);
            int yEnd = Math.max(ldi[1], tri[1]);
            int zStart = Math.min(ldi[2], tri[2]);
            int zEnd = Math.max(ldi[2], tri[2]);

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    for (int z = zStart; z <= zEnd; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (this.world.getBlockState(pos).isOpaqueFullCube(this.world, pos)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private float element(Vec3d v, int i) {
        switch (i) {
            case 0:
                return (float) v.x;
            case 1:
                return (float) v.y;
            case 2:
                return (float) v.z;
            default:
                return 0.0F;
        }
    }

    private int leadEdgeToInt(float f, int step) {
        return MathHelper.floor(f + (step > 0 ? -EPSILON : EPSILON));
    }

    private int trailEdgeToInt(float f, int step) {
        return MathHelper.floor(f + (step > 0 ? EPSILON : -EPSILON));
    }
}
