package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.trongthang.welcometomyworld.features.HostileMobsAwareness.TRACKED_MOBS;

@Mixin(HostileEntity.class)
public class HostileFollowPlayersMixin {
    private static final int COOLDOWN = 600;
    private int cooldownTimer = 300;

    private static final int maxGoToCooldown = 300;
    private int goToCounter = 0;

    private static final double MAX_DISTANCE_SQ = 120.0 * 120.0;

    private Vec3d targetPos = null;
    private UUID targetPlayer = null;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        HostileEntity mob = (HostileEntity) (Object) this;

        if (mob.getTarget() != null) {
            return;
        }

        if (targetPos != null) {
            if(mob.getTarget() != null){
                reset();
                return;
            }

            goToTargetPos(mob);

            goToCounter++;
            if (goToCounter > maxGoToCooldown) {
                reset();
            }

            return;
        }


        if (cooldownTimer-- <= 0) {
            cooldownTimer = COOLDOWN;
            if (mob.getWorld().isNight() && !mob.getWorld().isClient() && mob.getTarget() == null && mob.getWorld().getRegistryKey() == World.OVERWORLD) {
                if (shouldTrackPlayer(mob)) {
                    updatePlayerTarget(mob);

                    ServerPlayerEntity player = mob.getServer().getPlayerManager().getPlayer(targetPlayer);
                    if (player != null) {
                        if (mob.distanceTo(player) > 50) {
                            reset();
                        }
                    }
                }
            }
        }
    }

    private void reset() {
        goToCounter = 0;
        targetPos = null;
        targetPlayer = null;
    }

    private boolean shouldTrackPlayer(HostileEntity mob) {
        // 1. Get entity type ID
        Identifier mobId = EntityType.getId(mob.getType());

        // 2. Check against our tracking list
        boolean isTrackable = TRACKED_MOBS.contains(mobId);

        // 3. Environment checks
        boolean validWorld = mob.getWorld().getRegistryKey() == World.OVERWORLD
                && mob.getWorld().isNight();

        // 4. State checks
        boolean validState = !mob.getWorld().isClient()
                && mob.getTarget() == null;

        return isTrackable && validWorld && validState;
    }

    private void updatePlayerTarget(HostileEntity mob) {
        if (targetPos != null || targetPlayer != null) return;

        World world = mob.getWorld();
        List<ServerPlayerEntity> players = ((ServerWorld) world).getPlayers();

        List<ServerPlayerEntity> validPlayers = players.stream()
                .filter(p -> !p.isCreative() && !p.isSpectator() && p.isAlive())
                .filter(p -> p.squaredDistanceTo(mob) <= MAX_DISTANCE_SQ && p.getWorld().getRegistryKey() == World.OVERWORLD)
                .collect(Collectors.toList());

        if (!validPlayers.isEmpty()) {
            ServerPlayerEntity target = validPlayers.get(world.random.nextInt(validPlayers.size()));
            targetPos = target.getPos().add(0, 1, 0);
            targetPlayer = target.getUuid();
        } else {
            reset();
        }
    }

    private void goToTargetPos(HostileEntity mob) {
        if (Math.abs(targetPos.getX() - mob.getX()) < 15 || Math.abs(targetPos.getZ() - mob.getZ()) < 15) {
            reset();
            return;
        }

        mob.getNavigation().startMovingTo(
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ(),
                1.1f
        );
    }
}
