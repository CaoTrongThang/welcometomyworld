package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameableEntity.class)
public class TameableEntityMixin implements TameableEntityInterface {
    @Unique
    private int damageLevel = 0;
    @Unique
    private int healthLevel = 0;
    @Unique
    private int defenseLevel = 0;
    @Unique
    private int speedLevel = 0;
    @Unique
    private float currentLevel = 0;
    @Unique
    private float nextLevelRequireExp = 20; // Default value
    @Unique
    private float currentLevelExp = 0;
    @Unique
    private int pointAvailable = 0;

    // Inject into the constructor to initialize default values
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.nextLevelRequireExp = 20; // Ensure default value is set
    }

    // Save custom data to NBT
    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void injectWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("DamageLevel", this.damageLevel);
        nbt.putInt("HealthLevel", this.healthLevel);
        nbt.putInt("DefenseLevel", this.defenseLevel);
        nbt.putInt("SpeedLevel", this.speedLevel);
        nbt.putFloat("CurrentLevel", this.currentLevel);
        nbt.putFloat("NextLevelRequireExp", this.nextLevelRequireExp);
        nbt.putFloat("CurrentLevelExp", this.currentLevelExp);
        nbt.putInt("PointAvailable", this.pointAvailable);
    }

    // Load custom data from NBT
    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void injectReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.damageLevel = nbt.contains("DamageLevel") ? nbt.getInt("DamageLevel") : 0;
        this.healthLevel = nbt.contains("HealthLevel") ? nbt.getInt("HealthLevel") : 0;
        this.defenseLevel = nbt.contains("DefenseLevel") ? nbt.getInt("DefenseLevel") : 0;
        this.speedLevel = nbt.contains("SpeedLevel") ? nbt.getInt("SpeedLevel") : 0;
        this.currentLevel = nbt.contains("CurrentLevel") ? nbt.getFloat("CurrentLevel") : 0;
        this.nextLevelRequireExp = nbt.contains("NextLevelRequireExp") ? nbt.getFloat("NextLevelRequireExp") : 20; // Default value
        this.currentLevelExp = nbt.contains("CurrentLevelExp") ? nbt.getFloat("CurrentLevelExp") : 0;
        this.pointAvailable = nbt.contains("PointAvailable") ? nbt.getInt("PointAvailable") : 0;
    }

    // Getters and Setters (unchanged)
    @Override
    public int getDamageLevel() {
        return this.damageLevel;
    }

    @Override
    public void setDamageLevel(int damageLevel) {
        this.damageLevel = damageLevel;
    }

    @Override
    public int getHealthLevel() {
        return this.healthLevel;
    }

    @Override
    public void setHealthLevel(int healthLevel) {
        this.healthLevel = healthLevel;
    }

    @Override
    public int getDefenseLevel() {
        return this.defenseLevel;
    }

    @Override
    public void setDefenseLevel(int defenseLevel) {
        this.defenseLevel = defenseLevel;
    }

    @Override
    public int getSpeedLevel() {
        return this.speedLevel;
    }

    @Override
    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    @Override
    public float getCurrentLevel() {
        return this.currentLevel;
    }

    @Override
    public void setCurrentLevel(float currentLevel) {
        this.currentLevel = currentLevel;
    }

    @Override
    public float getNextLevelRequireExp() {
        return this.nextLevelRequireExp;
    }

    @Override
    public void setNextLevelRequireExp(float nextLevelRequireExp) {
        this.nextLevelRequireExp = nextLevelRequireExp;
    }

    @Override
    public float getCurrentLevelExp() {
        return this.currentLevelExp;
    }

    @Override
    public void setCurrentLevelExp(float currentLevelExp) {
        this.currentLevelExp = currentLevelExp;
    }

    @Override
    public int getPointAvailalble() {
        return this.pointAvailable;
    }

    @Override
    public void setPointAvailalble(int pointAvailable) {
        this.pointAvailable = pointAvailable;
    }
}