package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.StartAnimation;
import com.trongthang.welcometomyworld.client.ClientScheduler;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.entities.Enderchester.DEFAULT_PARTICLE_SPAWN_COOLDOWN;

public class EnderPest extends MobEntity implements StartAnimation {

    public static int totalEnderPests = 0;
    public static final int MAX_ENDER_PESTS = 12;

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    private static final TrackedData<Boolean> CAN_DISAPPEAR = DataTracker.registerData(EnderPest.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_SCAM = DataTracker.registerData(EnderPest.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ATE_ENDER_EYES = DataTracker.registerData(EnderPest.class, TrackedDataHandlerRegistry.INTEGER);

    private static final int WALK_CYCLE_DURATION_MS = 5380;
    private static final int[] FOOTSTEP_TIMINGS_MS = {1280, 4000};

    private int previousWalkPosition = -1;

    private static final int MOUNTH_OPEN_DURATION_MS = 8000;
    private static final int[] MOUTH_OPEN_TIMINGS_MS = {0, 330, 790, 1170, 1500, 1790, 2040, 2250, 2420, 2500, 2558};
    private final Set<Integer> portalPlayedFrames = new HashSet<>();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState mouthOpenAnimationState = new AnimationState();
    public final AnimationState disappearAnimationState = new AnimationState();
    public final AnimationState scamAnimationState = new AnimationState();
    public final AnimationState eatItemsAnimationState = new AnimationState();

    private boolean isDisappearStarting = false;

    public int animationTimeout = 0;
    public static final int DEFAULT_ANIMATION_TIMEOUT = 15;
    private int particleSpawnCounter;
    private boolean completeOpenMouthSound = false;

    private int healthDecreaseCooldown = 300;
    private int healthDecreaseCounter = 0;
    private int healthDecrease = 5;
    private int healthDisappear = 10;

    private double scamChance = 50;
    private int enderEyeScanCooldown = 40;
    private int enderEyesScanColldownCounter = 0;
    private int eyeOfEnderNeeded = 3;
    private boolean isScammingDisappear = false;

    private int minItem = 5;
    private int maxItem = 10;

    public EnderPest(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);

        eyeOfEnderNeeded = WelcomeToMyWorld.random.nextInt(3, 6);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.MOUTH_OPEN, mouthOpenAnimationState);
        animationHashMap.put(AnimationName.DISAPPEAR, disappearAnimationState);
        animationHashMap.put(AnimationName.SCAM, scamAnimationState);
        animationHashMap.put(AnimationName.EAT_ITEMS, eatItemsAnimationState);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CAN_DISAPPEAR, false);
        this.dataTracker.startTracking(IS_SCAM, false);
        this.dataTracker.startTracking(ATE_ENDER_EYES, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.getGoals().removeIf(g ->
                g.getGoal() instanceof WanderAroundGoal ||
                        g.getGoal() instanceof WanderAroundFarGoal
        );
    }

    public void setAnimationStates() {
        if (this.getWorld().isClient) {

            if (this.getIsScam()) {
                if (!this.scamAnimationState.isRunning()) {
                    startAnimation(AnimationName.SCAM, 400);
                }
                return;
            }

            if (this.getCanDisappear() && !isDisappearStarting) {
                if (!this.mouthOpenAnimationState.isRunning()) {
                    startAnimation(AnimationName.MOUTH_OPEN, 500);
                }
                isDisappearStarting = true;
                ClientScheduler.schedule(() -> this.startAnimation(AnimationName.DISAPPEAR, 20), 150);
                return;
            }

            if (!isDisappearStarting) {
                Vec3d velocity = this.getVelocity();
                boolean isMoving = velocity.x != 0 || velocity.z != 0;
                {
                    if (animationTimeout <= 0) {
                        if (!idleAnimationState.isRunning() && !isMoving) {
                            startAnimation(AnimationName.IDLE);
                        }
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

        enderEyesScanColldownCounter++;
        if (enderEyesScanColldownCounter > enderEyeScanCooldown) {
            eatEnderEyesOnGround();
            enderEyesScanColldownCounter = 0;
        }

        if (!this.getWorld().isClient) {
            if (this.getIsScam() && !this.isScammingDisappear) {
                Utils.addRunAfter(() -> {
                    Utils.playSound(this.getWorld(), this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT);
                    this.discard();
                }, 23);
                this.isScammingDisappear = true;
            }

            if (this.getAteEnderEye() >= this.eyeOfEnderNeeded && !this.getCanDisappear() && !this.getIsScam()) {
                if (WelcomeToMyWorld.random.nextInt(0, 100) < scamChance && !this.isScammingDisappear) {
                    this.setIsScam(true);
                } else {
                    if (!this.getCanDisappear() && !this.getIsScam()) {
                        openEnderPest();
                    }
                }
            }
            if (this.getHealth() > healthDisappear) {
                healthDecreaseCounter++;
                if (healthDecreaseCounter > healthDecreaseCooldown) {
                    healthDecreaseCounter = 0;
                    this.setHealth(this.getHealth() - healthDecrease);
                }
            } else if (this.getHealth() < healthDisappear) {
                Utils.playSound(this.getWorld(), this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT);
                this.discard();
                return;
            }
        }

        setAnimationStates();

        if (!this.getWorld().isClient) {
            if (this.getCanDisappear()) {
                if (!isDisappearStarting) {
                    isDisappearStarting = true;
                    Utils.addRunAfter(this::discard, 168);
                }
            }
        }

        handleAnimationSounds();
        spawnParticlesAround();
    }

    public void openEnderPest(){
        this.setCanDisappear(true);
        List<Item> items = getRandomItem(WelcomeToMyWorld.random.nextInt(minItem, maxItem));
        int startingTime = 51;
        if (!items.isEmpty()) {
            for (Item item : items) {
                startingTime += 3;
                Utils.addRunAfter(() -> {
                    if(this.isDead()) return;
                    shootItemUp(this.getWorld(), new Vec3d(this.getX(), this.getY(), this.getZ()), item);
                }, startingTime);
            }
        }
    }

    public void openEnderPest(int itemsAmount){
        this.setCanDisappear(true);
        List<Item> items = getRandomItem(itemsAmount);
        int startingTime = 51;
        if (!items.isEmpty()) {
            for (Item item : items) {
                startingTime += 3;
                Utils.addRunAfter(() -> {
                    shootItemUp(this.getWorld(), new Vec3d(this.getX(), this.getY(), this.getZ()), item);
                }, startingTime);
            }
        }
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    private void handleAnimationSounds() {
        if (!this.getWorld().isClient()) return;

        if (mouthOpenAnimationState.isRunning() && !completeOpenMouthSound) {
            handleSwitchSounds();
        }
    }

    private void handleSwitchSounds() {
        if (!this.getWorld().isClient()) return;
        if (!mouthOpenAnimationState.isRunning()) return;

        long animTime = mouthOpenAnimationState.getTimeRunning();
        int currentPos = (int) (animTime % MOUNTH_OPEN_DURATION_MS);

        // Check each sound point with 25ms tolerance (half tick)
        for (int timing : MOUTH_OPEN_TIMINGS_MS) {
            int windowStart = timing - 25;
            int windowEnd = timing + 25;

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;
            boolean cycleWrap = timing > MOUNTH_OPEN_DURATION_MS - 25 &&
                    currentPos < timing - MOUNTH_OPEN_DURATION_MS + 25;

            if ((inWindow || cycleWrap) && !portalPlayedFrames.contains(timing)) {
                triggerMouthOpenSound(timing);
                portalPlayedFrames.add(timing);
            }
        }

        // Reset tracking at animation end
        if (currentPos > MOUNTH_OPEN_DURATION_MS - 150) {
            portalPlayedFrames.clear();
        }
    }

    private void triggerMouthOpenSound(int timing) {
        SoundEvent soundId;

        if (timing >= 2510 && timing <= 2558) {
            soundId = SoundsManager.ENDER_PEST_MOUTH_OPEN;
            this.completeOpenMouthSound = true;

            if (this.getWorld().isClient) {
                createSquarePillarEffect(20, 3.0f, -0.1f, 0.7f);
            }


        } else {
            soundId = SoundsManager.ENDER_PEST_SHAKE1;
        }

        Utils.sendSoundPacketFromClient(soundId, this.getBlockPos());
    }

    public static void shootItemUp(World world, Vec3d pos, Item item) {
        if (world.isClient) return;

        ItemStack stack = new ItemStack(item);
        ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, stack);

        // Random velocity calculations
        Random random = world.random;
        double angle = random.nextDouble() * Math.PI * 3; // Random horizontal angle
        double horizontalSpeed = 0.08;
        double verticalSpeed = 0.79;

        Vec3d velocity = new Vec3d(
                Math.cos(angle) * horizontalSpeed,
                verticalSpeed,
                Math.sin(angle) * horizontalSpeed
        );

        // Add slight random variation
        velocity = velocity.add(
                (random.nextDouble() - 0.5) * 0.1,
                random.nextDouble() * 0.1,
                (random.nextDouble() - 0.5) * 0.1
        );

        itemEntity.setVelocity(velocity);
        world.spawnEntity(itemEntity);
    }

    private static final int MAX_ATTEMPTS = 50; // Prevent infinite loops

    public static List<Item> getRandomItem(int totalItems) {
        List<Item> items = new ArrayList<>();

        int registrySize = Registries.ITEM.size();
        if (registrySize == 0) return items;

        for (int x = 0; x < totalItems; x++) {
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                int randomIndex = WelcomeToMyWorld.random.nextInt(registrySize);
                Item item = Registries.ITEM.get(randomIndex);

                if (isValidItem(item)) {
                    items.add(item);
                    break;
                }
            }
        }

        return items;
    }

    private static boolean isValidItem(Item item) {
        // Customize your exclusion criteria here
        return item != Items.AIR &&
                !(item instanceof SpawnEggItem) &&
                !isHiddenItem(item) &&
                !(item instanceof BlockItem) &&
                !item.getName().toString().toLowerCase().contains("dev");
    }

    private static boolean isHiddenItem(Item item) {
        // Example: Exclude items from specific mods
        String namespace = Registries.ITEM.getId(item).getNamespace();
        return namespace.equals("minecraft") && item instanceof EnchantedBookItem;
    }

    @Override
    public boolean collidesWith(Entity other) {

        if ( other instanceof Wanderer) {
            return false;
        }

        return super.collidesWith(other);
    }

    public void spawnParticlesAround() {
        particleSpawnCounter++;

        // Every 20 ticks, spawn particles around the mob
        if (particleSpawnCounter >= DEFAULT_PARTICLE_SPAWN_COOLDOWN) {
            particleSpawnCounter = 0;  // Reset the counter after each 20 ticks

            World world = this.getWorld();
            if (world.isClient) {
                double x = this.getX();
                double y = this.getY() + this.getHeight() / 3;
                double z = this.getZ();

                // Spawn particles in a circle around the mob
                for (int i = 0; i < 5; i++) {  // Adjust number of particles as needed
                    double angle = Math.random() * 2 * Math.PI;  // Random angle in radians
                    double radius = 0.8;  // Radius of the particle circle

                    double offsetX = radius * Math.cos(angle);
                    double offsetZ = radius * Math.sin(angle);

                    // Add particle at random angle and radius around the mob's position
                    world.addParticle(ParticleTypes.PORTAL, x + offsetX, y, z + offsetZ, 0, 0, 0);
                }
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


    public void eatEnderEyesOnGround() {

        if (this.getWorld().isClient) return;
        if (this.isDead()) return;
        if (this.getServer() == null) {
            return;
        }

        Box checkArea = new Box(this.getBlockPos()).expand(1);
        List<ItemEntity> itemEntities = this.getWorld().getEntitiesByClass(ItemEntity.class, checkArea, entity -> true);
        ItemEntity itemEn = null;
        int totalEnderEyesEaten = this.getAteEnderEye();
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getStack();
            if (stack.getItem() == Items.ENDER_EYE && totalEnderEyesEaten < this.eyeOfEnderNeeded) {
                totalEnderEyesEaten++;
                itemEn = itemEntity;
                itemEntity.discard();
            }

            if (totalEnderEyesEaten >= this.eyeOfEnderNeeded) {
                break;
            }
        }

        this.setAteEnderEye(totalEnderEyesEaten);

        if (itemEn != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.getId());
            buf.writeEnumConstant(AnimationName.EAT_ITEMS);
            buf.writeInt(12);

            for (ServerPlayerEntity p : this.getWorld().getServer().getPlayerManager().getPlayerList()) {
                if (p.distanceTo(this) < 64) {
                    ServerPlayNetworking.send(p, ANIMATION_PACKET, buf);
                }
            }

            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.8f, 1.0f);
        }
    }


    // Add to your EnderPest class
    private void createSquarePillarEffect(int durationTicks, float maxHeight, float yOffset, float maxRadius) {
        if (!this.getWorld().isClient()) return;

        final int[] currentTick = {0};
        final Vec3d startPos = this.getPos().add(0, yOffset, 0);

        Runnable effectTask = new Runnable() {
            @Override
            public void run() {
                if (currentTick[0] > durationTicks) return;

                float progress = (float) currentTick[0] / durationTicks;
                float currentHeight = maxHeight * progress;
                float currentRadius = maxRadius * progress;

                // Calculate positions for square perimeter
                for (int side = 0; side < 4; side++) {
                    for (int i = 0; i <= 8; i++) { // 10 particles per side
                        double x = 0, z = 0;

                        switch (side) {
                            case 0: // Top side (Z+)
                                x = -currentRadius + (2 * currentRadius * i / 10);
                                z = currentRadius;
                                break;
                            case 1: // Right side (X+)
                                x = currentRadius;
                                z = currentRadius - (2 * currentRadius * i / 10);
                                break;
                            case 2: // Bottom side (Z-)
                                x = currentRadius - (2 * currentRadius * i / 10);
                                z = -currentRadius;
                                break;
                            case 3: // Left side (X-)
                                x = -currentRadius;
                                z = -currentRadius + (2 * currentRadius * i / 10);
                                break;
                        }

                        Vec3d particlePos = startPos.add(x, currentHeight, z);
                        spawnPillarParticle(particlePos);
                    }
                }

                currentTick[0]++;
                if (currentTick[0] <= durationTicks) {
                    ClientScheduler.schedule(this, 1);
                }
            }
        };

        ClientScheduler.schedule(effectTask, 0);
    }

    private void spawnPillarParticle(Vec3d pos) {
        if (this.getWorld().isClient) {
            this.getWorld().addParticle(
                    ParticleTypes.END_ROD,
                    pos.x,
                    pos.y,
                    pos.z,
                    (this.random.nextFloat() - 0.5f) * 0.1f,
                    0.1f,
                    (this.random.nextFloat() - 0.5f) * 0.1f
            );
        }
    }

    public boolean getCanDisappear() {
        return this.dataTracker.get(CAN_DISAPPEAR);
    }

    public void setCanDisappear(boolean canDisappear) {
        this.dataTracker.set(CAN_DISAPPEAR, canDisappear);
    }

    public boolean getIsScam() {
        return this.dataTracker.get(IS_SCAM);
    }

    public void setIsScam(boolean canDisappear) {
        this.dataTracker.set(IS_SCAM, canDisappear);
    }

    public int getAteEnderEye() {
        return this.dataTracker.get(ATE_ENDER_EYES);
    }

    public void setAteEnderEye(int ateEnderEye) {
        this.dataTracker.set(ATE_ENDER_EYES, ateEnderEye);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("canDisappear", this.getCanDisappear());
        nbt.putBoolean("isScam", this.getIsScam());
        nbt.putInt("ateEnderEye", this.getAteEnderEye());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setCanDisappear(nbt.getBoolean("canDisappear"));
        this.setIsScam(nbt.getBoolean("isScam"));
        this.setAteEnderEye(nbt.getInt("ateEnderEye"));
    }
}