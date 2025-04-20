package com.trongthang.welcometomyworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.MOD_ID;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class ConfigLoader {
    private static final String CONFIG_FILE_NAME = "welcometomyworld.json";
    private static ConfigLoader INSTANCE;

    @Expose
    @SerializedName("introOfTheWorld")
    public boolean introOfTheWorld = true;
    @Expose
    @SerializedName("clearItemsBeforeGivingStartingItems")
    public boolean clearItemsBeforeGivingStartingItems = true;
    @Expose
    @SerializedName("giveStartingItems")
    public boolean giveStartingItems = true;

    @Expose
    @SerializedName("noMoreF3B")
    public boolean noMoreF3B = true;

    @Expose
    @SerializedName("oneShotDebugLog")
    public boolean oneShotDebugLog = false;

    @Expose
    @SerializedName("hostileMobsEventsStopSpawningDay")
    public int hostileMobsEventsStopSpawningDay = 500;

    public static void loadConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE_NAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = gson.fromJson(reader, ConfigLoader.class);
                if (INSTANCE == null) {
                    INSTANCE = new ConfigLoader(); // Fallback to default if JSON was empty or malformed
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            INSTANCE = new ConfigLoader();
        }

        saveConfig(gson, configFile); // Save current config, including defaults if they were missing
    }

    private static void saveConfig(Gson gson, File configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }
}
