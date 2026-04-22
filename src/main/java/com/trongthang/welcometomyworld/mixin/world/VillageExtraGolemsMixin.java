package com.trongthang.welcometomyworld.mixin.world;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.util.math.BlockBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.gen.structure.Structure;

@Mixin(StructureStart.class)
public abstract class VillageExtraGolemsMixin {

    @Shadow
    public abstract Structure getStructure();

    @Shadow
    public abstract BlockBox getBoundingBox();

    @Inject(method = "place", at = @At("TAIL"))
    private void onPlace(StructureWorldAccess world, StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
            CallbackInfo ci) {

        // Get the structure ID correctly using the registry manager
        Identifier id = world.toServerWorld().getRegistryManager().get(RegistryKeys.STRUCTURE)
                .getId(this.getStructure());
        String structureId = id != null ? id.toString() : "";

        // Check if it's a village
        if (structureId.contains("minecraft:village")) {
            // We only want to spawn the extra golems ONCE per village.
            // A reliable way is to check if the current chunk contains the structure's
            // center block.
            BlockPos center = this.getBoundingBox().getCenter();
            ChunkPos centerChunk = new ChunkPos(center);

            if (chunkPos.equals(centerChunk)) {
                // Spawn 2 extra Iron Golems
                for (int i = 0; i < 2; i++) {
                    // Offset slightly from center to avoid stacking
                    int offsetX = random.nextInt(11) - 5;
                    int offsetZ = random.nextInt(11) - 5;
                    BlockPos spawnPos = center.add(offsetX, 0, offsetZ);

                    // Find safe Y level (surface)
                    int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, spawnPos.getX(), spawnPos.getZ());
                    spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());

                    IronGolemEntity golem = EntityType.IRON_GOLEM.create(world.toServerWorld());
                    if (golem != null) {
                        golem.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
                        golem.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.STRUCTURE, null, null);
                        world.spawnEntityAndPassengers(golem);
                    }
                }
            }
        }
    }
}
