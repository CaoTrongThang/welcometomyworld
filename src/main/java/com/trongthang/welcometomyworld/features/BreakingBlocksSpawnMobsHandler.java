package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.trongthang.welcometomyworld.Utilities.Utils.spawnMob;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class BreakingBlocksSpawnMobsHandler {
    // Block and mob configuration holder
    class BlockToMobConfig {
        List<String> blockIds;
        List<MobWithSpawnChance> mobs = new ArrayList<>();
        boolean spawnOnlyOneMob;
        int blocksCooldown;
        int currentBlocksCooldown = 0;

        public BlockToMobConfig(List<String> blockIds, List<MobWithSpawnChance> mobs, boolean spawnOnlyOneMob, int blocksCooldown) {
            this.blockIds = blockIds;
            // Sort the mobs by spawn chance in descending order in the constructor
            this.mobs = new ArrayList<>(mobs); // Create a new list to avoid modifying the original reference
            this.mobs.sort(Comparator.comparingDouble(MobWithSpawnChance::getSpawnChance).reversed());
            this.spawnOnlyOneMob = spawnOnlyOneMob;
            this.blocksCooldown = blocksCooldown;
        }
    }

    // Mob configuration with spawn probability
    class MobWithSpawnChance {
        String mobId;
        double spawnChance;

        public MobWithSpawnChance(String mobId, double spawnChance) {
            this.mobId = mobId;
            this.spawnChance = spawnChance;
        }

        public double getSpawnChance() {
            return spawnChance;
        }
    }

    // Example block and mob configurations
    List<BlockToMobConfig> blockMobConfigs = List.of(
            new BlockToMobConfig(List.of("minecraft:grass_block", "minecraft:dirt"), List.of(
                    new MobWithSpawnChance("minecraft:zombie", 0.04)
            ), true, 5),
            new BlockToMobConfig(List.of("minecraft:sand"), List.of(
                    new MobWithSpawnChance("iceandfire:deathworm", 0.05),
                    new MobWithSpawnChance("minecraft:skeleton", 0.03)
            ), true, 6),
            new BlockToMobConfig(List.of("minecraft:cobblestone", "minecraft:stone"), List.of(
                    new MobWithSpawnChance("minecraft:skeleton", 0.03),
                    new MobWithSpawnChance("creeperoverhaul:hills_creeper", 0.04)
            ), false, 10),
            new BlockToMobConfig(List.of("minecraft:gravel"), List.of(
                    new MobWithSpawnChance("wildlife:anglerfish", 0.03),
                    new MobWithSpawnChance("wildlife:bettafish", 0.03),
                    new MobWithSpawnChance("wildlife:bluegill", 0.03),
                    new MobWithSpawnChance("wildlife:catfish", 0.03),
                    new MobWithSpawnChance("wildlife:goldfish", 0.03),
                    new MobWithSpawnChance("wildlife:koi", 0.03),
                    new MobWithSpawnChance("wildlife:rainbow_trout", 0.03),
                    new MobWithSpawnChance("minecraft:cod", 0.03),
                    new MobWithSpawnChance("minecraft:salmon", 0.03),
                    new MobWithSpawnChance("iceandfire:sea_serpent", 0.8),
                    new MobWithSpawnChance("iceandfire:siren", 0.03)
            ), true, 5),
            new BlockToMobConfig(List.of("minecraft:obsidian"), List.of(
                    new MobWithSpawnChance("netherexp:wisp", 0.1)
            ), true, 0),
            new BlockToMobConfig(List.of("minecraft:oak_leaves", "minecraft:birch_leaves", "minecraft:jungle_leaves", "minecraft:acacia_leaves", "minecraft:dark_oak_leaves", "minecraft:mangrove_leaves", "minecraft:azalea_leaves", "minecraft:flowering_azalea_leaves", "minecraft:spruce_leaves"), List.of(
                    new MobWithSpawnChance("wildlife:cottonbird", 0.03),
                    new MobWithSpawnChance("wildlife:bluebird", 0.03),
                    new MobWithSpawnChance("wildlife:lark", 0.03),
                    new MobWithSpawnChance("wildlife:milk_snake", 0.03),
                    new MobWithSpawnChance("wildlife:monkey", 0.03),
                    new MobWithSpawnChance("wildlife:meadowlark", 0.03),
                    new MobWithSpawnChance("wildlife:king_snake", 0.03),
                    new MobWithSpawnChance("minecraft:creeper", 0.08),
                    new MobWithSpawnChance("minecraft:chicken", 0.03),
                    new MobWithSpawnChance("netherexp:wisp", 0.01)
            ), true, 5),

            new BlockToMobConfig(List.of("minecraft:oak_log", "minecraft:birch_log", "minecraft:jungle_log"), List.of(
                    new MobWithSpawnChance("knightquest:ratman", 0.04),
                    new MobWithSpawnChance("netherexp:stampede", 0.01)
            ), true, 3),

            new BlockToMobConfig(List.of("minecraft:redstone_ore", "minecraft:deepslate_redstone_ore"), List.of(
                    new MobWithSpawnChance("minecraft:skeleton", 0.03)
            ), true, 5),
            new BlockToMobConfig(List.of("minecraft:bamboo"), List.of(
                    new MobWithSpawnChance("creeperoverhaul:bamboo_creeper", 0.1)
            ), true, 5)
    );

    private final Random rand = new Random();

    // Handle mob spawning at block position based on configuration
    public void handleBlockBreakMobSpawn(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {

        if (world.isClient) {
            return; // Skip client-side execution
        }

        // Loop through block-to-mob configurations to determine if block break spawns a mob
        for (BlockToMobConfig blockMobConfig : blockMobConfigs) {
            for (String blockId : blockMobConfig.blockIds) {
                if (isMatchingBlock(blockState, blockId)) {

                    blockMobConfig.currentBlocksCooldown++;

                    if (blockMobConfig.currentBlocksCooldown <= blockMobConfig.blocksCooldown) break;

                    spawnMobsBasedOnConfig(world, player, blockPos, blockMobConfig);
                    blockMobConfig.currentBlocksCooldown = 0;

                }
            }
        }
    }

    // Check if the block state matches the specified block ID
    private boolean isMatchingBlock(BlockState blockState, String blockId) {
        Identifier blockIdentifier = new Identifier(blockId.toLowerCase());
        return Registries.BLOCK.containsId(blockIdentifier) && blockState.isOf(Registries.BLOCK.get(blockIdentifier));
    }

    // Spawns mobs according to the configuration's spawn chances
    private void spawnMobsBasedOnConfig(World world, PlayerEntity player, BlockPos blockPos, BlockToMobConfig config) {
        if (config.spawnOnlyOneMob) {
            for (MobWithSpawnChance mobWithChance : config.mobs) {
                int randomMob = rand.nextInt(0, config.mobs.size());
                MobWithSpawnChance mob = config.mobs.get(randomMob);
                if (rand.nextDouble() < mob.spawnChance) {
                    Entity entity = spawnMob(world, blockPos, config.mobs.get(randomMob).mobId);
                    ((MobEntity) entity).setTarget(player);
                    return;
                }
            }
        } else {
            for (MobWithSpawnChance mobWithChance : config.mobs) {
                if (rand.nextDouble() < mobWithChance.spawnChance) {
                    Entity entity = spawnMob(world, blockPos, mobWithChance.mobId);
                    ((MobEntity) entity).setTarget(player);
                }
            }
        }
    }
}
