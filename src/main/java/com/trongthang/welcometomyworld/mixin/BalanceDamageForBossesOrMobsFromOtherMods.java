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

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public class BalanceDamageForBossesOrMobsFromOtherMods {
    private static final Map<Identifier, DamageCalculator> DAMAGE_MODIFIERS = new HashMap<>();

    static {
        // Example entries
        DAMAGE_MODIFIERS.put(new Identifier("dungeonnowloading", "chaos_spawner"),
                (original, attacker, damageSource) -> original * 0.85f);

        DAMAGE_MODIFIERS.put(new Identifier("species", "cruncher"),
                (original, attacker, damageSource) -> original + (float) Math.min(150, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.005f));

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "alpha_yeti"),
                (original, attacker, damageSource) -> {
                    if (original <= 15) {
                        if (damageSource.getType().msgId().equals("twilightforest.yeeted")) {
                            return original;
                        }
                        return original + (float) Math.min(70, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.009f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "giant_miner"),
                (original, attacker, damageSource) -> original * 2.2f);

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "ur_ghast"),
                (original, attacker, damageSource) -> {
                    if (original <= 40) {
                        return original + (float) Math.min(80, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.009f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "carminite_ghastguard"),
                (original, attacker, damageSource) -> {
                    if (original <= 15) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.015f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "carminite_ghastling"),
                (original, attacker, damageSource) -> {
                    if (original <= 15) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.02f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "knight_phantom"),
                (original, attacker, damageSource) -> {
                    if (original <= 40) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 2f);
                    }
                    return 0;
                });


        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "slime_beetle"),
                (original, attacker, damageSource) -> {
                    if (original <= 30) {
                        return original * 1.5f;
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "blockchain_goblin"),
                (original, attacker, damageSource) -> {

                    return original * 0.9f;


                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "upper_goblin_knight"),
                (original, attacker, damageSource) -> {
                    return original * 0.9f;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "knight_phantom"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 3f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "minotaur"),
                (original, attacker, damageSource) -> {
                    if (original <= 40) {
                        return Math.min(70, original * 1.5f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("twilightforest", "minoshroom"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return Math.min(70, original * 1.2f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("minecraft", "ghast"),
                (original, attacker, damageSource) -> {
                    if (original <= 40) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.008f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "captain_cornelia"),
                (original, attacker, damageSource) -> {
                    if (original <= 100) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0075f);
                    }
                    return original;
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "maw"),
                (original, attacker, damageSource) -> {
                    return Math.min(100, original * 2);
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "anglerfish"),
                (original, attacker, damageSource) -> {
                    if (original <= 100) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0065f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "tortured_soul"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0068f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "eel"),
                (original, attacker, damageSource) -> {
                    return Math.min(100, original * 1.10f);
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "maze_mother"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0075f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("aquamirae", "maze_mother"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0075f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "sculk_centipede"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.01f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "sculk_leech"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original * 4;
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "sculk_snapper"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return Math.min(100, original * 2);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "shattered"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.0075f);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "shriek_worm"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return original + (float) Math.min(100, attacker.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) * 0.004f);
                    }
                    return 0;
                });


        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "sludge"),
                (original, attacker, damageSource) -> {
                    if (original <= 50) {
                        return Math.min(100, original * 2);
                    }
                    return 0;
                });

        DAMAGE_MODIFIERS.put(new Identifier("deeperdarker", "stalker"),
                (original, attacker, damageSource) -> {
                    return Math.min(100, original * 0.8f);
                });

        DAMAGE_MODIFIERS.put(new Identifier("minecraft", "warden"),
                (original, attacker, damageSource) -> {
                    if (damageSource.getType().msgId().equals("sonic_boom")) {
                        return 10;
                    }
                    return Math.min(100, original * 0.5f);
                });

        DAMAGE_MODIFIERS.put(new Identifier("graveyard", "lich"),
                (original, attacker, damageSource) -> {
                    if (original <= 25) {
                        if (damageSource.getType().msgId().equals("explosion.player")) {
                            return Math.min(original * 1.8f, 40);
                        } else if (damageSource.getType().msgId().equals("indirectMagic")) {
                            return Math.min(original * 1.8f, 40);
                        }
                    }

                    return original;
                });

        DAMAGE_MODIFIERS.put(new Identifier("minecells", "concierge"),
                (original, attacker, damageSource) -> {

                    if (damageSource.getType().msgId().equals("minecells.aura")) {
                        return original * 0.45f;
                    }

                    return original;
                });

        DAMAGE_MODIFIERS.put(new Identifier("minecells", "conjunctivius"),
                (original, attacker, damageSource) -> {

                    if(original > 250){
                        original = 115;
                    } else if(original > 150){
                        original = 85;
                    } else if (original < 50){
                        original = 50;
                    }

                    return Math.min(120, original);
                });
    }

    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", // Explicit method descriptor
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float modifyDamageAmount(float originalDamage, DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker != null) {

//            WelcomeToMyWorld.LOGGER.info("DAMAGE: " + originalDamage);
//            WelcomeToMyWorld.LOGGER.info("ATTACKER: " + source.getAttacker());
//            WelcomeToMyWorld.LOGGER.info("TARGET: " + this);
//            WelcomeToMyWorld.LOGGER.info("ID ATTACKER: " + Registries.ENTITY_TYPE.getId(attacker.getType()).toString());
//            WelcomeToMyWorld.LOGGER.info("TYPE: " + source.getType());

            if (attacker != null) {
                if (source.getType().msgId().equals("thorns")) {
                    return originalDamage;
                }

                DamageCalculator calculator = DAMAGE_MODIFIERS.get(Registries.ENTITY_TYPE.getId(attacker.getType()));

                if (calculator != null) {
                    float scaledDamage = calculator.calculate(originalDamage, (LivingEntity) attacker, source);
//                    WelcomeToMyWorld.LOGGER.info("SCALED DAMAGE: " + scaledDamage);
                    return scaledDamage;
                }
            }
        }

        return originalDamage;
    }
}
