package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.trongthang.welcometomyworld.GlobalVariables.MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.GlobalVariables.MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

@Mixin(MobEntity.class)
public class HostileEntityStatsBuffAndAttackTameableCreatures {

    @Shadow
    protected GoalSelector targetSelector;

    @Unique
    private boolean isMobBuffed = false;

    private void buff() {
        MobEntity mobEntity = (MobEntity) (Object) this;

        if (this.isMobBuffed) {
            return;
        }

        this.isMobBuffed = true;

        long currentTime = mobEntity.getWorld().getTimeOfDay();
        double currentDay = Math.min(currentTime / 24000, 200);

        EntityAttributeInstance healthAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        EntityAttributeInstance armorAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

        if (healthAttribute != null) {
            double baseHealth = healthAttribute.getBaseValue();
            double newHealth = Math.min(baseHealth + (currentDay / 10) * random.nextDouble(0, 2.5f), MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS);
            healthAttribute.setBaseValue(baseHealth + newHealth);
            mobEntity.setHealth((float) (baseHealth + newHealth));
        }

        if (armorAttribute != null) {
            double baseArmor = armorAttribute.getBaseValue();
            double newArmor = Math.min(baseArmor + (currentDay * random.nextFloat(0.1f, 0.2f)), MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS);
            armorAttribute.setBaseValue(baseArmor + newArmor);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("isMobBuffed", this.isMobBuffed);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("isMobBuffed")) {
            this.isMobBuffed = nbt.getBoolean("isMobBuffed");
        }
    }

    @Inject(method = "initialize", at = @At("TAIL"))
    protected void initDataTracker(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
        MobEntity mobEntity = (MobEntity) (Object) this;

        buff();

        Utils.addRunAfter(() -> {
            if (mobEntity instanceof HostileEntity entity)
            {
                if (!(entity instanceof EndermanEntity)) {
                    this.targetSelector.add(15, new ActiveTargetGoal<>(entity, TameableEntity.class, true));
                }
            }
        }, 10);
    }
}
