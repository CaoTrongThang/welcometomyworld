package com.trongthang.welcometomyworld.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class RepairBox extends Item {

    public static final double REPAIR_PERCENT = 5.0; // Repair 5% of durability

    public RepairBox(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // Get the RepairBox item being used
        ItemStack wrenchStack = player.getStackInHand(hand);
        boolean repaired = false;

        // Only run on the server
        if (!world.isClient) {
            // Loop through the player's inventory
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack itemStack = player.getInventory().getStack(i);

                // Check if the item is damageable and has damage
                if (!itemStack.isEmpty() && itemStack.isDamageable() && itemStack.getDamage() > 0) {
                    int maxDurability = itemStack.getMaxDamage();
                    int currentDamage = itemStack.getDamage();

                    // Calculate repair amount (5% of max durability)
                    int repairAmount = (int) (maxDurability * REPAIR_PERCENT / 100);

                    // Repair the item by reducing its damage
                    int newDamage = Math.max(currentDamage - repairAmount, 0);
                    itemStack.setDamage(newDamage);
                    repaired = true; // Mark that a repair was made

                    // Only repair the first eligible item
                    break;
                }
            }

            // Consume the RepairBox only if something was repaired
            if (repaired) {
                wrenchStack.decrement(1); // Decrease the RepairBox count by 1
                return TypedActionResult.success(wrenchStack, true); // Mark as successful
            }
        }

        // If no repairs were made, return failure
        return TypedActionResult.fail(wrenchStack);
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        // Line 1: Static message - "Only works in offhand slot"
        Text line3 = Text.literal("Repair all the items in your inventory,")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        Text line4 = Text.literal("consume it to use.")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        // Line 2: Static message - "Repair Item By"
        Text line1Part1 = Text.literal("Repair Item By: ")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)); // Gray part

        // Variable part: Repair Amount
        Text repairAmount = Text.literal(String.valueOf(REPAIR_PERCENT) + "%")
                .setStyle(Style.EMPTY.withColor(Formatting.GOLD)); // Gold for the number

        // Combine the parts using Text's formatting
        tooltip.add(Text.literal("").append(line1Part1).append(repairAmount)); // Repair Item By

        tooltip.add(Text.literal("")); // Cooldown

        tooltip.add(line3);
        tooltip.add(line4);
    }
}
