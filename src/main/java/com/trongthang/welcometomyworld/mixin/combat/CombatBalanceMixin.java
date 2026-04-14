package com.trongthang.welcometomyworld.mixin.combat;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(LivingEntity.class)
public class CombatBalanceMixin {

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", // Explicit method descriptor
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float modifyDamageAmount(float originalDamage, DamageSource source) {
        Entity attacker = source.getAttacker();
        boolean showMobDamageLogs = com.trongthang.welcometomyworld.ConfigLoader
                .getInstance().damageBalancing.showMobDamageLogs;

        LivingEntity mob = (LivingEntity) (Object) this;

        if (attacker != null) {
            if (source.getType().msgId().equals("thorns")) {
                return originalDamage;
            }
            if (showMobDamageLogs) {
                // only showed if attacker or target is player
                if (!(attacker instanceof PlayerEntity) && !(mob instanceof PlayerEntity))
                    return originalDamage;

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
                        float scaledDamage = applyRuleAction(rule.action, originalDamage, attacker);
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
            float originalDamage, Entity attacker) {
        if (action.fixedValue != null) {
            float fixed = action.fixedValue;
            if (action.maxFinalDamage != null) {
                fixed = Math.min(action.maxFinalDamage, fixed);
            }
            return fixed;
        }

        float base = originalDamage * action.multiplier;
        float bonus = 0f;

        if (action.addMaxHealthFraction > 0 && attacker instanceof LivingEntity livingAttacker) {
            bonus = (float) livingAttacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH)
                    * action.addMaxHealthFraction;
            if (action.maxHealthBonusCap != null) {
                bonus = Math.min(action.maxHealthBonusCap, bonus);
            }
        }

        float finalDamage = base + bonus;
        if (action.maxFinalDamage != null) {
            finalDamage = Math.min(action.maxFinalDamage, finalDamage);
        }

        return finalDamage;
    }

}
