package com.trongthang.welcometomyworld.datagen;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class ModWorldGenerator extends FabricDynamicRegistryProvider {
    public ModWorldGenerator(FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        // ONLY keep these if you actually have Java Bootstrap methods written for them!
        // If you are using the manual JSON files in src/main/resources, you do NOT need
        // these lines.

        // entries.addAll(registries.getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE));
        // entries.addAll(registries.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE));
        // entries.addAll(registries.getWrapperOrThrow(RegistryKeys.BIOME));

        // You still need this one because you wrote a Java Bootstrap for your
        // DimensionType in VoidDimension.java
        entries.addAll(registries.getWrapperOrThrow(RegistryKeys.DIMENSION_TYPE));
    }

    @Override
    public String getName() {
        return "World Gen";
    }
}