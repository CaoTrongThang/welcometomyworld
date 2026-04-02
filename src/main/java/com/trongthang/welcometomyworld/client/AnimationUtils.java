package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.ConfigLoader;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public class AnimationUtils {
    /**
     * Plays a keyframe animation for the given player.
     * 
     * @param player        The player to animate
     * @param modId         The mod ID providing the animation
     * @param animationName The name of the animation (jsond file name in
     *                      assets/modid/animations/)
     */
    public static void playAnimation(AbstractClientPlayerEntity player, String modId, String animationName) {
        if (!ConfigLoader.getInstance().enableCustomAnimations) {
            return;
        }

        IAnimatedPlayer animatedPlayer = (IAnimatedPlayer) player;
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(new Identifier(modId, animationName));
        if (anim != null) {
            animatedPlayer.welcometomyworld_playAnimation(anim, animationName);
        }
    }

    public static void stopAnimation(AbstractClientPlayerEntity player) {
        ((IAnimatedPlayer) player).welcometomyworld_stopAnimation();
    }

    public static boolean isAnimationPlaying(AbstractClientPlayerEntity player, String animationName) {
        return ((IAnimatedPlayer) player).welcometomyworld_isAnimationPlaying(animationName);
    }
}
