package com.trongthang.welcometomyworld.mixin.client;

import mcp.mobius.waila.config.WailaConfig;
import mcp.mobius.waila.gui.hud.theme.ThemeDefinition;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WailaConfig.Overlay.Color.class, remap = false)
public class WailaConfigMixin {
    @Inject(method = "getThemeDef", at = @At("RETURN"), remap = false, cancellable = true)
    private void welcometomyworld_fixNpeFallback(CallbackInfoReturnable<ThemeDefinition<?>> cir) {
        if (cir.getReturnValue() == null) {
            try {
                Class<?> registryClass = Class.forName("mcp.mobius.waila.gui.hud.theme.ThemeRegistry", true,
                        Thread.currentThread().getContextClassLoader());
                Object instance = registryClass.getField("INSTANCE").get(null);
                Object themeDef = registryClass.getMethod("getThemeDef", Identifier.class).invoke(instance,
                        new Identifier("wthit", "dark"));
                if (themeDef != null) {
                    cir.setReturnValue((ThemeDefinition<?>) themeDef);
                }
            } catch (Exception e) {
                // Ignore exception and let it return null eventually causing NPE, but we tried
                // our best
            }
        }
    }
}
