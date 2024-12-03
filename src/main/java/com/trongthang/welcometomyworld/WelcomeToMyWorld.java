package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.CustomEntitiesManager;
import com.trongthang.welcometomyworld.features.*;
import com.trongthang.welcometomyworld.items.RepairTalisman;
import com.trongthang.welcometomyworld.items.BuffTalisman;
import com.trongthang.welcometomyworld.saveData.PlayerClass;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.trongthang.welcometomyworld.GlobalConfig.*;

import java.util.*;


//! TODO: some events when playing progressing the world like: mobs could be spawned when players break leaves or stones, punching blocks with bare hand will get damaged

public class WelcomeToMyWorld implements ModInitializer {
    public static final String MOD_ID = "welcometomyworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static DataHandler dataHandler = new DataHandler();
    public static CompatityChecker compatityChecker = new CompatityChecker();
    public static BlocksPlacedAndBrokenByMobsHandler blocksPlacedAndBrokenByMobsHandler = new BlocksPlacedAndBrokenByMobsHandler();

    public static final Identifier FIRST_ORIGIN_CHOOSING_SCREEN = new Identifier(MOD_ID, "first_origin_choosing_screen");
    public static final Identifier STOP_SENDING_ORIGINS_SCREEN = new Identifier(MOD_ID, "stop_sending_origins_screen");

    public static final Identifier PLAY_BLOCK_PORTAL_TRAVEL = new Identifier(MOD_ID, "play_block_portal_travel");
    public static final Identifier PLAY_BLOCK_LEVER_CLICK = new Identifier(MOD_ID, "play_block_lever_click");
    public static final Identifier PLAY_EXPERIENCE_ORB_PICK_UP = new Identifier(MOD_ID, "play_experience_orb_pick_up");
    public static final Identifier PLAY_ENTITY_PLAYER_LEVELUP = new Identifier(MOD_ID, "play_entity_player_levelup");
    public static final Identifier PLAY_WOLF_HOWL = new Identifier(MOD_ID, "play_wolf_howl");
    public static final Identifier PLAY_BELL = new Identifier(MOD_ID, "play_bell");

    public static final Identifier CHANGE_PERSPECTIVE = new Identifier(MOD_ID, "change_perspective_packet");
    public static final Identifier PLAYER_BREAKING_BLOCK = new Identifier(MOD_ID, "breaking_block");

    public static final Identifier FALLING_TO_WATER = new Identifier(MOD_ID, "falling_to_water");

    public static DeathCounter deathCounter = new DeathCounter();


    public static FallingToWaterDamage fallingToWaterDamage = new FallingToWaterDamage();
    public static GiveStartingItemsHandler giveStartingItemsHandler = new GiveStartingItemsHandler();
    public static SwitchPerspectiveFirstJoin switchPerspectiveFirsJoin = new SwitchPerspectiveFirstJoin();
    public static IntroOfTheWorldHandler introOfTheWorldHandler = new IntroOfTheWorldHandler();
    public static BreakingBlocksSpawnMobsHandler breakingBlocksSpawnMobsHandler = new BreakingBlocksSpawnMobsHandler();
    public static BedExplosionHandler bedExplosionHandler = new BedExplosionHandler();
    public static DayAndNightCounterAnimationHandler dayAndNightCounterAnimationHandler = new DayAndNightCounterAnimationHandler();
    public static PowerUpNearByHostileMobs powerUpNearByHostileMobs = new PowerUpNearByHostileMobs();
    public static PunchingBlocksPenalties punchingBlocksPenalties = new PunchingBlocksPenalties();
    public static WorldDifficultyBasedOnInGameDay worldDifficultyBasedOnInGameDay = new WorldDifficultyBasedOnInGameDay();
    public static AchievementsHandler achievementsHandler = new AchievementsHandler();
    public static NauseaInWaterHandler nauseaInWaterHandler = new NauseaInWaterHandler();
    public static BossesSpawningHandler bossesSpawningHandler = new BossesSpawningHandler();
    public static LightningsStrikePlayersInRain lightningsStrikePlayersInRain = new LightningsStrikePlayersInRain();
    public static PhantomSpawnHandler phantomSpawnHandler = new PhantomSpawnHandler();


    @Override
    public void onInitialize() {
        compatityChecker.OriginCheck();

        ServerLifecycleEvents.SERVER_STARTING.register((t) -> {
            introOfTheWorldHandler.alreadySpawnedPhantom = false;
            dataHandler.initializeWorldData(t);

            dayAndNightCounterAnimationHandler.resetFields();

            registerEvents();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((t) -> {
            dataHandler.saveData(t);
        });

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> performAllActionsFirstJoin(serverPlayNetworkHandler.getPlayer()));

        PlayerBlockBreakEvents.AFTER.register((world, playerEntity, blockPos, blockState, blockEntity) -> {
            if (!canBreakBlockSpawnMobs) return;
            breakingBlocksSpawnMobsHandler.handleBlockBreakMobSpawn(world, playerEntity, blockPos, blockState, blockEntity);
        });

        ServerTickEvents.START_SERVER_TICK.register(this::onStartServerTick);
        ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);

        punchingBlocksPenalties.handlePunchingBlock();

        deathCounter.startCountingDeaths();

        if (canWaterFallDamage) {
            fallingToWaterDamage.handleFallingToWaterDamage();
        }

        ItemsManager.initialize();
        phantomSpawnHandler.register();
        CustomEntitiesManager.register();
    }

    private void performAllActionsFirstJoin(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();

        LOGGER.info("Checking if player {} is a first-time joiner in this world...", player.getName().getString());

        if (!dataHandler.playerDataMap.containsKey(playerUUID)) {
            LOGGER.info("Player {} is joining for the first time in this world. Adding to firstTimePlayers set and teleporting.", player.getName().getString());
            dataHandler.playerDataMap.put(playerUUID, new PlayerClass(true));
        } else {
            LOGGER.info("Player {} has already joined before in this world. Skipping teleport.", player.getName().getString());

            return;
        }

        if (player.isAlive()) {
            if (canSwitchPerspective) {
                player.sendMessage(
                        Text.literal("Easycraft - Creator: TrongThang").styled(style -> style.withItalic(true).withColor(Formatting.GRAY))
                );
            }
        }
    }

    private void onStartServerTick(MinecraftServer server) {
    }

    private void onEndServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            if (canBedsExplode) {
                bedExplosionHandler.checkAndExplodeIfSleeping(player);
            }

            if (canDayCounter) {
                dayAndNightCounterAnimationHandler.onServerTick(player);
            }

            if (canPowerUpNearbyHostileMobs) {
                powerUpNearByHostileMobs.checkAndPowerUpMobs(server.getOverworld(), player);
            }

            if (canIntroOfTheWorld) {
                introOfTheWorldHandler.handlePlayerFirstJoin(player);
            }

            if (canNauseaInWater) {
                nauseaInWaterHandler.onServerTick(server);
            }

            if (canBossesSpawningHanlder) {
                bossesSpawningHandler.spawnZombieNearPlayers(server.getOverworld());
            }

            if (canGiveStartingItems) {
                GiveStartingItemsHandler.giveItemHandler(player, canClearItemsBeforeGivingStartingItems);
            }

            ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_IRON).onServerTick(player, ItemsManager.REPAIR_TALISMAN_IRON);
            ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_GOLD).onServerTick(player, ItemsManager.REPAIR_TALISMAN_GOLD);
            ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_EMERALD).onServerTick(player, ItemsManager.REPAIR_TALISMAN_EMERALD);

            ((BuffTalisman) ItemsManager.POWER_TALISMAN).onServerTick(player, ItemsManager.POWER_TALISMAN);
            ((BuffTalisman) ItemsManager.SPEED_TALISMAN).onServerTick(player, ItemsManager.SPEED_TALISMAN);
            ((BuffTalisman) ItemsManager.LIFE_TALISMAN).onServerTick(player, ItemsManager.LIFE_TALISMAN);
            ((BuffTalisman) ItemsManager.RESISTANCE_TALISMAN).onServerTick(player, ItemsManager.RESISTANCE_TALISMAN);
        }

        if (canWorldDifficultyBasedOnDay) {
            worldDifficultyBasedOnInGameDay.onServerTick(server);
        }

        if (canAchievementHandler) {
            achievementsHandler.onServerTick(server);
        }

        if (canLightningsStrikePlayersInRain) {
            lightningsStrikePlayersInRain.onServerTick(server);
        }

        blocksPlacedAndBrokenByMobsHandler.onSererTick(server);

        Utils.onServerTick(server);

    }

    public void registerEvents() {
        bossesSpawningHandler.bossDropsRegister();
        introOfTheWorldHandler.registerIntroEvents();
    }

}
