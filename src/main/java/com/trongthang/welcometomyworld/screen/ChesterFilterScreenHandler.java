package com.trongthang.welcometomyworld.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

/**
 * Custom screen handler that lays out:
 * - 10 filter slots (indices 0-9, left column)
 * - chestRows*9 slots (indices 10+, main area)
 * - player inventory (27 slots)
 * - player hotbar (9 slots)
 *
 * chestRows = 3 for Enderchester, 6 for Chester.
 */
public class ChesterFilterScreenHandler extends ScreenHandler {

    public static final int FILTER_SLOTS = 10;

    private final Inventory chestInventory;
    private final Inventory filterInventory;
    public final int chestRows;

    // Server-side constructor (called by the entity)
    public ChesterFilterScreenHandler(ScreenHandlerType<?> type, int syncId,
            PlayerInventory playerInv,
            Inventory chestInventory,
            Inventory filterInventory,
            int chestRows) {
        super(type, syncId);
        this.chestInventory = chestInventory;
        this.filterInventory = filterInventory;
        this.chestRows = chestRows;

        checkSize(chestInventory, chestRows * 9);
        checkSize(filterInventory, FILTER_SLOTS);

        chestInventory.onOpen(playerInv.player);

        // Filter slots: left column, x=8, y spaced 18px
        // Use custom Slot to prevent duplicates
        for (int i = 0; i < FILTER_SLOTS; i++) {
            this.addSlot(new FilterSlot(filterInventory, i, 8, 18 + i * 18));
        }

        // Chest slots (x=44, y=18)
        int chestX = 44;
        for (int row = 0; row < chestRows; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(chestInventory, col + row * 9, chestX + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (Exactly as vanilla GenericContainerScreenHandler)
        int playerInvY = (chestRows == 3) ? 84 : 140;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, chestX + col * 18, playerInvY + row * 18));
            }
        }

        // Player hotbar
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, chestX + col * 18, hotbarY));
        }
    }

    // Client-side factory constructor
    public ChesterFilterScreenHandler(int syncId, PlayerInventory playerInv) {
        this(null, syncId, playerInv,
                new SimpleInventory(54),
                new SimpleInventory(FILTER_SLOTS),
                6);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.chestInventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.chestInventory.onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            int filterEnd = FILTER_SLOTS;
            int chestEnd = filterEnd + chestRows * 9;
            int invEnd = chestEnd + 27;
            int hotbarEnd = invEnd + 9;

            if (index < chestEnd) {
                // From filter or chest → player inventory
                if (!this.insertItem(stack, chestEnd, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player → chest
                if (!this.insertItem(stack, filterEnd, chestEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, stack);
        }

        return result;
    }

    /**
     * Custom slot for the filter to prevent duplicate items.
     */
    private class FilterSlot extends Slot {
        public FilterSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            // Check if the item already exists in ANY filter slot
            for (int i = 0; i < FILTER_SLOTS; i++) {
                ItemStack existing = filterInventory.getStack(i);
                if (!existing.isEmpty() && existing.isOf(stack.getItem())) {
                    return false;
                }
            }
            return super.canInsert(stack);
        }
    }
}
