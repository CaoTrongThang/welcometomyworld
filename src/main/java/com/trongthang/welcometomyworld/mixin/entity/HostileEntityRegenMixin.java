package com.trongthang.welcometomyworld.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mixin(LivingEntity.class)
public class HostileEntityRegenMixin {

    @Unique
    private final Map<LivingEntity, Integer> enemies = new HashMap<>();

    @Unique
    private int outCombatCounter = 0;

    private final int outCombatTime = 600;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient)
            return;

        if (entity.getHealth() <= 0) {
            return;
        }

        if (this.outCombatTime > 0) {
            this.outCombatCounter--;
        }

        // Cleanup enemies map
        if (!enemies.isEmpty()) {
            Iterator<Map.Entry<LivingEntity, Integer>> iterator = enemies.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LivingEntity, Integer> entry = iterator.next();
                LivingEntity enemy = entry.getKey();
                int timer = entry.getValue();

                if (timer <= 0 || !enemy.isAlive() || enemy.isRemoved()) {
                    iterator.remove();
                } else {
                    entry.setValue(timer - 1);
                }
            }
        }

        if (enemies.isEmpty())
            return;

        if (enemies.size() <= 3)
            return;

        if (entity instanceof HostileEntity) {
            if (entity.getWorld().getTickOrder() % 40 == 0) {
                applyHealthRegen();
            }
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (!((Object) this instanceof HostileEntity))
            return;

        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity livingAttacker && !attacker.getWorld().isClient) {
            enemies.put(livingAttacker, outCombatTime);
            this.outCombatCounter = outCombatTime;
        }
    }

    private void applyHealthRegen() {
        LivingEntity entity = (LivingEntity) (Object) this;
        int enemyCount = enemies.size();

        float maxHealth = entity.getMaxHealth();
        int additionalEnemies = enemyCount - 3;

        float regenPercentage = Math.min(additionalEnemies * 0.0001f, 0.01f);
        float regenAmount = Math.min(maxHealth * regenPercentage, 150);

        entity.setHealth(Math.min(entity.getHealth() + regenAmount, maxHealth));
    }
}
