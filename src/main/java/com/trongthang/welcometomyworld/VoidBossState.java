package com.trongthang.welcometomyworld;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class VoidBossState extends PersistentState {
    public UUID bossUuid = null;
    public BlockPos lastBossPos = null;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (bossUuid != null) {
            nbt.putUuid("BossUuid", bossUuid);
        }
        if (lastBossPos != null) {
            nbt.putLong("LastBossPos", lastBossPos.asLong());
        }
        return nbt;
    }

    public static VoidBossState createFromNbt(NbtCompound nbt) {
        VoidBossState state = new VoidBossState();
        if (nbt.containsUuid("BossUuid")) {
            state.bossUuid = nbt.getUuid("BossUuid");
        }
        if (nbt.contains("LastBossPos")) {
            state.lastBossPos = BlockPos.fromLong(nbt.getLong("LastBossPos"));
        }
        return state;
    }

    public static VoidBossState getServerState(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(
                VoidBossState::createFromNbt,
                VoidBossState::new,
                "void_boss_state");
    }
}
