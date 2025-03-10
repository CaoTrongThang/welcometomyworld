package com.trongthang.welcometomyworld.API;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.tameablePacket.StrongTameableEntityDefault;
import mcp.mobius.waila.api.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
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
            if (accessor.getEntity() instanceof StrongTameableEntityDefault entity) {
                // Your existing logic here
                MutableText name = Text.literal(entity.getType().getName().getString());
                if (!entity.isTamed() || entity.getOwner() == null) {
                    name.append(String.format(" (Tame Chance: %d%%)", (int) entity.getTameChance()));
                }
                if (entity.getScoreboardTeam() != null) {
                    name = Team.decorateName(entity.getScoreboardTeam(), name);
                }
                tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, name);
            }
        }
    }
}