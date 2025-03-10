package com.trongthang.welcometomyworld;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public final Map<UUID, RegistryKey<World>> playerDimensions = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        playerDimensions.forEach((uuid, dimensionKey) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putString("dimension", dimensionKey.getValue().toString());
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("player_dimensions", playersNbt);
        return nbt;
    }

    // Add RegistryWrapper.WrapperLookup parameter to match parent class signature
    public static StateSaverAndLoader createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = nbt.getCompound("player_dimensions");

        playersNbt.getKeys().forEach(key -> {
            String dimensionStr = playersNbt.getCompound(key).getString("dimension");
            state.playerDimensions.put(
                    UUID.fromString(key),
                    RegistryKey.of(RegistryKeys.WORLD, Identifier.tryParse(dimensionStr))
            );
        });

        return state;
    }

//    public static StateSaverAndLoader getServerState(ServerWorld world) {
//        PersistentStateManager manager = world.getPersistentStateManager();
//        return manager.getOrCreate(
//                new Type<>(
//                        StateSaverAndLoader::new,
//                        (nbt, lookup) -> createFromNbt(nbt, lookup), // Updated to match new signature
//                        null
//                ),
//                "last_dimension_state"
//        );
//    }
}