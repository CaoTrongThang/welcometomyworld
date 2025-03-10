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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalVariables.MAX_ARMOR_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.GlobalVariables.MAX_HEALTH_ADD_TO_HOSTILE_MOB_BY_DAYS;
import static com.trongthang.welcometomyworld.Utilities.Utils.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {

    private static final double POWER_UP_CHANCE = 0.18;
    private static final int mobEffectDuration = 200;

    private int checkInterval = 80;
    private int counter = 0;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        HostileEntity hostileEntity = (HostileEntity) (Object) this;

        if(hostileEntity.getWorld().isDay()) return;

        if (hostileEntity.getTarget() instanceof PlayerEntity) {
            if (random.nextDouble() < POWER_UP_CHANCE) {
                MinecraftServer server = hostileEntity.getServer();
                if (server == null) return;
                ServerWorld world = server.getOverworld();

                Utils.summonLightning(hostileEntity.getBlockPos(), world, true);
                spawnParticles(world, hostileEntity.getBlockPos(), ParticleTypes.FLAME);
                applyEffectForMobs(hostileEntity, 3, mobEffectDuration);
            }
        }
    }


}
