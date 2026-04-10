package com.trongthang.welcometomyworld.mixin.client;

import com.trongthang.welcometomyworld.client.CameraShakeManager;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    private float pitch;

    @Shadow
    private float yaw;

    @Inject(method = "update", at = @At("TAIL"))
    private void addCameraShake(CallbackInfo ci) {
        float pitchOffset = CameraShakeManager.getPitchOffset();
        float yawOffset = CameraShakeManager.getYawOffset();

        if (pitchOffset != 0.0f || yawOffset != 0.0f) {
            this.setRotation(this.yaw + yawOffset, this.pitch + pitchOffset);
        }
    }
}
