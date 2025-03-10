package com.trongthang.welcometomyworld.mixin;


import com.trongthang.welcometomyworld.ConfigLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

@Mixin(GameRules.BooleanRule.class)
public class BooleanRuleMixin {

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void onSet(boolean value, @Nullable MinecraftServer server, CallbackInfo ci) {
        // Cast 'this' to the target class (GameRules.BooleanRule)
        GameRules.BooleanRule rule = (GameRules.BooleanRule) (Object) this;

        // Compare the rule's key to GameRules.KEEP_INVENTORY
        if (rule.equals(GameRules.KEEP_INVENTORY)) {

            if (ConfigLoader.getInstance().keepInventoryDebugLog) {
                LOGGER.info("keepInventory was set to: " + value + " This is a debug log of WelcomeToMyWorld mod, it's used to debug what mod sometimes set the KeepInventory to true, please send me the log if you see this message");
                LOGGER.info("Blocked attempt to set keepInventory to true");
            }

            // Log the change and stack trace
            Thread.dumpStack(); // Prints the stack trace to identify the caller

            // Optionally cancel the modification
            if (value) {
                ci.cancel(); // Prevents the rule from being set to true
            }
        }

        if (rule.equals(GameRules.REDUCED_DEBUG_INFO)) {
            if (ConfigLoader.getInstance().noMoreF3B) {
                if (value) {
                    LOGGER.info("You can't use F3 + B, can turn on in welcometomyworldconfig");
                    ci.cancel();
                }
            }
        }

    }
}