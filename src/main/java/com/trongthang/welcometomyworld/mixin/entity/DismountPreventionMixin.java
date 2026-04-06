package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.interfaces.PhantomGrabber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class DismountPreventionMixin {

    @Shadow
    public abstract World getWorld();

    @Inject(method = "dismountVehicle", at = @At("HEAD"), cancellable = true)
    public void onDismountVehicle(CallbackInfo ci) {
        Entity vehicle = ((Entity) (Object) this).getVehicle();

        if (vehicle == null || this.getWorld().isClient)
            return;

        Entity entity = (Entity) (Object) this;

        if (vehicle instanceof Unknown unknown) {
            boolean usingSkill = unknown.isUsingSkill();
            int skillId = unknown.getSkillId();
            if (usingSkill && skillId == Unknown.GRAB_JUMP_SLAM.id) {
                ci.cancel();
            }
        }

        // check if phantom then also return
        if (vehicle instanceof PhantomEntity phantom) {
            if (entity instanceof PlayerEntity player) {
                // If the phantom is explicitly grabbing the player
                if (phantom instanceof PhantomGrabber grabber && grabber.welcomeToMyWorld$isGrabbingPlayer()) {
                    // Check drop conditions as safety stop for the cancellation
                    if (player.isCreative() || player.isSpectator() || player.isDead() ||
                            phantom.getHealth() < phantom.getMaxHealth() / 3 ||
                            phantom.getY() > 300 ||
                            !phantom.getWorld().isAir(phantom.getBlockPos().up(1)) ||
                            phantom.getPhantomSize() < 9) {
                        return;
                    }
                    ci.cancel();
                }
            }
        }
    }
}
