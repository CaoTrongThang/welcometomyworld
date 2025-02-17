package com.trongthang.welcometomyworld.features;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class NauseaInWaterHandler {
    public int checkInverval = 60;
    public int counter = 0;

    public int effectDuration = 120;
    public int amplifier = 0;

    public double chance = 0.3;

    Random rand = new Random();
    public void onServerTick(MinecraftServer server){
        counter++;
        if(counter <= checkInverval) return;
        counter = 0;

        double r = rand.nextDouble();

        if(r > chance) return;
        for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
            if(player.isCreative() || player.isSpectator()) return;
            if(player.getInventory().armor.get(3).isEmpty()){
                if(player.isInsideWaterOrBubbleColumn() && player.isSubmergedInWater()){
                    StatusEffectInstance currentEffect = player.getStatusEffect(StatusEffects.NAUSEA);

                    if (currentEffect != null) {
                        int newAmplifier = Math.min(currentEffect.getAmplifier() + 1, 4);
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, effectDuration, newAmplifier));
                    } else {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, effectDuration, amplifier)); // Duration: 600 ticks (30 seconds)
                    }
                }
            }
        }
    }
}
