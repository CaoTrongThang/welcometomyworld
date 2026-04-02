package com.trongthang.welcometomyworld.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;

public interface IAnimatedPlayer {
    /**
     * @return Mod animation container for this player
     */
    ModifierLayer<IAnimation> welcometomyworld_getModAnimation();

    void welcometomyworld_playAnimation(dev.kosmx.playerAnim.core.data.KeyframeAnimation anim, String name);

    boolean welcometomyworld_isAnimationPlaying(String animationName);

    void welcometomyworld_stopAnimation();

    boolean welcometomyworld_isLastJumpLeft();

    void welcometomyworld_setLastJumpLeft(boolean val);

    double welcometomyworld_getFallStartHeight();
}
