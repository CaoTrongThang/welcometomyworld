package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.datagen.ModBlockTagProvider;
import com.trongthang.welcometomyworld.datagen.ModItemTagProvider;
import com.trongthang.welcometomyworld.datagen.ModBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class WelcomeToMyWorldDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(ModBlockLootTableProvider::new);
        pack.addProvider(ModItemTagProvider::new);
        pack.addProvider(ModBlockTagProvider::new);

        WelcomeToMyWorld.LOGGER.info("GENERATED DATA GENERATOR!!!");
    }
}
