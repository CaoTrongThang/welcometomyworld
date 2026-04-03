package com.trongthang.welcometomyworld.mixin.world;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.client.AnimationUtils;

import net.minecraft.util.math.Direction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(targets = "me.adda.enhanced_falling_trees.entity.TreeEntity", remap = false)
public abstract class FallingTreeMixin {

    @org.spongepowered.asm.mixin.Unique
    private final java.util.Set<LivingEntity> hitEntities = new java.util.HashSet<>();

    @Inject(method = "tick", at = @At("TAIL"))
    private void addTreeDamage(CallbackInfo ci) {
        if (!com.trongthang.welcometomyworld.ConfigLoader.getInstance().enableTreeFallingDamage)
            return;

        Entity self = (Entity) (Object) this;

        try {
            // Accessing mod methods via reflection
            Map<?, ?> blocks = (Map<?, ?>) invoke(self, "getBlocks");
            if (blocks == null || blocks.isEmpty())
                return;

            int height = (Integer) invoke(self, "getTreeHeight");
            if (height <= 1)
                return;

            // Fabric version uses getHorizontalFacing instead of getDirection
            Direction direction = (Direction) invokeAny(self, "getHorizontalFacing", "getDirection");
            if (direction == null)
                return;

            float targetAngle = (Float) invoke(self, "getTargetAngle");
            if (targetAngle == 0)
                targetAngle = 90.0f;

            Object treeType = invoke(self, "getTreeType");
            if (treeType == null)
                return;
            float fallAnimLength = (Float) invoke(treeType, "getFallAnimLength");

            float time = (self.age / 20.0f) * (3.1415927f / 2.0f) / fallAnimLength;

            // Limit damage to only when falling (stop damaging completely once fully
            // fallen)
            if (time > 1.6f)
                return;

            double clampedTime = java.lang.Math.min(java.lang.Math.max(-3.1415927, time), 3.1415927);
            float bumpCos = (float) java.lang.Math.max(0.0, java.lang.Math.cos(clampedTime));
            float totalAnimation = bumpCos * targetAngle - targetAngle;

            // Wait a bit before damaging (don't damage when tree is upright)
            if (java.lang.Math.abs(totalAnimation) < 10.0f)
                return;

            // Tree Renderer uses the opposite of horizontal facing for rotation origin
            Direction rendererDir = direction.getOpposite();

            float angleRad = (float) java.lang.Math.toRadians(totalAnimation);
            Vector3f rotationVector = new Vector3f(angleRad, 0, 0);
            rotationVector.rotateY((float) java.lang.Math.toRadians(-rendererDir.asRotation()));

            Quaternionf rotation = new Quaternionf().identity()
                    .rotateX(rotationVector.x)
                    .rotateZ(rotationVector.z);
            Quaternionf invRotation = new Quaternionf(rotation).invert();

            float edgeDist = 0.5f;
            try {
                edgeDist = (Float) invoke(treeType, "fallAnimationEdgeDistance");
            } catch (Exception ignored) {
            }

            Vector3f pivot = new Vector3f(0f, 0f, 0.5f * edgeDist);
            pivot.rotateY((float) java.lang.Math.toRadians(-rendererDir.asRotation()));

            Vec3d basePos = self.getPos();

            // Find actual max bounds of the tree to properly size the search area for big
            // trees
            int maxDist = height;
            for (Object key : blocks.keySet()) {
                net.minecraft.util.math.BlockPos bp = (net.minecraft.util.math.BlockPos) key;
                int d = java.lang.Math.max(
                        java.lang.Math.max(java.lang.Math.abs(bp.getX()), java.lang.Math.abs(bp.getY())),
                        java.lang.Math.abs(bp.getZ()));
                if (d > maxDist)
                    maxDist = d;
            }

            // Broad phase box search
            Box searchBox = new Box(
                    basePos.x - maxDist - 2.0, basePos.y - maxDist - 2.0, basePos.z - maxDist - 2.0,
                    basePos.x + maxDist + 2.0, basePos.y + maxDist + 2.0, basePos.z + maxDist + 2.0);

            List<LivingEntity> nearbyEntities = self.getWorld().getEntitiesByClass(LivingEntity.class, searchBox,
                    e -> true);

            // Pre-fetch method before loop for performance
            java.lang.reflect.Method baseBlockCheckMethod = null;
            try {
                baseBlockCheckMethod = treeType.getClass().getMethod("baseBlockCheck",
                        net.minecraft.block.BlockState.class);
            } catch (Exception e) {
            }

            for (LivingEntity target : nearbyEntities) {
                if (hitEntities.contains(target))
                    continue;

                Box targetBox = target.getBoundingBox();
                Vec3d targetCenter = targetBox.getCenter();

                // Vector from upright tree base center to entity
                Vector3f relTarget = new Vector3f(
                        (float) (targetCenter.x - basePos.x),
                        (float) (targetCenter.y - basePos.y),
                        (float) (targetCenter.z - basePos.z));

                // Inverse transformation to tree's unrotated coordinate space around pivot
                relTarget.add(pivot);
                invRotation.transform(relTarget);
                relTarget.sub(pivot);

                float entityHalfWidth = (float) targetBox.getXLength() / 2.0f;
                float entityHalfHeight = (float) targetBox.getYLength() / 2.0f;

                // Entity bounds in local space
                float localXMin = relTarget.x - entityHalfWidth;
                float localXMax = relTarget.x + entityHalfWidth;
                float localYMin = relTarget.y - entityHalfHeight;
                float localYMax = relTarget.y + entityHalfHeight;
                float localZMin = relTarget.z - entityHalfWidth;
                float localZMax = relTarget.z + entityHalfWidth;

                // Find overlapping local blocks
                int minX = java.lang.Math.round(localXMin);
                int maxX = java.lang.Math.round(localXMax);
                int minY = (int) java.lang.Math.floor(localYMin);
                int maxY = (int) java.lang.Math.floor(localYMax);
                int minZ = java.lang.Math.round(localZMin);
                int maxZ = java.lang.Math.round(localZMax);

                boolean hit = false;

                // Narrow phase: check intersection in unrotated space with ACTUAL blocks
                for (int x = minX; x <= maxX && !hit; x++) {
                    for (int y = minY; y <= maxY && !hit; y++) {
                        for (int z = minZ; z <= maxZ && !hit; z++) {
                            net.minecraft.util.math.BlockPos checkPos = new net.minecraft.util.math.BlockPos(x, y, z);
                            Object state = blocks.get(checkPos);

                            if (state != null) {
                                boolean hurts = true;
                                if (baseBlockCheckMethod != null) {
                                    try {
                                        hurts = (Boolean) baseBlockCheckMethod.invoke(treeType, state);
                                    } catch (Exception e) {
                                    }
                                }
                                if (hurts) {
                                    hit = true;
                                }
                            }
                        }
                    }
                }

                if (hit) {
                    hitEntities.add(target);
                    // Tree mass logic: base 15 damage (75% of 20hp) + scaling for big trees
                    float damageAmt = (float) (15.0f + (blocks.size() / 15.0f));

                    if (self.getWorld().isClient()) {
                        // Play animation forcefully on the client
                        if (target == net.minecraft.client.MinecraftClient.getInstance().player) {
                            try {
                                AnimationUtils.playAnimation(
                                        (net.minecraft.client.network.AbstractClientPlayerEntity) target,
                                        "welcometomyworld", "landing_fail");
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        if (target.damage(self.getWorld().getDamageSources().generic(), damageAmt)) {
                            Vec3d knockback = new Vec3d(direction.getOffsetX(), 0.3, direction.getOffsetZ())
                                    .multiply(1.0);
                            target.setVelocity(target.getVelocity().add(knockback));

                            // Extreme debuffs to essentially "stun" the player while animating
                            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                    net.minecraft.entity.effect.StatusEffects.SLOWNESS, 40, 255, false, false, true));
                            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                    net.minecraft.entity.effect.StatusEffects.WEAKNESS, 40, 255, false, false, true));
                            target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                    net.minecraft.entity.effect.StatusEffects.MINING_FATIGUE, 40, 255, false, false,
                                    true));
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (self.age < 5) {
                WelcomeToMyWorld.LOGGER.error("err", e.getMessage());
            }
        }
    }

    private static Object invoke(Object obj, String method) throws Exception {
        return obj.getClass().getMethod(method).invoke(obj);
    }

    private static Object invokeAny(Object obj, String... methods) throws Exception {
        for (String method : methods) {
            try {
                return obj.getClass().getMethod(method).invoke(obj);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("None of the specified methods found: " + java.util.Arrays.toString(methods));
    }
}