package com.trongthang.welcometomyworld.mixin.ai;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.features.MobsGearsUp;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

@Mixin(MobEntity.class)
public class HostileMobsStrongerInBloodMoonMixin {
        @Inject(method = "initialize", at = @At("HEAD"))
        private void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                        @Nullable EntityData entityData, @Nullable NbtCompound entityNbt,
                        CallbackInfoReturnable<EntityData> cir) {
                MobEntity mob = (MobEntity) (Object) this;

                if (WelcomeToMyWorld.dataHandler.worldData.isBloodMoon && mob instanceof HostileEntity) {
                        Utils.addRunAfter(() -> {
                                mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                                                .setBaseValue(mob.getMaxHealth() + (mob.getMaxHealth() * 2f));
                                mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(
                                                mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                                                .getBaseValue() + 15);
                                mob.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                                                .setBaseValue(mob.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                                                                .getBaseValue() + 15);
                                mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                                                .setBaseValue(mob
                                                                .getAttributeInstance(
                                                                                EntityAttributes.GENERIC_MOVEMENT_SPEED)
                                                                .getBaseValue()
                                                                + 0.2f);

                                mob.setHealth(mob.getMaxHealth());
                        }, 30);
                }
        }
}
