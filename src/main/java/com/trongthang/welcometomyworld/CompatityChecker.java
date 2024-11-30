package com.trongthang.welcometomyworld;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Collection;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class CompatityChecker {
    public boolean originMod = false;

    public boolean OriginCheck(){
        if(containMod("origins", "Apace")){
            LOGGER.info("Origins mod found, doing my best to work with it!");
            originMod = true;

            return true;
        }
        return false;
    }

    public boolean ImprovedMobsCheck(){
        if(containMod("origins", "Apace")){
            LOGGER.info("Origins mod found, doing my best to work with it!");
            originMod = true;

            return true;
        }
        return false;
    }

    public void getModInfo(String modId) {
        FabricLoader.getInstance().getModContainer(modId).ifPresent(container -> {
            ModMetadata metadata = container.getMetadata();
            System.out.println("Mod ID: " + metadata.getId());
            System.out.println("Name: " + metadata.getName());
            System.out.println("Version: " + metadata.getVersion().getFriendlyString());
            System.out.println("Description: " + metadata.getDescription());
        });
    }

    public boolean containMod(String modId, String authors){
        Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
        for (ModContainer mod : mods){
            ModMetadata metadata = mod.getMetadata();
            if(metadata.getId().equals(modId)){
                return true;
            }
        }
        return false;
    }


    public void ShowAllMods() {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            ModMetadata metadata = modContainer.getMetadata();

            System.out.println("------------------------------");
            System.out.println("Mod ID: " + metadata.getId());
            System.out.println("Mod Name: " + metadata.getName());
            System.out.println("Version: " + metadata.getVersion().getFriendlyString());
            System.out.println("Description: " + metadata.getDescription());
            System.out.println("Authors: " + metadata.getAuthors().stream()
                    .map(person -> person.getName())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Unknown"));
            System.out.println("------------------------------");
        });

    }
}
