package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;

@Mixin(LivingEntity.class)
public class IncreaseHealthRegenOfHostileMobs {

    @Unique
    private HashSet<LivingEntity> enemies = new HashSet<>();

    @Unique
    private int outCombatCounter = 0;

    private int outCombatTime = 600;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if(entity.getWorld().isClient) return;

        if(this.outCombatTime > 0){
            this.outCombatCounter--;
        }


        if(enemies.isEmpty()) return;
        if(enemies.size() <= 3) return;

        if(outCombatCounter <= 0 && !enemies.isEmpty()){
            enemies.clear();
            return;
        }

        if (entity instanceof HostileEntity) {
            if(entity.getWorld().getTickOrder() % 40 == 0){
                applyHealthRegen();
            }

        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci) {

        if(source.getAttacker() == null) return;

        Entity entity =  source.getAttacker();

        if(!((LivingEntity)(Object) this instanceof HostileEntity)) return;

        if(source.getAttacker() != entity) return;

        if(entity.getWorld().isClient) return;
        if (!enemies.contains(entity)) {
            if (entity instanceof PlayerEntity) {
                enemies.add((LivingEntity) entity);
                this.outCombatCounter = outCombatTime;
            } else if (entity instanceof TameableEntity tameable){
                if(tameable.isTamed() && tameable.getOwner() != null){
                    enemies.add((LivingEntity) entity);
                    this.outCombatCounter = outCombatTime;
                }
            }
        }
    }


    private void applyHealthRegen() {
        LivingEntity entity = (LivingEntity) (Object) this;
        int enemyCount = enemies.size();

        float maxHealth = entity.getMaxHealth();
        int additionalEnemies = enemyCount - 3;

        float regenPercentage = Math.min(additionalEnemies * 0.0005f, 0.01f);
        float regenAmount = Math.min(maxHealth * regenPercentage, 1000);

        entity.setHealth(Math.min(entity.getHealth() + regenAmount, maxHealth));
    }
}
