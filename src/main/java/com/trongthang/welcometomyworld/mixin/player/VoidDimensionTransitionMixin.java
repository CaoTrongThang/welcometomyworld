package com.trongthang.welcometomyworld.mixin.player;

import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class VoidDimensionTransitionMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void checkVoidDrop(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        World world = player.getWorld();

        if (player.getY() < world.getBottomY() - 30) {
            Entity vehicleToTeleport = player.getRootVehicle();
            if (vehicleToTeleport == null) {
                vehicleToTeleport = player;
            }

            final Entity finalVehicle = vehicleToTeleport;

            if (!world.getRegistryKey().equals(VoidDimension.VOID_DIM_LEVEL_KEY)) {
                ServerWorld voidWorld = player.getServer().getWorld(VoidDimension.VOID_DIM_LEVEL_KEY);
                if (voidWorld != null) {
                    boolean wasElytraFlying = player.isFallFlying();
                    Vec3d pos = new Vec3d(finalVehicle.getX(), 700, finalVehicle.getZ());
                    Vec3d vel = finalVehicle.getVelocity();
                    TeleportTarget target = new TeleportTarget(pos, vel, finalVehicle.getYaw(),
                            finalVehicle.getPitch());

                    player.getServer().execute(() -> {
                        FabricDimensions.teleport(finalVehicle, voidWorld, target);
                        player.fallDistance = 0f;
                        if (wasElytraFlying) {
                            player.startFallFlying();
                        }
                    });
                }
            } else {
                if (player.getY() < world.getBottomY() - 60) {
                    boolean wasElytraFlying = player.isFallFlying();
                    Vec3d pos = new Vec3d(finalVehicle.getX(), 350, finalVehicle.getZ());
                    Vec3d vel = finalVehicle.getVelocity();
                    TeleportTarget target = new TeleportTarget(pos, vel, finalVehicle.getYaw(),
                            finalVehicle.getPitch());

                    player.getServer().execute(() -> {
                        FabricDimensions.teleport(finalVehicle, (ServerWorld) world, target);
                        player.fallDistance = 0f;
                        if (wasElytraFlying) {
                            player.startFallFlying();
                        }
                    });
                }
            }
        }
    }
}
