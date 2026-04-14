package com.trongthang.welcometomyworld.mixin.ai;

import com.trongthang.welcometomyworld.ConfigLoader;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerWorld.class, priority = 10000)
public class MobsStatsSetMixin {

        @Inject(method = "addEntity", at = @At("TAIL"), cancellable = true)
        private void onAddedToWorld(Entity entity, CallbackInfoReturnable<Boolean> ci) {
                if (!(entity instanceof MobEntity))
                        return;

                MobEntity mob = (MobEntity) entity;

                // Mark with a tag so we only set stats once per lifespan
                if (mob.getCommandTags().contains("WTMW_StatsSet"))
                        return;

                Identifier mobId = EntityType.getId(mob.getType());
                String mobIdString = mobId.toString();

                ConfigLoader.MobFixedStatsConfig config = ConfigLoader.getInstance().mobsSetFixedStats.mobs
                                .get(mobIdString);

                if (config != null) {
                        mob.addCommandTag("WTMW_StatsSet");

                        if (config.maxHealth != null) {
                                EntityAttributeInstance healthAttr = mob
                                                .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                                if (healthAttr != null) {
                                        healthAttr.setBaseValue(config.maxHealth);
                                        mob.setHealth(config.maxHealth);
                                }
                        }

                        if (config.damage != null) {
                                EntityAttributeInstance damageAttr = mob
                                                .getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                                if (damageAttr != null) {
                                        damageAttr.setBaseValue(config.damage);
                                }
                        }

                        if (config.armor != null) {
                                EntityAttributeInstance armorAttr = mob
                                                .getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
                                if (armorAttr != null) {
                                        armorAttr.setBaseValue(config.armor);
                                }
                        }
                }
        }
}
