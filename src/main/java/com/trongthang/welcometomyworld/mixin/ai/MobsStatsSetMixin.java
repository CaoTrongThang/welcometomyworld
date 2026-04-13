package com.trongthang.welcometomyworld.mixin.ai;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobEntity.class, priority = 10000)
public class MobsStatsSetMixin {

        @Inject(method = "initialize", at = @At("RETURN"))
        private void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                        @Nullable EntityData entityData, @Nullable NbtCompound entityNbt,
                        CallbackInfoReturnable<EntityData> cir) {
                MobEntity mob = (MobEntity) (Object) this;
                Identifier mobId = EntityType.getId(mob.getType());
                String mobIdString = mobId.toString();

                ConfigLoader.MobFixedStatsConfig config = ConfigLoader.getInstance().mobsSetFixedStats.mobs
                                .get(mobIdString);

                if (config != null) {
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
