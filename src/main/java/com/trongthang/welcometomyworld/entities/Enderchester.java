package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.StartAnimation;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.Utilities.SpawnParticiles.spawnParticlesAroundEntity;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;
import static com.trongthang.welcometomyworld.client.ClientData.LAST_INTERACTED_MOB_ID;


public class Enderchester extends TameableEntity implements StartAnimation {

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    private static final TrackedData<Boolean> IS_SLEEPING = DataTracker.registerData(Enderchester.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_SLEEP = DataTracker.registerData(Enderchester.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_OPENING_CHEST = DataTracker.registerData(Enderchester.class, TrackedDataHandlerRegistry.BOOLEAN);

    List<Item> hateItems = List.of(
            Items.DIRT,
            Items.GRASS_BLOCK);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState mouthOpenAnimationState = new AnimationState();
    public final AnimationState mouthCloseAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState splitAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    public int animationTimeout = 0;
    public boolean chestIsClose = true;

    public double chanceToSleep = 0.35;
    public int sleepingCheckCooldown = 2000;
    public int sleepingCheckCounter = 0;
    public int sleepingMinDuration = 400;
    private int sleepDurationCurrent = 0;

    public int canSleepCooldown = 400;
    public int canSleepCounter = 0;

    private ParticleEffect particleEffect = ParticleTypes.PORTAL;

    public int particleSpawnCounter = 0;
    public int eatingThingsCouter = 0;
    public int passiveHealingCounter = 0;

    public boolean isInCombat = false;
    public int inCombatCooldown = 20;
    public int inCombatCounter = 0;

    public Item tameFood = Items.ENDER_PEARL;

    public float passiveHealingAmount = 6;

    public static int DEFAULT_ANIMATION_TIMEOUT = 15;
    public static int DEFAULT_PARTICLE_SPAWN_COOLDOWN = 20;
    public static int DEFAULT_EATING_COOLDOWN = 20;
    public static int EAT_AREA = 7;
    public static int PASSIVE_HEALING_COOLDOWN = 60;
    private int checkMusicCounter = 0;
    private int checkMusicCooldown = 25;

    private int checkOwnerOfflineCounter = 40;
    private int checkOwnerOfflineCooldown = 0;

    public Enderchester(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.EAT_ITEMS, eatAnimationState);
        animationHashMap.put(AnimationName.WALK, walkAnimationState);
        animationHashMap.put(AnimationName.MOUTH_CLOSE, mouthCloseAnimationState);
        animationHashMap.put(AnimationName.MOUTH_OPEN, mouthOpenAnimationState);
        animationHashMap.put(AnimationName.SPLIT_ITEMS, splitAnimationState);
        animationHashMap.put(AnimationName.SIT, sitAnimationState);
        animationHashMap.put(AnimationName.JUMP, jumpAnimationState);
        animationHashMap.put(AnimationName.SLEEP, sleepAnimationState);
        animationHashMap.put(AnimationName.ATTACK, attackAnimationState);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        // Start with WolfEntity's default attributes
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_SLEEPING, false);
        this.dataTracker.startTracking(CAN_SLEEP, true);
        this.dataTracker.startTracking(IS_OPENING_CHEST, false);
    }

    public void setIsSleepingData(boolean value) {
        this.dataTracker.set(IS_SLEEPING, value);
    }

    public boolean getIsSleepingData() {
        return this.dataTracker.get(IS_SLEEPING);
    }

    public void setCanSleepData(boolean value) {
        this.dataTracker.set(CAN_SLEEP, value);
    }

    public boolean getCanSleepData() {
        return this.dataTracker.get(CAN_SLEEP);
    }

    public void setIsOpeningChestData(boolean value) {
        this.dataTracker.set(IS_OPENING_CHEST, value);
    }

    public boolean getIsOpeningChestData() {
        return this.dataTracker.get(IS_OPENING_CHEST);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new CustomSitGoal(this));
        this.goalSelector.add(3, new SwimGoal(this));
        this.goalSelector.add(5, new SleepingNoMove(this));
        this.goalSelector.add(6, new StopMovementGoalWhenOpeningChest(this));
        this.goalSelector.add(7, new FollowOwnerGoal(this, 3, 8.0F, 2.0F, false));
        this.goalSelector.add(8, new FleeFromNearbyPlayersGoal(this, 15, 2.8));
        this.goalSelector.add(9, new MeleeAttackGoal(this, 2.2, true));
        this.goalSelector.add(10, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(12, new LookAroundGoal(this));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    }

    @Override
    public void onAttacking(Entity target) {
        super.onAttacking(target);

        PacketByteBuf id = PacketByteBufs.create();
        id.writeInt(this.getId());

        animationTimeout = 25;

        for (ServerPlayerEntity p : this.getServer().getPlayerManager().getPlayerList()) {
            if (p.canSee(this)) {
                ServerPlayNetworking.send(p, A_LIVING_CHEST_ATTACK, id);
            }
        }
    }


    @Override
    public void setTamed(boolean tamed) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte) (b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte) (b & -5));
        }

        // Reset max health to your custom value after taming
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(100);
        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15f);
        this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(30);

        this.setHealth(50);
    }

    private void setupAnimationStates() {

        // Handle sleep animation
        if (this.getWorld().isClient) {
            boolean isSleeping = this.getIsSleepingData();

            if (isSleeping) {
                if (!sleepAnimationState.isRunning()) {
                    startAnimation(AnimationName.SLEEP, this.sleepingMinDuration);
                }
            } else {
                if (sleepAnimationState.isRunning()) {
                    sleepAnimationState.stop();
                }
            }

            Vec3d velocity = this.getVelocity(); // Current velocity of the entity
            boolean isMoving = velocity.x != 0 || velocity.z != 0; // Check if entity is moving

            if (animationTimeout <= 0) {
                if (!this.getIsOpeningChestData()) {
                    if (!isSitting()) {
                        if (isMoving) {
                            if (!walkAnimationState.isRunning()) {
                                startAnimation(AnimationName.WALK);
                            }
                        } else {
                            if (!idleAnimationState.isRunning()) { // Start idle animation
                                startAnimation(AnimationName.IDLE);
                            }
                        }
                    } else {
                        if (!sitAnimationState.isRunning()) {
                            startAnimation(AnimationName.SIT);
                        }
                    }
                    if (chestIsClose) {
                        startAnimation(AnimationName.MOUTH_CLOSE);
                        chestIsClose = false;
                    }
                } else {
                    if (this.getIsOpeningChestData() && !chestIsClose) {
                        if (!mouthOpenAnimationState.isRunning()) {
                            startAnimation(AnimationName.MOUTH_OPEN);
                            chestIsClose = true;
                        }
                    }
                }
            }

            if (animationTimeout > 0) {
                animationTimeout--;
            }
        }

        if (!this.getWorld().isClient()) {
            eatingThingsCouter++;

            if (eatingThingsCouter >= DEFAULT_EATING_COOLDOWN) {
                eatItemsOnGround();
                eatingThingsCouter = 0;
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("sleepDurationCurrent", this.sleepDurationCurrent);
    }

    // Load inventory from NBT
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.sleepDurationCurrent = nbt.getInt("sleepDurationCurrent");
    }

    @Override
    public void tick() {
        super.tick();

//        if(this.getWorld().isClient){
//            LOGGER.info("CLIENT: " + this.isSitting());
//        }else {
//            LOGGER.info("SERVER: " + this.isSitting());
//        }

        if (!this.getWorld().isClient()) {
            if (this.getCanSleepData()) {

                if(this.getOwner() != null){
                    if(checkOwnerOfflineCounter < checkOwnerOfflineCooldown){
                        checkOwnerOfflineCounter++;
                    }

                    if(checkOwnerOfflineCounter >= checkOwnerOfflineCooldown){
                        checkOwnerOfflineCounter = 0;
                        if(this.getIsOpeningChestData()){
                            ServerPlayerEntity owner = ((ServerWorld) this.getWorld()).getServer()
                                    .getPlayerManager().getPlayer(this.getOwnerUuid());
                            if(owner == null){
                                this.setIsOpeningChestData(false);
                            }
                        }
                    }
                }


                if (!this.getIsSleepingData() && this.getOwner() == null) {
                    if (this.checkMusicCounter < checkMusicCooldown) {
                        this.checkMusicCounter++;
                    }
                    if (this.checkMusicCounter >= this.checkMusicCooldown) {
                        if (this.isHearingMusic()) {
                            this.setIsSleepingData(true);
                            sleepingCheckCounter = 0;
                        }
                    }

                    if (this.sleepingCheckCounter < sleepingCheckCooldown) {
                        sleepingCheckCounter++;
                    }
                    if (sleepingCheckCounter > sleepingCheckCooldown) {
                        sleepingCheckCounter = 0;
                        if (random.nextDouble() < chanceToSleep) { // Server's decision
                            this.setIsSleepingData(true);
                            sleepDurationCurrent = 0; // Reset sleep duration
                        }
                    }
                }
            }

            // Handle sleep duration and termination
            if (this.getIsSleepingData()) {
                sleepDurationCurrent++;
                if (sleepDurationCurrent >= sleepingMinDuration) {
                    this.setIsSleepingData(false);
                    canSleepCounter = 0;
                    sleepDurationCurrent = 0;
                }
            }

            // Update CAN_SLEEP cooldown
            if (!this.getCanSleepData()) {
                if (canSleepCounter < canSleepCooldown) {
                    canSleepCounter++;
                }
                if (canSleepCounter >= canSleepCooldown) {
                    this.setCanSleepData(true);
                    canSleepCounter = 0;
                }
            }
        }

        setupAnimationStates(); // Handles animations on both sides based on synced data

        if (this.getHealth() <= 0) {
            return;
        }


        if (!this.getCanSleepData()) {
            canSleepCounter++;
            if (canSleepCounter > canSleepCooldown) {
                this.setCanSleepData(true);
                canSleepCounter = 0;
            }
        }


        if (isInCombat) {
            if (inCombatCounter <= inCombatCooldown) {
                inCombatCounter++;
            }
            if (inCombatCounter > inCombatCooldown) {
                isInCombat = false;
            }
        }

        passiveHealingCounter++;
        if (passiveHealingCounter >= PASSIVE_HEALING_COOLDOWN) {
            if (this.getOwner() == null) {
                if (this.isTamed()) {
                    this.setTamed(false);
                }
            }
            passiveHealingCounter = 0;
            this.setHealth(this.getHealth() + passiveHealingAmount);
        }

        particleSpawnCounter++;

        // Every 20 ticks, spawn particles around the mob
        if (particleSpawnCounter >= DEFAULT_PARTICLE_SPAWN_COOLDOWN) {

            spawnParticlesAroundEntity(this, this.getParticleEffect(), 0.5, 3);

            particleSpawnCounter = 0;
        }
    }

    private boolean isHearingMusic() {
        boolean hasMusic = false;

        Box box = new Box(this.getBlockPos()).expand(5);
        for (BlockPos pos : BlockPos.iterate(
                MathHelper.floor(box.minX),
                MathHelper.floor(box.minY),
                MathHelper.floor(box.minZ),
                MathHelper.floor(box.maxX),
                MathHelper.floor(box.maxY),
                MathHelper.floor(box.maxZ))) {

            BlockState state = this.getWorld().getBlockState(pos);
            if (state.getBlock() instanceof JukeboxBlock) {
                BlockEntity blockEntity = this.getWorld().getBlockEntity(pos);
                if (blockEntity instanceof JukeboxBlockEntity) {
                    hasMusic = ((JukeboxBlockEntity) blockEntity).isPlayingRecord();
                    if (hasMusic) {
                        return true;
                    }
                    ;
                }
            }
        }

        return hasMusic;
    }


    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        World world = this.getWorld();

        // Client-side preview (UI, animation, etc.)
        if (world.isClient) {
            // If the mob is tamed and the player is the owner, show the chest UI (preview)
            if (this.isTamed() && this.getOwner() == player) {
                LAST_INTERACTED_MOB_ID = this.getId();
                this.chestIsClose = false;

                // Open mob chest preview on the client side
                openMobChest(player);
                return ActionResult.SUCCESS;
            }

            // If the mob is sleeping and the player is holding the correct food, consume the item
            if (this.getIsSleepingData() && itemStack.isOf(this.getTameFood())) {
                return ActionResult.CONSUME;
            }

            return ActionResult.PASS;
        }

        // Server-side logic (gameplay-related)
        if (this.isTamed() && this.getOwner() == player) {
            // Update mob chest state and animation only on the server side
            LAST_INTERACTED_MOB_ID = this.getId();
            this.setIsOpeningChestData(true); // Trigger the chest opening animation
            this.chestIsClose = false;
            this.animationTimeout = 1;

            // Open the mob chest (this may trigger an actual network packet to the client)
            openMobChest(player);

            return ActionResult.SUCCESS;
        }

        // If the mob is sleeping and the player is holding the tame food
        if (this.getIsSleepingData() && itemStack.isOf(this.getTameFood())) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1); // Consume one item from the player's inventory
            }

            // Chance to tame the mob
            if (this.random.nextInt(3) == 0) {
                this.setOwner(player);
                this.setTamed(true);
                this.setIsSleepingData(false);
                this.setSitting(false);
                this.animationTimeout = 1;

                // Show particle effects (for all players in range, not just the one interacting)
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);

                // Send a packet to the player who tamed the mob (you can use this for specific reactions or animations)
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(this.getId());
                for (ServerPlayerEntity p : this.getServer().getPlayerManager().getPlayerList()) {
                    if (p.canSee(this)) {
                        ServerPlayNetworking.send(p, A_LIVING_CHEST_JUMP, buf);
                    }
                }

                if(this instanceof Chester){
                    Utils.grantAdvancement((ServerPlayerEntity) player, "tameable/is_that_a_dog");
                } else {
                    Utils.grantAdvancement((ServerPlayerEntity) player, "tameable/ender_puppu");
                }

            } else {
                // Negative reaction if tame attempt fails
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }


    public void openMobChest(PlayerEntity player) {
        EnderChestInventory enderChestInventory = player.getEnderChestInventory();
        player.openHandledScreen(
                new SimpleNamedScreenHandlerFactory(
                        (syncId, inventory, playerx) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, enderChestInventory),
                        Text.translatable("container.enderchest")
                ));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Check if the attacker is the owner
        if (source.getAttacker() instanceof PlayerEntity player && player == this.getOwner() && player.isSneaking()) {
            this.setSitting(!this.isSitting());
            return false; // Prevent damage if the owner hits the mob
        }

        isInCombat = true;
        inCombatCounter = 0;
        if (this.getIsSleepingData()) {
            this.setIsSleepingData(false);
            this.animationTimeout = 1;
        }

        return super.damage(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SoundEvents.ENTITY_WOLF_PANT;
        } else {
            return random.nextDouble() > 50 ? SoundsManager.ENDERCHESTER_AMBIENT : SoundsManager.ENDERCHESTER_AMBIENT2;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundsManager.ENDERCHESTER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsManager.ENDERCHESTER_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    public ParticleEffect getParticleEffect() {
        return this.particleEffect;
    }

    public Item getTameFood() {
        return this.tameFood;
    }

    public void eatItemsOnGround() {

        if (this.getWorld().isClient) return;
        if (this.isDead()) return;
        if (this.getIsSleepingData()) return;

        // Ensure the mob has an owner
        if (this.getOwner() == null) {
            return;
        }

        if (this.getServer() == null) {
            return;
        }

        var player = this.getServer().getPlayerManager().getPlayer(this.getOwner().getUuid());
        if (player == null) {
            return;
        }

        Box checkArea = new Box(this.getBlockPos()).expand(EAT_AREA);
        List<ItemEntity> itemEntities = this.getWorld().getEntitiesByClass(ItemEntity.class, checkArea, entity -> true);

        SimpleInventory enderChestInventory = this.getChest(player);


        // Iterate through the found items
        boolean isAdded = false;
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getStack();
            // If the item is not something the mob hates, try to add it to the inventory
            if (!hateItems.contains(stack.getItem())) {
                boolean added = addItemToChest(enderChestInventory, stack);

                // If the item was successfully added to the chest, remove it from the world
                if (added) {
                    isAdded = true;
                    itemEntity.discard();  // Remove the item from the world
                }
            }
        }
        if (isAdded) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeInt(this.getId());

            startAnimation(AnimationName.EAT_ITEMS, 30);
            ServerPlayNetworking.send((ServerPlayerEntity) this.getOwner(), A_LIVING_CHEST_EAT_ANIMATION, buf);
            ServerPlayNetworking.send((ServerPlayerEntity) this.getOwner(), A_LIVING_CHEST_EATING_SOUND, PacketByteBufs.empty());
        }
    }

    public SimpleInventory getChest(PlayerEntity player) {
        return player.getEnderChestInventory();
    }


    public boolean addItemToChest(SimpleInventory chest, ItemStack stack) {
        int newStack = 0;

        for (int i = 0; i < chest.size(); i++) {
            ItemStack slotStack = chest.getStack(i);

            // If the slot is empty, put the stack there
            if (slotStack.isEmpty()) {
                chest.setStack(i, stack);
                return true;
            }

            // If the slot has the same item and can be merged, merge them
            if (ItemStack.canCombine(slotStack, stack)) {
                int transferAmount = Math.min(slotStack.getMaxCount() - slotStack.getCount(), stack.getCount());
                slotStack.increment(transferAmount);
                stack.decrement(transferAmount);

                if (stack.isEmpty()) {
                    return true; // Entire stack was added
                }
            }
        }

        return false; // Could not fully add the stack
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
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
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    class StopMovementGoalWhenOpeningChest extends Goal {
        private final Enderchester mob;

        public StopMovementGoalWhenOpeningChest(Enderchester mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE)); // Controls movement
        }

        @Override
        public boolean canStart() {
            return mob.getIsOpeningChestData();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }

        @Override
        public boolean shouldContinue() {
            return mob.getIsOpeningChestData();
        }

        @Override
        public void tick() {
            mob.getNavigation().stop();
        }
    }

    class SleepingNoMove extends Goal {
        private final Enderchester mob;

        public SleepingNoMove(Enderchester mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getIsSleepingData();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();  // Stop navigation when sleeping
        }

        @Override
        public boolean shouldContinue() {
            return mob.getIsSleepingData();
        }

        @Override
        public void tick() {
            if (!mob.getIsSleepingData()) {
                return; // If the mob stops sleeping, continue normal movement
            }
            mob.getNavigation().stop(); // Continue to stop movement while sleeping
        }
    }

    public class FleeFromNearbyPlayersGoal extends Goal {
        private final Enderchester mob;
        private final double fleeSpeed;
        private final float checkDistance;
        private PlayerEntity threateningEntity;
        private int cooldown;


        public FleeFromNearbyPlayersGoal(Enderchester mob, float checkDistance, double fleeSpeed) {
            this.mob = mob;
            this.checkDistance = checkDistance;
            this.fleeSpeed = fleeSpeed;
            this.setControls(EnumSet.of(Control.MOVE)); // Required for movement-based goals
        }

        @Override
        public boolean canStart() {
            if (mob.getIsSleepingData() || mob.getIsOpeningChestData() || mob.getOwner() != null) return false;

            if (cooldown > 0) {
                cooldown--;
                return false;
            }

            // Find nearest threatening entity
            threateningEntity = this.mob.getWorld().getClosestEntity(
                    PlayerEntity.class,
                    TargetPredicate.createNonAttackable().setBaseMaxDistance(checkDistance),
                    this.mob,
                    this.mob.getX(),
                    this.mob.getY(),
                    this.mob.getZ(),
                    this.mob.getBoundingBox().expand(checkDistance)
            );

            if (threateningEntity == null) return false;

            if (threateningEntity.isSpectator() || threateningEntity.isCreative()) {
                return false;
            }

            // No threat found
            if (threateningEntity == null) return false;

            // Don't flee from owner or self
            if (threateningEntity.equals(mob) ||
                    (mob.isTamed() && threateningEntity.equals(mob.getOwner()))) {
                return false;
            }

            return true;
        }

        @Override
        public void start() {
            // Calculate flee direction away from the threat
            Vec3d threatPos = threateningEntity.getPos();
            Vec3d mobPos = mob.getPos();
            Vec3d fleeDir = new Vec3d(mobPos.x - threatPos.x, 0, mobPos.z - threatPos.z).normalize();

            Vec3d targetPos = mobPos.add(fleeDir.multiply(25));

            // Navigate to safe position
            mob.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, fleeSpeed);
            cooldown = 15; // 1-second cooldown after fleeing
        }

        @Override
        public boolean shouldContinue() {
            if (mob.getIsSleepingData() || mob.getIsOpeningChestData() || mob.getOwner() != null) return false;

            boolean shouldContinue = !mob.getNavigation().isIdle() &&
                    threateningEntity.isAlive() &&
                    threateningEntity.distanceTo(mob) < checkDistance * 1.5;

            mob.setCanSleepData(shouldContinue);
            mob.canSleepCounter = 0;
            mob.sleepingCheckCounter = 0;

            return shouldContinue;
        }

        @Override
        public void stop() {
            threateningEntity = null;
            mob.getNavigation().stop();
        }
    }

    class CustomSitGoal extends Goal {
        private final Enderchester mob;

        public CustomSitGoal(Enderchester mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return mob.isSitting();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
            mob.setInSittingPose(true);
        }

        @Override
        public void stop() {
            mob.setInSittingPose(false);
        }

        @Override
        public boolean shouldContinue() {
            return mob.isSitting();
        }
    }
}

