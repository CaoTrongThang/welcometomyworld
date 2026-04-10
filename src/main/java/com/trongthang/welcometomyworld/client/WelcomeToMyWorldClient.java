package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.ConfigLoader;
import com.trongthang.welcometomyworld.ModKeybindings;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.RequestMobStatsPacket;
import com.trongthang.welcometomyworld.screen.MobUpgradeScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.client.world.ClientWorld;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import com.trongthang.welcometomyworld.managers.FluidsManager;

import java.util.List;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class WelcomeToMyWorldClient implements ClientModInitializer {
    private Perspective previousPerspective = Perspective.FIRST_PERSON;
    private boolean wasPlayerDead = false;

    private boolean stopSendingOriginsScreen = false;
    private boolean stopSendingLoadingTerrainScreen = false;

    private boolean removeMessagesFirstJoin = true;

    private int WATER_FALL_DAMAGE_COOLDOWN = 20;
    private int WATER_FALL_DAMAGE_COUNTER = 0;

    private double lastFallDistance = 0;

    public static BlockPos skullRevivePosition = null;

    private int messageCounter = 0;
    // List<String> removeMessages = List.of("Installed datapacks:",
    // "You can return to this menu with /function nucleus:menu",
    // "Nucleus v0.2.0 installed successfully",
    // "Sanguine v0.4.0 (Commands | Wiki)",
    // "has made the advancement [Ice and Fire]",
    // "Manic v1.1.0 (Commands | Wiki)");

    List<String> removeMessages = List.of("has made the advancement [Ice and Fire]",
            "has made the advancement [Mobs of Mythology]");

    @Override
    public void onInitializeClient() {
        ModKeybindings.registerKeybindings();
        SoundsClientHandler.register();
        ScreenClientHandler.register();
        ClientScheduler.init();
        CameraShakeManager.init();

        ClientPlayConnectionEvents.JOIN.register((handler, client, c) -> {
            preRenderChunks(c);
            removeMessagesFirstJoin = true;
            stopSendingLoadingTerrainScreen = false;
            messageCounter = 0;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            stopSendingOriginsScreen = false;
            stopSendingLoadingTerrainScreen = false;
        });

        if (compatityChecker.OriginCheck()) {
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if (stopSendingOriginsScreen)
                    return;
                if (screen != null) {
                    String screenTitle = screen.getTitle() != null ? screen.getTitle().getString() : "No Title";
                    // Check for the Origins screen
                    if (screenTitle.equals("origins.screen.choose_origin")) {
                        if (client.player != null) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(1);

                            if (client.player != null) {
                                buf.writeUuid(client.player.getUuid());
                                ClientPlayNetworking.send(FIRST_ORIGIN_CHOOSING_SCREEN, buf);
                            }
                        }
                    } else {
                        if (client.player != null) {
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(0);

                            if (client.player != null) {
                                buf.writeUuid(client.player.getUuid());
                                ClientPlayNetworking.send(FIRST_ORIGIN_CHOOSING_SCREEN, buf);
                            }
                        }
                    }
                }
            });
        } else {
            stopSendingOriginsScreen = true;
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onTicks);

        ClientPlayNetworking.registerGlobalReceiver(CLIENT_HAS_DATA, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                stopSendingOriginsScreen = true;
                stopSendingLoadingTerrainScreen = true;
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(STOP_SENDING_ORIGINS_SCREEN,
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        stopSendingLoadingTerrainScreen = true;
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(CHANGE_PERSPECTIVE, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                stopSendingOriginsScreen = true;
                client.execute(() -> {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                });
            });
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((a, b) -> {

            if (a.getString().toLowerCase().contains("detected: ")) {
                return false;
            }

            if (!removeMessagesFirstJoin)
                return true;

            for (String m : removeMessages) {
                if (a.getString().toLowerCase().contains(m.toLowerCase())) {
                    messageCounter++;
                    if (messageCounter >= removeMessages.size()) {
                        removeMessagesFirstJoin = false;
                    }
                    return false;
                }
            }
            return true;
        });
        ClientPlayNetworking.registerGlobalReceiver(BLOOD_MOON_SYNC, (client, handler, buf, responseSender) -> {
            boolean active = buf.readBoolean();
            boolean instant = false;
            if (buf.isReadable()) {
                instant = buf.readBoolean();
            }
            final boolean finalInstant = instant;
            client.execute(() -> {
                BloodMoonClient.isBloodMoon = active;
                if (finalInstant) {
                    BloodMoonClient.overlayAlpha = active ? 1.0f : 0.0f;
                }
            });
        });

        setupFluidRendering();
    }

    private void setupFluidRendering() {
        FluidRenderHandlerRegistry.INSTANCE.register(FluidsManager.STILL_DEATH_WATER, FluidsManager.FLOWING_DEATH_WATER,
                new SimpleFluidRenderHandler(
                        new Identifier("welcometomyworld:block/death_water_still"),
                        new Identifier("welcometomyworld:block/death_water_flow"),
                        0x9933FF // Purple tint for Death's Water
                ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), FluidsManager.STILL_DEATH_WATER,
                FluidsManager.FLOWING_DEATH_WATER);
    }

    private void onTicks(MinecraftClient client) {
        if (client.player != null && client.world != null) {
            this.waterFallDamage(client);
            this.switchPerspectiveOnDeath(client);
            handleTerrainScreen(client);

            if (client.world.getRegistryKey().equals(World.OVERWORLD)) {
                boolean inCave = isInCave(client.player.getBlockPos(), client.world);
                boolean shouldShow = BloodMoonClient.isBloodMoon && !inCave;

                float speed = 0.0033333334F;
                if (shouldShow) {
                    BloodMoonClient.overlayAlpha = Math.min(1f, BloodMoonClient.overlayAlpha + speed);
                } else {
                    BloodMoonClient.overlayAlpha = Math.max(0f, BloodMoonClient.overlayAlpha - speed);
                }
            }
        }

        if (ModKeybindings.openMobStats.wasPressed()) {
            handleOpenMobStats(client);
        }

        if (ModKeybindings.testCameraShake.wasPressed()) {
            CameraShakeManager.addShake(15.0f, 60);
        }
    }

    private boolean lastScreenTerain = false;

    private void handleTerrainScreen(MinecraftClient client) {
        if (stopSendingLoadingTerrainScreen)
            return;

        if (lastScreenTerain) {
            if (!(client.currentScreen instanceof DownloadingTerrainScreen)) {
                stopSendingLoadingTerrainScreen = true;
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(1);

                if (client.player != null) {
                    buf.writeUuid(client.player.getUuid());
                    ClientPlayNetworking.send(FIRST_LOADING_TERRAIN_SCREEN, buf);
                }
            }
        }

        if (client.currentScreen instanceof DownloadingTerrainScreen) {
            lastScreenTerain = true;
        }
    }

    private void handleOpenMobStats(MinecraftClient client) {
        if (client.player == null || client.world == null)
            return;

        HitResult hitResult = client.crosshairTarget;
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity targetEntity = entityHitResult.getEntity();

            if (targetEntity instanceof TameableEntity tameableEntity) {
                String tameableId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(tameableEntity.getType())
                        .toString();
                if (Utils.matchesPattern(tameableId,
                        ConfigLoader.getInstance().excludedUpgradeMobs)) {
                    return;
                }

                if (tameableEntity.isTamed() && tameableEntity.getOwnerUuid() != null &&
                        tameableEntity.getOwnerUuid().equals(client.player.getUuid())) {
                    RequestMobStatsPacket requestPacket = new RequestMobStatsPacket(tameableEntity.getId());
                    ClientPlayNetworking.send(REQUEST_MOB_STATS_PACKET,
                            requestPacket.encode(new PacketByteBuf(Unpooled.buffer())));

                    client.setScreen(new MobUpgradeScreen(tameableEntity));
                }
            }
        }
    }

    private void switchPerspectiveOnDeath(MinecraftClient client) {
        boolean isDead = client.player.isDead();
        if (isDead && !wasPlayerDead) {
            // Player has just died, save current perspective only if not already in
            // third-person
            if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
                previousPerspective = Perspective.FIRST_PERSON;
                client.execute(() -> {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                });
                ; // Switch to third-person view on death
            } else {
                previousPerspective = client.options.getPerspective(); // Save the current third-person perspective
            }
            wasPlayerDead = true; // Set dead status

        } else if (!isDead && wasPlayerDead) {
            client.execute(() -> {
                client.options.setPerspective(previousPerspective);
            });
            wasPlayerDead = false; // Reset dead status
        }
    }

    private void waterFallDamage(MinecraftClient client) {
        if (WATER_FALL_DAMAGE_COUNTER < WATER_FALL_DAMAGE_COOLDOWN) {
            WATER_FALL_DAMAGE_COUNTER++;
            return;
        }

        ClientPlayerEntity player = client.player;

        assert player != null;

        if (player.isCreative() || player.isSpectator() || player.isDead())
            return;

        Vec3d veloc = player.getVelocity();

        if (veloc.getY() < -0.85) {
            if (this.hasLandedWater(player)) {
                float damageAmount = Utils.calculateDamageWithArmor(calculateFallDamage(lastFallDistance), player);

                if (player.isSneaking()) {
                    damageAmount /= 2;
                }

                if (damageAmount > 6.5) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeFloat(damageAmount);
                    buf.writeUuid(player.getUuid());

                    ClientPlayNetworking.send(FALLING_TO_WATER, buf);
                    WATER_FALL_DAMAGE_COUNTER = 0;
                }

                lastFallDistance = 0;
            }
        }
    }

    private boolean hasLandedWater(ClientPlayerEntity player) {
        // Check if the player has landed (on the ground or on any block or water below
        // them)
        BlockPos below = player.getBlockPos().down(2);
        BlockState blockState = player.getWorld().getBlockState(below);

        // Check if the player is on the ground or touching a water block
        boolean land = blockState.isLiquid() && !player.getWorld().isAir(below);
        if (land) {
            lastFallDistance = player.fallDistance;

        }
        return land;
    }

    private float calculateFallDamage(double fallDistance) {
        return (float) (((Math.abs(fallDistance - 5))));
    }

    private void preRenderChunks(MinecraftClient client) {

        if (client.world == null || client.player == null) {
            return;
        }

        // Get the world's spawn position and corresponding chunk position
        BlockPos spawn = client.world.getSpawnPos();
        ChunkPos spawnChunk = new ChunkPos(spawn);

        // Loop through a 5x5 chunk area centered around the spawn
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                ChunkPos chunkPos = new ChunkPos(spawnChunk.x + dx, spawnChunk.z + dz);
                // Ensure the chunk is fully loaded
                Chunk chunk = client.world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
                if (chunk != null) { // Check if chunk exists
                    // Mark the chunk's range for rendering
                    client.worldRenderer.scheduleBlockRenders(
                            chunkPos.getStartX(), 0, chunkPos.getStartZ(),
                            chunkPos.getStartX() + 15, client.world.getHeight(), chunkPos.getStartZ() + 15);
                }
            }
        }
    }

    private boolean isInCave(BlockPos pos, ClientWorld level) {
        int y = pos.getY();
        if (y >= 50)
            return false;

        int skyLight = level.getLightLevel(LightType.SKY, pos);
        BlockState above = level.getBlockState(pos.up());
        return !level.isSkyVisible(pos) && skyLight <= 2 &&
                !above.isAir() && above.isOpaque();
    }
}
