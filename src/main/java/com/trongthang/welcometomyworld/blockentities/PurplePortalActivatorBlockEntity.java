package com.trongthang.welcometomyworld.blockentities;

import com.trongthang.welcometomyworld.entities.PurplePortalEntity;
import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PurplePortalActivatorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID portalUuid;
    private long lastActivatedTime = 0;
    private static final long COOLDOWN = 20;

    public PurplePortalActivatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlocksEntitiesManager.PURPLE_PORTAL_ACTIVATOR_BLOCK_ENTITY, pos, state);
    }

    public void togglePortal() {
        if (this.world == null || this.world.isClient)
            return;

        long time = this.world.getTime();
        if (time - lastActivatedTime < COOLDOWN) {
            return;
        }
        lastActivatedTime = time;

        if (portalUuid != null) {
            Entity existing = ((ServerWorld) world).getEntity(portalUuid);
            if (existing instanceof PurplePortalEntity portal) {
                if (!portal.isTurningOff()) {
                    portal.turnOff();
                    this.portalUuid = null;
                    this.markDirty();
                }
                return;
            }
        }

        // Summon new portal
        PurplePortalEntity portal = new PurplePortalEntity(EntitiesManager.PURPLE_PORTAL_ENTITY, world);

        // Position it on top of the block, centered.
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        // Get rotation from block
        float yaw = 0;
        BlockState state = world.getBlockState(pos);
        if (state.getProperties().contains(com.trongthang.welcometomyworld.blocks.PurplePortalActivatorBlock.FACING)) {
            net.minecraft.util.math.Direction facing = state
                    .get(com.trongthang.welcometomyworld.blocks.PurplePortalActivatorBlock.FACING);
            yaw = facing.asRotation();
        }

        portal.refreshPositionAndAngles(x, y, z, yaw, 0);
        world.spawnEntity(portal);
        this.portalUuid = portal.getUuid();
        this.markDirty();
        world.updateListeners(pos, state, state, 3);
    }

    public void removePortal() {
        if (this.world == null || this.world.isClient || portalUuid == null)
            return;

        Entity existing = ((ServerWorld) world).getEntity(portalUuid);
        if (existing != null) {
            existing.discard();
        }
        this.portalUuid = null;
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (portalUuid != null) {
            nbt.putUuid("PortalUuid", portalUuid);
        }
        nbt.putLong("LastActivatedTime", lastActivatedTime);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("PortalUuid")) {
            this.portalUuid = nbt.getUuid("PortalUuid");
        }
        if (nbt.contains("LastActivatedTime")) {
            this.lastActivatedTime = nbt.getLong("LastActivatedTime");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations for the block itself for now
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
