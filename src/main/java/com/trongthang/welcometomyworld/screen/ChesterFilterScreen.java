package com.trongthang.welcometomyworld.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChesterFilterScreen extends HandledScreen<ChesterFilterScreenHandler> {

    // We reuse vanilla's generic container texture; it's wide enough for 9 columns.
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");

    // The filter panel sits at x=0 in GUI space; the chest area starts to the
    // right.
    // Filter column visual offset from backgroundX
    private static final int FILTER_PANEL_X = 7; // left-edge relative to backgroundX
    private static final int CHEST_PANEL_X = 43; // where the chest slots start

    public ChesterFilterScreen(ChesterFilterScreenHandler handler,
            PlayerInventory playerInv,
            Text title) {
        super(handler, playerInv, title);

        // backgroundWidth = standard chest width (176) + filter column width (36)
        this.backgroundWidth = 212;
        // backgroundHeight = standard chest height for chestRows + player inventory
        // 18 (top padding) + rows*18 + 14 (gap) + 54 (player inv) + 6 (bottom)
        this.backgroundHeight = 114 + handler.chestRows * 18;

        // Push the title and player-inv label to account for the extended width
        this.titleX = CHEST_PANEL_X + 4;
        this.playerInventoryTitleX = CHEST_PANEL_X + 4;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        int chestX = x + CHEST_PANEL_X - 7;

        // 1. Top bar + Chest slots
        // generic_54.png top bar + slots up to 6 rows.
        // If 3 rows, we crop at 17 + 3*18 = 71.
        // If 6 rows, we crop at 17 + 6*18 = 125.
        int topHeight = 17 + handler.chestRows * 18;
        context.drawTexture(TEXTURE, chestX, y, 0, 0, 176, topHeight);

        // 2. Player inventory section
        // Standard GenericContainer texture has the inventory at the very bottom (rows
        // 126-222).
        // We stitch it right after the chest slots.
        context.drawTexture(TEXTURE, chestX, y + topHeight, 0, 126, 176, 96);

        drawFilterPanel(context, x, y);
    }

    private void drawFilterPanel(DrawContext context, int x, int y) {
        int px = x + FILTER_PANEL_X - 3;
        int py = y;
        int pw = 26;
        int ph = this.backgroundHeight;

        // 1. Vanilla grey background (matches the chest texture)
        context.fill(px, py, px + pw, py + ph, 0xFFC6C6C6);

        // 2. Draw 10 slot backgrounds from generic_54.png
        // Standard slot UV in generic_54 is (7, 17, 18, 18)
        for (int i = 0; i < 10; i++) {
            // Slots in handler are at (8, 18 + i*18).
            // We draw the background at (x+7, y+17 + i*18) to frame them perfectly.
            context.drawTexture(TEXTURE, x + FILTER_PANEL_X, y + 17 + i * 18, 7, 17, 18, 18);
        }

        // 3. Vanilla-style border
        context.fill(px, py, px + pw, py + 1, 0xFFFFFFFF); // Top (highlight)
        context.fill(px, py, px + 1, py + ph, 0xFFFFFFFF); // Left (highlight)
        context.fill(px + pw - 1, py, px + pw, py + ph, 0xFF555555); // Right (shadow)
        context.fill(px, py + ph - 1, px + pw, py + ph, 0xFF555555); // Bottom (shadow)
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Title (e.g. "Chester's Stomach")
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x404040, false);

        // "Filter" label above the filter column
        context.drawText(this.textRenderer,
                Text.literal("Filter"),
                FILTER_PANEL_X - 2, 5,
                0x404040, false);

        // Player inventory label
        context.drawText(this.textRenderer,
                this.playerInventoryTitle,
                this.playerInventoryTitleX,
                this.playerInventoryTitleY,
                0x404040, false);
    }
}
