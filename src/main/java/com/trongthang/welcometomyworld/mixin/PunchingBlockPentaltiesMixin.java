package com.trongthang.welcometomyworld.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.PLAYER_BREAKING_BLOCK;
import static com.trongthang.welcometomyworld.GlobalConfig.*;

@Mixin(MinecraftClient.class)
public class PunchingBlockPentaltiesMixin {
    // Inject right before the method handles block-breaking
    @Inject(method = "handleBlockBreaking", at = @At("HEAD"))
    private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (canPunchingBlockPenalties && breaking) {
            MinecraftClient client = MinecraftClient.getInstance();

            if(client.player == null) return;
            if(client.player.isSpectator() || client.player.isCreative()) return;

            if (client.crosshairTarget instanceof BlockHitResult hitResult) {
                BlockPos blockPos = hitResult.getBlockPos();
                ClientPlayerEntity player = client.player;

                if (player != null) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(blockPos.getX());
                    buf.writeInt(blockPos.getY());
                    buf.writeInt(blockPos.getZ());
                    buf.writeUuid(player.getUuid());
                    ClientPlayNetworking.send(PLAYER_BREAKING_BLOCK, buf);
                }
            }
        }
    }
}
