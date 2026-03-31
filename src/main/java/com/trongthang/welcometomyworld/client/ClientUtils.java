package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.classes.CustomPositionedSound;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.SOUND_PACKET_ID;

@Environment(EnvType.CLIENT)
public class ClientUtils {

    public static void sendSoundPacketFromClient(SoundEvent sound, BlockPos pos) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(pos.getX())
                .writeDouble(pos.getY())
                .writeDouble(pos.getZ());
        buf.writeIdentifier(sound.getId());

        ClientPlayNetworking.send(
                SOUND_PACKET_ID,
                buf);
    }

    public static void playClientSound(BlockPos pos, SoundEvent sound, int maxDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        Vec3d playerPos = client.player.getPos();
        double distance = playerPos.distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

        if (distance <= maxDistance) {
            float volume = Math.max(0.1F, 1.0F - (float) (distance / maxDistance));
            SoundInstance soundInstance = new CustomPositionedSound(
                    sound,
                    pos,
                    SoundCategory.BLOCKS,
                    volume,
                    1.0F);
            client.getSoundManager().play(soundInstance);
        }
    }

    public static void playClientSound(BlockPos pos, SoundEvent sound, int maxDistance, float volume, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        Vec3d playerPos = client.player.getPos();
        double distance = playerPos.distanceTo(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

        if (distance <= maxDistance) {
            SoundInstance soundInstance = new CustomPositionedSound(
                    sound,
                    pos,
                    SoundCategory.BLOCKS,
                    volume,
                    pitch);
            client.getSoundManager().play(soundInstance);
        }
    }
}
