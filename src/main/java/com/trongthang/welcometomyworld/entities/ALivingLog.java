package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utils;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.List;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class ALivingLog extends PathAwareEntity {

    private int autoBoneMealCooldown = 100;
    private int counter = 0;
    private boolean canCooldown = false;
    private boolean isMovingToTargetPos = false;

    private int stuckCheckCooldown = 40;
    private int counterStuckCheck = 0;

    private java.util.Random rand = new java.util.Random();

    private BlockPos targetPos;

    protected ALivingLog(EntityType<? extends ALivingLog> entityType, World world) {
        super(entityType, world);
        this.setCustomNameVisible(true);
        this.goalSelector.add(0, new WanderAroundGoal(this, 1.0D)); // Wander goal
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
        if(counterStuckCheck > stuckCheckCooldown){
            counterStuckCheck = 0;
            boolean isStuck = isStuck();
            if(isStuck){
                resetSkill();
                navigateOutOfStuckBlock();
                return;
            }
        }

        if (!this.getWorld().isClient) {
            // Only proceed if targetPos is not null
            tryFloatingOnWater();

            if (this.goalSelector.getRunningGoals().anyMatch(goal -> goal.getGoal() instanceof FleeEntityGoal)) {
                resetSkill();
                return;
            }

            if (targetPos != null) {
                canCooldown = false;
                BlockPos pos = this.getBlockPos();
                // Check if the log is near the target position
                if (Math.abs(targetPos.getX() - pos.getX()) <= 3
                        && Math.abs(targetPos.getY() - pos.getY()) <= 3
                        && Math.abs(targetPos.getZ() - pos.getZ()) <= 3) {

                    tryJumpUp();
                    useBoneMealOnSapling(targetPos);

                    return;
                }


                // Only navigate to the target if we have a valid position
                if (canNavigateTo(targetPos)) {
                    if (!isMovingToTargetPos) {
                        this.goalSelector.remove(new WanderAroundGoal(this, 1.0));
                        this.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0D);
                        isMovingToTargetPos = true;
                    }
                } else {
                    this.goalSelector.add(0, new WanderAroundGoal(this, 1.0D)); // Wander goal
                    resetSkill();
                }

            }

            // Manage cooldown for bone meal usage
            if (canCooldown) {
                counter++;
                if (counter < autoBoneMealCooldown) return;
                counter = 0;
            }

            if (targetPos != null) return;

            canCooldown = true;
            findSapling();
        }
    }

    @Override
    public boolean canBreatheInWater(){
        return true;
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    private void findSapling() {
        if (!this.getWorld().isClient && targetPos == null) {
            World world = this.getWorld();

            // Create a bounding box around the ALivingLog (adjust coordinates as needed)
            Box searchBox = new Box(this.getPos().add(-10, -1, -10), this.getPos().add(10, 1, 10));

            // Iterate through all the block positions within the box's bounds
            for (double x = searchBox.minX; x <= searchBox.maxX; x++) {
                for (double y = searchBox.minY; y <= searchBox.maxY; y++) {
                    for (double z = searchBox.minZ; z <= searchBox.maxZ; z++) {
                        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);  // Generate block position
                        BlockState state = world.getBlockState(pos);

                        if (state.getBlock() instanceof SaplingBlock) {
                            targetPos = pos;
                        }
                    }
                }
            }
        }
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

    private void useBoneMealOnSapling(BlockPos pos) {

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlockState state = serverWorld.getBlockState(pos);

            if (state.getBlock() instanceof SaplingBlock) {
                SaplingBlock sapling = (SaplingBlock) state.getBlock();

                if (sapling.canGrow(serverWorld, random, pos, state)) {
                    Random random = this.getRandom();
                    sapling.grow(serverWorld, random, pos, state);
                }
            }

            //After use the bonemeal, the target pos will be null, the log can move freely

            resetSkill();

            ServerWorld world = getServerWorld();
            if (world == null) return;

            Utils.spawnParticles(world, pos, ParticleTypes.HAPPY_VILLAGER);
            Utils.playSound(world, pos, SoundEvents.ITEM_BONE_MEAL_USE);
        }
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

        boolean killedByAxe = false;

        if (damageSource.getAttacker() instanceof PlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            if (weapon.isOf(Items.WOODEN_AXE) || weapon.isOf(Items.STONE_AXE) ||
                    weapon.isOf(Items.IRON_AXE) || weapon.isOf(Items.GOLDEN_AXE) ||
                    weapon.isOf(Items.DIAMOND_AXE) || weapon.isOf(Items.NETHERITE_AXE)) {
                killedByAxe = true;
            }
        }

        // Drop log or sapling based on the cause of death
        if (killedByAxe) {
            Utils.SpawnItem(world, this.getPos(), new ItemStack(Blocks.OAK_LOG, rand.nextInt(1, 3))); // Bonus drops
        }

        if(rand.nextDouble() < 0.5){
            BlockState state = world.getBlockState(this.getBlockPos());
            if (state.isAir() &&
                    isPlantableGround(this.getBlockPos().down())) {
                world.setBlockState(this.getBlockPos(), Blocks.OAK_SAPLING.getDefaultState());
            }
        }

        Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_WOOD_BREAK);
        Utils.spawnBlockBreakParticles(world, this.getBlockPos(), new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_LOG.getDefaultState()));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        super.damage(source, amount);
        resetSkill();
        ServerWorld world = getServerWorld();
        if (world == null) return false;
        Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_WOOD_HIT);
        this.goalSelector.add(1, new FleeEntityGoal<>(this, LivingEntity.class, 10.0F, 1.0D, 1.5D));
        return false;
    }

    @Override
    protected void playHurtSound(DamageSource source) {

        ServerWorld world = getServerWorld();
        if (world == null) return;

        Utils.playSound(world, this.getBlockPos(), SoundEvents.BLOCK_WOOD_HIT);
    }

    private ServerWorld getServerWorld() {
        MinecraftServer server = this.getServer();
        if (server == null) return null;
        ServerWorld world = server.getOverworld();
        if (world == null) return null;
        return world;
    }

    public static boolean canSpawn(EntityType<ALivingLog> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        // Example condition: only spawn on grass and in daylight
        BlockState state = world.getBlockState(pos.down());
        return state.isOf(Blocks.GRASS_BLOCK) && world.getLightLevel(pos) > 8;
    }

    private void resetSkill() {
        targetPos = null;
        canCooldown = true;
        isMovingToTargetPos = false;
    }

    private boolean isPlantableGround(BlockPos pos) {
        BlockState blockState = this.getWorld().getBlockState(pos.down()); // Get block below
        Block block = blockState.getBlock();

        // Check if the block is one of the plantable types for saplings
        return block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.PODZOL ||
                block == Blocks.FARMLAND ||
                block == Blocks.MUD;
    }
}
