package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.tameablePacket.StrongTameableEntityDefault;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EntityView;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.*;

//PORTALER: This mob is a portal that can move and can switch portal randomly, players can go to the portal to go to the end or the nether
public class FallenKnight extends StrongTameableEntityDefault {

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_BE_TAMED_SET = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_BE_TAMED = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_PATROLLING = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> ALL_SKILL_COOLDOWN = DataTracker.registerData(FallenKnight.class, TrackedDataHandlerRegistry.FLOAT);

    private static final int WALK_CYCLE_DURATION_MS = 4000;
    private static final int[] FOOTSTEP_TIMINGS_MS = {1080, 3210};

    private int previousWalkPosition = -1;

    private static final int ATTACK_2_DURATION_MS = 1460;
    private static final int[] ATTACK_2_SOUND_TIMINGS_MS = {670};
    private final Set<Integer> attack2PlayedFrames = new HashSet<>();

    private static final int ATTACK_3_DURATION_MS = 2500;
    private static final int[] ATTACK_3_SOUND_TIMINGS_MS = {400, 1220};
    private final Set<Integer> attack3PlayedFrames = new HashSet<>();

    private static final int TELEPORT_DURATION_MS = 8000;
    private static final int[] TELEPORT_SOUND_TIMINGS_MS = {0, 4300, 4900};
    private final Set<Integer> teleportPlayedFrames = new HashSet<>();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState attack2AnimationState = new AnimationState();
    public final AnimationState attack3AnimationState = new AnimationState();
    public final AnimationState teleportAnimationState = new AnimationState();

    public final AnimationState tameableAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    private double skillCooldownDecreasedBasedOnMobScale = 1;

    private int useSkillCooldownCounter = 0;

    private int attack2Range = 8;

    private int attack3Range = 17;

    private int maxScale = 8;

    private int patrolRadius = 7;
    public BlockPos patrolCenterPos = null;

    public int animationTimeout = 0;
    public static final int DEFAULT_ANIMATION_TIMEOUT = 15;

    private int healthUpdateCooldown = 100;
    private int getHealthUpdateCounter = 0;
    private double healthDecreaseWhenTameablePercent = 0.01f;
    private float healthIncreaseWhenTamed = 0.005f;

    private float percentHealthToBeTamed = 0.15f;
    private int canBeTamedChance = 70;

    private int attack2DamageMultiply = 3;
    private int attack3DamageMultiply = 4;

    private int canChangeTargetCounter = 0;
    private int canChangeTargetCooldown = 80;

    public FallenKnight(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.WALK, walkAnimationState);
        animationHashMap.put(AnimationName.ATTACK, attackAnimationState);
        animationHashMap.put(AnimationName.ATTACK2, attack2AnimationState);
        animationHashMap.put(AnimationName.ATTACK3, attack3AnimationState);
        animationHashMap.put(AnimationName.TAMEABLE, tameableAnimationState);
        animationHashMap.put(AnimationName.SIT, sitAnimationState);
        animationHashMap.put(AnimationName.MOVEMENT, teleportAnimationState);

        if (!this.getWorld().isClient && !this.getIsRandomFirstTime()) {
            double scale = WelcomeToMyWorld.random.nextDouble(1, maxScale);
            skillCooldownDecreasedBasedOnMobScale = scale;


            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) * scale);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (double) (scale / 3));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR) * (double) (scale / 5F));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * (double) (scale / 2.5F));

            if (skillCooldownDecreasedBasedOnMobScale / 3 > 1) {
                this.setAllSkillCooldown((float) (this.getAllSkillCooldown() / (skillCooldownDecreasedBasedOnMobScale / 3)));
            }

            if (this.getAllSkillCooldown() <= 80) {
                this.setAllSkillCooldown(80);
            }

            this.setHealth(this.getMaxHealth());

            this.setIsRandomFirstTime(true);
        }
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 40f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40f)
                .add(EntityAttributes.GENERIC_ARMOR, 10f)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10f)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("welcometomyworld", "entities/fallen_knight");
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(CAN_BE_TAMED, false);
        this.dataTracker.startTracking(CAN_BE_TAMED_SET, false);
        this.dataTracker.startTracking(IS_PATROLLING, false);
        this.dataTracker.startTracking(ALL_SKILL_COOLDOWN, 200f);

    }


    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void initGoals() {
        super.initGoals(); // Adds default HostileEntity goals including MeleeAttackGoal

        this.goalSelector.add(1, new StopMoveAndLookWhenCanBeTamed(this));
        this.goalSelector.add(2, new SwimGoal(this));
        this.goalSelector.add(3, new SitGoal(this));
        this.goalSelector.add(4, new PatrollingGoal(this));
        this.goalSelector.add(5, new CustomFollowOwnerGoal(this, 0.8, 15, 25, false));
        this.goalSelector.add(6, new StopWhenUsingSkill(this));
        this.goalSelector.add(7, new CustomMeleeAttackGoal(this, 27, 1.5f));
        this.goalSelector.add(8, new LargeEntityWanderGoal(this, 1.0, 1));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(11, new LookAroundGoal(this));

        this.targetSelector.add(2, new CustomTrackOwnerAttackGoal(this));
        this.targetSelector.add(3, new CustomAttackWithOwnerGoal(this));
        this.targetSelector.add(4, new CustomRevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        setAnimationStates();

//        if (this.getWorld().isClient) {
//            WelcomeToMyWorld.LOGGER.info("[CLIENT] SITTING: " + this.isInSittingPose());
//        } else {
//            WelcomeToMyWorld.LOGGER.info("[SERVER] SITTING: " + this.isInSittingPose());
//        }

        if (!this.getWorld().isClient) {
            if (this.getTarget() != null) {
                if (this.getTarget().isDead()) {
                    this.setTarget(null);
                }
            }

            if (this.getHealthUpdateCounter <= this.healthUpdateCooldown) {
                this.getHealthUpdateCounter++;
            }

            if (this.getHealthUpdateCounter > this.healthUpdateCooldown) {
                this.getHealthUpdateCounter = 0;
                if (this.getCanBeTamed()) {
                    this.damage(this.getWorld().getDamageSources().generic(), (float) (this.getMaxHealth() * healthDecreaseWhenTameablePercent));
                } else {
                    this.setHealth(this.getHealth() + Math.min(this.getMaxHealth() * this.healthIncreaseWhenTamed, this.maxRegenHealthPerUpdate));
                }
            }
        }

        if(this.canChangeTargetCounter < this.canChangeTargetCooldown){
            this.canChangeTargetCounter++;
        }

        usingSkillsHandler();
        handleAnimationSoundsAndEffect();
    }


    public void setAnimationStates() {
        if (this.getWorld().isClient) {
            if (!this.getCanBeTamed()) {
                if (this.isInSittingPose()) {
                    if (!sitAnimationState.isRunning()) {
                        startAnimation(AnimationName.SIT);
                    }
                } else {
                    Vec3d velocity = this.getVelocity();
                    boolean isMoving = velocity.x != 0 || velocity.z != 0;
                    if (animationTimeout <= 0 && !this.getIsUsingSkill()) {
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
                }


            } else if (this.getCanBeTamed() && this.getOwner() == null) {
                if (!this.tameableAnimationState.isRunning()) {
                    this.startAnimation(AnimationName.TAMEABLE);
                }
            }
        } else {
            if (!this.getCanBeTamedSet()) {
                if (this.getHealth() <= this.getMaxHealth() * percentHealthToBeTamed && (this.getOwner() == null || !this.isTamed())) {
                    if (!this.getCanBeTamed()) {
                        this.setCanBeTamed(WelcomeToMyWorld.random.nextInt(0, 100) < this.canBeTamedChance);

                        this.setCanBeTamedSet(true);

                        if (this.getCanBeTamed()) {
                            Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.FALLEN_KNIGHT_ARMOR_SHAKING, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.1f));
                            Utils.addRunAfter(() -> Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.FALLEN_KNIGHT_FALL, 0.5f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.1f)), 12);
                        }
                    }
                }
            }
        }

        if (animationTimeout >= 0) {
            animationTimeout--;
        }
    }

    private void usingSkillsHandler() {
        if (!this.getWorld().isClient && !this.getCanBeTamed() && !this.isInSittingPose()) {
            if (this.getIsUsingSkill()) {
                return;
            }

            if (this.useSkillCooldownCounter < this.getAllSkillCooldown() && !this.getIsUsingSkill()) {
                this.useSkillCooldownCounter++;
                return;
            }

            if (this.getTarget() != null) {
                double distance = this.distanceTo(this.getTarget());
                int timeout = 0;

                if (distance > 15 || (this.getTarget().getY() > this.getY() && (Math.abs(this.getTarget().getY() - this.getY()) > 4))) {
                    timeout = 160;
                    Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.MOVEMENT, timeout);
                    Utils.addRunAfter(() -> {
                        if (this.getTarget() != null) {
                            this.teleport(this.getTarget().getX(), this.getTarget().getY() + 1, this.getTarget().getZ());
                        }
                    }, 70);
                    Utils.addRunAfter(() -> {
                        createShockwave();
                    }, 100);

                    this.useSkillCooldownCounter = 0;
                    resetSkill(timeout);
                    return;
                }

                if (distance <= attack2Range) {
                    int rand = WelcomeToMyWorld.random.nextInt(0, 100);
                    if (rand < 60) {
                        timeout = 29;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK2, timeout);
                        Utils.addRunAfter(this::createDamageBox, 15);
                    } else {
                        timeout = 50;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK3, timeout);
                        Utils.addRunAfter(this::createShockwave, 30);
                    }

                    this.useSkillCooldownCounter = 0;
                    resetSkill(timeout);
                    return;
                }

                if (distance > 10) {
                    int rand = WelcomeToMyWorld.random.nextInt(0, 100);
                    if (rand < 60) {
                        timeout = 50;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK3, timeout);
                        Utils.addRunAfter(() -> {
                            createShockwave();
                        }, 30);
                    } else {
                        timeout = 160;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.MOVEMENT, timeout);
                        Utils.addRunAfter(() -> {
                            if (this.getTarget() != null) {
                                this.teleport(this.getTarget().getX(), this.getTarget().getY() + 1, this.getTarget().getZ());
                            }
                        }, 70);
                        Utils.addRunAfter(() -> {
                            createShockwave();
                        }, 100);
                    }

                    this.useSkillCooldownCounter = 0;
                    resetSkill(timeout);
                }
            }
        }
    }

    private void resetSkill(int timeout) {
        this.setIsUsingSkill(true);
        Utils.addRunAfter(() -> {
            this.setIsUsingSkill(false);
        }, timeout);
    }


    private void handleAnimationSoundsAndEffect() {
        if (!this.getWorld().isClient()) return;

        if (walkAnimationState.isRunning()) {
            handleWalkSounds();
        }

        if (attack2AnimationState.isRunning()) {
            handleAttack2Sounds();
        }

        if (attack3AnimationState.isRunning()) {
            handleAttack3Sounds();
        }

        if (teleportAnimationState.isRunning()) {
            handleTeleportSounds();
        }
    }

    private void handleWalkSounds() {
        if (!this.getWorld().isClient()) return;

        long animTime = walkAnimationState.getTimeRunning();
        int currentPos = (int) (animTime % WALK_CYCLE_DURATION_MS);

        for (int timing : FOOTSTEP_TIMINGS_MS) {
            int timingInCycle = timing % WALK_CYCLE_DURATION_MS;

            if (previousWalkPosition != -1) {
                boolean normalCross = previousWalkPosition < timingInCycle && currentPos >= timingInCycle;
                boolean wrapAround = timingInCycle == 0 && previousWalkPosition > currentPos;


                if (normalCross || wrapAround) {
//                    Utils.sendSoundPacket(SoundsManager.FALLEN_KNIGHT_STEP, this.getBlockPos());
                    //        Utils.sendSoundPacket(soundId, this.getBlockPos());
                    Utils.playClientSound(this.getBlockPos(), SoundsManager.FALLEN_KNIGHT_STEP, 20);
                }
            }
        }
        previousWalkPosition = currentPos;
    }

    private void handleAttack2Sounds() {
        if (!this.getWorld().isClient()) return;

        long animTime = attack2AnimationState.getTimeRunning();
        int currentPos = (int) (animTime % ATTACK_2_DURATION_MS);

        for (int timing : ATTACK_2_SOUND_TIMINGS_MS) {
            int windowStart = timing - 25;
            int windowEnd = timing + 25;

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;
            boolean cycleWrap = timing > ATTACK_2_DURATION_MS - 25 &&
                    currentPos < timing - ATTACK_2_DURATION_MS + 25;

            if ((inWindow || cycleWrap) && !attack2PlayedFrames.contains(timing)) {
                triggerAttack2SoundSound(timing);
                attack2PlayedFrames.add(timing);
            }
        }

        if (currentPos > ATTACK_2_DURATION_MS - 200) {
            attack2PlayedFrames.clear();
        }
    }

    private void triggerAttack2SoundSound(int timing) {
        SoundEvent soundId = null;

        if (timing >= 670) {
            soundId = SoundsManager.FALLEN_KNIGHT_SWING;
        }

        if (soundId == null) return;

        Utils.sendSoundPacketFromClient(soundId, this.getBlockPos());
    }

    private void handleAttack3Sounds() {
        if (!this.getWorld().isClient()) return;

        long animTime = attack3AnimationState.getTimeRunning();
        int currentPos = (int) (animTime % ATTACK_3_DURATION_MS);

        for (int timing : ATTACK_3_SOUND_TIMINGS_MS) {
            int windowStart = timing - 25;
            int windowEnd = timing + 25;

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;
            boolean cycleWrap = timing > ATTACK_3_DURATION_MS - 25 &&
                    currentPos < timing - ATTACK_3_DURATION_MS + 25;

            if ((inWindow || cycleWrap) && !attack3PlayedFrames.contains(timing)) {
                triggerAttack3SoundSound(timing);
                attack3PlayedFrames.add(timing);
            }
        }

        if (currentPos > ATTACK_3_DURATION_MS - 150) {
            attack3PlayedFrames.clear();
        }
    }

    private void triggerAttack3SoundSound(int timing) {
        SoundEvent soundId = null;

        if (timing >= 400 && timing <= 500) {
            soundId = SoundsManager.FALLEN_KNIGHT_SWING_UP;
        } else if (timing >= 1220 && timing <= 1350) {
            soundId = SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT;
        }

        if (soundId == null) return;

//        Utils.sendSoundPacket(soundId, getBlockPos());
        Utils.playClientSound(this.getBlockPos(), soundId, 30);
    }

    private void handleTeleportSounds() {
        if (!this.getWorld().isClient()) return;

        long animTime = teleportAnimationState.getTimeRunning();
        int currentPos = (int) animTime; // Remove modulo

        for (int timing : TELEPORT_SOUND_TIMINGS_MS) {
            int windowStart = timing - 50; // Increased window to 50 ms
            int windowEnd = timing + 50;

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;

            // Remove cycleWrap check as animation isn't looping
            if (inWindow && !teleportPlayedFrames.contains(timing)) {
                triggerTeleportSound(timing);
                teleportPlayedFrames.add(timing);
            }
        }

        // Clear after animation ends (currentPos exceeds duration)
        if (currentPos > TELEPORT_DURATION_MS) {
            teleportPlayedFrames.clear();
        }
    }

    private void triggerTeleportSound(int timing) {
        SoundEvent soundId = null;

        if (timing >= 0 && timing <= 150) {
            soundId = SoundsManager.FALLEN_KNIGHT_PORTAL_AMBIENT;
        } else if (timing >= 4250 && timing <= 4400) {
            soundId = SoundsManager.FALLEN_KNIGHT_PORTAL_OPEN;
        } else if (timing >= 4850 && timing <= 5200) {
            soundId = SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT;
        }

        if (soundId == null) return;
        Utils.playClientSound(this.getBlockPos(), soundId, 30);
//        Utils.sendSoundPacket(soundId, this.getBlockPos());


    }

    private void createShockwave() {
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            BlockPos center = this.getBlockPos();

            Box checkArea = new Box(this.getBlockPos()).expand(attack3Range);
            List<LivingEntity> damageTarget = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

            // Set to track blocks where particles have been spawned
            Set<BlockPos> particleSpawnedBlocks = new HashSet<>();

            for (int x = -attack3Range; x <= attack3Range; x++) {
                for (int z = -attack3Range; z <= attack3Range; z++) {
                    if (x * x + z * z <= attack3Range * attack3Range) {
                        for (int yOffset = 0; yOffset >= -1; yOffset--) {
                            BlockPos targetPos = center.add(x, yOffset, z);
                            BlockState state = serverWorld.getBlockState(targetPos);

                            BlockPos blockAbove = targetPos.up();
                            if (yOffset < 0 && particleSpawnedBlocks.contains(blockAbove)) {
                                continue;
                            }

                            if (!state.isAir()) {
                                spawnParticles(serverWorld, targetPos, state);
                                particleSpawnedBlocks.add(targetPos); // Mark this block as having particles spawned
                            }
                        }
                    }
                }
            }


            for (LivingEntity target : damageTarget) {
                if (target == this) continue;
                if (this.getOwner() != null) {
                    if (target == this.getOwner()) continue;
                }
                if (target instanceof TameableEntity tameable) {
                    if (tameable.isTamed() && tameable.getOwner() != null) {
                        if (tameable.getOwner() == this.getOwner()) continue;
                    }
                }

                if(target instanceof PlayerEntity){
                    if(this.isTamed() || this.getOwner() != null){
                        continue;
                    }
                }

                // Handle FallenKnight-specific logic
                if (target instanceof FallenKnight knight) {
                    // Skip untamed vs. untamed damage
                    if (!this.isTamed() && !knight.isTamed()) {
                        continue;
                    }

                    // Skip tamed vs. tamed damage if they have the same owner
                    if (this.isTamed() && this.getOwner() != null && knight.isTamed() && knight.getOwner() != null) {
                        if (this.getOwner().equals(knight.getOwner())) {
                            continue;
                        }
                    }
                }

                float damage = (float) this.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * this.attack3DamageMultiply;
                damageBlockingShield(target, damage);
                if(target instanceof PlayerEntity){
                    target.addVelocity(0, 1.4f, 0);
                } else {
                    target.addVelocity(0, 0.8f, 0);
                }

                target.damage(this.getWorld().getDamageSources().mobAttack(this), damage);
            }
        }
    }

    private void createDamageBox() {
        if (!this.getWorld().isClient) {
            Box checkArea = new Box(this.getBlockPos()).expand(attack2Range);
            List<LivingEntity> targetDamage = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

            for (LivingEntity target : targetDamage) {
                if (target == this) continue;
                if (this.getOwner() != null) {
                    if (target == this.getOwner()) continue;
                }
                if (target instanceof TameableEntity tameable) {
                    if (tameable.isTamed() && tameable.getOwner() != null) {
                        if (tameable.getOwner() == this.getOwner()) continue;
                    }
                }

                // Handle FallenKnight-specific logic
                if (target instanceof FallenKnight knight) {
                    // Skip untamed vs. untamed damage
                    if (!this.isTamed() && !knight.isTamed()) {
                        continue;
                    }

                    // Skip tamed vs. tamed damage if they have the same owner
                    if (this.isTamed() && this.getOwner() != null && knight.isTamed() && knight.getOwner() != null) {
                        if (this.getOwner().equals(knight.getOwner())) {
                            continue;
                        }
                    }
                }

                Vec3d knockbackDirection = new Vec3d(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ()).normalize();
                target.addVelocity(knockbackDirection.multiply(2, 0, 2));

                float damage = (float) this.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * this.attack2DamageMultiply;
                damageBlockingShield(target, damage);
                target.damage(this.getWorld().getDamageSources().mobAttack(this), damage);
            }
        }
    }

    private void damageBlockingShield(LivingEntity target, float damage) {

        if (this.getTarget() == null) return;
        if (target.isBlocking() && target.getActiveItem().isDamageable()) {
            target.getActiveItem().damage((int) damage, target,
                    entity -> entity.sendToolBreakStatus(target.getActiveHand()));
        }
    }

    public static void spawnParticles(ServerWorld world, BlockPos pos, BlockState state) {
        ParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        world.spawnParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.5, 0.2, 0.1);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        World world = this.getWorld();

        // Client-side checks
        if (world.isClient) {
            if (this.getCanBeTamed() && itemStack.isOf(this.getTameFood()) && (this.getOwner() == null || !this.isTamed())) {
                return ActionResult.CONSUME;
            }

            if (this.isTamed() && this.getOwner() == player) {
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }


        // Handle sitting/unsitting when tamed and interacted by the owner
        if (this.isTamed() && this.getOwner() == player) {
            this.setSitting(!this.isSitting());
            this.setIsPatrolling(false);
            this.targetSelector.remove(new ActiveTargetGoal<>(this, HostileEntity.class, true));
            this.setTarget(null);

            return ActionResult.SUCCESS;
        }

        // Taming logic
        if (this.getCanBeTamed() && itemStack.isOf(this.getTameFood())) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            if (this.random.nextInt(2) == 0) {
                this.setOwner(player);
                this.setTamed(true);
                this.setSitting(false);
                this.animationTimeout = 1;
                this.setTarget(null);
                this.setCanBeTamed(false);

                Utils.grantAdvancement((ServerPlayerEntity) player, "tameable/you_are_worthy");

                this.setHealth(this.getMaxHealth() / 2);

                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private Item getTameFood() {
        return Items.SOUL_CAMPFIRE;
    }

    @Override
    public void setMovementSpeed(float speed) {
        super.setMovementSpeed(speed); // Don't limit speed
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
            // Clear played frames when starting the teleport animation
            if (na == AnimationName.MOVEMENT) {
                teleportPlayedFrames.clear();
            }
        }
    }

    public void stopAllAnimation() {
        AnimationName na = null;
        for (AnimationName n : animationHashMap.keySet()) {
            animationHashMap.get(n).stop();
        }

    }

    // Store the goal instance
    private ActiveTargetGoal<HostileEntity> hostileTargetGoal;

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isDead()) return false;

        if (!this.getWorld().isClient) {
            if(source.getSource() instanceof ArrowEntity){
                amount /= 2;
            }

            if (source.getAttacker() instanceof PlayerEntity player && player == this.getOwner() && player.isSneaking()) {
                this.patrolCenterPos = this.getBlockPos();
                this.setIsPatrolling(!this.getIsPatrolling());

                if (this.getIsPatrolling()) {
                    // Create and add the goal if it doesn't exist
                    if (this.hostileTargetGoal == null) {
                        this.hostileTargetGoal = new ActiveTargetGoal<>(this, HostileEntity.class, true);
                        this.targetSelector.add(1, this.hostileTargetGoal);
                    }
                } else {
                    // Remove the goal if it exists
                    if (this.hostileTargetGoal != null) {
                        this.targetSelector.remove(this.hostileTargetGoal);
                        this.hostileTargetGoal = null; // Clear the reference
                    }
                    this.patrolCenterPos = null;
                }

                return super.damage(source, 0);
            }

            if (!this.getCanBeTamed() && source.getAttacker() != this.getOwner()) {
                if (source.getAttacker() instanceof PlayerEntity player) {
                    if (player.isCreative() || player.isSpectator()) return super.damage(source, amount);
                }
            }
        }

        return super.damage(source, amount);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    public void onAttacking(Entity target) {
        super.onAttacking(target);
        if (!this.getWorld().isClient) {
            Vec3d knockbackDirection = new Vec3d(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ()).normalize();

            if(target instanceof PlayerEntity){
                target.addVelocity(knockbackDirection.multiply(2, 0, 2));
            } else {
                target.addVelocity(knockbackDirection);
            }

            // Send animation packet to all nearby players
            for (ServerPlayerEntity player : ((ServerWorld) this.getWorld()).getPlayers()) {
                if (player.canSee(this)) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(this.getId());
                    buf.writeEnumConstant(AnimationName.ATTACK);
                    buf.writeInt(15); // Animation timeout
                    ServerPlayNetworking.send(player, ANIMATION_PACKET, buf);
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
    }

    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    public boolean getIsUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    public void setIsUsingSkill(boolean variant) {
        this.dataTracker.set(IS_USING_SKILL, variant);
    }

    public boolean getCanBeTamed() {
        return this.dataTracker.get(CAN_BE_TAMED);
    }

    public void setCanBeTamed(boolean variant) {
        this.dataTracker.set(CAN_BE_TAMED, variant);
    }

    public boolean getIsPatrolling() {
        return this.dataTracker.get(IS_PATROLLING);
    }

    public void setIsPatrolling(boolean variant) {
        this.dataTracker.set(IS_PATROLLING, variant);
    }

    public float getAllSkillCooldown() {
        return this.dataTracker.get(ALL_SKILL_COOLDOWN);
    }

    public void setAllSkillCooldown(float variant) {
        this.dataTracker.set(ALL_SKILL_COOLDOWN, variant);
    }

    public boolean getCanBeTamedSet() {
        return this.dataTracker.get(CAN_BE_TAMED_SET);
    }

    public void setCanBeTamedSet(boolean variant) {
        this.dataTracker.set(CAN_BE_TAMED_SET, variant);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("canBeTamed", this.getCanBeTamed());
        nbt.putBoolean("canBeTamedSet", this.getCanBeTamedSet());
        nbt.putFloat("allSkillCooldown", this.getAllSkillCooldown());
        nbt.putBoolean("isRandomFirstTime", this.getIsRandomFirstTime());

        if (this.patrolCenterPos != null) {
            NbtList homePos = new NbtList();
            homePos.add(NbtDouble.of(this.getBlockX()));
            homePos.add(NbtDouble.of(this.getBlockY()));
            homePos.add(NbtDouble.of(this.getBlockZ()));
            nbt.put("patrolPos", homePos);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setCanBeTamed(nbt.getBoolean("canBeTamed"));
        this.setCanBeTamedSet(nbt.getBoolean("canBeTamedSet"));
        this.setIsRandomFirstTime(nbt.getBoolean("isRandomFirstTime"));

        if (nbt.getFloat("allSkillCooldown") > 0) {
            this.setAllSkillCooldown(nbt.getFloat("allSkillCooldown"));
        }


        if (nbt.contains("patrolPos", NbtElement.LIST_TYPE)) {
            NbtList homePos = nbt.getList("patrolPos", NbtElement.DOUBLE_TYPE);
            this.patrolCenterPos = new BlockPos((int) homePos.getDouble(0), (int) homePos.getDouble(1), (int) homePos.getDouble(2));
        } else {
            this.patrolCenterPos = null;
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
            this.mob.getNavigation().startMovingTo(this.targetX, this.targetY + 0.15, this.targetZ, this.speed);
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }
    }

    public class StopWhenUsingSkill extends Goal {
        private final FallenKnight mob;

        public StopWhenUsingSkill(FallenKnight mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getIsUsingSkill();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();  // Stop navigation when sleeping
        }

        @Override
        public boolean shouldContinue() {
            return mob.getIsUsingSkill();
        }

        @Override
        public void tick() {
            if (!mob.getIsUsingSkill()) {
                return; // If the mob stops sleeping, continue normal movement
            }
            mob.getNavigation().stop(); // Continue to stop movement while sleeping
        }
    }

    public class StopMoveAndLookWhenCanBeTamed extends Goal {
        private final FallenKnight mob;

        public StopMoveAndLookWhenCanBeTamed(FallenKnight mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getCanBeTamed();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();  // Stop navigation when sleeping
        }

        @Override
        public boolean shouldContinue() {
            return mob.getCanBeTamed();
        }

        @Override
        public void tick() {
            mob.getNavigation().stop(); // Continue to stop movement while sleeping
        }
    }

    public class CustomMeleeAttackGoal extends Goal {
        private final int attackCooldown;
        private int counter = 0;
        private float speed = 1;
        private FallenKnight mob;

        public CustomMeleeAttackGoal(FallenKnight mob, int attackCooldown, float speed) {
            this.attackCooldown = attackCooldown;
            this.counter = attackCooldown;
            this.mob = mob;
            this.speed = speed;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.mob.getTarget() != null;
        }

        @Override
        public boolean shouldContinue() {
            return this.mob.getTarget() != null;
        }

        @Override
        public void stop() {
            this.mob.setAttacking(false);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            super.tick();

            if (counter < attackCooldown) {
                counter++;
                return;
            }

            LivingEntity target = this.mob.getTarget();
            if (target == null) return;

            this.mob.getNavigation().startMovingTo(target, speed);
            this.mob.getLookControl().lookAt(target);

            if (this.mob.distanceTo(target) < 4.5f) {
                counter = 0;
                this.mob.setAttacking(true);
                this.mob.tryAttack(target);
            }
        }
    }

    public class PatrollingGoal extends Goal {
        private final FallenKnight mob;
        private final double maxTeleportDistance = 20.0; // Teleport if beyond this
        private int cooldown = 0;
        // Add a max chase distance field
        private final double maxChaseDistance = 14.0; // 2 * patrolRadius (7 * 2)

        public PatrollingGoal(FallenKnight mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getIsPatrolling() && mob.patrolCenterPos != null;
        }

        @Override
        public boolean shouldContinue() {
            return mob.getIsPatrolling() && mob.patrolCenterPos != null;
        }

        @Override
        public void tick() {
            if (cooldown > 0) {
                cooldown--;
                return;
            }

            Vec3d patrolPos = mob.patrolCenterPos.toCenterPos();
            double distance = mob.getPos().distanceTo(patrolPos);

            if (distance > maxTeleportDistance) {
                if (mob.teleport(patrolPos.x, patrolPos.y, patrolPos.z, true)) {
                    cooldown = 20; // Reduced from 100 to 20 ticks
                    mob.setTarget(null); // Clear target after teleport
                }
                return;
            } else if (distance > maxChaseDistance) {
                mob.setTarget(null); // Clear target if too far
                moveToPatrolPos();
            } else if (distance > mob.patrolRadius) {
                moveToPatrolPos();
            } else if (this.mob.getTarget() == null) {
                wanderAroundPatrolPos();
            }
        }

        private void moveToPatrolPos() {
            mob.getNavigation().startMovingTo(
                    mob.patrolCenterPos.getX(),
                    mob.patrolCenterPos.getY(),
                    mob.patrolCenterPos.getZ(),
                    1.2 // Faster speed when returning
            );
        }

        private void wanderAroundPatrolPos() {
            if (mob.getNavigation().isIdle()) { // Only find new target when stopped
                Vec3d randomTarget = findRandomTargetBlock();
                mob.getNavigation().startMovingTo(
                        randomTarget.x,
                        randomTarget.y,
                        randomTarget.z,
                        0.8 // Normal patrol speed
                );
            }
        }

        private Vec3d findRandomTargetBlock() {
            Random random = mob.getRandom();
            return new Vec3d(
                    mob.patrolCenterPos.getX() + (random.nextDouble() - 0.5) * 10,
                    mob.patrolCenterPos.getY(),
                    mob.patrolCenterPos.getZ() + (random.nextDouble() - 0.5) * 10
            );
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            cooldown = 0;
        }
    }

    public class CustomFollowOwnerGoal extends Goal {
        private final FallenKnight mob;
        private LivingEntity owner;
        private final World world;
        private final double speed;
        private final float minFollowDistance;
        private final float maxFollowDistance;
        private final boolean leavesAllowed;
        private int teleportCooldown;
        private int pathUpdateCooldown;

        public CustomFollowOwnerGoal(FallenKnight mob, double speed,
                                     float minDistance, float maxDistance,
                                     boolean leavesAllowed) {
            this.mob = mob;
            this.world = mob.getWorld();
            this.speed = speed;
            this.minFollowDistance = minDistance;
            this.maxFollowDistance = maxDistance;
            this.leavesAllowed = leavesAllowed;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (mob.getIsPatrolling() || mob.getTarget() != null) return false;

            LivingEntity owner = mob.getOwner();
            if (owner == null) return false;

            double distanceSq = mob.squaredDistanceTo(owner);
            return distanceSq > (maxFollowDistance * maxFollowDistance);
        }

        @Override
        public boolean shouldContinue() {
            if (mob.getIsPatrolling() || mob.getTarget() != null) return false;

            LivingEntity owner = mob.getOwner();
            if (owner == null) return false;

            double distanceSq = mob.squaredDistanceTo(owner);
            return distanceSq > (minFollowDistance * minFollowDistance);
        }

        @Override
        public void start() {
            this.teleportCooldown = 0;
            this.pathUpdateCooldown = 0;
            this.owner = mob.getOwner();
        }

        @Override
        public void tick() {
            mob.getLookControl().lookAt(owner, 10.0F, mob.getMaxLookPitchChange());
            double distance = mob.distanceTo(owner);

            handleTeleportation(distance);
            handlePathfinding(distance);
        }

        private void handleTeleportation(double distance) {
            if (teleportCooldown > 0) teleportCooldown--;

            if (distance > maxFollowDistance && teleportCooldown <= 0) {
                if (tryTeleportToOwner()) {
                    teleportCooldown = 40; // 2 second cooldown
                    pathUpdateCooldown = 0;
                }
            }
        }

        private void handlePathfinding(double distance) {
            if (pathUpdateCooldown > 0) {
                pathUpdateCooldown--;
                return;
            }

            if (distance > minFollowDistance) {
                pathUpdateCooldown = 10; // Update path every 0.5 seconds
                mob.getNavigation().startMovingTo(owner, speed);
            } else {
                mob.getNavigation().stop();
            }
        }

        private boolean tryTeleportToOwner() {
            if (!leavesAllowed && !world.getBlockState(owner.getBlockPos()).isAir()) return false;

            return mob.teleport(
                    owner.getX(),
                    owner.getY(),
                    owner.getZ(),
                    true
            );
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            this.owner = null;
            this.teleportCooldown = 0;
            this.pathUpdateCooldown = 0;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true; // Important for smooth updates
        }
    }

    class CustomTrackOwnerAttackGoal extends TrackTargetGoal {
        private final FallenKnight mob;
        private LivingEntity attacker;
        private int lastAttackedTime;

        public CustomTrackOwnerAttackGoal(FallenKnight mob) {
            super(mob, false);
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (this.mob.isTamed() && !this.mob.isSitting() && !this.mob.getIsPatrolling()) {
                LivingEntity livingEntity = this.mob.getOwner();
                if (livingEntity == null) {
                    return false;
                } else {
                    this.attacker = livingEntity.getAttacker();
                    int i = livingEntity.getLastAttackedTime();
                    return i != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT) && this.mob.canAttackWithOwner(this.attacker, livingEntity);
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            LivingEntity livingEntity = this.mob.getOwner();
            if (livingEntity != null) {
                this.lastAttackedTime = livingEntity.getLastAttackedTime();
            }

            super.start();
        }
    }

    public class CustomAttackWithOwnerGoal extends TrackTargetGoal {
        private final FallenKnight tameable;
        private LivingEntity attacking;
        private int lastAttackTime;

        public CustomAttackWithOwnerGoal(FallenKnight tameable) {
            super(tameable, false);
            this.tameable = tameable;
            this.setControls(EnumSet.of(Goal.Control.TARGET));
        }

        @Override
        public boolean canStart() {
            if (this.tameable.isTamed() && !this.tameable.isSitting() && !this.tameable.getIsPatrolling()) {
                LivingEntity livingEntity = this.tameable.getOwner();
                if (livingEntity == null) {
                    return false;
                } else {
                    this.attacking = livingEntity.getAttacking();
                    int i = livingEntity.getLastAttackTime();
                    if(this.attacking instanceof TameableEntity target){
                        if(target.getOwner() != null){
                           if(target.getOwner() == this.tameable.getOwner()){
                               return false;
                           }
                        }
                    }
                    return i != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT) && this.tameable.canAttackWithOwner(this.attacking, livingEntity);
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacking);
            LivingEntity livingEntity = this.tameable.getOwner();
            if (livingEntity != null) {
                this.lastAttackTime = livingEntity.getLastAttackTime();
            }

            super.start();
        }
    }
}