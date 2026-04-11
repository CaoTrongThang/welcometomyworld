package com.trongthang.welcometomyworld.mixin.player;

import com.trongthang.welcometomyworld.managers.FluidsManager;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimension;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.HEAVEN_TRANSITION_PACKET;

/**
 * Tracks how long a ServerPlayerEntity is continuously submerged in Death
 * Water.
 * After 10 s (200 ticks) it:
 * 1. Sends the heaven-transition packet so the client fades to white.
 * 2. Teleports the player to the White Dimension at Y = 0 after a short
 * delay that matches the fade-in window, so the screen is already white
 * when they arrive.
 *
 * Uses a @Unique counter per player — negligible overhead when outside death
 * water.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class DeathWaterTransitionMixin {

    @Unique
    private int wtmw_deathWaterTicks = 0;
    @Unique
    private boolean wtmw_transitionSent = false;
    @Unique
    private int wtmw_teleportCountdown = -1; // -1 = idle
    @Unique
    private ServerWorld wtmw_teleportDest = null;
    @Unique
    private TeleportTarget wtmw_teleportTarget = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (player.isCreative() || player.isSpectator()) {
            wtmw_reset();
            return;
        }

        // --- Countdown for pending teleport ---
        if (wtmw_teleportCountdown > 0) {
            wtmw_teleportCountdown--;
            if (wtmw_teleportCountdown == 0 && wtmw_teleportDest != null) {
                if (player.isAlive()) {
                    FabricDimensions.teleport(player, wtmw_teleportDest, wtmw_teleportTarget);
                    player.fallDistance = 0f;
                }
                wtmw_teleportCountdown = -1;
                wtmw_teleportDest = null;
                wtmw_teleportTarget = null;
            }
        }

        // --- Submersion check ---
        if (wtmw_isInDeathWater(player)) {
            wtmw_deathWaterTicks++;
            if (wtmw_deathWaterTicks >= 200 && !wtmw_transitionSent) {
                wtmw_transitionSent = true;
                wtmw_triggerTransition(player);
            }
        } else {
            wtmw_reset();
        }
    }

    @Unique
    private void wtmw_reset() {
        wtmw_deathWaterTicks = 0;
        wtmw_transitionSent = false;
        // Do NOT clear pending teleport — let it finish if already scheduled.
    }

    @Unique
    private boolean wtmw_isInDeathWater(ServerPlayerEntity player) {
        // Check both feet and eye positions for full submersion detection
        BlockPos feetPos = player.getBlockPos();
        FluidState feet = player.getWorld().getFluidState(feetPos);
        if (feet.getFluid() == FluidsManager.STILL_DEATH_WATER
                || feet.getFluid() == FluidsManager.FLOWING_DEATH_WATER) {
            return true;
        }
        BlockPos eyePos = BlockPos.ofFloored(player.getEyePos());
        FluidState eye = player.getWorld().getFluidState(eyePos);
        return eye.getFluid() == FluidsManager.STILL_DEATH_WATER
                || eye.getFluid() == FluidsManager.FLOWING_DEATH_WATER;
    }

    @Unique
    private void wtmw_triggerTransition(ServerPlayerEntity player) {
        // 1. Tell client to start the white fade-in now
        ServerPlayNetworking.send(player, HEAVEN_TRANSITION_PACKET, new PacketByteBuf(Unpooled.buffer()));

        // 2. Schedule the actual teleport 100 ticks (5 s) from now.
        // That matches HeavenTransitionManager.FADE_IN_TICKS so the screen is
        // fully white when they land.
        ServerWorld whiteWorld = player.getServer().getWorld(WhiteDimension.WHITE_DIM_LEVEL_KEY);
        if (whiteWorld == null)
            return;

        Vec3d dest = new Vec3d(player.getX(), 0, player.getZ());
        wtmw_teleportDest = whiteWorld;
        wtmw_teleportTarget = new TeleportTarget(dest, Vec3d.ZERO, player.getYaw(), player.getPitch());
        wtmw_teleportCountdown = 100;
    }
}
