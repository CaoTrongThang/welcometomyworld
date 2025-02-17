package com.trongthang.welcometomyworld.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class RepairTalisman extends Item {

    public float REPAIR_AMOUNT = 5;

    public RepairTalisman(Settings settings) {
        super(settings);
    }

    public RepairTalisman(Settings settings, float repairAmount) {
        super(settings.maxCount(1));
        this.REPAIR_AMOUNT = repairAmount;
    }

    private int checkInterval = 60;
    private int counter = 0;

    public void onServerTick(MinecraftServer server, Item compareItem){
        counter++;
        if(counter < checkInterval) return;
        counter = 0;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()){

            if(p.getOffHandStack().getItem() == compareItem || p.getMainHandStack().getItem() == compareItem){
                for (int i = 0; i < p.getInventory().size(); i++) {
                    ItemStack itemStack = p.getInventory().getStack(i);

                    // Check if the item is damageable and has damage
                    if (!itemStack.isEmpty() && itemStack.isDamageable() && itemStack.getDamage() > 0) {
                        // Repair by subtracting damage
                        int currentDamage = itemStack.getDamage();
                        int newDamage = Math.max(0, currentDamage - (int) REPAIR_AMOUNT); // Ensure damage doesn't go below 0
                        itemStack.setDamage(newDamage);
                    }
                }
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

        Text line3 = Text.literal("Automatically repair all the items in your inventory")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line4 = Text.literal("after a certain time.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line5 = Text.literal("Only works in offhand and main hand.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line1Part1 = Text.literal("Repair Item By: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)); // Gray part


        Text repairAmount = Text.literal(String.valueOf(REPAIR_AMOUNT))
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD)); // Gold for the number

        Text line2Part1 = Text.literal("Cooldown: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)); // Gray part

        Text cooldown = Text.literal(String.valueOf(checkInterval / 20) + "s")
                .setStyle(Style.EMPTY.withColor(Formatting.RED)); // Red for the number


        // Combine the parts using Text's formatting
        tooltip.add(Text.literal("").append(line1Part1).append(repairAmount));

        tooltip.add(Text.literal("").append(line2Part1).append(cooldown));

        tooltip.add(Text.literal(""));

        tooltip.add(line3);
        tooltip.add(line4);
        tooltip.add(line5);
    }
}
