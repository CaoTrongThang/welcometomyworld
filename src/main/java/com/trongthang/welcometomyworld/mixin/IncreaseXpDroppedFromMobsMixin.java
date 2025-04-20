package com.trongthang.welcometomyworld.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin(MobEntity.class)
public class IncreaseXpDroppedFromMobsMixin {
    @ModifyReturnValue(
            method = "getXpToDrop()I",
            at = @At("RETURN")
    )
    private int scaleXpBasedOnStats(int originalXp) {
        MobEntity mob = (MobEntity) (Object) this;
        if (!(mob instanceof HostileEntity)) return originalXp;

        float maxHealth = mob.getMaxHealth();
        double armor = mob.getAttributeValue(EntityAttributes.GENERIC_ARMOR);

        double healthXP = Math.sqrt(maxHealth * 0.35f);

        double armorXP = armor * 0.25;

        // Combine and round
        int scaledXp = (int) (healthXP + armorXP);

        scaledXp = Math.max(scaledXp, originalXp);
        scaledXp = Math.min(scaledXp, 100);

        if(scaledXp <= originalXp){
            return originalXp;
        }

        return scaledXp;
    }
}
