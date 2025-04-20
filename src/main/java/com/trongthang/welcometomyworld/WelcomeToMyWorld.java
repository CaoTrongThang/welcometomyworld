package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.ModTagsManager;
import com.trongthang.welcometomyworld.events.SpawnEvents;
import com.trongthang.welcometomyworld.managers.*;
import com.trongthang.welcometomyworld.features.*;
import com.trongthang.welcometomyworld.items.RepairTalisman;
import com.trongthang.welcometomyworld.items.BuffTalisman;
import com.trongthang.welcometomyworld.classes.PlayerData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.render.entity.feature.EndermanEyesFeatureRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.explosion.Explosion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.trongthang.welcometomyworld.GlobalConfig.*;

import java.util.*;


//! TODO: some events when playing progressing the world like: mobs could be spawned when players break leaves or stones, punching blocks with bare hand will get damaged

public class WelcomeToMyWorld implements ModInitializer {
    public static Random random = new Random();
    public static final String MOD_ID = "welcometomyworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static DataHandler dataHandler = new DataHandler();
    public static CompatityChecker compatityChecker = new CompatityChecker();
    public static BlocksPlacedAndBrokenByMobsHandler blocksPlacedAndBrokenByMobsHandler = new BlocksPlacedAndBrokenByMobsHandler();

    public static final Identifier REQUEST_MOB_STATS_PACKET = new Identifier(MOD_ID, "request_mob_stats");
    public static final Identifier UPDATE_MOB_STAT = new Identifier(MOD_ID, "update_mob_stat");
    public static final Identifier SYNC_MOB_STATS_CLIENT = new Identifier(MOD_ID, "sync_mob_stats");

    public static final Identifier FIRST_ORIGIN_CHOOSING_SCREEN = new Identifier(MOD_ID, "first_origin_choosing_screen");
    public static final Identifier STOP_SENDING_ORIGINS_SCREEN = new Identifier(MOD_ID, "stop_sending_origins_screen");

    public static final Identifier FIRST_LOADING_TERRAIN_SCREEN = new Identifier(MOD_ID, "first_loading_terrian_screen");

    public static final Identifier CLIENT_HAS_DATA = new Identifier(MOD_ID, "client_has_data");

    public static final Identifier PLAY_BLOCK_PORTAL_TRAVEL = new Identifier(MOD_ID, "play_block_portal_travel");
    public static final Identifier PLAY_BLOCK_LEVER_CLICK = new Identifier(MOD_ID, "play_block_lever_click");
    public static final Identifier PLAY_EXPERIENCE_ORB_PICK_UP = new Identifier(MOD_ID, "play_experience_orb_pick_up");
    public static final Identifier PLAY_ENTITY_PLAYER_LEVELUP = new Identifier(MOD_ID, "play_entity_player_levelup");
    public static final Identifier PLAY_WOLF_HOWL = new Identifier(MOD_ID, "play_wolf_howl");
    public static final Identifier PLAY_ANVIL_USE = new Identifier(MOD_ID, "play_anvil_use");
    public static final Identifier TIRED_SOUND = new Identifier(MOD_ID, "tired_sound");

    public static final Identifier A_LIVING_CHEST_EATING_SOUND = new Identifier(MOD_ID, "a_living_chest_eating_sound");
    public static final Identifier A_LIVING_CHEST_MOUTH_CLOSE = new Identifier(MOD_ID, "a_living_chest_mouth_close");
    public static final Identifier A_LIVING_CHEST_MOUTH_OPEN = new Identifier(MOD_ID, "a_living_chest_mouth_open");
    public static final Identifier A_LIVING_CHEST_JUMP = new Identifier(MOD_ID, "a_living_chest_jump");
    public static final Identifier A_LIVING_CHEST_EAT_ANIMATION = new Identifier(MOD_ID, "eat_animation");
    public static final Identifier A_LIVING_CHEST_ATTACK = new Identifier(MOD_ID, "a_living_chest_attack");

    public static final Identifier PORTALER_COMPLETE_PORTAL_CHANGING = new Identifier(MOD_ID, "portaler_complete_portal_changing");
    public static final Identifier PLAY_BELL = new Identifier(MOD_ID, "play_bell");

    public static final Identifier SOUND_PACKET_ID = new Identifier(MOD_ID, "sound_packet");
    public static final Identifier ANIMATION_PACKET = new Identifier(MOD_ID, "animation_packet");

    public static final Identifier CHANGE_PERSPECTIVE = new Identifier(MOD_ID, "change_perspective_packet");
    public static final Identifier PLAYER_BREAKING_BLOCK = new Identifier(MOD_ID, "breaking_block");

    public static final Identifier FALLING_TO_WATER = new Identifier(MOD_ID, "falling_to_water");

    public static DeathCounter deathCounter = new DeathCounter();

    public static FallingToWaterDamage fallingToWaterDamage = new FallingToWaterDamage();
    public static GiveStartingItemsHandler giveStartingItemsHandler = new GiveStartingItemsHandler();
    public static IntroOfTheWorldHandler introOfTheWorldHandler = new IntroOfTheWorldHandler();
    public static BreakingBlocksSpawnMobsHandler breakingBlocksSpawnMobsHandler = new BreakingBlocksSpawnMobsHandler();
    public static AwakeHandler awakeHandler = new AwakeHandler();
    public static DayAndNightCounterAnimationHandler dayAndNightCounterAnimationHandler = new DayAndNightCounterAnimationHandler();
    public static PunchingBlocksPenalties punchingBlocksPenalties = new PunchingBlocksPenalties();
    public static WorldDifficultyBasedOnInGameDay worldDifficultyBasedOnInGameDay = new WorldDifficultyBasedOnInGameDay();
    public static AchievementsHandler achievementsHandler = new AchievementsHandler();
    public static NauseaInWaterHandler nauseaInWaterHandler = new NauseaInWaterHandler();
    public static BossesSpawningHandler bossesSpawningHandler = new BossesSpawningHandler();
    public static LightningsStrikePlayersInRain lightningsStrikePlayersInRain = new LightningsStrikePlayersInRain();

    MinecraftServer server;

    @Override
    public void onInitialize() {
        ModTagsManager.registerTags();
        ConfigLoader.loadConfig();
        compatityChecker.OriginCheck();

        ServerLifecycleEvents.SERVER_STARTING.register((t) -> {
            introOfTheWorldHandler.alreadySpawnedPhantom = false;
            dataHandler.initializeWorldData(t);

            dayAndNightCounterAnimationHandler.resetFields();
            registerEvents();
        });

        ServerLifecycleEvents.SERVER_STARTED.register((t) -> {

            ServerWorld world = t.getOverworld();
            IntroOfTheWorldHandler.playersDeathChanceInTheIntro = t.isHardcore() ? 0.05 : 0.15;

            if (world != null) {
                world.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(100, t);
            }

            if (ConfigLoader.getInstance().noMoreF3B) {
                if (world != null) {
                    world.getGameRules().get(GameRules.REDUCED_DEBUG_INFO).set(true, t);
                }
            }

            if (world != null) {
                world.getGameRules().get(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK).set(true, t);
            }

            server = t;
        });

        ServerLifecycleEvents.SERVER_STOPPED.register((t) -> {
            dataHandler.saveData(t);
            server = null;
        });

        MusicPlayer.initialize();

        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) ->
        {
            performAllActionsFirstJoin(serverPlayNetworkHandler.getPlayer());

            UUID playerUUID = serverPlayNetworkHandler.getPlayer().getUuid();

            if (!dataHandler.playerDataMap.containsKey(playerUUID)) {
                PlayerData pl = dataHandler.playerDataMap.get(playerUUID);
                if(pl.firstTouchGround){
                    ServerPlayNetworking.send(serverPlayNetworkHandler.getPlayer(), CLIENT_HAS_DATA, PacketByteBufs.empty());
                }
            }
        });


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
        EntitiesManager.register();
        BlocksEntitiesManager.initialize();
        BlocksManager.registerModBlocks();

        SoundsManager.registerSounds();
        ServerNetworking.register();
        SpawnEvents.register();
        AwakeHandler.register();
        HostileMobsAwareness.registerEvents();
        MinecellsDimensionSarcastic.registerEvents();
        AllowSleepAllTime.registerEvents();
    }



    private void performAllActionsFirstJoin(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();

        LOGGER.info("Checking if player {} is a first-time joiner in this world...", player.getName().getString());

        if (!dataHandler.playerDataMap.containsKey(playerUUID)) {
            LOGGER.info("Player {} is joining for the first time in this world. Adding to firstTimePlayers set and teleporting.", player.getName().getString());
            dataHandler.playerDataMap.put(playerUUID, new PlayerData(true));
            ServerPlayNetworking.send(player, CHANGE_PERSPECTIVE, PacketByteBufs.empty());
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

        if (canBedsExplode) {
            awakeHandler.awakeCheck(server);
        }

        SpawnSingleMonsterEverySeconds.spawnMonsters(server.getOverworld(), dayAndNightCounterAnimationHandler.currentDay);

        HostileMobsAwareness.onServerTick(server);


        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (ConfigLoader.getInstance().introOfTheWorld) {
                introOfTheWorldHandler.handlePlayerFirstJoin(player);
            }

            if (canDayAndNightCounterAnimation) {
                dayAndNightCounterAnimationHandler.onServerTick(player);
            }

//           if (canNauseaInWater) {
//               nauseaInWaterHandler.onServerTick(server);
//           }

            if (ConfigLoader.getInstance().giveStartingItems) {
                GiveStartingItemsHandler.giveItemHandler(player, ConfigLoader.getInstance().clearItemsBeforeGivingStartingItems);
            }
        }

        if (canBossesSpawningHanlder) {
            bossesSpawningHandler.spawnBossNearPlayers(server.getOverworld());
        }

        ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_IRON).onServerTick(server, ItemsManager.REPAIR_TALISMAN_IRON);
        ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_GOLD).onServerTick(server, ItemsManager.REPAIR_TALISMAN_GOLD);
        ((RepairTalisman) ItemsManager.REPAIR_TALISMAN_EMERALD).onServerTick(server, ItemsManager.REPAIR_TALISMAN_EMERALD);

        ((BuffTalisman) ItemsManager.POWER_TALISMAN).onServerTick(server, ItemsManager.POWER_TALISMAN);
        ((BuffTalisman) ItemsManager.SPEED_TALISMAN).onServerTick(server, ItemsManager.SPEED_TALISMAN);
        ((BuffTalisman) ItemsManager.LIFE_TALISMAN).onServerTick(server, ItemsManager.LIFE_TALISMAN);
        ((BuffTalisman) ItemsManager.RESISTANCE_TALISMAN).onServerTick(server, ItemsManager.RESISTANCE_TALISMAN);

        if (canEventsOfTheWorld) {
            EventsOfTheWorld.onServerTick(server);
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
        SpawnMonstersPackEveryMins.spawnMonsters(server);
    }

    public void registerEvents() {
        bossesSpawningHandler.bossDropsRegister();
        introOfTheWorldHandler.registerIntroEvents();
    }
}
