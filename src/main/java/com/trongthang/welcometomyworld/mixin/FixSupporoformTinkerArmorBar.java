package com.trongthang.welcometomyworld.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = InGameHud.class)
public class FixSupporoformTinkerArmorBar {
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    // Accessor to retrieve the private ICONS field
    @Accessor("ICONS")
    public static Identifier getIconsTexture() {
        throw new AssertionError("Mixin failed to apply!");
    }

    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"
            )
    )
    private void redirectArmorDraw(
            DrawContext context, Identifier texture,
            int x, int y, int u, int v, int width, int height
    ) {
        // Check if the texture is ICONS and matches armor icon UV coordinates
        if (texture.equals(getIconsTexture()) && v == 9 && (u == 16 || u == 25 || u == 34)) {
            // Override Y-position to place armor at the original position (scaledHeight - 49)
            int newY = this.scaledHeight - 49;
            context.drawTexture(texture, x, newY, u, v, width, height);
        } else {
            // Draw other textures normally
            context.drawTexture(texture, x, y, u, v, width, height);
        }
    }
}
