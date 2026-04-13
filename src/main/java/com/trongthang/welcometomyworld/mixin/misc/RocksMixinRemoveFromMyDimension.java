package com.trongthang.welcometomyworld.mixin.misc;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import java.util.function.Predicate;

@Pseudo
@Mixin(targets = "eu.midnightdust.motschen.rocks.world.FeatureInjector", remap = false)
public class RocksMixinRemoveFromMyDimension {

    // Intercept the Predicate rule the Rocks mod gives to BiomeModifications
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/biome/v1/BiomeModifications;addFeature(Ljava/util/function/Predicate;Lnet/minecraft/world/gen/GenerationStep$Feature;Lnet/minecraft/registry/RegistryKey;)V", remap = true), index = 0)
    private static Predicate<BiomeSelectionContext> welcometomyworld$modifyRocksRules(
            Predicate<BiomeSelectionContext> originalRule) {

        // Return our own custom wrapper rule
        return ctx -> {
            if (ctx.getBiomeKey() != null) {
                // If it is any biome from your mod, instantly return false (do not spawn
                // rocks!)
                if (ctx.getBiomeKey().getValue().getNamespace().equals("welcometomyworld")) {
                    return false;
                }
            }

            // If it's a normal biome (like Overworld plains), let the Rocks mod run its
            // normal checks
            return originalRule.test(ctx);
        };
    }
}