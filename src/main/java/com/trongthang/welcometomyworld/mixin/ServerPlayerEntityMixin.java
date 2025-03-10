package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.IServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

import static com.trongthang.welcometomyworld.features.MinecellsDimensionSarcastic.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements IServerPlayerEntity {
    @Unique
    private Vec3d lastPosMinecells = null;
    @Unique
    private String lastDimensionMinecells = null;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo ci) {

        if (lastPosMinecells != null) {
            NbtCompound posNbt = new NbtCompound();
            posNbt.putDouble("x", lastPosMinecells.x);
            posNbt.putDouble("y", lastPosMinecells.y);
            posNbt.putDouble("z", lastPosMinecells.z);
            nbt.put("lastPosMinecells", posNbt);
        }
        if (this.lastDimensionMinecells != null) {
            nbt.putString("lastDimension", this.lastDimensionMinecells);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("lastPosMinecells", 10)) {
            NbtCompound posNbt = nbt.getCompound("lastPosMinecells");
            this.lastPosMinecells = new Vec3d(posNbt.getDouble("x"), posNbt.getDouble("y"), posNbt.getDouble("z"));
        }

        if (nbt.contains("lastDimensionMinecells")) {
            this.lastDimensionMinecells = nbt.getString("lastDimensionMinecells");
        }

        if (this.lastDimensionMinecells != null && this.lastPosMinecells != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            Utils.addRunAfter(() -> {
                TeleportToPreviousWorld(player, this.lastDimensionMinecells, this.lastPosMinecells);
            }, 20);
        }
    }

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    public void moveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        if (isRestrictedDimension(destination) && player.getWorld().isNight()) {
            cir.setReturnValue((Entity) (Object) this);
            cir.cancel();
        }

        if(isRestrictedDimension(destination) && !isRestrictedDimension(player.getWorld())){
            this.lastPosMinecells = player.getPos();
            this.lastDimensionMinecells = player.getWorld().getRegistryKey().toString();
        }
    }

    @Override
    public void setLastPosMinecells(Vec3d pos) {
        this.lastPosMinecells = pos;
    }

    @Override
    public Vec3d getLastPosMinecells() {
        return this.lastPosMinecells;
    }

    @Override
    public void setLastDimensionMinecells(String dim) {
        this.lastDimensionMinecells = dim;
    }

    @Override
    public String getLastDimensionMinecells() {
        return this.lastDimensionMinecells;
    }
}