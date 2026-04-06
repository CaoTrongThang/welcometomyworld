package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.ConfigLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class PlayerAnimationHandler {
    private static final String MOD_ID = "welcometomyworld";

    public static void update(AbstractClientPlayerEntity player, boolean wasOnGround, float fallDistance) {
        if (player == null)
            return;

        if (!ConfigLoader.getInstance().enableCustomAnimations) {
            AnimationUtils.stopAnimation(player);
            return;
        }

        // Riding a vehicle or swimming or in water — stop any playing animation
        if (player.hasVehicle() || player.isSwimming() || player.isInsideWaterOrBubbleColumn()) {
            AnimationUtils.stopAnimation(player);
            return;
        }

        boolean isOnGround = player.isOnGround();

        // Flying (creative) or elytra — play flying animation, skip fall/land/jump
        // logic
        if (player.getAbilities().flying || player.isFallFlying()) {
            handleFlying(player);
            return;
        }

        // 1. Handle Landing Event (Transition from air to ground)
        if (isOnGround && !wasOnGround) {
            handleLanding(player, fallDistance);
            return;
        }

        // 2. Handle Air State
        if (!isOnGround) {
            if (wasOnGround && player.getVelocity().y > 0) {
                handleJump(player);
            }
            handleAir(player, fallDistance);
            AnimationUtils.setAnimationSpeed(player, 1.0f);
        }
        // 3. Handle Grounded State
        else {
            handleGrounded(player, fallDistance);
        }
    }

    private static void handleLanding(AbstractClientPlayerEntity player, float fallDistance) {
        IAnimatedPlayer animatedPlayer = (IAnimatedPlayer) player;
        float customFallDist = (float) Math.max(fallDistance,
                animatedPlayer.welcometomyworld_getFallStartHeight() - player.getY());

        if (player.hurtTime > 0 || customFallDist > 6.0f) {
            AnimationUtils.playAnimation(player, MOD_ID, "landing_fail");
        } else if (customFallDist > 3.0f) {
            // landing_success only plays for meaningful falls (2+ blocks) without damage
            AnimationUtils.playAnimation(player, MOD_ID, "landing_success");
        } else {
            // Short drop — just stop the falling animation if it exists
            if (AnimationUtils.isAnimationPlaying(player, "falling")) {
                AnimationUtils.stopAnimation(player);
            }
        }
        AnimationUtils.setAnimationSpeed(player, 1.0f);
    }

    private static void handleJump(AbstractClientPlayerEntity player) {
        // Only trigger jump animations if space is actually held or we have upward
        // momentum
        net.minecraft.client.network.ClientPlayerEntity clientPlayer = (net.minecraft.client.network.ClientPlayerEntity) player;
        if (!clientPlayer.input.jumping)
            return;

        boolean isMoving = player.limbAnimator.getSpeed() > 0.05f;
        IAnimatedPlayer animatedPlayer = (IAnimatedPlayer) player;

        if (isMoving) {
            boolean jumpLeft = !animatedPlayer.welcometomyworld_isLastJumpLeft();
            animatedPlayer.welcometomyworld_setLastJumpLeft(jumpLeft);
            String jumpAnim = jumpLeft ? "jumping_left" : "jumping_right";
            AnimationUtils.playAnimation(player, MOD_ID, jumpAnim);
        } else {
            AnimationUtils.playAnimation(player, MOD_ID, "jumping_idle");
        }
        AnimationUtils.setAnimationSpeed(player, 1.0f);
    }

    private static void handleAir(AbstractClientPlayerEntity player, float fallDistance) {
        // Interrupt ground animations if we start falling (large distance) or jumping
        // Grace period for small drops (stepping off stairs/slabs)

        if (fallDistance > 0.5f || player.getVelocity().y > 0.1) {
            if (AnimationUtils.isAnimationPlaying(player, "walking") ||
                    AnimationUtils.isAnimationPlaying(player, "running") ||
                    AnimationUtils.isAnimationPlaying(player, "landing_fail") ||
                    AnimationUtils.isAnimationPlaying(player, "landing_success")) {
                AnimationUtils.stopAnimation(player);
            }
        }

        IAnimatedPlayer animatedPlayer = (IAnimatedPlayer) player;
        float customFallDist = (float) Math.max(fallDistance,
                animatedPlayer.welcometomyworld_getFallStartHeight() - player.getY());

        if (customFallDist > 3.0f && !AnimationUtils.isAnimationPlaying(player, "falling")) {
            AnimationUtils.playAnimation(player, MOD_ID, "falling");
        }
    }

    private static void handleGrounded(AbstractClientPlayerEntity player, float fallDistance) {
        // Stop airborne animations if we are on ground (fallback)
        if (AnimationUtils.isAnimationPlaying(player, "falling") ||
                AnimationUtils.isAnimationPlaying(player, "jumping_idle") ||
                AnimationUtils.isAnimationPlaying(player, "jumping_left") ||
                AnimationUtils.isAnimationPlaying(player, "jumping_right")) {
            AnimationUtils.stopAnimation(player);
        }

        // If we are playing success but just got hurt, upgrade to fail animation.
        // This handles cases where the damage packet arrives a few ticks after landing.
        if (AnimationUtils.isAnimationPlaying(player, "landing_success") && player.hurtTime > 0) {
            AnimationUtils.playAnimation(player, MOD_ID, "landing_fail");
            return;
        }

        // Use limbAnimator to accurately detect walking state, avoids velocity
        // zero-crossing jitters
        boolean isMoving = player.limbAnimator.getSpeed() > 0.05f;

        // Do not interrupt landing animations unless the player is actively moving
        // (pressing keys)
        if (AnimationUtils.isAnimationPlaying(player, "landing_fail") ||
                AnimationUtils.isAnimationPlaying(player, "landing_success")) {

            boolean hasInput = false;
            if (player instanceof net.minecraft.client.network.ClientPlayerEntity clientPlayer) {
                hasInput = clientPlayer.input.movementForward != 0 || clientPlayer.input.movementSideways != 0;
            }

            // If player isn't pressing movement keys, residual momentum keeps them in
            // landing animation.
            // If they are pressing keys, wait for limb speed to hit normal walking
            // threshold before breaking out.
            // NEW CASE: If they are extremely slow (Slowness 255 stun), stay in
            // landing_fail.
            if (!hasInput || player.limbAnimator.getSpeed() < 0.2f ||
                    (AnimationUtils.isAnimationPlaying(player, "landing_fail") && player.getMovementSpeed() < 0.01f)) {
                return;
            }
        }

        if (isMoving) {
            String walkAnim = player.isSprinting() ? "running" : "walking";
            if (!AnimationUtils.isAnimationPlaying(player, walkAnim)) {
                AnimationUtils.playAnimation(player, MOD_ID, walkAnim);
            }

            // getMovementSpeed() = player speed attribute. Base walk = 0.1f.
            // Scales correctly with Speed effects, Slowness, sprinting, etc.
            final float BASE_WALK_SPEED = 0.1f;
            float speedMultiplier = player.getMovementSpeed() / BASE_WALK_SPEED;
            speedMultiplier = Math.max(0.5f, Math.min(speedMultiplier, 3.0f));
            AnimationUtils.setAnimationSpeed(player, speedMultiplier);
        } else {
            // If not falling, moving, or landing, stop walking if it's playing
            if (AnimationUtils.isAnimationPlaying(player, "walking") ||
                    AnimationUtils.isAnimationPlaying(player, "running")) {
                AnimationUtils.stopAnimation(player);
            }
        }
    }

    private static void handleFlying(AbstractClientPlayerEntity player) {
        boolean isMoving = player.limbAnimator.getSpeed() > 0.4f;

        if (isMoving) {
            String animName = player.isFallFlying() ? "flying_elytra" : "flying";
            if (!AnimationUtils.isAnimationPlaying(player, animName)) {
                AnimationUtils.playAnimation(player, MOD_ID, animName);
            }
            // Optional: speed scaling for flying? User didn't ask but maybe good.
            // For now use default speed.
            AnimationUtils.setAnimationSpeed(player, 1.0f);
        } else {
            // Idle while flying — stop custom animation, let vanilla handle it
            if (AnimationUtils.isAnimationPlaying(player, "flying") ||
                    AnimationUtils.isAnimationPlaying(player, "flying_elytra") ||
                    AnimationUtils.isAnimationPlaying(player, "falling") ||
                    AnimationUtils.isAnimationPlaying(player, "landing_fail") ||
                    AnimationUtils.isAnimationPlaying(player, "landing_success") ||
                    AnimationUtils.isAnimationPlaying(player, "jumping_idle") ||
                    AnimationUtils.isAnimationPlaying(player, "jumping_left") ||
                    AnimationUtils.isAnimationPlaying(player, "jumping_right")) {
                AnimationUtils.stopAnimation(player);
            }
        }
    }
}
