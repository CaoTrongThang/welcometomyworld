package com.trongthang.welcometomyworld.mixin.combat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards against enchantments with a null `target` field (e.g. broken
 * registrations
 * from other mods like Apotheosis custom enchants) crashing the server when
 * ImprovedMobs tries to enchant mob gear on entity load.
 *
 * Crash chain: ImprovedMobs.enchantGear →
 * Apotheosis.getAvailableEnchantmentResults
 * → TridentItem.canApplyAtEnchantingTable → ench.target.isAcceptableItem (NPE)
 */
@Mixin(TridentItem.class)
public class TridentEnchantNullGuardMixin {

    /**
     * NOTE: "canApplyAtEnchantingTable" is NOT a vanilla method. It is added by
     * Zenith (Apotheosis)
     * at runtime via its own mixins (which is why the crash log shows it at line
     * 2022).
     * We use remap = false so this mixin works even if the method isn't in the
     * vanilla source.
     */
    @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false)
    private void guardNullTarget(Enchantment ench, CallbackInfoReturnable<Boolean> cir) {
        if (ench == null || ench.target == null) {
            cir.setReturnValue(false);
        }
    }
}
