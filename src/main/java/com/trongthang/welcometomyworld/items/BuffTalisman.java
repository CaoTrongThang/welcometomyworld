package com.trongthang.welcometomyworld.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class BuffTalisman extends Item {

    public StatusEffect statusEffect;
    public int amplifier;
    public int effectDuration = 120;
    private StatusEffectInstance currentEffect;

    public BuffTalisman(Settings settings, StatusEffect statusEffects, int amplifier) {
        super(settings.maxCount(1));

        this.amplifier = amplifier;
        this.statusEffect = statusEffects;
    }

    private int checkInterval = 60;
    private int counter = 0;

    public void onServerTick(MinecraftServer server, Item compareItem) {
        counter++;
        if (counter < checkInterval) return;
        counter = 0;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()){
            if (p.getOffHandStack().getItem() == compareItem || p.getMainHandStack().getItem() == compareItem) {

                StatusEffectInstance currentEffect = p.getStatusEffect(statusEffect);

                if (currentEffect != null) {
                    int newAmplifier = Math.min(currentEffect.getAmplifier() + amplifier, 128);
                    p.addStatusEffect(new StatusEffectInstance(statusEffect, currentEffect.getDuration(), newAmplifier));
                } else {
                    // Apply new effect
                    p.addStatusEffect(new StatusEffectInstance(statusEffect, effectDuration, amplifier)); // Duration: 600 ticks (30 seconds)
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

        Text line1 = Text.literal("Effect: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY));

        Text line1Part1 = Text.literal(String.valueOf(statusEffect.getTranslationKey().replace("effect.minecraft.", "").toUpperCase()))
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD));

        Text line2 = Text.literal("Cooldown: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY));

        Text line2Part1 = Text.literal(String.valueOf(checkInterval / 20) + "s")
                .setStyle(Style.EMPTY.withColor(Formatting.RED));

        Text line3 = Text.literal("Give you an effect after a")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line4 = Text.literal("certain time.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line5 = Text.literal("Only works in offhand and main hand.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        tooltip.add(Text.literal("").append(line1).append(line1Part1));
        tooltip.add(Text.literal("").append(line2).append(line2Part1));

        tooltip.add(Text.literal("")); // Cooldown

        tooltip.add(line3);
        tooltip.add(line4);
        tooltip.add(line5);
    }
}
