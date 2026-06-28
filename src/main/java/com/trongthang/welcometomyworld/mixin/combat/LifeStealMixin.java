package com.trongthang.welcometomyworld.mixin.combat;

import com.trongthang.welcometomyworld.items.enchantments.LifeStealEnchantment;
import com.trongthang.welcometomyworld.managers.ItemsManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class LifeStealMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void captureTargetHealthBefore(Entity target, CallbackInfo ci) {
        if (!(target instanceof LivingEntity livingTarget))
            return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack weapon = player.getMainHandStack();
        int level = EnchantmentHelper.getLevel(ItemsManager.lifeStealEnchantment, weapon);
        if (level <= 0)
            return;

        welcometomyworld$preAttackHealth.set(livingTarget.getHealth());
        welcometomyworld$preAttackTarget.set(livingTarget);
        welcometomyworld$enchantLevel.set(level);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void applyLifeSteal(Entity target, CallbackInfo ci) {
        LivingEntity livingTarget = welcometomyworld$preAttackTarget.get();
        if (livingTarget == null)
            return;

        float healthBefore = welcometomyworld$preAttackHealth.get();
        int level = welcometomyworld$enchantLevel.get();

        welcometomyworld$preAttackTarget.set(null);
        welcometomyworld$preAttackHealth.set(0f);
        welcometomyworld$enchantLevel.set(0);

        float actualDamage = healthBefore - livingTarget.getHealth();
        if (actualDamage <= 0)
            return;

        float healAmount = Math.min(actualDamage * LifeStealEnchantment.getStealFraction(level),
                LifeStealEnchantment.getHealCap(level));

        PlayerEntity player = (PlayerEntity) (Object) this;
        player.heal(healAmount);
    }

    @Unique
    private static final ThreadLocal<Float> welcometomyworld$preAttackHealth = ThreadLocal.withInitial(() -> 0f);
    @Unique
    private static final ThreadLocal<LivingEntity> welcometomyworld$preAttackTarget = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<Integer> welcometomyworld$enchantLevel = ThreadLocal.withInitial(() -> 0);
}
