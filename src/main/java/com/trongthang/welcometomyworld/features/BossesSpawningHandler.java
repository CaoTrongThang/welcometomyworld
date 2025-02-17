package com.trongthang.welcometomyworld.features;

import com.trongthang.welcometomyworld.managers.ItemsManager;
import com.trongthang.welcometomyworld.Utilities.Utils;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.GlobalConfig.*;

public class BossesSpawningHandler {
    public int checkInterval = 5000;
    public int counter = 0;

    public double bossSpawnChance = 0.5;

    final int SPAWN_MIN_DISTANCE = 32; // Minimum distance from player
    final int SPAWN_MAX_DISTANCE = 64; // Maximum distance from player


    private final ConcurrentHashMap<String, EntityType> ancientMobs = new ConcurrentHashMap<>();
    List<AncientMobDrops> ancientMobDrops = List.of(new AncientMobDrops(ItemsManager.ANCIENT_FRAGMENT, 1), new AncientMobDrops(ItemsManager.REPAIR_KNOWLEDGE, 0.6));

    class AncientMobDrops {
        private ItemStack dropItem;
        private double chance;

        public AncientMobDrops(Item dropItem, double chance) {
            this.dropItem = new ItemStack(dropItem);
            this.chance = chance;
        }

        public ItemStack getDropItem() {
            return dropItem;
        }

        public double getDropChance() {
            return chance;
        }
    }

    private final Random random = new Random();

    public BossesSpawningHandler() {
        ancientMobs.put("Grimstalker", EntityType.ZOMBIE);
        ancientMobs.put("Bonecrusher", EntityType.ZOMBIE);
        ancientMobs.put("Fleshgrinder", EntityType.SPIDER);
        ancientMobs.put("Shadowmancer", EntityType.WITCH);
        ancientMobs.put("Dreadfiend", EntityType.ZOMBIE);
        ancientMobs.put("Soulstealer", EntityType.SPIDER);
        ancientMobs.put("Deathbringer", EntityType.ZOMBIE);
        ancientMobs.put("Rotting Revenant", EntityType.ZOMBIE);
        ancientMobs.put("Plaguebearer", EntityType.SPIDER);
        ancientMobs.put("Creeping Dread", EntityType.SPIDER);
        ancientMobs.put("Ghoulmancer", EntityType.ZOMBIE);
        ancientMobs.put("Fleshweaver", EntityType.ZOMBIE);
        ancientMobs.put("Necrofiend", EntityType.SPIDER);
        ancientMobs.put("Hallow's Wrath", EntityType.WITCH);
        ancientMobs.put("Nightcrawler", EntityType.SPIDER);
        ancientMobs.put("Blightbringer", EntityType.ZOMBIE);
        ancientMobs.put("Venomous Viper", EntityType.SPIDER);
        ancientMobs.put("The Unseen", EntityType.ZOMBIE);
        ancientMobs.put("Vile Wretch", EntityType.ZOMBIE);
        ancientMobs.put("Chillbringer", EntityType.ZOMBIE);
        ancientMobs.put("Soul Siphoner", EntityType.WITCH);
        ancientMobs.put("Cursed One", EntityType.ZOMBIE);
        ancientMobs.put("Maw of Darkness", EntityType.ZOMBIE);
        ancientMobs.put("Gravewalker", EntityType.ZOMBIE);
        ancientMobs.put("Boneclaw", EntityType.ZOMBIE);
        ancientMobs.put("Scourgecaller", EntityType.SPIDER);
        ancientMobs.put("Doomharbinger", EntityType.ZOMBIE);
        ancientMobs.put("Slime King", EntityType.SLIME);
        ancientMobs.put("The Unholy", EntityType.SPIDER);
        ancientMobs.put("Death's Grin", EntityType.ZOMBIE);
        ancientMobs.put("Spiteful Specter", EntityType.SPIDER);
        ancientMobs.put("Fleshrot", EntityType.ZOMBIE);
        ancientMobs.put("Black Widow", EntityType.SPIDER);
        ancientMobs.put("The Hollow Lord", EntityType.ZOMBIE);
        ancientMobs.put("Screeching Terror", EntityType.SPIDER);
        ancientMobs.put("Rotface", EntityType.ZOMBIE);
        ancientMobs.put("Fleshbeast", EntityType.SPIDER);
        ancientMobs.put("Shattered Soul", EntityType.ZOMBIE);
        ancientMobs.put("Hellspawn", EntityType.ZOMBIE);
        ancientMobs.put("Swarmlord", EntityType.SPIDER);
        ancientMobs.put("Venomous Shade", EntityType.SPIDER);
        ancientMobs.put("Soulflayer", EntityType.ZOMBIE);
        ancientMobs.put("Ender Wraith", EntityType.ENDERMAN);
        ancientMobs.put("Enderenderman", EntityType.ENDERMAN);

        ConcurrentHashMap<String, EntityType> updatedMap = new ConcurrentHashMap<>();

        // Iterate through the map entries
        ancientMobs.forEach((key, value) -> {
            String newKey = "Ancient " + key; // Add prefix to the key
            updatedMap.put(newKey, value); // Add the new key-value pair to the updated map
        });

        // Replace old map with updated map
        ancientMobs.clear();
        ancientMobs.putAll(updatedMap);
    }

    // Run in ticks
    public void spawnZombieNearPlayers(ServerWorld world) {
        counter++;
        if (counter < checkInterval) return;
        counter = 0;

        double r = random.nextDouble();

        if (r > bossSpawnChance) return;

        world.getPlayers().forEach(player -> {
            BlockPos playerPos = player.getBlockPos();

            // Try to find a valid spawn position
            BlockPos spawnPos = findValidSpawnPosition(world, playerPos);

            if (spawnPos != null) {
                // Randomly select a mob name and associated EntityType
                List<String> mobNames = new ArrayList<>(ancientMobs.keySet());
                String randomMobName = mobNames.get(random.nextInt(mobNames.size()));
                EntityType<? extends MobEntity> mobEntityType = ancientMobs.get(randomMobName);

                // Create the mob entity based on the random type
                MobEntity mob = mobEntityType.create(world);
                if (mob != null) {
                    mob.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
                    mob.setCustomName(Text.literal(randomMobName).styled(style -> style.withColor(Formatting.DARK_PURPLE)));  // Set custom name to the randomly selected name


                    Utils.applyEffectForMobs(mob, 3, 200);

                    Text message = Text.literal("☠").styled(style -> style.withColor(Formatting.DARK_PURPLE))
                            .append(Text.literal(" An ").styled(style -> style.withColor(Formatting.WHITE))
                                    .append(Text.literal(randomMobName)
                                            .styled(style -> style.withColor(Formatting.DARK_PURPLE)))
                                    .append(Text.literal(" just spawned nearby, be careful...")
                                            .styled(style -> style.withColor(Formatting.WHITE))));

                    player.sendMessage(message);

                    // Spawn the beacon beam effect
                    spawnLightningForNoticePlayers(world, mob);

                    // Spawn the mob in the world
                    world.spawnEntity(mob);

                    spawnParticlesUpToTheSky(world, mob);

                    Utils.addRunAfter(() -> {
                        mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(mob.getMaxHealth() + 200);
                        mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(mob.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getBaseValue() + 10);
                        mob.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(mob.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).getBaseValue() + 10);

                        mob.setHealth(mob.getMaxHealth());
                    }, 30);
                }
            }
        });
    }


    private BlockPos findValidSpawnPosition(ServerWorld world, BlockPos playerPos) {
        // No spawning at day
        if (world.isDay()) return null;

        for (int i = 0; i < 10; i++) { // Try 10 random positions
            double angle = world.random.nextDouble() * 2 * Math.PI; // Random angle
            int distance = SPAWN_MIN_DISTANCE + world.random.nextInt(SPAWN_MAX_DISTANCE - SPAWN_MIN_DISTANCE + 1); // Random distance within range
            int offsetX = (int) (Math.cos(angle) * distance);
            int offsetZ = (int) (Math.sin(angle) * distance);

            BlockPos randomOffset = playerPos.add(offsetX, 0, offsetZ);
            BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, randomOffset);

            return surfacePos;
        }
        return null;
    }

    //spawn a pillar up to the sky to notice the players there's a zombie just spawned
    private void spawnParticlesUpToTheSky(ServerWorld world, MobEntity mob) {
        BlockPos mobPos = mob.getBlockPos();
        int startY = mobPos.getY();  // Starting Y position of the mob
        int maxY = 256;  // You can set the max height of the pillar here, or use world.getHeight() for the height limit

        // Loop from the mob's Y position to the maximum Y (or until the world height limit)
        for (int y = startY; y <= maxY; y++) {
            BlockPos particlePos = new BlockPos(mobPos.getX(), y, mobPos.getZ());

            // Spawn the particle at the given position
            world.spawnParticles(ParticleTypes.FLAME, particlePos.getX() + 0.5, particlePos.getY() + 0.5, particlePos.getZ() + 0.5, 1, 0, 0, 0, 0.05);
        }
    }

    // Need to do the spawn beacon particle to notice the player there's a zombie spawn right at the location
    private void spawnLightningForNoticePlayers(ServerWorld world, MobEntity mob) {
        BlockPos pos = mob.getBlockPos();
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
        lightning.setCosmetic(true); // Makes the lightning deal no damage
        world.spawnEntity(lightning);
    }


    public void bossDropsRegister() {
        if (!canBossesSpawningHanlder) return;
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, world1) -> {
            if (entity instanceof MobEntity) {
                if (entity.getCustomName() != null && ancientMobs.containsKey((entity.getCustomName().getString()))) {
                    BlockPos deathPos = entity.getBlockPos();
                    ServerWorld serverWorld = entity.getServer().getOverworld();

                    if (serverWorld == null) return;

                    for (AncientMobDrops i : ancientMobDrops) {
                        double r = random.nextDouble();
                        if (r < i.getDropChance()) {
                            serverWorld.spawnEntity(new ItemEntity(serverWorld, deathPos.getX(), deathPos.getY() + 1, deathPos.getZ(), i.getDropItem()));
                        }
                    }
                }
            }
        });
    }
}
