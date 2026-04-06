package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.datagen.ModBlockTagProvider;
import com.trongthang.welcometomyworld.datagen.ModItemTagProvider;
import com.trongthang.welcometomyworld.datagen.ModWorldGenerator;
import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import com.trongthang.welcometomyworld.datagen.ModBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class WelcomeToMyWorldDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModBlockLootTableProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModBlockTagProvider::new);
        pack.addProvider(ModWorldGenerator::new);

        WelcomeToMyWorld.LOGGER.info("GENERATED DATA GENERATOR!!!");
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        // TODO Auto-generated method stub
        DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
        registryBuilder.addRegistry(RegistryKeys.DIMENSION_TYPE, VoidDimension::bootstrapType);
    }
}
