package com.trongthang.welcometomyworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
        private static final String CONFIG_FILE_NAME = "welcometomyworld.json";
        private static ConfigLoader INSTANCE;

        // send to url if you're playing the modpack
        @Expose
        @SerializedName("_info")
        public List<String> _info = List.of(
                        "Welcome to My World Mod Configuration.",
                        "Standard JSON doesn't support comments, so we use these '_info' fields for documentation.");

        @Expose
        @SerializedName("urlToSendChart")
        public String urlToSendChart = "none";

        @Expose
        @SerializedName("modpackVersion")
        public String modpackVersion = "0.0.0";

        // the falling intro and stuff to the world
        @Expose
        @SerializedName("enableIntroOfTheWorld")
        public boolean enableIntroOfTheWorld = true;

        @Expose
        @SerializedName("clearItemsBeforeGivingStartingItems")
        public boolean clearItemsBeforeGivingStartingItems = false;

        @Expose
        @SerializedName("giveStartingItems")
        public boolean giveStartingItems = false;

        @Expose
        @SerializedName("noMoreF3B")
        public boolean noMoreF3B = true;

        @Expose
        @SerializedName("oneShotDebugLog")
        public boolean oneShotDebugLog = false;

        @Expose
        @SerializedName("enableCustomAnimations")
        public boolean enableCustomAnimations = true;

        @Expose
        @SerializedName("_disableTreeFallingDamage_info")
        public List<String> _disableTreeFallingDamage_info = List.of(
                        "Disable tree falling damage.",
                        "This only support if you have Enhanced Falling Trees mod for Fabric 1.20.1.");

        @Expose
        @SerializedName("enableTreeFallingDamage")
        public boolean enableTreeFallingDamage = true;

        // Usually there'll be alot of hostile mobs at night spawned from my mod
        @Expose
        @SerializedName("hostileMobsEventsStopSpawningDay")
        public int hostileMobsEventsStopSpawningDay = 500;

        // Mobs here won't be spawned in the world, you can add "*" to disable all
        // mobs from a mod example: "minecraft:*" will disable all mobs from minecraft
        @Expose
        @SerializedName("_disabledMobs_info")
        public List<String> _disabledMobs_info = List.of(
                        "Mobs here won't be spawned.",
                        "Use 'modid:*' to disable all mobs from a mod.");

        @Expose
        @SerializedName("disabledMobs")
        public List<String> disabledMobs = new ArrayList<>(
                        List.of("mobs_of_mythology:kobold", "iceandfire:hippocampus"));

        // Mobs here can't be upgraded, you can't upgrade them by pressing "M" to open
        // the upgrade menu
        // you can config for all mobs from that mod by using the "*" example:
        // "saintdragons:*"
        @Expose
        @SerializedName("_excludedUpgradeMobs_info")
        public List<String> _excludedUpgradeMobs_info = List.of(
                        "Mobs here can't be leveled up or upgraded via the 'M' menu.",
                        "Use 'modid:*' for wildcards.");

        @Expose
        @SerializedName("excludedUpgradeMobs")
        public List<String> excludedUpgradeMobs = new ArrayList<>(List.of("saintdragons:*"));

        @Expose
        @SerializedName("darknessLevels")
        public DarknessLevelsConfig darknessLevels = new DarknessLevelsConfig();

        public static class DarknessLevelsConfig {
                @Expose
                @SerializedName("_info")
                public List<String> _info = List.of(
                                "Darkness levels per dimension.",
                                "Value is the curve power (e.g. 7.0 is very dark, 1.0 is vanilla).",
                                "If you use the /welcometomyworld reloadconfig command, you have to switch world then switch back to see the changes");

                @Expose
                @SerializedName("dimensions")
                public Map<String, Float> dimensions = new HashMap<>();

                public DarknessLevelsConfig() {
                        dimensions.put("minecraft:overworld", 7.0f);
                        dimensions.put("minecraft:the_nether", 1.0f);
                        dimensions.put("minecraft:the_end", 1.0f);
                        dimensions.put("welcometomyworld:void_dim", 7.0f);
                }
        }

        @Expose
        @SerializedName("bloodMoon")
        public BloodMoonConfig bloodMoon = new BloodMoonConfig();

        public static class BloodMoonConfig {
                @Expose
                @SerializedName("_info")
                public List<String> _info = List.of(
                                "Blood Moon settings.",
                                "disableBloodMoon: set to true to turn off all blood moon effects.",
                                "bloodMoonChance: probability (0.0-1.0) that any given night becomes a blood moon.",
                                "bloodMoonWorldWhitelist: dimensions where Blood Moon can occur (e.g. 'minecraft:overworld').");

                @Expose
                @SerializedName("disableBloodMoon")
                public boolean disableBloodMoon = false;

                @Expose
                @SerializedName("bloodMoonChance")
                public float bloodMoonChance = 0.1f;

                @Expose
                @SerializedName("bloodMoonWorldWhitelist")
                public List<String> bloodMoonWorldWhitelist = new ArrayList<>(List.of("minecraft:overworld"));
        }

        @Expose
        @SerializedName("mobsSetFixedStats")
        public MobsSetFixedStatsConfig mobsSetFixedStats = new MobsSetFixedStatsConfig();

        public static class MobsSetFixedStatsConfig {
                @Expose
                @SerializedName("_info")
                public List<String> _info = List.of(
                                "Fixed stats for specific mobs.",
                                "Damage, armor, and max health can be set.");

                @Expose
                @SerializedName("mobs")
                public Map<String, MobFixedStatsConfig> mobs = new HashMap<>();

                public MobsSetFixedStatsConfig() {
                        // Example: minecraft:zombie
                        mobs.put("minecraft:cat", new MobFixedStatsConfig(30f, 0f, 0f));
                }
        }

        public static class MobFixedStatsConfig {
                @Expose
                @SerializedName("maxHealth")
                public Float maxHealth = null;

                @Expose
                @SerializedName("damage")
                public Float damage = null;

                @Expose
                @SerializedName("armor")
                public Float armor = null;

                public MobFixedStatsConfig() {
                }

                public MobFixedStatsConfig(Float maxHealth, Float damage, Float armor) {
                        this.maxHealth = maxHealth;
                        this.damage = damage;
                        this.armor = armor;
                }
        }

        // All mobs can be geared with weapons, armors,... if you want specific mobs to
        // be geared the way you want, add them to validMobs
        // you can config for all mobs from that mod by using the "*" example:
        // "saintdragons:*"
        @Expose
        @SerializedName("mobsGearsUp")

        public MobsGearsUpConfig mobsGearsUp = new MobsGearsUpConfig();

        public static class MobsGearsUpConfig {
                @Expose
                @SerializedName("_info")
                public List<String> _info = List.of(
                                "Specific mobs gear settings.",
                                "Use 'modid:*' for all mobs in a mod, or 'modid:name' for specific.",
                                "Default settings are used if not listed.");

                @Expose
                @SerializedName("specificMobs")
                public Map<String, MobSettingsConfig> specificMobs = new HashMap<>();

                @Expose
                @SerializedName("allowedEnchantments")
                public List<String> allowedEnchantments = new ArrayList<>();

                @Expose
                @SerializedName("allowedOffhands")
                public List<String> allowedOffhands = new ArrayList<>();

                public MobsGearsUpConfig() {
                        // Default valid mobs
                        specificMobs.put("welcometomyworld:void_worm",
                                        new MobSettingsConfig(false, false, false, false, false, false, false, false));
                        specificMobs.put("welcometomyworld:unknown",
                                        new MobSettingsConfig(false, false, false, false, false, false, false, false));
                        specificMobs.put("minecraft:zombie", new MobSettingsConfig());
                        specificMobs.put("minecraft:zombie_villager", new MobSettingsConfig());
                        specificMobs.put("minecraft:vindicator", new MobSettingsConfig());
                        specificMobs.put("minecraft:giant", new MobSettingsConfig());
                        specificMobs.put("minecraft:enderman",
                                        new MobSettingsConfig(true, false, false, true, true, true, true, true));
                        specificMobs.put("minecraft:spider",
                                        new MobSettingsConfig(true, false, false, true, true, true, true, true));
                        specificMobs.put("minecraft:skeleton",
                                        new MobSettingsConfig(false, true, true, true, true, true, true, true));
                        specificMobs.put("minecraft:ravager",
                                        new MobSettingsConfig(true, false, false, true, true, true, true, true));
                        specificMobs.put("myths_of_the_sea:leviathan",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));
                        specificMobs.put("companions:sacred_pontiff",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));
                        specificMobs.put("saintsdragons:nulljaw",
                                        new MobSettingsConfig(false, false, false, true, false, true, true, false));
                        specificMobs.put("saintsdragons:ignivorus",
                                        new MobSettingsConfig(false, false, false, true, false, true, true, false));
                        specificMobs.put("saintsdragons:raevyx",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));
                        specificMobs.put("saintsdragons:cindervane",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));
                        specificMobs.put("saintsdragons:stegonaut",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));
                        specificMobs.put("takesapillage:legioner", new MobSettingsConfig());
                        specificMobs.put("takesapillage:skirmisher", new MobSettingsConfig());
                        specificMobs.put("takesapillage:archer",
                                        new MobSettingsConfig(true, false, true, true, true, true, true, true));
                        specificMobs.put("palegardenbackport:creaking", new MobSettingsConfig());
                        specificMobs.put("eldritch_end:eye",
                                        new MobSettingsConfig(false, false, false, true, true, true, true, true));

                        // Default valid enchantments
                        allowedEnchantments.addAll(List.of(
                                        "minecraft:protection", "minecraft:fire_protection",
                                        "minecraft:feather_falling",
                                        "minecraft:blast_protection", "minecraft:projectile_protection",
                                        "minecraft:respiration",
                                        "minecraft:aqua_affinity", "minecraft:thorns", "minecraft:depth_strider",
                                        "minecraft:frost_walker", "minecraft:binding_curse", "minecraft:soul_speed",
                                        "minecraft:swift_sneak", "minecraft:sharpness", "minecraft:smite",
                                        "minecraft:bane_of_arthropods", "minecraft:knockback", "minecraft:fire_aspect",
                                        "minecraft:looting", "minecraft:sweeping", "minecraft:efficiency",
                                        "minecraft:silk_touch", "minecraft:unbreaking", "minecraft:power",
                                        "minecraft:punch", "minecraft:flame", "minecraft:infinity",
                                        "minecraft:impaling", "minecraft:riptide", "minecraft:channeling",
                                        "minecraft:multishot", "minecraft:quick_charge", "minecraft:piercing",
                                        "minecraft:vanishing_curse", "mcda:burning", "mcdw:ambush",
                                        "mcdw:multi_shot", "mcdw:cobweb_shot", "mcda:poison_focus",
                                        "mcdw:weakening", "mcdw:fuse_shot", "mcdw:gravity", "mcdw:levitation_shot"));

                        // Default extra off-hands
                        allowedOffhands.addAll(List.of(
                                        "minecraft:flint_and_steel", "minecraft:ender_pearl", "minecraft:lava_bucket"));
                }
        }

        public static class MobSettingsConfig {
                @Expose
                @SerializedName("melee")
                public boolean melee = true;

                @Expose
                @SerializedName("range")
                public boolean range = false;

                @Expose
                @SerializedName("offhand")
                public boolean offhand = true;

                @Expose
                @SerializedName("helmet")
                public boolean helmet = true;

                @Expose
                @SerializedName("chestplate")
                public boolean chestplate = true;

                @Expose
                @SerializedName("leggings")
                public boolean leggings = true;

                @Expose
                @SerializedName("boots")
                public boolean boots = true;

                @Expose
                @SerializedName("enchantment")
                public boolean enchantment = true;

                public MobSettingsConfig() {
                }

                public MobSettingsConfig(boolean melee, boolean range, boolean offhand, boolean helmet,
                                boolean chestplate,
                                boolean leggings, boolean boots, boolean enchantment) {
                        this.melee = melee;
                        this.range = range;
                        this.offhand = offhand;
                        this.helmet = helmet;
                        this.chestplate = chestplate;
                        this.leggings = leggings;
                        this.boots = boots;
                        this.enchantment = enchantment;
                }
        }

        @Expose
        @SerializedName("damageBalancing")
        public DamageBalancingConfig damageBalancing = new DamageBalancingConfig();

        public static class DamageBalancingConfig {
                @Expose
                @SerializedName("_info")
                public List<String> _info = List.of(
                                "Damage balancing rules.",
                                "Checked top-to-bottom.",
                                "Condition: filters by minOriginalDamage, maxOriginalDamage, and damageSourceTypes (e.g. 'thorns', 'sonic_boom').",
                                "Action Formula: [final = (original * multiplier) + min(maxHealthBonusCap, AttackerMaxHP * addMaxHealthFraction)].",
                                "Capped by maxFinalDamage. fixedValue overrides all.");

                @Expose
                @SerializedName("showMobDamageLogs")
                public boolean showMobDamageLogs = false;

                @Expose
                @SerializedName("mobs")
                public Map<String, List<DamageRuleConfig>> mobs = new HashMap<>();

                public DamageBalancingConfig() {
                        // Default rules ported from Mixin

                        // dungeonnowloading:chaos_spawner -> original * 0.85f
                        mobs.put("dungeonnowloading:chaos_spawner", List.of(DamageRuleConfig.create().mult(0.85f)));

                        // species:cruncher -> original + min(150, maxHealth * 0.005f)
                        mobs.put("species:cruncher", List.of(DamageRuleConfig.create().addHp(0.005f, 150f)));

                        // twilightforest:alpha_yeti -> if <= 15: if "twilightforest.yeeted" return
                        // original else original + min(70, hp*0.009). else 0.
                        mobs.put("twilightforest:alpha_yeti", List.of(
                                        DamageRuleConfig.create().maxDmg(15f).msgIds("twilightforest.yeeted"), // action
                                                                                                               // is
                                                                                                               // default
                                                                                                               // (original
                                                                                                               // * 1.0)
                                        DamageRuleConfig.create().maxDmg(15f).addHp(0.009f, 70f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:giant_miner -> original * 2.2f
                        mobs.put("twilightforest:giant_miner", List.of(DamageRuleConfig.create().mult(2.2f)));

                        // twilightforest:ur_ghast -> if <= 40: original + min(80, hp*0.009). else 0.
                        mobs.put("twilightforest:ur_ghast", List.of(
                                        DamageRuleConfig.create().maxDmg(40f).addHp(0.009f, 80f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:carminite_ghastguard -> if <= 15: original + min(100,
                        // hp*0.015). else 0.
                        mobs.put("twilightforest:carminite_ghastguard", List.of(
                                        DamageRuleConfig.create().maxDmg(15f).addHp(0.015f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:carminite_ghastling -> if <= 15: original + min(100, hp*0.02).
                        // else 0.
                        mobs.put("twilightforest:carminite_ghastling", List.of(
                                        DamageRuleConfig.create().maxDmg(15f).addHp(0.02f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:knight_phantom -> if <= 40: original + min(100, hp*2.0). else
                        // 0.
                        // Note: Mixin had duplicate knight_phantom keys, combining them (original <= 50
                        // vs <= 40). We'll use the latter <= 50, hp*3f which overwrote the previous.
                        mobs.put("twilightforest:knight_phantom", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(3f, 100f),
                                        DamageRuleConfig.create().fixed(0f) // Catch-all returns 0
                        ));

                        // twilightforest:slime_beetle -> if <= 30: original * 1.5. else 0.
                        mobs.put("twilightforest:slime_beetle", List.of(
                                        DamageRuleConfig.create().maxDmg(30f).mult(1.5f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:blockchain_goblin -> original * 0.9.
                        mobs.put("twilightforest:blockchain_goblin", List.of(DamageRuleConfig.create().mult(0.9f)));
                        mobs.put("twilightforest:upper_goblin_knight", List.of(DamageRuleConfig.create().mult(0.9f)));

                        // twilightforest:minotaur -> if <= 40: min(70, original * 1.5). else 0.
                        mobs.put("twilightforest:minotaur", List.of(
                                        DamageRuleConfig.create().maxDmg(40f).mult(1.5f).capFinal(70f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // twilightforest:minoshroom -> if <= 50: min(70, original * 1.2). else 0.
                        mobs.put("twilightforest:minoshroom", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).mult(1.2f).capFinal(70f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // minecraft:ghast -> if <= 40: original + min(100, hp*0.008). else 0.
                        mobs.put("minecraft:ghast", List.of(
                                        DamageRuleConfig.create().maxDmg(40f).addHp(0.008f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // aquamirae:captain_cornelia -> if <= 100: original + min(100, hp*0.0075). else
                        // original (no fixed 0 catch-all needed).
                        mobs.put("aquamirae:captain_cornelia", List.of(
                                        DamageRuleConfig.create().maxDmg(100f).addHp(0.0075f, 100f)));

                        // aquamirae:maw -> min(100, original * 2)
                        mobs.put("aquamirae:maw", List.of(DamageRuleConfig.create().mult(2f).capFinal(100f)));

                        // aquamirae:anglerfish -> if <= 100: original + min(100, hp*0.0065). else 0.
                        mobs.put("aquamirae:anglerfish", List.of(
                                        DamageRuleConfig.create().maxDmg(100f).addHp(0.0065f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // aquamirae:tortured_soul -> if <= 50: original + min(100, hp*0.0068). else 0.
                        mobs.put("aquamirae:tortured_soul", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(0.0068f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // aquamirae:eel -> min(100, original * 1.1)
                        mobs.put("aquamirae:eel", List.of(DamageRuleConfig.create().mult(1.1f).capFinal(100f)));

                        // aquamirae:maze_mother -> if <= 50: original + min(100, hp*0.0075). else 0.
                        mobs.put("aquamirae:maze_mother", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(0.0075f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:sculk_centipede -> if <= 50: original + min(100, hp*0.01). else
                        // 0.
                        mobs.put("deeperdarker:sculk_centipede", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(0.01f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:sculk_leech -> if <= 50: original * 4. else 0.
                        mobs.put("deeperdarker:sculk_leech", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).mult(4f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:sculk_snapper -> if <= 50: min(100, original * 2). else 0.
                        mobs.put("deeperdarker:sculk_snapper", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).mult(2f).capFinal(100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:shattered -> if <= 50: original + min(100, hp*0.0075). else 0.
                        mobs.put("deeperdarker:shattered", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(0.0075f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:shriek_worm -> if <= 50: original + min(100, hp*0.004). else 0.
                        mobs.put("deeperdarker:shriek_worm", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).addHp(0.004f, 100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:sludge -> if <= 50: min(100, original * 2). else 0.
                        mobs.put("deeperdarker:sludge", List.of(
                                        DamageRuleConfig.create().maxDmg(50f).mult(2f).capFinal(100f),
                                        DamageRuleConfig.create().fixed(0f)));

                        // deeperdarker:stalker -> min(100, original * 0.8)
                        mobs.put("deeperdarker:stalker", List.of(DamageRuleConfig.create().mult(0.8f).capFinal(100f)));

                        // minecraft:warden -> if sonic_boom: 10. else min(100, original * 0.5)
                        mobs.put("minecraft:warden", List.of(
                                        DamageRuleConfig.create().msgIds("sonic_boom").fixed(10f),
                                        DamageRuleConfig.create().mult(0.5f).capFinal(100f)));

                        // graveyard:lich -> if <= 25 and (explosion.player or indirectMagic):
                        // min(original*1.8, 40). else original
                        mobs.put("graveyard:lich", List.of(
                                        DamageRuleConfig.create().maxDmg(25f)
                                                        .msgIds("explosion.player", "indirectMagic").mult(1.8f)
                                                        .capFinal(40f) // default drops through to original
                        ));

                        // minecells:concierge -> if minecells.aura: original * 0.45, else original
                        mobs.put("minecells:concierge",
                                        List.of(DamageRuleConfig.create().msgIds("minecells.aura").mult(0.45f)));

                        // minecells:conjunctivius -> if > 250: min(120, 115). if > 150: min(120, 85).
                        // if < 50: min(120, 50). else min(120, original).
                        mobs.put("minecells:conjunctivius", List.of(
                                        DamageRuleConfig.create().minDmg(250.01f).fixed(115f), // Handing strictly > 250
                                        DamageRuleConfig.create().minDmg(150.01f).maxDmg(250f).fixed(85f), // Handing
                                                                                                           // strictly >
                                                                                                           // 150
                                        DamageRuleConfig.create().maxDmg(49.99f).fixed(50f), // Handling strictly < 50
                                        DamageRuleConfig.create().capFinal(120f) // Handling the else & final cap
                        ));

                        // palegardenbackport:creaking -> fixed 100
                        mobs.put("palegardenbackport:creaking", List.of(DamageRuleConfig.create().fixed(100f)));

                        // endermanoverhaul (many simple caps/multipliers)
                        mobs.put("endermanoverhaul:end_enderman",
                                        List.of(DamageRuleConfig.create().mult(0.8f).capFinal(100f)));
                        mobs.put("endermanoverhaul:badlands_enderman",
                                        List.of(DamageRuleConfig.create().capFinal(90f)));
                        mobs.put("endermanoverhaul:crimson_forest_enderman",
                                        List.of(DamageRuleConfig.create().capFinal(90f)));
                        mobs.put("endermanoverhaul:dark_oak_enderman",
                                        List.of(DamageRuleConfig.create().capFinal(90f)));
                        mobs.put("endermanoverhaul:end_islands_enderman",
                                        List.of(DamageRuleConfig.create().mult(1.4f).capFinal(90f)));
                        mobs.put("endermanoverhaul:coral_enderman",
                                        List.of(DamageRuleConfig.create().mult(1.9f).capFinal(90f)));
                        mobs.put("endermanoverhaul:soulsand_valley_enderman",
                                        List.of(DamageRuleConfig.create().mult(1.5f).capFinal(90f)));
                        mobs.put("endermanoverhaul:snowy_enderman",
                                        List.of(DamageRuleConfig.create().mult(1.5f).capFinal(90f)));
                        mobs.put("endermanoverhaul:warped_forest_enderman",
                                        List.of(DamageRuleConfig.create().mult(2f).capFinal(90f)));
                        mobs.put("endermanoverhaul:windswept_hills_enderman",
                                        List.of(DamageRuleConfig.create().mult(1.8f).capFinal(90f)));
                        mobs.put("endermanoverhaul:scarab", List.of(DamageRuleConfig.create().mult(3f).capFinal(90f)));
                        mobs.put("endermanoverhaul:ice_spikes_enderman",
                                        List.of(DamageRuleConfig.create().mult(0.7f).capFinal(90f)));
                        mobs.put("endermanoverhaul:spirit",
                                        List.of(DamageRuleConfig.create().mult(1.5f).capFinal(90f)));

                        // wandering_orc
                        mobs.put("wandering_orc:female_orc_elite",
                                        List.of(DamageRuleConfig.create().mult(0.8f).capFinal(150f)));
                        mobs.put("wandering_orc:spriggan", List.of(DamageRuleConfig.create().mult(3f).capFinal(50f)));
                        mobs.put("wandering_orc:orc_champion",
                                        List.of(DamageRuleConfig.create().mult(0.9f).capFinal(120f)));
                        mobs.put("wandering_orc:ogre", List.of(DamageRuleConfig.create().mult(0.9f).capFinal(115f)));
                        mobs.put("wandering_orc:crawler", List.of(DamageRuleConfig.create().mult(1.1f).capFinal(95f)));
                        mobs.put("wandering_orc:orc_chief", List.of(
                                        DamageRuleConfig.create().minDmg(150.01f).fixed(150f),
                                        DamageRuleConfig.create().capFinal(150f)));

                        // myths_of_the_sea
                        mobs.put("myths_of_the_sea:leviathan",
                                        List.of(DamageRuleConfig.create().mult(0.55f).capFinal(110f)));
                        mobs.put("myths_of_the_sea:bunyip",
                                        List.of(DamageRuleConfig.create().mult(0.55f).capFinal(80f)));
                        mobs.put("myths_of_the_sea:bake_kujira",
                                        List.of(DamageRuleConfig.create().mult(0.6f).capFinal(80f)));
                        mobs.put("myths_of_the_sea:kraken",
                                        List.of(DamageRuleConfig.create().mult(0.85f).capFinal(95f)));

                        // saintsdragons
                        mobs.put("saintsdragons:nulljaw", List.of(DamageRuleConfig.create().capFinal(95f)));
                }
        }

        public static class DamageRuleConfig {
                @Expose
                @SerializedName("condition")
                public DamageConditionConfig condition = new DamageConditionConfig();

                @Expose
                @SerializedName("action")
                public DamageActionConfig action = new DamageActionConfig();

                public static DamageRuleConfig create() {
                        return new DamageRuleConfig();
                }

                public DamageRuleConfig minDmg(float v) {
                        this.condition.minOriginalDamage = v;
                        return this;
                }

                public DamageRuleConfig maxDmg(float v) {
                        this.condition.maxOriginalDamage = v;
                        return this;
                }

                public DamageRuleConfig msgIds(String... ids) {
                        this.condition.damageSourceTypes = List.of(ids);
                        return this;
                }

                public DamageRuleConfig excMsgIds(String... ids) {
                        this.condition.excludeDamageSourceTypes = List.of(ids);
                        return this;
                }

                public DamageRuleConfig mult(float v) {
                        this.action.multiplier = v;
                        return this;
                }

                public DamageRuleConfig addHp(float fraction, float cap) {
                        this.action.addMaxHealthFraction = fraction;
                        this.action.maxHealthBonusCap = cap;
                        return this;
                }

                public DamageRuleConfig capFinal(float v) {
                        this.action.maxFinalDamage = v;
                        return this;
                }

                public DamageRuleConfig fixed(float v) {
                        this.action.fixedValue = v;
                        return this;
                }
        }

        public static class DamageConditionConfig {
                @Expose
                @SerializedName("minOriginalDamage")
                public Float minOriginalDamage = null;

                @Expose
                @SerializedName("maxOriginalDamage")
                public Float maxOriginalDamage = null;

                @Expose
                @SerializedName("damageSourceTypes")
                public List<String> damageSourceTypes = null;

                @Expose
                @SerializedName("excludeDamageSourceTypes")
                public List<String> excludeDamageSourceTypes = null;
        }

        public static class DamageActionConfig {
                @Expose
                @SerializedName("multiplier")
                public float multiplier = 1.0f;

                @Expose
                @SerializedName("addMaxHealthFraction")
                public float addMaxHealthFraction = 0.0f;

                @Expose
                @SerializedName("maxHealthBonusCap")
                public Float maxHealthBonusCap = null;

                @Expose
                @SerializedName("maxFinalDamage")
                public Float maxFinalDamage = null;

                @Expose
                @SerializedName("fixedValue")
                public Float fixedValue = null;
        }

        public static void loadConfig() {
                File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE_NAME);
                Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation()
                                .disableHtmlEscaping().create();

                if (configFile.exists()) {
                        try (FileReader reader = new FileReader(configFile)) {
                                INSTANCE = gson.fromJson(reader, ConfigLoader.class);
                                if (INSTANCE == null) {
                                        INSTANCE = new ConfigLoader(); // Fallback to default if JSON was empty or
                                                                       // malformed
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
