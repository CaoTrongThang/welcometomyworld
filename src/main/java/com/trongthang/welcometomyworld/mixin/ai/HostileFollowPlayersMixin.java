package com.trongthang.welcometomyworld.mixin.ai;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.mob.PathAwareEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.trongthang.welcometomyworld.features.HostileMobsAwareness.TRACKED_MOBS;

@Mixin(HostileEntity.class)
public abstract class HostileFollowPlayersMixin extends PathAwareEntity {

    protected HostileFollowPlayersMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType<? extends HostileEntity> entityType, World world, CallbackInfo ci) {
        if (!world.isClient()) {
            this.goalSelector.add(5, new TrackPlayerGoal((HostileEntity) (Object) this));
        }
    }

    private static class TrackPlayerGoal extends net.minecraft.entity.ai.goal.Goal {
        private final HostileEntity mob;
        private static final int INITIAL_COOLDOWN = 600;
        private int cooldownTimer = 300;
        private static final int MAX_GOTO_COOLDOWN = 300;
        private int goToCounter = 0;
        private static final double MAX_DISTANCE_SQ = 120.0 * 120.0;

        private Vec3d targetPos = null;
        private UUID targetPlayer = null;

        public TrackPlayerGoal(HostileEntity mob) {
            this.mob = mob;
            this.setControls(java.util.EnumSet.of(net.minecraft.entity.ai.goal.Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (cooldownTimer > 0) {
                cooldownTimer--;
                return false;
            }
            cooldownTimer = INITIAL_COOLDOWN;

            if (!mob.getWorld().isNight() || mob.getWorld().getRegistryKey() != World.OVERWORLD
                    || mob.getTarget() != null) {
                return false;
            }

            Identifier mobId = EntityType.getId(mob.getType());
            if (!TRACKED_MOBS.contains(mobId)) {
                return false;
            }

            updatePlayerTarget();
            return targetPos != null;
        }

        @Override
        public boolean shouldContinue() {
            if (mob.getTarget() != null || targetPos == null || goToCounter > MAX_GOTO_COOLDOWN) {
                return false;
            }

            if (mob.squaredDistanceTo(targetPos) < 225.0) { // 15 blocks squared
                return false;
            }

            if (targetPlayer != null && mob.getServer() != null) {
                ServerPlayerEntity player = mob.getServer().getPlayerManager().getPlayer(targetPlayer);
                if (player != null && mob.distanceTo(player) > 50) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void start() {
            goToCounter = 0;
            if (targetPos != null) {
                mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1f);
            }
        }

        @Override
        public void tick() {
            goToCounter++;
            if (targetPos != null && mob.getNavigation().isIdle()) {
                mob.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.1f);
            }
        }

        @Override
        public void stop() {
            goToCounter = 0;
            targetPos = null;
            targetPlayer = null;
            mob.getNavigation().stop();
        }

        private void updatePlayerTarget() {
            World world = mob.getWorld();
            if (!(world instanceof ServerWorld serverWorld))
                return;

            List<ServerPlayerEntity> validPlayers = serverWorld.getPlayers().stream()
                    .filter(p -> !p.isCreative() && !p.isSpectator() && p.isAlive())
                    .filter(p -> p.squaredDistanceTo(mob) <= MAX_DISTANCE_SQ)
                    .collect(Collectors.toList());

            if (!validPlayers.isEmpty()) {
                ServerPlayerEntity target = validPlayers.get(world.random.nextInt(validPlayers.size()));
                targetPos = target.getPos().add(0, 1, 0);
                targetPlayer = target.getUuid();
            } else {
                targetPos = null;
                targetPlayer = null;
            }
        }
    }
}
