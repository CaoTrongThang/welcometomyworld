package com.trongthang.welcometomyworld.mixin.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 10000)
public class ZenithDamageCapMixin {

    @Unique
    private static EntityAttribute cachedAttr = null;
    @Unique
    private static boolean attemptedResolve = false;

    @Unique
    private static EntityAttribute resolveAttr() {
        if (attemptedResolve)
            return cachedAttr;
        attemptedResolve = true;
        try {
            cachedAttr = Registries.ATTRIBUTE.get(new Identifier("zenith_attributes", "current_hp_damage"));
            if (cachedAttr == null)
                cachedAttr = Registries.ATTRIBUTE.get(new Identifier("zenith", "current_hp_damage"));
        } catch (Exception ignored) {
        }
        return cachedAttr;
    }

    private static final float MAX_ADDITIONAL_DAMAGE = 100.0f;

    @ModifyVariable(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float capZenithCurrentHpDamage(float amount, DamageSource source) {
        String sourceName = source.getName();

        // If the damage is specifically from Zenith's HP% attribute event
        if ("zenith_attributes:current_hp_damage".equals(sourceName) || "zenith:current_hp_damage".equals(sourceName)) {
            if (amount > MAX_ADDITIONAL_DAMAGE) {
                return MAX_ADDITIONAL_DAMAGE;
            }
        }

        // Safety check for normal hits if Zenith somehow bundles it (though logs show
        // it doesn't)
        // We resolve the attribute to keep the logic robust if things change.
        EntityAttribute attr = resolveAttr();
        if (attr == null)
            return amount;

        if (!(source.getAttacker() instanceof LivingEntity attacker))
            return amount;
        if (!attacker.getAttributes().hasAttribute(attr))
            return amount;

        float attrValue = (float) attacker.getAttributeValue(attr);
        if (attrValue <= 0)
            return amount;

        // Note: For source "player", we don't want to subtract extraDamage anymore
        // because we see Zenith deals it as a separate
        // "zenith_attributes:current_hp_damage" source.

        return amount;
    }
}
