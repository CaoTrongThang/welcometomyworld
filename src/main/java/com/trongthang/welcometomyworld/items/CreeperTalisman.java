package com.trongthang.welcometomyworld.items;

import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class CreeperTalisman extends TrinketItem {

    public CreeperTalisman(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(Text.literal("Creeper's explosion won't explode blocks if ")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)));
        tooltip.add(Text.literal("it's targeting you while you hold or wear the ")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)));
        tooltip.add(Text.literal("Creeper's Talisman.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)));

        tooltip.add(Text.literal(""));

        tooltip.add(Text.literal("This can be equipped in the necklace trinket slot.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)));
    }
}
