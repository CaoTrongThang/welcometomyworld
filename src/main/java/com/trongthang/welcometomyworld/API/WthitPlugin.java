package com.trongthang.welcometomyworld.API;

import com.trongthang.welcometomyworld.classes.tameablePacket.StrongTameableEntityDefault;
import com.trongthang.welcometomyworld.entities.PurplePortalEntity;
import com.trongthang.welcometomyworld.entities.RiftPortalEntity;
import com.trongthang.welcometomyworld.entities.TinyGolem.TinyGolem;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormPartEntity;

import mcp.mobius.waila.api.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SpyglassItem;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class WthitPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        // Register raycast provider for spyglass
        registrar.addRayCastVector(new SpyglassRayCastProvider());
        // Existing entity component registration
        registrar.addComponent(new EntityComponentProvider(), TooltipPosition.HEAD, StrongTameableEntityDefault.class);
        registrar.addComponent(new UnknownComponentProvider(), TooltipPosition.HEAD, Unknown.class);
        registrar.addComponent(new UnknownComponentProvider(), TooltipPosition.BODY, Unknown.class);

        registrar.override(new PortalComponentProvider(), PurplePortalEntity.class, 0);
        registrar.override(new PortalComponentProvider(), RiftPortalEntity.class, 0);
        registrar.override(new VoidWormPartComponentProvider(), VoidWormPartEntity.class, 0);
    }

    public static class SpyglassRayCastProvider implements IRayCastVectorProvider {
        @Override
        public Vec3d getOrigin(float tickDelta) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return Vec3d.ZERO; // Fallback for safety
            }
            return player.getCameraPosVec(tickDelta);
        }

        @Override
        public Vec3d getDirection(float tickDelta) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return Vec3d.ZERO; // Fallback for safety
            }

            Vec3d baseDirection = player.getRotationVec(tickDelta);
            double distance = isUsingSpyglass(player) ? 60.0 : 1;
            return baseDirection.multiply(distance);
        }

        private boolean isUsingSpyglass(PlayerEntity player) {
            return player.getActiveItem().getItem() instanceof SpyglassItem;
        }
    }

    // Entity component provider for StrongTameableEntityDefault
    public static class EntityComponentProvider implements IEntityComponentProvider {
        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            Entity entity = accessor.getEntity();
            if (entity instanceof StrongTameableEntityDefault && !(entity instanceof TinyGolem)) {
                MutableText name = Text.literal(entity.getType().getName().getString());
                if (!((StrongTameableEntityDefault) entity).isTamed()
                        || ((StrongTameableEntityDefault) entity).getOwner() == null) {
                    name.append(String.format(" (Tame Chance: %d%%)", (int) ((StrongTameableEntityDefault) entity)
                            .getTameChance()));
                }
                if (entity.getScoreboardTeam() != null) {
                    name = Team.decorateName(entity.getScoreboardTeam(), name);
                }
                tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, name);
            }
        }
    }

    // Entity component provider for Unknown
    public static class UnknownComponentProvider implements IEntityComponentProvider {
        @Override
        public void appendHead(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof Unknown entity) {

                if (entity.getHealth() >= entity.getMaxHealth() * 0.9f) {
                    tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, Text.literal("§k" + generateRandomGlitchString(8)));
                }
            }
        }

        private String generateRandomGlitchString(int length) {
            String chars = "!@#$%^&*()_+-=[]{}|;':\",.<>/?0123456789";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            return sb.toString();
        }
    }

    // Entity component provider for PurplePortal (Hides the tooltip)
    public static class PortalComponentProvider implements IEntityComponentProvider {
        @Override
        public Entity getOverride(IEntityAccessor accessor, IPluginConfig config) {
            return EMPTY_ENTITY;
        }
    }

    // Entity component provider for VoidWormPart (Shows head's health/name)
    public static class VoidWormPartComponentProvider implements IEntityComponentProvider {
        @Override
        public Entity getOverride(IEntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof VoidWormPartEntity part) {
                VoidWormEntity head = part.getHead();
                if (head != null) {
                    return head;
                }
            }
            return accessor.getEntity();
        }
    }
}