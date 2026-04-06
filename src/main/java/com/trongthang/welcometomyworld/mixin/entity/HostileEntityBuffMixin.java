package com.trongthang.welcometomyworld.mixin.entity;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
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
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin(MobEntity.class)
public class HostileEntityBuffMixin {

    @Shadow
    protected GoalSelector targetSelector;

    @Unique
    private boolean isMobBuffed = false;

    @Unique
    private double getDifficulty(MobEntity mob) {
        float difficulty = com.trongthang.welcometomyworld.compat.ImprovedMobsCompat.getDifficulty(mob.getWorld(), mob);
        if (difficulty >= 0) {
            return difficulty / 4.6; // Scale difficulty to match day-based progression roughly
        }
        long currentTime = mob.getWorld().getTimeOfDay();
        return (double) currentTime / 24000;
    }

    private void buff(MobEntity mobEntity) {

        if (this.isMobBuffed) {
            return;
        }

        this.isMobBuffed = true;

        double currentDay = Math.min(getDifficulty(mobEntity), 200);

        EntityAttributeInstance healthAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        EntityAttributeInstance armorAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

        if (healthAttribute != null) {
            double baseHealth = healthAttribute.getBaseValue();
            double newHealth = Math.min(baseHealth + (currentDay / 10) * random.nextDouble(0, 2.5f),
                    MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS);
            healthAttribute.setBaseValue(baseHealth + newHealth);
            mobEntity.setHealth((float) (baseHealth + newHealth));
        }

        if (armorAttribute != null) {
            double baseArmor = armorAttribute.getBaseValue();
            double newArmor = Math.min(baseArmor + (currentDay * random.nextFloat(0.1f, 0.2f)),
                    MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS);
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
    protected void initDataTracker(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            @Nullable EntityData entityData, @Nullable NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
        MobEntity mobEntity = (MobEntity) (Object) this;
        Identifier mobId = EntityType.getId(mobEntity.getType());
        String mobIdString = mobId.toString();

        ConfigLoader.MobFixedStatsConfig config = ConfigLoader.getInstance().mobsSetFixedStats.mobs
                .get(mobIdString);

        if (config == null) {
            buff(mobEntity);
        }

        Utils.addRunAfter(() -> {
            if (mobEntity instanceof HostileEntity entity) {
                if (!(entity instanceof EndermanEntity) && !(entity instanceof Unknown)) {
                    this.targetSelector.add(15, new ActiveTargetGoal<>(entity, TameableEntity.class, true,
                            target -> !Registries.ENTITY_TYPE.getId(target.getType()).getNamespace()
                                    .contains("iceandfire")));
                }
            }
        }, 20);
    }
}
