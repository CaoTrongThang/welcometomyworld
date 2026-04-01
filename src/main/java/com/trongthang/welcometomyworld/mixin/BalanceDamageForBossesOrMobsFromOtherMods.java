package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.DamageCalculator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(LivingEntity.class)
public class BalanceDamageForBossesOrMobsFromOtherMods {

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", // Explicit method descriptor
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyDamageAmount(float originalDamage, DamageSource source) {
        Entity attacker = source.getAttacker();
        boolean showMobDamageLogs = com.trongthang.welcometomyworld.ConfigLoader
                .getInstance().damageBalancing.showMobDamageLogs;

        if (attacker != null) {
            if (source.getType().msgId().equals("thorns")) {
                return originalDamage;
            }
            if (showMobDamageLogs) {
                WelcomeToMyWorld.LOGGER.info("DAMAGE: " + originalDamage);
                WelcomeToMyWorld.LOGGER.info("ATTACKER: " + source.getAttacker());
                WelcomeToMyWorld.LOGGER.info("TARGET: " + this);
                WelcomeToMyWorld.LOGGER
                        .info("ID ATTACKER: " +
                                Registries.ENTITY_TYPE.getId(attacker.getType()).toString());
                WelcomeToMyWorld.LOGGER.info("TYPE: " + source.getType());
            }

            Identifier attackerId = Registries.ENTITY_TYPE.getId(attacker.getType());
            List<com.trongthang.welcometomyworld.ConfigLoader.DamageRuleConfig> rules = getRulesForMob(attackerId);

            if (rules != null) {
                for (com.trongthang.welcometomyworld.ConfigLoader.DamageRuleConfig rule : rules) {
                    if (ruleMatches(rule.condition, originalDamage, source)) {
                        float scaledDamage = applyRuleAction(rule.action, originalDamage, (LivingEntity) attacker);
                        if (showMobDamageLogs) {
                            WelcomeToMyWorld.LOGGER.info("SCALED DAMAGE: " + scaledDamage);
                        }
                        return scaledDamage;
                    }
                }
            }
        }

        return originalDamage;
    }

    private List<com.trongthang.welcometomyworld.ConfigLoader.DamageRuleConfig> getRulesForMob(Identifier id) {
        java.util.Map<String, List<com.trongthang.welcometomyworld.ConfigLoader.DamageRuleConfig>> mobs = com.trongthang.welcometomyworld.ConfigLoader
                .getInstance().damageBalancing.mobs;
        String idStr = id.toString();
        if (mobs.containsKey(idStr))
            return mobs.get(idStr);
        String wildcard = id.getNamespace() + ":*";
        if (mobs.containsKey(wildcard))
            return mobs.get(wildcard);
        if (mobs.containsKey("*"))
            return mobs.get("*");
        return null;
    }

    private boolean ruleMatches(com.trongthang.welcometomyworld.ConfigLoader.DamageConditionConfig condition,
            float originalDamage, DamageSource source) {
        if (condition.minOriginalDamage != null && originalDamage < condition.minOriginalDamage)
            return false;
        if (condition.maxOriginalDamage != null && originalDamage > condition.maxOriginalDamage)
            return false;

        String msgId = source.getType().msgId();
        if (condition.damageSourceTypes != null && !condition.damageSourceTypes.contains(msgId))
            return false;
        if (condition.excludeDamageSourceTypes != null && condition.excludeDamageSourceTypes.contains(msgId))
            return false;

        return true;
    }

    private float applyRuleAction(com.trongthang.welcometomyworld.ConfigLoader.DamageActionConfig action,
            float originalDamage, LivingEntity attacker) {
        if (action.fixedValue != null) {
            return Math.min(action.maxFinalDamage, action.fixedValue);
        }

        float base = originalDamage * action.multiplier;
        float bonus = 0f;
        if (action.addMaxHealthFraction > 0) {
            bonus = Math.min(action.maxHealthBonusCap,
                    (float) attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)
                            * action.addMaxHealthFraction);
        }

        return Math.min(action.maxFinalDamage, base + bonus);
    }

}
