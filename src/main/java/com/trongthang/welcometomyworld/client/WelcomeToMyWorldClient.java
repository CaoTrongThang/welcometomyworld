package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.entities.ALivingFlowerRenderer;
import com.trongthang.welcometomyworld.entities.CustomEntitiesManager;
import com.trongthang.welcometomyworld.entities.ALivingLogRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

public class WelcomeToMyWorldClient implements ClientModInitializer {
    private Perspective previousPerspective = Perspective.FIRST_PERSON;
    private boolean wasPlayerDead = false;
    private boolean stopSendingOriginsScreen = false;
    private boolean isIntro = false;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CustomEntitiesManager.A_LIVING_LOG, ALivingLogRenderer::new);
        EntityRendererRegistry.register(CustomEntitiesManager.A_LIVING_FLOWER, ALivingFlowerRenderer::new);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            stopSendingOriginsScreen = false;
        });

        if(compatityChecker.OriginCheck()){
            ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
                if(stopSendingOriginsScreen) return;
                if (screen != null) {
                    String screenTitle = screen.getTitle() != null ? screen.getTitle().getString() : "No Title";
                    // Check for the Origins screen
                    if (screenTitle.equals("origins.screen.choose_origin")) {
                        if (client.player != null) {
                            sendPacketToServer(1);
                        }
                    } else {
                        if (client.player != null) {
                            sendPacketToServer(0);
                        }
                    }
                }
            });
        } else {
            stopSendingOriginsScreen = true;
        }

        ClientTickEvents.END_CLIENT_TICK.register(this::onTicks);

        ClientPlayNetworking.registerGlobalReceiver(STOP_SENDING_ORIGINS_SCREEN, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                stopSendingOriginsScreen = true;
            });
        });


        // Handle networking for sounds or other events, this PLAY_BLOCK_PORTAL_TRAVEL only happens once when player first time join the world
        ClientPlayNetworking.registerGlobalReceiver(PLAY_BLOCK_PORTAL_TRAVEL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                assert client.player != null;
                client.player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.4f, 1f);

            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_WOLF_HOWL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                assert client.player != null;
                client.player.playSound(SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.BLOCKS, 0.3f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_BELL, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                assert client.player != null;
                client.player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 0.4f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_EXPERIENCE_ORB_PICK_UP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_ENTITY_PLAYER_LEVELUP, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.4f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PLAY_BLOCK_LEVER_CLICK, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                // Play sound locally
                client.player.playSound(SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.6f, 1f);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CHANGE_PERSPECTIVE, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.execute(() -> {
                            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                            previousPerspective = client.options.getPerspective();
                        }
                );
            });
        });
    }

    private void onTicks(MinecraftClient client) {
        if (client.player != null) {
            this.fallDamageClient(client);
            this.switchPerspectiveOnDeath(client);
        }
    }

    private void switchPerspectiveOnDeath(MinecraftClient client){
        boolean isDead = client.player.isDead();
        if (isDead && !wasPlayerDead) {
            // Player has just died, save current perspective only if not already in third-person
            if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
                previousPerspective = Perspective.FIRST_PERSON;
                client.execute(() -> {
                            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                        }
                );
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

    private void fallDamageClient(MinecraftClient client){

        ClientPlayerEntity player = client.player;

        if (player.isCreative() || player.isSpectator()) return;  // Skip if player is creative or spectator

        boolean hasLandedWater = this.hasLandedWater(player);
        Vec3d veloc = player.getVelocity();

        if (hasLandedWater && veloc.getY() < -0.85) {

            float damageAmount = calculateFallDamage(veloc);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(damageAmount);
            buf.writeUuid(player.getUuid());
            ClientPlayNetworking.send(FALLING_TO_WATER, buf);
        }
    }

    private boolean hasLandedWater(ClientPlayerEntity player) {
        // Check if the player has landed (on the ground or on any block or water below them)
        BlockPos below = player.getBlockPos().down();
        BlockState blockState = player.getWorld().getBlockState(below);

        // Check if the player is on the ground or touching a water block
        return blockState.isLiquid() && !player.getWorld().isAir(below);
    }

    private boolean hasLandedGround(ClientPlayerEntity player) {
        // Check if the player has landed (on the ground or on any block or water below them)
        BlockPos below = player.getBlockPos().down();

        // Check if the player is on the ground or touching a water block
        return player.isOnGround() && !player.getWorld().isAir(below);
    }

    private float calculateFallDamage(Vec3d veloc) {
        return (float) (((Math.abs(veloc.getY() - 3))) * 3F);
    }

    private void sendPacketToServer(int state) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(state);

        // Safely get the player's UUID
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            buf.writeUuid(client.player.getUuid());
            ClientPlayNetworking.send(FIRST_ORIGIN_CHOOSING_SCREEN, buf);
        } else {
            System.err.println("Player object is null, unable to send packet!");
        }
    }
}

