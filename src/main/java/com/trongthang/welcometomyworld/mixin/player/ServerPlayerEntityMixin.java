package com.trongthang.welcometomyworld.mixin.player;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.IServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimension;
import com.trongthang.welcometomyworld.VoidBossState;
import com.trongthang.welcometomyworld.entities.VoidWorm.VoidWormEntity;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.minecraft.util.math.BlockPos;

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

        if (isRestrictedDimension(destination) && !isRestrictedDimension(player.getWorld())) {
            this.lastPosMinecells = player.getPos();
            this.lastDimensionMinecells = player.getWorld().getRegistryKey().toString();
        }

        // Void Worm Boss Spawn Check
        if (destination.getRegistryKey().equals(VoidDimension.VOID_DIM_LEVEL_KEY)) {
            checkAndSpawnVoidBoss(destination);
            Utils.grantAdvancement((ServerPlayerEntity) player, "first_time_to_void_dimension");
        }

        if (destination.getRegistryKey().equals(WhiteDimension.WHITE_DIM_LEVEL_KEY)) {
            Utils.grantAdvancement((ServerPlayerEntity) player, "first_time_to_white_dimension");
            Utils.removeHarmfulEffects(player);
        }
    }

    @Unique
    private void checkAndSpawnVoidBoss(ServerWorld voidWorld) {
        VoidBossState state = VoidBossState.getServerState(voidWorld);

        // If a boss is already tracked in the state, assume it exists (could be
        // unloaded)
        if (state.bossUuid != null) {
            com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER
                    .info("Void Worm spawn skipped: already exists with UUID " + state.bossUuid);
            return;
        }

        // Otherwise, spawn a new one
        VoidWormEntity newBoss = new VoidWormEntity(EntitiesManager.VOID_WORM, voidWorld);
        newBoss.refreshPositionAndAngles(0, 100, 0, 0, 0);

        // Set the UUID in the state BEFORE spawning to avoid potential race conditions
        state.bossUuid = newBoss.getUuid();
        state.lastBossPos = new BlockPos(0, 100, 0);
        state.markDirty();

        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("Spawning new Void Worm boss: " + state.bossUuid);
        voidWorld.spawnEntity(newBoss);

        String[] voidMessages = {
                "The Void is trembling...",
                "Something ancient awakens in the dark...",
                "A colossal presence enters the void...",
                "The abyss hungers..."
        };
        String randomMessage = voidMessages[voidWorld.getRandom().nextInt(voidMessages.length)];
        Utils.sendMessageToAllPlayersAfter(voidWorld.getServer(), randomMessage, 60);
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
