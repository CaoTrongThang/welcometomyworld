package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.StartAnimation;
import com.trongthang.welcometomyworld.client.ClientScheduler;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//PORTALER: This mob is a portal that can move and can switch portal randomly, players can go to the portal to go to the end or the nether
public class Portaler extends PathAwareEntity implements StartAnimation {

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    //Texture Variant is used for new texture when it switches to new portal, only 2 available
    private static final TrackedData<Integer> TEXTURE_VARIANT = DataTracker.registerData(Portaler.class, TrackedDataHandlerRegistry.INTEGER);

    //If the mob is switching portal, it can't move, and play the animation, player can't go into the portal
    private static final TrackedData<Boolean> IS_SWITCHING_PORTAL = DataTracker.registerData(Portaler.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final int WALK_CYCLE_DURATION_MS = 5380;
    private static final int[] FOOTSTEP_TIMINGS_MS = {1280, 4000};

    private int previousWalkPosition = -1;

    private static final int PORTAL_ANIM_DURATION_MS = 1800;
    private static final int[] PORTAL_SOUND_TIMINGS_MS = {50, 1500};
    private final Set<Integer> portalPlayedFrames = new HashSet<>();


    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState switchingPortalAnimationState = new AnimationState();

    public int animationTimeout = 0;
    public static final int DEFAULT_ANIMATION_TIMEOUT = 15;

    private double chanceToChancePortalFirstSpawn = 10;
    private double switchingPortalChance = 10;
    private double switchingPortalCooldown = 20000;
    private double switchingPortalCounter = 0;

    private int switchingTimer = 36;
    private int switchingTimerCounter = 0;
    public boolean completeSwitich = false;

    public Portaler(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.WALK, walkAnimationState);
        animationHashMap.put(AnimationName.SWITCHING_PORTAL, switchingPortalAnimationState);

        if (!this.getWorld().isClient) {
            Utils.addRunAfter(() -> {
                if (WelcomeToMyWorld.random.nextInt(0, 100) <= chanceToChancePortalFirstSpawn) {
                    this.setIsSwitchingPortal(true);
                    switchingTimerCounter = switchingTimer;
                }
            }, 200);
        }
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TEXTURE_VARIANT, 0);
        this.dataTracker.startTracking(IS_SWITCHING_PORTAL, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.getGoals().removeIf(g ->
                g.getGoal() instanceof WanderAroundGoal ||
                        g.getGoal() instanceof WanderAroundFarGoal
        );

        this.goalSelector.add(1, new StopMovingAndLookingWhenSwitichPortalGoal(this));
        this.goalSelector.add(2, new LargeEntityWanderGoal(this, 1f, 1));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 5.0F));
    }

    public void setAnimationStates() {

        if (!this.getWorld().isClient) {
            if (this.switchingTimerCounter > 0) {
                switchingTimerCounter--;
            }
            if (this.getIsSwitchingPortal()) {
                if (switchingTimerCounter <= 0) {
                    this.setIsSwitchingPortal(false);
                    int nextVariant = (getTextureVariant() + 1) % 2;
                    setTextureVariant(nextVariant);
                }
            }

        }

        if (this.getWorld().isClient) {
            Vec3d velocity = this.getVelocity();
            boolean isMoving = velocity.x != 0 || velocity.z != 0;

            if (!switchingPortalAnimationState.isRunning()) {
                if (isMoving) {
                    if (!walkAnimationState.isRunning()) {
                        startAnimation(AnimationName.WALK);
                    }
                } else {
                    if (!idleAnimationState.isRunning()) {
                        startAnimation(AnimationName.IDLE);
                    }
                }
            }

            if (animationTimeout <= 0) {
                if (this.getIsSwitchingPortal() && !switchingPortalAnimationState.isRunning()) {
                    startAnimation(AnimationName.SWITCHING_PORTAL);
                    if (getTextureVariant() == 1) {
                        spawnPillarEffect(this.getBlockPos(), this.getWorld(), ParticleTypes.PORTAL);
                    } else {
                        spawnPillarEffect(this.getBlockPos(), this.getWorld(), ParticleTypes.END_ROD);
                    }
                }

                if (!this.getIsSwitchingPortal()) {
                    if (switchingPortalAnimationState.isRunning()) {
                        switchingPortalAnimationState.stop();
                        animationTimeout = 0; // Reset the timeout here
                    }
                }
            }
        }

        if (animationTimeout >= 0) {
            animationTimeout--;
        }
    }


    @Override
    public void tick() {
        super.tick();
        setAnimationStates();

        if (!this.getWorld().isClient) {
            switchingPortalCounter++;
            if (switchingPortalCounter > switchingPortalCooldown) {
                switchingPortalCounter = 0;
                if (this.getTextureVariant() == 0) {
                    if (WelcomeToMyWorld.random.nextInt(0, 100) <= switchingPortalChance) {
                        this.setIsSwitchingPortal(true);
                        switchingTimerCounter = switchingTimer;
                    }
                } else {
                    this.setIsSwitchingPortal(true);
                    switchingTimerCounter = switchingTimer;
                }
            }
        }

        handleAnimationSounds();
        updateRotation(); // Ensure rotation updates
    }

    private void updateRotation() {
        Vec3d velocity = this.getVelocity();
        if (velocity.lengthSquared() > 0.0001) { // Ensure entity is moving
            double angle = Math.atan2(-velocity.x, velocity.z); // Minecraft uses -x for left/right
            this.setYaw((float) Math.toDegrees(angle)); // Convert radians to degrees
            this.bodyYaw = this.getYaw();
            this.headYaw = this.getYaw();
        }
    }

    private void handleAnimationSounds() {
        if (!this.getWorld().isClient()) return;

        if (walkAnimationState.isRunning()) {
            handleWalkSounds();
        }

        if (switchingPortalAnimationState.isRunning()) {
            handleSwitchSounds();
        }
    }

    private void handleWalkSounds() {
        if (!this.getWorld().isClient()) return;
        if (!walkAnimationState.isRunning()) return;

        long animTime = walkAnimationState.getTimeRunning();
        int currentPos = (int) (animTime % WALK_CYCLE_DURATION_MS);

        for (int timing : FOOTSTEP_TIMINGS_MS) {
            int timingInCycle = timing % WALK_CYCLE_DURATION_MS;

            if (previousWalkPosition != -1) {
                boolean normalCross = previousWalkPosition < timingInCycle && currentPos >= timingInCycle;
                boolean wrapAround = timingInCycle == 0 && previousWalkPosition > currentPos;

                if (normalCross || wrapAround) {
                    Utils.sendSoundPacketFromClient(SoundsManager.PORTALER_STEP, this.getBlockPos());
                }
            }
        }

        previousWalkPosition = currentPos;
    }

    private void handleSwitchSounds() {
        if (!this.getWorld().isClient()) return;
        if (!switchingPortalAnimationState.isRunning()) return;

        long animTime = switchingPortalAnimationState.getTimeRunning();
        int currentPos = (int) (animTime % PORTAL_ANIM_DURATION_MS);

        // Check each sound point with 25ms tolerance (half tick)
        for (int timing : PORTAL_SOUND_TIMINGS_MS) {
            int windowStart = timing - 25;
            int windowEnd = timing + 25;

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;
            boolean cycleWrap = timing > PORTAL_ANIM_DURATION_MS - 25 &&
                    currentPos < timing - PORTAL_ANIM_DURATION_MS + 25;

            if ((inWindow || cycleWrap) && !portalPlayedFrames.contains(timing)) {
                triggerPortalSound(timing);
                portalPlayedFrames.add(timing);
            }
        }

        // Reset tracking at animation end
        if (currentPos > PORTAL_ANIM_DURATION_MS - 200) {
            portalPlayedFrames.clear();
        }
    }

    private void triggerPortalSound(int timing) {
        SoundEvent soundId;

        if (timing >= 0 && timing <= 50) {
            soundId = SoundsManager.PORTALER_SPIN;
        } else {
            soundId = SoundsManager.PORTALER_PORTAL_CHANGE;
        }

        if(soundId != null) {
            Utils.sendSoundPacketFromClient(soundId, this.getBlockPos());
        }

    }

    @Override
    public void setMovementSpeed(float speed) {
        super.setMovementSpeed(speed); // Don't limit speed
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        super.onPlayerCollision(player);

        if (!this.getWorld().isClient) {
            if (this.getVehicle() != null) return;
            ServerPlayerEntity p = (ServerPlayerEntity) player;
            if (this.getTextureVariant() == 0) {
                teleportPlayerRelativeToMob(p, World.NETHER);
            } else {
                teleportPlayerRelativeToMob(p, World.END);
            }

            Utils.playSound(p.getServerWorld(), p.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT);
        }

    }

    private void teleportPlayerRelativeToMob(ServerPlayerEntity player, RegistryKey<World> dimension) {

        ServerWorld targetWorld = player.getServer().getWorld(dimension);

        if (targetWorld != null) {
            int x = this.getBlockPos().getX();
            int z = this.getBlockPos().getZ();
            int y = this.getBlockPos().getY() + 30;

            if (dimension == World.NETHER) {
                if (y <= 0) {
                    y = 10;
                } else if (y >= 128) {
                    y = 123;
                }
                player.teleport(targetWorld, x, y, z, player.getYaw(), player.getPitch());
            } else {
                player.teleport(targetWorld, x, y, z, player.getYaw(), player.getPitch());
            }
        }
    }

    public void startAnimation(AnimationName name) {
        AnimationName na = null;

        for (AnimationName n : animationHashMap.keySet()) {
            if (n.equals(name)) {
                na = n;
            } else {
                animationHashMap.get(n).stop();
            }
        }

        if (na != null) {
            animationHashMap.get(na).start(this.age);
            animationTimeout = DEFAULT_ANIMATION_TIMEOUT;
        }
    }

    public void startAnimation(AnimationName name, int timeout) {
        AnimationName na = null;
        for (AnimationName n : animationHashMap.keySet()) {
            if (n.equals(name)) {
                na = n;
            } else {
                animationHashMap.get(n).stop();
            }
        }
        if (na != null) {
            animationHashMap.get(na).start(this.age);
            animationTimeout = timeout;
        }
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundsManager.PORTALER_HURT;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient) {
            Utils.playSound(this.getWorld(), this.getBlockPos(), SoundsManager.PORTALER_DEATH);
        }
    }

    public int getTextureVariant() {
        return this.dataTracker.get(TEXTURE_VARIANT);
    }

    public void setTextureVariant(int variant) {
        this.dataTracker.set(TEXTURE_VARIANT, variant);
    }

    public boolean getIsSwitchingPortal() {
        return this.dataTracker.get(IS_SWITCHING_PORTAL);
    }

    public void setIsSwitchingPortal(boolean variant) {
        this.dataTracker.set(IS_SWITCHING_PORTAL, variant);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("TextureVariant", this.getTextureVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setTextureVariant(nbt.getInt("TextureVariant"));
    }

    class StopMovingAndLookingWhenSwitichPortalGoal extends Goal {
        private final Portaler mob;

        public StopMovingAndLookingWhenSwitichPortalGoal(Portaler mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getIsSwitchingPortal();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }

        @Override
        public boolean shouldContinue() {
            return mob.getIsSwitchingPortal();
        }

        @Override
        public void tick() {
            mob.getNavigation().stop();
        }
    }


    public class LargeEntityWanderGoal extends Goal {
        private final PathAwareEntity mob;
        private double targetX, targetY, targetZ;
        private final double speed;
        private final int chance;
        private final Random random;

        public LargeEntityWanderGoal(PathAwareEntity mob, double speed, int chance) {
            this.mob = mob;
            this.speed = speed;
            this.chance = chance;
            this.random = mob.getRandom();
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.mob.hasPassengers()) return false;
            if (this.mob.getRandom().nextInt(toGoalTicks(this.chance)) != 0) return false;

            Vec3d target = getWanderTarget();
            if (target == null) return false;

            this.targetX = target.x;
            this.targetY = target.y;
            this.targetZ = target.z;
            return true;
        }

        @Nullable
        protected Vec3d getWanderTarget() {
            double width = mob.getWidth(); // Get entity width
            double height = mob.getHeight(); // Get entity height

            // Ensure movement is at least 5 blocks and up to 20 blocks
            double minDistance = 5 + width;  // Ensure no clipping
            double maxDistance = 20 + width; // Large mobs move farther

            // Generate a valid movement range
            double distance = minDistance + (random.nextDouble() * (maxDistance - minDistance));

            // Get a random direction
            double angle = random.nextDouble() * 2 * Math.PI; // 0 to 360 degrees
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;

            Vec3d target = NoPenaltyTargeting.find(mob, (int) distance, (int) height);
            if (target == null) {
                return new Vec3d(mob.getX() + offsetX, mob.getY(), mob.getZ() + offsetZ);
            }
            return target;
        }

        @Override
        public boolean shouldContinue() {
            return !this.mob.getNavigation().isIdle();
        }

        @Override
        public void start() {
            this.mob.getNavigation().startMovingTo(this.targetX, this.targetY + 0.1, this.targetZ, this.speed);
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }
    }

    public void spawnPillarEffect(BlockPos pos, World world, ParticleEffect particleEffect) {
        if (!this.getWorld().isClient) return;

        final int totalTicks = 80; // Total duration in ticks
        final int layers = 10; // Number of vertical circles
        final double totalHeight = 10; // Total pillar height in blocks

        AtomicInteger tickCount = new AtomicInteger(0);

        Runnable spinningTask = new Runnable() {
            @Override
            public void run() {
                if (tickCount.get() < totalTicks) {
                    // First 10 ticks to build up the pillar
                    if (tickCount.get() <= layers) {
                        spawnVerticalBuildUp(pos, world, particleEffect, tickCount.get(), totalHeight, layers);
                    }
                    // Subsequent ticks for spinning animation
                    else {
                        spawnSpinningParticlesAround(pos, world, tickCount.get(), particleEffect, totalHeight, layers);
                    }

                    ClientScheduler.schedule(this, 1);
                    tickCount.incrementAndGet();
                }
            }
        };

        ClientScheduler.schedule(spinningTask, 0);
    }

    private void spawnVerticalBuildUp(BlockPos pos, World world, ParticleEffect particleEffect,
                                      int currentTick, double totalHeight, int layers) {
        double yIncrement = totalHeight / layers;
        double currentY = pos.getY() + (currentTick * yIncrement);

        // Create a solid circle at currentY
        int particlesPerLayer = 30;
        double radius = 1.0;

        for (int i = 0; i < particlesPerLayer; i++) {
            double angle = Math.PI * 2 * i / particlesPerLayer;
            double x = pos.getX() + radius * Math.cos(angle);
            double z = pos.getZ() + radius * Math.sin(angle);
            world.addParticle(particleEffect, x, currentY, z, 0, 0, 0);
        }
    }

    public void spawnSpinningParticlesAround(BlockPos pos, World world, int tickCount,
                                             ParticleEffect particleEffect, double totalHeight, int layers) {
        double radius = 4.0;
        int particlesPerLayer = 20;
        double yIncrement = totalHeight / layers;
        double baseY = pos.getY();

        // Calculate rotation angle based on tick count
        double rotationAngle = Math.PI * 2 * (tickCount % 100) / 100.0;

        // Create particles for each layer
        for (int layer = 0; layer < layers; layer++) {
            double currentY = baseY + (layer * yIncrement);

            for (int i = 0; i < particlesPerLayer; i++) {
                double particleAngle = rotationAngle + (Math.PI * 2 * i / particlesPerLayer);
                double x = pos.getX() + radius * Math.cos(particleAngle);
                double z = pos.getZ() + radius * Math.sin(particleAngle);

                // Add slight vertical variation
                double yOffset = 0.2 * Math.sin(particleAngle + rotationAngle);
                world.addParticle(particleEffect, x, currentY + yOffset, z, 0, 0.05, 0);
            }
        }
    }
}