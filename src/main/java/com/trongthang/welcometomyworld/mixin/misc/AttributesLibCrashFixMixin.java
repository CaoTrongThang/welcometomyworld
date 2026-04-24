package com.trongthang.welcometomyworld.mixin.misc;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "dev.shadowsoffire.attributeslib.client.AttributesLibClient")
public class AttributesLibCrashFixMixin {

    @WrapOperation(method = "getSortedModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Multimap;put(Ljava/lang/Object;Ljava/lang/Object;)Z"), remap = false)
    private static boolean preventCrashOnPut(Multimap<Object, Object> instance, Object key, Object value,
            Operation<Boolean> original) {
        try {
            return original.call(instance, key, value);
        } catch (NullPointerException e) {
            System.err
                    .println("[WelcomeToMyWorld] Prevented Zenith Attributes NPE crash on attribute modifier sorting.");
            return false;
        }
    }
}
