package com.trongthang.welcometomyworld.mixin.player;

import com.mojang.authlib.GameProfile;
import com.trongthang.welcometomyworld.client.AnimationMaskModifier;
import com.trongthang.welcometomyworld.client.IAnimatedPlayer;
import com.trongthang.welcometomyworld.client.PlayerAnimationHandler;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements IAnimatedPlayer {

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique
    private final ModifierLayer<IAnimation> modAnimationContainer = new ModifierLayer<>();

    @Unique
    private String currentAnimationName = null;

    @Unique
    private boolean lastOnGround = true;

    @Unique
    private float lastFallDistance = 0;

    @Unique
    private boolean lastJumpLeft = false;
    @Unique
    private double fallStartHeight = -1;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        PlayerAnimationAccess.getPlayerAnimLayer((AbstractClientPlayerEntity) (Object) this).addAnimLayer(1000,
                modAnimationContainer);
        // Add a modifier to mask arms when player is swinging (attacking)
        modAnimationContainer.addModifier(new AnimationMaskModifier((AbstractClientPlayerEntity) (Object) this), 0);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void welcometomyworld_onTickHead(CallbackInfo ci) {
        if (this.getWorld().isClient) {
            this.lastFallDistance = this.fallDistance;

            // Track starting height for falls (even for Slow Falling)
            if (this.isOnGround() || this.getAbilities().flying || this.isFallFlying()) {
                fallStartHeight = this.getY();
            } else if (this.getY() > fallStartHeight) {
                fallStartHeight = this.getY();
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void welcometomyworld_onTickTail(CallbackInfo ci) {
        if (this.getWorld().isClient) {
            PlayerAnimationHandler.update((AbstractClientPlayerEntity) (Object) this, lastOnGround, lastFallDistance);
            lastOnGround = this.isOnGround();
        }
    }

    @Override
    public ModifierLayer<IAnimation> welcometomyworld_getModAnimation() {
        return modAnimationContainer;
    }

    @Override
    public void welcometomyworld_playAnimation(KeyframeAnimation anim, String name) {
        if (anim != null) {
            KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(anim);

            // If we are currently playing an animation, transition to the new one directly
            if (this.currentAnimationName != null) {
                modAnimationContainer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(3, Ease.INOUTQUAD),
                        animPlayer);
            } else {
                modAnimationContainer.setAnimation(animPlayer);
                modAnimationContainer.addModifier(AbstractFadeModifier.standardFadeIn(5, Ease.INOUTQUAD), 0);
            }

            this.currentAnimationName = name;
        }
    }

    @Override
    public boolean welcometomyworld_isAnimationPlaying(String animationName) {
        return modAnimationContainer.isActive() && animationName.equals(currentAnimationName);
    }

    @Override
    public void welcometomyworld_stopAnimation() {
        if (this.currentAnimationName != null) {
            modAnimationContainer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(3, Ease.INOUTQUAD),
                    null);
            currentAnimationName = null;
        }
    }

    @Override
    public boolean welcometomyworld_isLastJumpLeft() {
        return this.lastJumpLeft;
    }

    @Override
    public void welcometomyworld_setLastJumpLeft(boolean val) {
        this.lastJumpLeft = val;
    }

    @Override
    public double welcometomyworld_getFallStartHeight() {
        return this.fallStartHeight;
    }
}
