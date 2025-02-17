package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.StartAnimation;
import com.trongthang.welcometomyworld.classes.SyncMobStatsPacket;
import com.trongthang.welcometomyworld.classes.TameableEntityInterface;
import com.trongthang.welcometomyworld.entities.Enderchester;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.TameableEntity;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

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

                    // Update the stats
                    entityInterface.setDamageLevel(packet.damageLevel);
                    entityInterface.setHealthLevel(packet.healthLevel);
                    entityInterface.setDefenseLevel(packet.defenseLevel);
                    entityInterface.setSpeedLevel(packet.speedLevel);
                    entityInterface.setCurrentLevel(packet.currentLevel);
                    entityInterface.setNextLevelRequireExp(packet.nextLevelRequireExp);
                    entityInterface.setCurrentLevelExp(packet.currentLevelExp);
                    entityInterface.setPointAvailalble(packet.pointAvailable);
                }
            });
        });
    }
}

