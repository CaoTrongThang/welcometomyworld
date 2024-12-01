package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.List;

public class ALivingFlower extends PathAwareEntity {

    private final int CHECK_INTERVAL = 200; // ticks (5 seconds)
    private int counter = 0;
    private final int BOX_RADIUS = 6; // Detection radius

    private int stuckCheckCooldown = 40;
    private int counterStuckCheck = 0;

    private java.util.Random rand = new java.util.Random();

    private BlockPos targetPos;

    protected ALivingFlower(EntityType<? extends ALivingFlower> entityType, World world) {
        super(entityType, world);
        this.setCustomNameVisible(true);
        this.goalSelector.add(0, new WanderAroundGoal(this, 0.5D)); // Wander goal
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        // No armor for the log
    }

    @Override
    public void tick() {
        super.tick();


        counterStuckCheck++;
        if (counterStuckCheck > stuckCheckCooldown) {
            counterStuckCheck = 0;
            boolean isStuck = isStuck();
            if (isStuck) {
                navigateOutOfStuckBlock();
                return;
            }
        }


        counter++;
        if(counter < CHECK_INTERVAL) return;
        counter = 0;

        ServerWorld world = this.getServerWorld();
        if(world == null) return;

        if (!world.isClient) {
            List<PlayerEntity> players = checkForPlayers(world, this.getBlockPos());
            if(!players.isEmpty()){
                for(PlayerEntity player : players){
                    Utils.giveEffect(player, StatusEffects.LUCK, CHECK_INTERVAL);
                    Utils.spawnParticles(world, this.getBlockPos(), ParticleTypes.BUBBLE);
                    Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER);
                }
            }
        }
    }

    private List<PlayerEntity> checkForPlayers(ServerWorld world, BlockPos centerPos) {
        // Create the detection box
        Box detectionBox = new Box(
                centerPos.add(-BOX_RADIUS, -BOX_RADIUS, -BOX_RADIUS),
                centerPos.add(BOX_RADIUS, BOX_RADIUS, BOX_RADIUS)
        );

        return world.getEntitiesByClass(PlayerEntity.class, detectionBox, player -> true);
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    private boolean isStuck() {
        BlockPos pos = this.getBlockPos();
        BlockState blockState = this.getWorld().getBlockState(pos);

        // Check if the block is solid (meaning the entity is stuck in or against it)
        return blockState.isFullCube(this.getWorld(), pos);  // Checks if the block is solid
    }

    public void navigateOutOfStuckBlock() {
        BlockPos currentPos = this.getBlockPos();
        World world = this.getWorld();

        // Define the radius of the search area (in blocks)
        int radius = 1;

        // Check all blocks within the 3x3x3 area centered around the current position
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Offset the current position by the coordinates in the loop
                    BlockPos checkPos = currentPos.add(x, y, z);

                    // Check if the block at the position is air (free space)
                    if (world.getBlockState(checkPos).isAir()) {
                        // Calculate the direction vector to the free space position
                        Vec3d direction = new Vec3d(checkPos.getX() - currentPos.getX(),
                                checkPos.getY() - currentPos.getY(),
                                checkPos.getZ() - currentPos.getZ());

                        // Normalize the direction vector
                        direction = direction.normalize();

                        // Set the velocity towards the target position
                        this.setVelocity(direction.x * 1, direction.y * 0.52, direction.z * 1);

                        // Optionally, you can add some damping to the velocity if necessary.
                        this.velocityDirty = true;

                        return; // Exit after setting the velocity to move the entity
                    }
                }
            }
        }
    }

    public boolean canNavigateTo(BlockPos targetPos) {
        EntityNavigation navigation = this.getNavigation();

        // Try to find a path to the target position
        Path path = navigation.findPathTo(targetPos, 10); // 10 is the range for pathfinding
        return path != null && path.reachesTarget(); // Check if a valid path exists and reaches the target
    }

    private void tryFloatingOnWater() {
        if (this.isTouchingWater()) {
            // Simulate buoyancy: apply a small upward velocity to make it float
            this.setVelocity(this.getVelocity().add(0, 0.05D, 0));  // Adjust this value for more/less buoyancy
        }
    }

    private void tryJumpUp() {
        // Adjust jump behavior
        Vec3d jumpVelocity = this.getRotationVec(0.0F).multiply(0, 0, 0).add(0, 0.4, 0);

        this.setVelocity(jumpVelocity);
        this.velocityDirty = true;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        ServerWorld world = getServerWorld();
        if (world == null) return;

        Utils.SpawnItem(world, this.getPos(), new ItemStack(Blocks.PEONY, rand.nextInt(1, 3))); // Bonus drops

    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        super.damage(source, amount);
        ServerWorld world = getServerWorld();
        if (world == null) return false;
        Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_FLOWERING_AZALEA_STEP);
        this.goalSelector.add(1, new FleeEntityGoal<>(this, LivingEntity.class, 10.0F, 1.0D, 1.5D));
        return false;
    }

    @Override
    protected void playHurtSound(DamageSource source) {

        ServerWorld world = getServerWorld();
        if (world == null) return;

        Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_FLOWERING_AZALEA_STEP);
    }

    private ServerWorld getServerWorld() {
        MinecraftServer server = this.getServer();
        if (server == null) return null;
        ServerWorld world = server.getOverworld();
        if (world == null) return null;
        return world;
    }

    public static boolean canSpawn(EntityType<ALivingFlower> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        // Example condition: only spawn on grass and in daylight
        BlockState state = world.getBlockState(pos.down());
        return state.isOf(Blocks.GRASS_BLOCK) && world.getLightLevel(pos) > 8;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource){
        return true;
    }
}
