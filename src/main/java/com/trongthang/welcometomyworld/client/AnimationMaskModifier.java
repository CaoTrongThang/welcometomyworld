package com.trongthang.welcometomyworld.client;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class AttackMaskModifier extends AbstractModifier {
    private final AbstractClientPlayerEntity player;

    public AttackMaskModifier(AbstractClientPlayerEntity player) {
        this.player = player;
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value) {
        // When swinging or carrying an object (CarryOn), pass `value` straight through
        // so the mod animation has no effect on arms.
        if ((player.handSwinging || com.trongthang.welcometomyworld.compat.CompatManager.isCarrying(player))
                && (modelName.equals("rightArm") || modelName.equals("leftArm")
                        || modelName.equals("rightItem") || modelName.equals("leftItem"))) {
            return value;
        }
        return super.get3DTransform(modelName, type, tickDelta, value);
    }
}
