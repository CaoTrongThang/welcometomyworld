package com.trongthang.welcometomyworld.mixin.combat;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PlayerEntity.class)
public class PlayerDamageDebugMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!ConfigLoader.getInstance().showPlayerDamageDebugLogs)
            return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient)
            return;

        Entity attacker = source.getAttacker();
        Entity sourceEntity = source.getSource(); // This is the projectile if it's a ranged attack

        boolean isProjectile = sourceEntity instanceof ProjectileEntity;

        String context = isProjectile ? "shot" : "hit";
        String attackerName = attacker != null ? attacker.getName().getString() : "Unknown";
        String sourceName = (isProjectile && sourceEntity != null)
                ? " (via " + sourceEntity.getType().getName().getString() + ")"
                : "";

        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack weapon = livingAttacker.getMainHandStack();
            // If it's a projectile, check both hands for a bow/crossbow just in case, but
            // usually main hand is fine.
            if (weapon.isEmpty() && isProjectile) {
                weapon = livingAttacker.getOffHandStack();
            }

            if (!weapon.isEmpty()) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(weapon);
                if (!enchantments.isEmpty()) {
                    WelcomeToMyWorld.LOGGER.info("Player {} by {}{} using {} with enchantments:",
                            context,
                            attackerName,
                            sourceName,
                            weapon.getItem().toString());

                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        Identifier id = Registries.ENCHANTMENT.getId(entry.getKey());
                        WelcomeToMyWorld.LOGGER.info(" - {}: level {}", id, entry.getValue());
                    }
                } else {
                    WelcomeToMyWorld.LOGGER.info("Player {} by {}{} using {} (no enchantments)",
                            context,
                            attackerName,
                            sourceName,
                            weapon.getItem().toString());
                }
            } else {
                WelcomeToMyWorld.LOGGER.info("Player {} by {}{} (no weapon)", context, attackerName, sourceName);
            }
        } else if (sourceEntity != null && sourceEntity != attacker) {
            WelcomeToMyWorld.LOGGER.info("Player {} by {} (Source: {})", context, attackerName,
                    sourceEntity.getType().getName().getString());
        } else {
            WelcomeToMyWorld.LOGGER.info("Player {} by {} using source type: {}", context, attackerName,
                    source.getType().msgId());
        }
    }
}
