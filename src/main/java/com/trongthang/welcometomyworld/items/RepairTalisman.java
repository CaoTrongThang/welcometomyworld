package com.trongthang.welcometomyworld.items;

import com.trongthang.welcometomyworld.managers.ItemsManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
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


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(world.isClient) return;

        if(entity instanceof PlayerEntity p){

            if(world.getTickOrder() % 100 != 0) return;

            Item offhand = p.getOffHandStack().getItem();
            Item mainHand = p.getMainHandStack().getItem();

            if(mainHand == this || offhand == this){
                for (int i = 0; i < p.getInventory().size(); i++) {
                    ItemStack itemStack = p.getInventory().getStack(i);

                    if (!itemStack.isEmpty() && itemStack.isDamageable() && itemStack.getDamage() > 0) {

                        int currentDamage = itemStack.getDamage();
                        int newDamage = Math.max(0, currentDamage - (int) REPAIR_AMOUNT);
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

        Text cooldown = Text.literal(String.valueOf("5") + "s")
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
