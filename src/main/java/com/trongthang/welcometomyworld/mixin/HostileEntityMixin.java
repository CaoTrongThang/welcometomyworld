package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.GlobalVariables;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalVariables.MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.GlobalVariables.MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dayAndNightCounterAnimationHandler;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    private static final double POWER_UP_CHANCE = 0.16;
    private static final int mobEffectDuration = 600;

    private int checkInterval = 60;
    private int counter = 0;



    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(CallbackInfo ci) {
        MobEntity mobEntity = (MobEntity) (Object) this;

        // Get the current day from your custom handler
        int currentDay = dayAndNightCounterAnimationHandler.currentDay;

        if(currentDay > 100) {
            currentDay = 100;

            EntityAttributeInstance healthAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            EntityAttributeInstance armorAttribute = mobEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);

            if (healthAttribute != null) {
                double baseHealth = healthAttribute.getBaseValue();
                double newHealth = Math.min(baseHealth + (currentDay * random.nextDouble(1, 5)), MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS);
                healthAttribute.setBaseValue(baseHealth + newHealth);
                mobEntity.setHealth((float) (baseHealth + newHealth));
            }


            if (armorAttribute != null) {
                double baseArmor = armorAttribute.getBaseValue();
                double newArmor = Math.min(baseArmor + (currentDay * random.nextFloat(0.1f, 0.2f)), MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS);
                armorAttribute.setBaseValue(baseArmor + newArmor);
            }
        }

    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        HostileEntity hostileEntity = (HostileEntity) (Object) this;

        if (hostileEntity.getTarget() instanceof PlayerEntity) {
            if (random.nextDouble() < POWER_UP_CHANCE) {
                MinecraftServer server = hostileEntity.getServer();
                if (server == null) return;
                ServerWorld world = server.getOverworld();

                Utils.playSound((ServerWorld) hostileEntity.getWorld(), hostileEntity.getBlockPos(), SoundsManager.HOSTILE_MOB_BUFF, 0.2f, random.nextFloat(0.8f, 1.3f));
                spawnParticles(world, hostileEntity.getBlockPos(), ParticleTypes.FLAME);
                applyEffectForMobs(hostileEntity, 3, mobEffectDuration);
            }
        }
    }
}
