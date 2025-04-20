package com.trongthang.welcometomyworld;

import com.trongthang.welcometomyworld.classes.RequestMobStatsPacket;
import com.trongthang.welcometomyworld.classes.tameablePacket.SyncMobStatsPacket;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import com.trongthang.welcometomyworld.classes.tameablePacket.UpdateMobStatPacket;
import com.trongthang.welcometomyworld.entities.Enderchester;
import com.trongthang.welcometomyworld.entities.Portaler;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static com.trongthang.welcometomyworld.GlobalVariables.*;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;


public class ServerNetworking {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(A_LIVING_CHEST_MOUTH_CLOSE,
                (server, player, handler, buf, responseSender) -> {
                    // Read the entity ID from the packet
                    int entityId = buf.readInt();
                    server.execute(() -> {
                        // Get the entity from the server world
                        Entity entity = player.getWorld().getEntityById(entityId);
                        if (entity instanceof Enderchester chest) {
                            chest.setIsOpeningChestData(false);
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(PORTALER_COMPLETE_PORTAL_CHANGING,
                (server, player, handler, buf, responseSender) -> {
                    // Read the entity ID from the packet
                    int entityId = buf.readInt();

                    server.execute(() -> {
                        // Get the entity from the server world
                        Entity entity = player.getWorld().getEntityById(entityId);
                        if (entity instanceof Portaler mob) {
                            mob.setIsSwitchingPortal(false);
                            mob.completeSwitich = false;
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(SOUND_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Identifier soundId = buf.readIdentifier();

                    server.execute(() -> {

                        World world = player.getWorld();

                        world.playSound(
                                null, // No player excluded (null means all players hear the sound)
                                x + 0.5,
                                y + 0.5,
                                z + 0.5,
                                Registries.SOUND_EVENT.get(soundId),
                                SoundCategory.BLOCKS,
                                1.0F, // Volume
                                0.8F + world.random.nextFloat() * 0.2F
                        );
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MOB_STAT, (server, player, handler, buf, responseSender) -> {
            UpdateMobStatPacket packet = UpdateMobStatPacket.decode(buf);

            server.execute(() -> {
                // Find the entity by its ID
                var entity = player.getWorld().getEntityById(packet.entityId);
                if (entity instanceof TameableEntity tameableEntity) {
                    TameableEntityInterface entityInterface = (TameableEntityInterface) tameableEntity;

                    if (entityInterface.getPointAvailalble() <= 0) return;

                    switch (packet.statName) {
                        case "damage" -> {
                            int newDamageLevel = entityInterface.getDamageLevel() + packet.amount;
                            entityInterface.setDamageLevel(newDamageLevel);

                            double oldStat = tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).getBaseValue();
                            double addStat = Math.max(MIN_DAMAGE_MOB, Math.min(MAX_DAMAGE_MOB, oldStat * DAMAGE_ADD_PER_LEVEL_MOB_PERCENT / 100));
                            double newStat = oldStat + random.nextDouble(addStat * 0.8f, addStat);

                            tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    .setBaseValue(newStat);
                        }
                        case "health" -> {
                            int newHealthLevel = entityInterface.getHealthLevel() + packet.amount;
                            entityInterface.setHealthLevel(newHealthLevel);

                            double oldStat = tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getBaseValue();

                            double addStat = Math.max(MIN_HEALTH_MOB, Math.min(MAX_HEALTH_MOB, oldStat * HEALTH_ADD_PER_LEVEL_PERCENT / 100));

                            double newStat = oldStat + random.nextDouble(addStat * 0.8f, addStat);

                            tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                                    .setBaseValue(newStat);
                        }
                        case "defense" -> {
                            int newDefenseLevel = entityInterface.getDefenseLevel() + packet.amount;
                            entityInterface.setDefenseLevel(newDefenseLevel);

                            double oldStat = tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).getBaseValue();

                            double addStat = Math.max(MIN_ARMOR_MOB, Math.min(MAX_ARMOR_MOB, oldStat * ARMOR_ADD_PER_LEVEL_MOB_PERCENT / 100));

                            double newStat = oldStat + random.nextDouble(addStat * 0.8f, addStat);

                            tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                                    .setBaseValue(newStat);
                        }
                        case "speed" -> {
                            int newSpeedLevel = entityInterface.getSpeedLevel() + packet.amount;
                            entityInterface.setSpeedLevel(newSpeedLevel);

                            // Update the movement speed attribute
                            tameableEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                                    .setBaseValue(tameableEntity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) + SPEED_ADD_PER_LEVEL);
                        }
                    }

                    entityInterface.setPointAvailalble(entityInterface.getPointAvailalble() - 1);

                    // Send the updated stats to all nearby players
                    SyncMobStatsPacket syncPacket = new SyncMobStatsPacket(tameableEntity.getId(), tameableEntity);
                    player.getWorld().getPlayers().forEach(p -> {
                        ServerPlayNetworking.send((ServerPlayerEntity) p, SYNC_MOB_STATS_CLIENT, syncPacket.encode(new PacketByteBuf(Unpooled.buffer())));
                    });
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_MOB_STATS_PACKET, (server, player, handler, buf, responseSender) -> {
            RequestMobStatsPacket packet = RequestMobStatsPacket.decode(buf);

            server.execute(() -> {
                var entity = player.getWorld().getEntityById(packet.entityId);
                if (entity instanceof TameableEntity tameableEntity) {
                    // Send the mob's stats to the client
                    SyncMobStatsPacket syncPacket = new SyncMobStatsPacket(tameableEntity.getId(), tameableEntity);
                    ServerPlayNetworking.send(player, SYNC_MOB_STATS_CLIENT, syncPacket.encode(new PacketByteBuf(Unpooled.buffer())));
                }
            });
        });
    }
}
