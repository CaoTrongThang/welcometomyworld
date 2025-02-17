package com.trongthang.welcometomyworld;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.trongthang.welcometomyworld.classes.PlayerData;
import com.trongthang.welcometomyworld.classes.PlayerStatsData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class DataHandler {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BlockPos.class, new BlockPosAdapter())
            .registerTypeAdapter(BlockState.class, new BlockStateAdapter())
            .registerTypeAdapter(Path.class, new PathAdapter())  // Add the Path adapter
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public Path playerDataSavePath;
    public static final Type PLAYER_DATA_TYPE = new TypeToken<ConcurrentHashMap<UUID, PlayerData>>() {}.getType();
    public ConcurrentHashMap<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public Path playerStatsDataSavePath;
    public static final Type PLAYER_STATS_DATA_TYPE = new TypeToken<ConcurrentHashMap<UUID, PlayerStatsData>>() {}.getType();
    public ConcurrentHashMap<UUID, PlayerStatsData> playerStatsData = new ConcurrentHashMap<>();

    public Path blocksPlacedByMobs;
    public static final Type BLOCKS_PLACED_BY_MOBS_DATA_TYPE = new TypeToken<ConcurrentHashMap<BlockPos, Integer>>() {}.getType();
    public ConcurrentHashMap<BlockPos, Integer> blocksPlacedByMobWillRemove = new ConcurrentHashMap<>();

    public Path blocksBrokenByMobs;
    public static final Type BLOCKS_BROKEN_BY_MOBS_DATA_TYPE = new TypeToken<ConcurrentHashMap<BlockPos, BlockState>>() {}.getType();
    public ConcurrentHashMap<BlockPos, BlockState> blocksBrokenByMobWillRestore = new ConcurrentHashMap<>();

    public void initializeWorldData(MinecraftServer server) {
        // Generate a file path specific to the world being loaded
        playerDataSavePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/welcometomyworld/playerdata.json");
        blocksPlacedByMobs = server.getSavePath(WorldSavePath.ROOT).resolve("data/welcometomyworld/blocksPlacedByMobs.json");
//        blocksBrokenByMobs = server.getSavePath(WorldSavePath.ROOT).resolve("data/welcometomyworld/blocksBrokenByMobs.json");
        playerStatsDataSavePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/welcometomyworld/playerStatsData.json");

        playerDataMap.clear();
        playerStatsData.clear();
        blocksPlacedByMobWillRemove.clear();
//        blocksBrokenByMobWillRestore.clear();

        loadPlayerData();
        loadPlayerStatsData();
        loadBlocksPlacedByMobsData();
//        loadBlocksBrokenByMobsData();

        findAndAddAlreadyExistPlayers(server);
    }


    public void saveData(MinecraftServer server) {
        if(this.playerDataSavePath == null){
            playerDataSavePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/welcometomyworld/playerdata.json");
        }
        savePlayerData();
        savePlayerStatsData();
        saveBlocksPlacedByMobsData();
//        saveBlocksBrokenByMobsData();

        playerDataMap.clear();
        playerStatsData.clear();
        blocksPlacedByMobWillRemove.clear();
        blocksBrokenByMobWillRestore.clear();
    }

    public void findAndAddAlreadyExistPlayers(MinecraftServer server) {
        Path playerdataPath = server.getSavePath(WorldSavePath.PLAYERDATA);

        if (!Files.exists(playerdataPath) || !Files.isDirectory(playerdataPath)) {
            LOGGER.warn("Playerdata directory does not exist: {}", playerdataPath);
            return;
        }

        try {
            Files.list(playerdataPath)
                    .filter(path -> path.toString().endsWith(".dat")) // Filter only `.dat` files
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        try {
                            // Extract the UUID from the file name (without the ".dat" extension)
                            UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));

                            // If the player is not in the map, add them with a new PlayerData entry
                            if (!playerDataMap.containsKey(uuid)) {
                                playerDataMap.put(uuid, PlayerData.CreateExistPlayer());
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("Invalid UUID found in playerdata file name: {}", fileName);
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Failed to read playerdata directory: {}", playerdataPath, e);
        }
    }

    public void loadPlayerData() {
        LOGGER.info("Loading player data from: {}", playerDataSavePath);

        if (Files.exists(playerDataSavePath)) {
            try (Reader reader = Files.newBufferedReader(playerDataSavePath)) {
                ConcurrentHashMap<UUID, PlayerData> loadedData = GSON.fromJson(reader, PLAYER_DATA_TYPE);
                if (loadedData != null) {
                    playerDataMap.putAll(loadedData);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load player data", e);
            }
        }
    }

    public void savePlayerData() {
        LOGGER.info("Saving player data to: {}", playerDataSavePath);

        try {
            Files.createDirectories(playerDataSavePath.getParent());
            try (Writer writer = Files.newBufferedWriter(playerDataSavePath)) {
                GSON.toJson(playerDataMap, PLAYER_DATA_TYPE, writer);
            }
            LOGGER.info("Saved {} player entries to data file.", playerDataMap.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save player data", e);
        }
    }

    public void loadPlayerStatsData() {
        LOGGER.info("Loading player stats data from: {}", playerStatsDataSavePath);

        if (Files.exists(playerStatsDataSavePath)) {
            try (Reader reader = Files.newBufferedReader(playerStatsDataSavePath)) {
                ConcurrentHashMap<UUID, PlayerStatsData> loadedData = GSON.fromJson(reader, PLAYER_STATS_DATA_TYPE);
                if (loadedData != null) {
                    playerStatsData.putAll(loadedData);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load player stats data", e);
            }
        }
    }

    public void savePlayerStatsData() {
        LOGGER.info("Saving player stats data to: {}", playerStatsDataSavePath);

        try {
            Files.createDirectories(playerStatsDataSavePath.getParent());
            try (Writer writer = Files.newBufferedWriter(playerStatsDataSavePath)) {
                GSON.toJson(playerStatsData, PLAYER_DATA_TYPE, writer);
            }
            LOGGER.info("Saved {} player stats entries to data file.", playerStatsData.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save player data", e);
        }
    }

    public void loadBlocksPlacedByMobsData() {
        LOGGER.info("Loading blocks placed by mobs data from: {}", blocksPlacedByMobs);

        if (Files.exists(blocksPlacedByMobs)) {
            try (Reader reader = Files.newBufferedReader(blocksPlacedByMobs)) {
                ConcurrentHashMap<BlockPos, Integer> loadedData = GSON.fromJson(reader, BLOCKS_PLACED_BY_MOBS_DATA_TYPE);
                if (loadedData != null) {
                    blocksPlacedByMobWillRemove.putAll(loadedData);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load blocks placed by mobs data", e);
            }
        }
    }

    public void saveBlocksPlacedByMobsData() {
        LOGGER.info("Saving blocks placed by mobs data to: {}", blocksPlacedByMobs);

        try {
            Files.createDirectories(blocksPlacedByMobs.getParent());
            try (Writer writer = Files.newBufferedWriter(blocksPlacedByMobs)) {
                GSON.toJson(blocksPlacedByMobWillRemove, BLOCKS_PLACED_BY_MOBS_DATA_TYPE, writer);
            }
            LOGGER.info("Saved {} blocks placed by mobs entries to data file.", blocksPlacedByMobWillRemove.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save blocks placed by mobs data", e);
        }
    }

    // Custom Adapter for BlockPos
    public static class BlockPosAdapter implements JsonSerializer<BlockPos>, JsonDeserializer<BlockPos> {
        @Override
        public JsonElement serialize(BlockPos src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            return obj;
        }

        @Override
        public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                return new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
            } else if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                // Handle stringified form "BlockPos{x=39, y=69, z=-67}"
                String blockPosStr = json.getAsString();
                try {
                    String[] parts = blockPosStr.replace("BlockPos{", "").replace("}", "").split(", ");
                    int x = Integer.parseInt(parts[0].split("=")[1]);
                    int y = Integer.parseInt(parts[1].split("=")[1]);
                    int z = Integer.parseInt(parts[2].split("=")[1]);
                    return new BlockPos(x, y, z);
                } catch (Exception e) {
                    throw new JsonParseException("Invalid BlockPos format: " + blockPosStr, e);
                }
            } else {
                throw new JsonParseException("Expected a JSON object or string for BlockPos, but got: " + json);
            }
        }
    }

    public static class BlockStateAdapter implements JsonSerializer<BlockState>, JsonDeserializer<BlockState> {
        @Override
        public JsonElement serialize(BlockState src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Block.STATE_IDS.getRawId(src));
        }

        @Override
        public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            int stateId = json.getAsInt();
            return Block.STATE_IDS.get(stateId);
        }
    }

    public static class PathAdapter implements JsonSerializer<Path>, JsonDeserializer<Path> {
        @Override
        public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Path.of(json.getAsString());
        }
    }
}
