package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.StartAnimation;
import com.trongthang.welcometomyworld.classes.tameablePacket.SyncMobStatsPacket;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import com.trongthang.welcometomyworld.entities.Enderchester;
import com.trongthang.welcometomyworld.screen.MobUpgradeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.client.WelcomeToMyWorldClient.skullRevivePosition;

public class PacketHandlerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_EAT_ANIMATION, (client, handler, buf, responseSender) -> {
            // Read data on the network thread while the buffer is valid
            int entityId = buf.readInt();

            client.execute(() -> {
                // Use the extracted data on the main thread
                assert client.world != null;
                Entity entity = client.world.getEntityById(entityId);
                if (entity instanceof Enderchester) {
                    ((Enderchester) entity).startAnimation(AnimationName.EAT_ITEMS, 30);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SOUND_PACKET_ID, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            Identifier soundId = buf.readIdentifier();

            Utils.playClientSound(new BlockPos((int)x,(int)y,(int)z), Registries.SOUND_EVENT.get(soundId), 16, 0.5f, 1f);
        });

        ClientPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_MOUTH_OPEN, (client, handler, buf, responseSender) -> {
            // Read data on the network thread while the buffer is valid
            int entityId = buf.readInt();

            client.execute(() -> {
                // Use the extracted data on the main thread
                assert client.world != null;
                Entity entity = client.world.getEntityById(entityId);
                if (entity instanceof Enderchester) {
                    ((Enderchester) entity).setIsOpeningChestData(true);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_JUMP, (client, handler, buf, responseSender) -> {
            // Read data on the network thread while the buffer is valid
            int entityId = buf.readInt();

            client.execute(() -> {
                // Use the extracted data on the main thread
                assert client.world != null;
                Entity entity = client.world.getEntityById(entityId);
                if (entity instanceof Enderchester) {
                    ((Enderchester) entity).startAnimation(AnimationName.JUMP, 30);
                }
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_ATTACK, (client, handler, buf, responseSender) -> {
            // Read data on the network thread while the buffer is valid
            int entityId = buf.readInt();

            client.execute(() -> {
                // Use the extracted data on the main thread
                assert client.world != null;
                Entity entity = client.world.getEntityById(entityId);
                if (entity instanceof Enderchester) {
                    ((Enderchester) entity).startAnimation(AnimationName.ATTACK, 25);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ANIMATION_PACKET, (client, handler, buf, responseSender) -> {
            // Read data on the network thread while the buffer is valid
            int entityId = buf.readInt();
            AnimationName anm = buf.readEnumConstant(AnimationName.class);
            int timeOut = buf.readInt();

            client.execute(() -> {
                // Use the extracted data on the main thread
                assert client.world != null;
                Entity entity = client.world.getEntityById(entityId);
                if (entity instanceof StartAnimation) {
                    ((StartAnimation)entity).startAnimation(anm, timeOut);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_MOB_STATS_CLIENT, (client, handler, buf, responseSender) -> {
            SyncMobStatsPacket packet = SyncMobStatsPacket.decode(buf);

            client.execute(() -> {
                var entity = client.world.getEntityById(packet.entityId);
                if (entity instanceof TameableEntity tameableEntity) {
                    TameableEntityInterface entityInterface = (TameableEntityInterface) tameableEntity;

                    // Refresh the screen if it's currently open
                    if (client.currentScreen instanceof MobUpgradeScreen screen && screen.tameableEntity == tameableEntity) {
                        screen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
                    }

                    // Update the stats
                    entityInterface.setDamageLevel(packet.damageLevel);
                    entityInterface.setHealthLevel(packet.healthLevel);
                    entityInterface.setDefenseLevel(packet.defenseLevel);
                    entityInterface.setSpeedLevel(packet.speedLevel);
                    entityInterface.setCurrentLevel(packet.currentLevel);
                    entityInterface.setNextLevelRequireExp(packet.nextLevelRequireExp);
                    entityInterface.setCurrentLevelExp(packet.currentLevelExp);
                    entityInterface.setPointAvailalble(packet.pointAvailable);

                    tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(packet.damageStat);
                    tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(packet.armorStat);
                    tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(packet.healthStat);
                    tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(packet.speedStat);
                }
            });
        });
    }
}

