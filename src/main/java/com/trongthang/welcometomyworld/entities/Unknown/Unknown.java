package com.trongthang.welcometomyworld.entities.Unknown;

import net.minecraft.entity.ai.goal.Goal;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.Vec3d;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import com.trongthang.welcometomyworld.WelcomeToMyWorld; // Added for logger

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ItemStackParticleEffect;

public class Unknown extends HostileEntity implements GeoEntity {

    // 0 = NONE, 1 = LEFT, 2 = RIGHT — synced to client every tick via DataTracker
    private static final TrackedData<Integer> DASH_DIR = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.INTEGER);

    // Skills
    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> IS_TAUNTING = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    private static final int SKILL_PUNCH = 1;
    private static final int SKILL_SLAM = 2;
    private static final int SKILL_DASH_FORWARD = 3;
    private static final int SKILL_LEG_TRIP = 4;
    private static final int SKILL_GROUND_SLAM_KICK = 5;
    private static final int SKILL_JUMP = 6;
    private static final int SKILL_SLAM_GROUND_AFTER_JUMP = 7;
    private static final int SKILL_STEAL_ITEM = 8;
    private static final int SKILL_USE_ITEM = 9;
    private static final int SKILL_DESTROY_ITEM = 10;
    private static final int SKILL_PREPARE_STEAL = 11;

    private static final int SLAM_HIT_TICK = 15; // 0.75s × 20 TPS
    private static final int PUNCH_HIT_TICK = 20; // 1s × 20 TPS
    private static final int LEG_TRIP_HIT_TICK = 10;
    private static final int GROUND_SLAM_KICK_HIT_TICK = 20;
    private static final int SLAM_AFTER_JUMP_HIT_TICK = 3; // Change if slam animation impact deviates
    private static final int STEAL_HIT_TICK = 8;
    private static final int USE_HIT_TICK = 43;
    private static final int[] DESTROY_HIT_TICKS = { 15, 38, 58 };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int dashTimer = 0;
    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int skillCooldown = 0;
    private int tauntCooldown = 0;
    private int dodgeCooldown = 0;
    private ItemStack stolenItemCandidate = ItemStack.EMPTY;

    public Unknown(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DASH_DIR, 0);
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(IS_TAUNTING, false);
    }

    @Override
    protected void initGoals() {
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);

        // --- Attack goals ---
        this.goalSelector.add(1, new StopMoveWhenUsingSkill(this));
        this.goalSelector.add(2, new ChaseTargetGoal(this, 1.2D));
        this.goalSelector.add(4, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(5, new LookAroundGoal(this));

        // --- Target goals ---
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, false));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world,
            net.minecraft.world.LocalDifficulty difficulty, net.minecraft.entity.SpawnReason spawnReason,
            @org.jetbrains.annotations.Nullable net.minecraft.entity.EntityData entityData,
            @org.jetbrains.annotations.Nullable net.minecraft.nbt.NbtCompound entityNbt) {

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        // Edge-detection flag
        int[] prevSkillId = { 0 };
        int[] prevDir = { 0 };

        // --- All animations in one controller for smooth cross-fading ---
        controllers.add(new AnimationController<>(this, "mainController", 5, state -> {
            int dir = this.dataTracker.get(DASH_DIR);
            boolean isUsingSkill = this.dataTracker.get(IS_USING_SKILL);
            int skillId = this.dataTracker.get(SKILL_ID);

            if (isUsingSkill) {
                state.getController().transitionLength(0);
                if (prevSkillId[0] != skillId) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;

                switch (skillId) {
                    case SKILL_PUNCH:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_saitama_punch"));
                    case SKILL_SLAM:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_slam_ground_kick"));
                    case SKILL_DASH_FORWARD:
                        // Placeholder if not exists, user said "unknown_dash_forward"
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_dash_forward"));
                    case SKILL_LEG_TRIP:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_leg_trip"));
                    case SKILL_GROUND_SLAM_KICK:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_slam_ground_kick"));
                    case SKILL_JUMP:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_jump"));
                    case SKILL_SLAM_GROUND_AFTER_JUMP:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_slam_ground_after_jump"));
                    case SKILL_STEAL_ITEM:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_steal_item"));
                    case SKILL_USE_ITEM:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_use_the_item"));
                    case SKILL_DESTROY_ITEM:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_destroy_the_item"));
                    case SKILL_PREPARE_STEAL:
                        return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_walking"));
                }
            }
            prevSkillId[0] = 0;

            // --- Dash takes priority over walking/idle ---
            if (dir != 0) {
                state.getController().transitionLength(0);
                if (prevDir[0] == 0)
                    state.getController().forceAnimationReset();
                prevDir[0] = dir;
                String anim = dir == 1 ? "unknown_dashing_left" : "unknown_dashing_right";
                return state.setAndContinue(RawAnimation.begin().thenPlay(anim));
            }

            // --- Normal movement ---
            state.getController().transitionLength(5);
            prevDir[0] = 0;
            boolean isMoving = this.getVelocity().x != 0 || this.getVelocity().z != 0; // best moving check for even
                                                                                       // slow movement
            if (isMoving) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_walking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_idle_ground"));
        })
                // Client-side: fire particles/sound at exactly 0.75s keyframe
                .setCustomInstructionKeyframeHandler(event -> {
                    if ("slam_aoe".equals(event.getKeyframeData().getInstructions())) {
                        // TODO: spawn ground-slam particles / play sound here (client only)
                    }
                }));

        controllers.add(new AnimationController<>(this, "tauntController", 5, state -> {
            if (this.dataTracker.get(IS_TAUNTING) && !this.dataTracker.get(IS_USING_SKILL)) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_taunt"));
            }
            return state.setAndContinue(RawAnimation.begin()); // Empty animation or stop
        }));
    }

    private void skillsHandler() {
        if (skillCooldown > 0) {
            skillCooldown--;
        }

        if (isUsingSkill()) {
            skillTick++;
            int skillId = this.dataTracker.get(SKILL_ID);

            // Face the target while using a skill
            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                this.getLookControl().lookAt(target, 30.0F, 30.0F);

                // Force rotation to target
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                this.setYaw(targetYaw);
                this.bodyYaw = targetYaw;
                this.headYaw = targetYaw;
            }

            // Handle skill effects (damage, etc.)
            handleSkillEffects(skillId);

            // Handle skill completion and chaining
            if (skillTick >= skillTotalTicks) {
                handleSkillCompletion(skillId);
            }
            return;
        }

        // --- Trigger logic ---
        if (skillCooldown <= 0) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double distSq = this.squaredDistanceTo(target);

                // Case 1: Close range (Slam or Steal)
                if (distSq <= 7.0 * 7.0) {
                    if (target instanceof PlayerEntity player && target.isUsingItem()) {
                        // Apply Slowness 255 for 10s (200 ticks)
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 254));
                        stolenItemCandidate = player.getMainHandStack().copy();
                        triggerSkill(SKILL_PREPARE_STEAL, 100);
                        skillCooldown = 200;
                    } else if (distSq <= 6.0 * 6.0) {
                        if (Math.random() < 0.5) {
                            triggerSkill(SKILL_PUNCH, 40);
                            skillCooldown = 80;
                        } else {
                            triggerSkill(SKILL_SLAM, 40);
                            skillCooldown = 120;
                        }
                    }
                }
                // Case 2: Mid range (Chain Skill)
                else if (distSq <= 15.0 * 15.0) {
                    triggerSkill(SKILL_DASH_FORWARD, 15);
                    skillCooldown = 150;
                }
                // Case 3: Far range (Punch or Jump)
                else {
                    if (distSq > 25.0 * 25.0) {
                        if (Math.random() < 0.5) {
                            triggerSkill(SKILL_JUMP, 20);
                            skillCooldown = 150;
                        } else {
                            triggerSkill(SKILL_PUNCH, 40);
                            skillCooldown = 120;
                        }
                    } else {
                        triggerSkill(SKILL_JUMP, 20);
                        skillCooldown = 150;
                    }
                }
            }
        }

        // --- Taunt logic ---
        if (skillCooldown > 0 && tauntCooldown <= 0 && !isUsingSkill()) {
            tauntCooldown = 200;
            LivingEntity tauntTarget = this.getTarget();
            if (tauntTarget != null && tauntTarget.isAlive()) {
                double distSq = this.squaredDistanceTo(tauntTarget);
                if (distSq > 10.0 * 10.0) {
                    this.dataTracker.set(IS_TAUNTING, true);
                    this.getLookControl().lookAt(tauntTarget, 30.0F, 30.0F);
                } else {
                    this.dataTracker.set(IS_TAUNTING, false);
                }
            } else {
                this.dataTracker.set(IS_TAUNTING, false);
            }
        }
    }

    /** Called by UnknownDodgeGoal on the server. */
    public void triggerDash(boolean left, int durationTicks) {
        this.dashTimer = durationTicks;
        this.dataTracker.set(DASH_DIR, left ? 1 : 2);
    }

    /**
     * Starts the slam animation and schedules the AoE hit at 0.75s.
     * 
     * @param totalTicks total animation length in ticks (e.g. 40 for a 2s anim)
     */
    public void triggerSkill(int skillId, int durationTicks) {
        if (this.getWorld().isClient())
            return;
        this.dataTracker.set(IS_USING_SKILL, true);
        this.dataTracker.set(SKILL_ID, skillId);
        this.skillTick = 0;
        this.skillTotalTicks = durationTicks;
        this.skillHitFired = false;

        // Stop movement during skill, except for PREPARE_STEAL
        if (skillId != SKILL_PREPARE_STEAL) {
            this.getNavigation().stop();
        }
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    private void handleSkillEffects(int skillId) {
        switch (skillId) {
            case SKILL_SLAM:
                if (skillTick == SLAM_HIT_TICK - 2) { // Play slightly before impact for better sync
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT, this.getSoundCategory(), 1.0F, 1.0F);
                }
                if (!skillHitFired && skillTick >= SLAM_HIT_TICK) {

                    skillHitFired = true;
                    dealAoeGroundDamage(6.0, (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
                            true);
                }

                break;
            case SKILL_PUNCH:
                if (!skillHitFired && skillTick >= PUNCH_HIT_TICK) {
                    skillHitFired = true;
                    dealLinearShockwaveDamage(3.0, 40.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f);
                }
                break;
            case SKILL_DASH_FORWARD:
                if (skillTick == 5) {
                    LivingEntity target = this.getTarget();
                    if (target != null) {
                        // Teleport to target with offset in front
                        Vec3d look = target.getRotationVec(1.0F);
                        double distance = 1.5;
                        double tx = target.getX() + look.x * distance;
                        double ty = target.getY();
                        double tz = target.getZ() + look.z * distance;

                        // Calculate yaw to face target from new position
                        double dx = target.getX() - tx;
                        double dz = target.getZ() - tz;
                        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

                        // Effects at old position
                        if (this.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1, this.getZ(),
                                    10, 0.5, 1.0, 0.5, 0.1);
                        }

                        this.refreshPositionAndAngles(tx, ty, tz, targetYaw, 0);
                        this.setPosition(tx, ty, tz);

                        // Effects at new position
                        if (this.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, tx, ty + 1, tz, 15, 0.5, 1.0, 0.5,
                                    0.05);
                        }
                    }
                }
                break;
            case SKILL_JUMP:
                if (skillTick == 1) {
                    double vx = 0, vz = 0;
                    LivingEntity target = this.getTarget();
                    if (target != null) {
                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist > 0) {
                            vx = (dx / dist) * 1.5;
                            vz = (dz / dist) * 1.5;
                        }
                    }
                    this.setVelocity(vx, 1.5, vz);
                    this.velocityModified = true;
                } else if (skillTick > 5) {
                    double vx = this.getVelocity().x;
                    double vz = this.getVelocity().z;
                    LivingEntity target = this.getTarget();
                    if (target != null) {
                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        if (dist > 0) {
                            vx = (dx / dist) * 0.3;
                            vz = (dz / dist) * 0.3;
                        }
                    }
                    // Float in the sky by combating gravity and move towards opponent
                    this.setVelocity(vx, 0.05, vz);
                    this.velocityModified = true;
                }
                break;
            case SKILL_LEG_TRIP:
                if (!skillHitFired && skillTick >= LEG_TRIP_HIT_TICK) {
                    skillHitFired = true;
                    List<LivingEntity> entities = dealAoeGroundDamage(4.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.7f, false);
                    for (LivingEntity entity : entities) {
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 255));
                    }

                }
                break;
            case SKILL_GROUND_SLAM_KICK:
                if (skillTick == GROUND_SLAM_KICK_HIT_TICK - 2) { // Play slightly before impact
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT, this.getSoundCategory(), 1.0F, 1.0F);
                }
                if (!skillHitFired && skillTick >= GROUND_SLAM_KICK_HIT_TICK) {

                    skillHitFired = true;
                    dealAoeGroundDamage(8.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 2.0f, true);
                }

                break;
            case SKILL_SLAM_GROUND_AFTER_JUMP:
                if (!skillHitFired && skillTick >= SLAM_AFTER_JUMP_HIT_TICK) {

                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY, this.getSoundCategory(), 1.0F, 1.0F);

                    skillHitFired = true;
                    dealAoeGroundDamage(9.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 2.5f, true);
                }

                break;
            case SKILL_PREPARE_STEAL:
                LivingEntity stealTarget = this.getTarget();
                if (stealTarget != null) {
                    if (stealTarget instanceof PlayerEntity player) {
                        player.stopUsingItem(); // Forced stop
                    }

                    this.getNavigation().startMovingTo(stealTarget, 1.5);
                    double distSq = this.squaredDistanceTo(stealTarget);

                    if (distSq <= 1) {
                        this.getNavigation().stop();
                        triggerSkill(SKILL_STEAL_ITEM, 60);
                    }
                }

                break;
            case SKILL_STEAL_ITEM:
                if (skillTick < STEAL_HIT_TICK) {
                    LivingEntity currentTarget = this.getTarget();
                    if (currentTarget instanceof PlayerEntity player) {
                        player.stopUsingItem();
                    }
                }
                if (!skillHitFired && skillTick >= STEAL_HIT_TICK) {
                    skillHitFired = true;
                    LivingEntity target = this.getTarget();
                    if (target instanceof PlayerEntity player) {
                        if (!stolenItemCandidate.isEmpty()) {
                            for (int i = 0; i < player.getInventory().size(); i++) {
                                ItemStack stack = player.getInventory().getStack(i);
                                if (ItemStack.canCombine(stack, stolenItemCandidate)) {
                                    this.setStackInHand(Hand.MAIN_HAND, stack.copy());
                                    player.getInventory().removeStack(i);
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case SKILL_USE_ITEM:
                // Play effects periodically while eating (ticks 10 to 43)
                if (skillTick >= 10 && skillTick <= USE_HIT_TICK) {
                    if (skillTick % 8 == 0) {
                        ItemStack bossStack = this.getMainHandStack();
                        if (bossStack.isFood() && this.getWorld() instanceof ServerWorld serverWorld) {
                            Vec3d lookVec = this.getRotationVec(1.0F);
                            double px = this.getX() + lookVec.x * 0.5;
                            double py = this.getY() + this.getStandingEyeHeight();
                            double pz = this.getZ() + lookVec.z * 0.5;

                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, bossStack),
                                    px, py, pz, 5, 0.05, 0.05, 0.05, 0.03);
                            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F, 1.0F);
                        }
                    }
                }

                if (!skillHitFired && skillTick >= USE_HIT_TICK) {
                    skillHitFired = true;
                    ItemStack bossStack = this.getMainHandStack();
                    if (bossStack.isFood()) {
                        // "Use" the item (remove it)
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        this.heal(10.0F * bossStack.getCount() * 2);

                        stolenItemCandidate.getItem().getFoodComponent().getStatusEffects().forEach(effect -> {
                            this.addStatusEffect(new StatusEffectInstance(effect.getFirst()));
                        });

                        this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
                    }
                }
                break;
            case SKILL_DESTROY_ITEM:
                // Play effects periodically during breaking sequence
                boolean isHitTick = skillTick == DESTROY_HIT_TICKS[0] || skillTick == DESTROY_HIT_TICKS[1]
                        || skillTick == DESTROY_HIT_TICKS[2];

                // Extra particles during the swing/impact parts (e.g., ±3 ticks around hits)
                boolean isNearHit = (skillTick >= 10 && skillTick <= 70) && (skillTick % 4 == 0);

                if (isHitTick || isNearHit) {
                    ItemStack bossStack = this.getMainHandStack();
                    if (!bossStack.isEmpty()) {
                        if (this.getWorld() instanceof ServerWorld serverWorld) {
                            double px = this.getX();
                            double py = this.getY() + this.getStandingEyeHeight() * 0.7;
                            double pz = this.getZ();

                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, bossStack),
                                    px, py, pz, isHitTick ? 15 : 3, 0.1, 0.1, 0.1, 0.05);

                            if (isHitTick) {
                                this.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.4F, 1.2F);
                            } else if (skillTick % 8 == 0) {
                                this.playSound(SoundEvents.BLOCK_GRINDSTONE_USE, 0.3F, 1.5F); // Scraping sound
                            }
                        }

                        if (isHitTick) {
                            int damageAmount = 500;
                            if (bossStack.isDamageable()) {
                                int remainingDurability = bossStack.getMaxDamage() - bossStack.getDamage();
                                if (remainingDurability < 500) {
                                    if (skillTick == DESTROY_HIT_TICKS[2]) {
                                        damageAmount = 500;
                                    } else {
                                        damageAmount = Math.max(1, remainingDurability / 3);
                                    }
                                }

                                int newDamage = bossStack.getDamage() + damageAmount;
                                if (newDamage >= bossStack.getMaxDamage()) {
                                    this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                    this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                                } else {
                                    bossStack.setDamage(newDamage);
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    private void handleSkillCompletion(int skillId) {
        switch (skillId) {
            case SKILL_DASH_FORWARD:
                triggerSkill(SKILL_LEG_TRIP, 20); // Chain to Leg Trip
                break;
            case SKILL_LEG_TRIP:
                triggerSkill(SKILL_GROUND_SLAM_KICK, 40); // Chain to Ground Slam Kick
                break;
            case SKILL_JUMP:
                LivingEntity target = this.getTarget();

                // Relentless boss logic: if target lost, find nearest opponent
                if (target == null && this.getWorld() instanceof ServerWorld serverWorld) {
                    target = serverWorld.getClosestPlayer(this.getX(), this.getY(), this.getZ(), 100.0, true);
                }

                if (target != null) {
                    // Teleport to target with offset in front
                    Vec3d look = target.getRotationVec(1.0F);
                    double distance = 1.5;
                    double tx = target.getX() + look.x * distance;
                    double ty = target.getY();
                    double tz = target.getZ() + look.z * distance;

                    // Calculate yaw to face target from new position
                    double dx = target.getX() - tx;
                    double dz = target.getZ() - tz;
                    float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

                    if (this.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1, this.getZ(),
                                10, 0.5, 1.0, 0.5, 0.1);
                    }

                    // Use a more robust teleport method if possible, or force position refresh
                    this.refreshPositionAndAngles(tx, ty, tz, targetYaw, 0);
                    this.setPosition(tx, ty, tz);
                    this.velocityModified = true;
                    this.velocityDirty = true;

                    if (this.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, tx, ty + 1, tz, 15, 0.5, 1.0, 0.5,
                                0.05);
                    }
                } else {
                    WelcomeToMyWorld.LOGGER.info("log(\"teleportTarget\", \"still null\")");
                }
                triggerSkill(SKILL_SLAM_GROUND_AFTER_JUMP, 30); // Chain to Slam After Jump
                break;
            case SKILL_SLAM_GROUND_AFTER_JUMP:
            case SKILL_USE_ITEM:
            case SKILL_DESTROY_ITEM:
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                break;
            case SKILL_PREPARE_STEAL:
                LivingEntity completionTarget = this.getTarget();
                double endDistSq = completionTarget != null ? this.squaredDistanceTo(completionTarget) : -1;

                if (completionTarget != null && endDistSq <= 3.0 * 3.0) {
                    triggerSkill(SKILL_STEAL_ITEM, 60);
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case SKILL_STEAL_ITEM:
                ItemStack stolenItem = this.getMainHandStack();
                if (!stolenItem.isEmpty()) {
                    if (stolenItem.isFood()) {
                        triggerSkill(SKILL_USE_ITEM, 70);
                    } else if (stolenItem.getItem() instanceof SwordItem
                            || stolenItem.getItem() instanceof ShieldItem) {
                        triggerSkill(SKILL_DESTROY_ITEM, 80);
                    } else {
                        this.dataTracker.set(IS_USING_SKILL, false);
                        this.dataTracker.set(SKILL_ID, 0);
                    }
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            default:
                // End skill
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                break;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (dashTimer > 0) {
            dashTimer--;
            if (dashTimer == 0)
                this.dataTracker.set(DASH_DIR, 0);
        }

        if (tauntCooldown > 0) {
            tauntCooldown--;
            if (tauntCooldown == 140) {
                this.dataTracker.set(IS_TAUNTING, false);
            }
        }

        if (!this.getWorld().isClient()) {
            skillsHandler();
            if (dodgeCooldown > 0)
                dodgeCooldown--;
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {

        if (!this.getWorld().isClient() && dodgeCooldown <= 0 && Math.random() < 0.45 && !isUsingSkill()) {
            tryDodge();
            return super.damage(source, 0);
        }
        return super.damage(source, amount);
    }

    /**
     * Deals damage to all LivingEntities (except self) within a radius.
     * Reusable for any ground-slam or AoE attack.
     */
    public List<LivingEntity> dealAoeGroundDamage(double radius, float damage, boolean createBlock) {
        if (this.getWorld().isClient())
            return List.of();
        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(
                LivingEntity.class, area, e -> e != this);
        DamageSource source = this.getDamageSources().mobAttack(this);
        for (LivingEntity target : nearby) {
            target.damage(source, damage);
        }

        // Spawn visual blocks representing the ground slam impact (ripple effect)
        if (createBlock && this.getWorld() instanceof ServerWorld serverWorld) {
            int delay = 0;
            // The boss position when the slam hits
            final double originX = this.getX();
            final double originY = this.getY();
            final double originZ = this.getZ();

            for (double r = 1.5; r <= radius + 0.5; r += 1.5) {
                final double currentRadius = r;
                final int finalDelay = delay;

                Utils.addRunAfter(() -> {
                    int blockCount = (int) (currentRadius * 8); // denser rings
                    for (int i = 0; i < blockCount; i++) {
                        double angle = 2 * Math.PI * i / blockCount;
                        double x = originX + currentRadius * Math.cos(angle);
                        double z = originZ + currentRadius * Math.sin(angle);
                        BlockPos spawnPos = BlockPos.ofFloored(x, originY, z);

                        // Try to use the block state from the ground for the effect
                        BlockState groundState = serverWorld.getBlockState(spawnPos.down());
                        if (groundState.isAir() || !groundState.isOpaqueFullCube(serverWorld, spawnPos.down())) {
                            groundState = Blocks.DIRT.getDefaultState();
                        }

                        Utils.CreateBlockSlamGround(serverWorld, groundState, spawnPos.down());
                    }
                }, finalDelay);

                delay += 1;
            }
        }
        return nearby;
    }

    /**
     * Deals damage in a straight line and spawns visual blocks + smoke
     * sequentially.
     */
    public void dealLinearShockwaveDamage(double width, double length, float damage) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld))
            return;

        // Direction from mob to target, or fallback to yaw if no target
        double dx, dz;
        LivingEntity targetEntity = this.getTarget();

        if (targetEntity != null) {
            double diffX = targetEntity.getX() - this.getX();
            double diffZ = targetEntity.getZ() - this.getZ();
            double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
            if (distance > 1.0E-4D) {
                dx = diffX / distance;
                dz = diffZ / distance;
            } else {
                float yaw = this.getYaw() * ((float) Math.PI / 180F);
                dx = -Math.sin(yaw);
                dz = Math.cos(yaw);
            }
        } else {
            float yaw = this.getYaw() * ((float) Math.PI / 180F);
            dx = -Math.sin(yaw);
            dz = Math.cos(yaw);
        }

        double originX = this.getX();
        double originY = this.getY();
        double originZ = this.getZ();

        // 1) Deal damage immediately
        Box checkArea = this.getBoundingBox().expand(length);
        List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, checkArea, e -> e != this);
        DamageSource source = this.getDamageSources().mobAttack(this);

        for (LivingEntity target : nearby) {
            double vecX = target.getX() - originX;
            double vecZ = target.getZ() - originZ;
            double projection = vecX * dx + vecZ * dz;

            // Target is in front and within length
            if (projection > 0 && projection <= length) {
                double closestX = originX + dx * projection;
                double closestZ = originZ + dz * projection;
                double distSq = (target.getX() - closestX) * (target.getX() - closestX) +
                        (target.getZ() - closestZ) * (target.getZ() - closestZ);

                if (distSq <= (width / 2.0) * (width / 2.0)) {
                    target.damage(source, damage);
                }
            }
        }

        // 2) Visuals propagating outwards
        int segments = (int) length;
        for (int i = 1; i <= segments; i++) {
            final double dist = i;
            final int delay = i; // 1 tick per block of distance = shockwave speed

            Utils.addRunAfter(() -> {
                double targetX = originX + dx * dist;
                double targetZ = originZ + dz * dist;

                // Spawn 3 blocks side by side for width
                for (int w = -1; w <= 1; w++) {
                    double orthoX = dz;
                    double orthoZ = -dx;
                    double spawnX = targetX + orthoX * w;
                    double spawnZ = targetZ + orthoZ * w;

                    BlockPos spawnPos = BlockPos.ofFloored(spawnX, originY, spawnZ);

                    BlockState groundState = serverWorld.getBlockState(spawnPos.down());
                    if (groundState.isAir() || !groundState.isOpaqueFullCube(serverWorld, spawnPos.down())) {
                        groundState = Blocks.DIRT.getDefaultState();
                    }
                    Utils.CreateBlockSlamGround(serverWorld, groundState, spawnPos.down());

                    serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            spawnPos.getX() + 0.5, spawnPos.getY() + 0.2, spawnPos.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.05);
                }
            }, delay);
        }
    }

    private boolean lastDodgeLeft = false;

    private void tryDodge() {
        lastDodgeLeft = !lastDodgeLeft;
        boolean dodgeLeft = lastDodgeLeft;

        float bossYaw = this.getYaw();
        double rightX = Math.cos(bossYaw * (Math.PI / 180.0));
        double rightZ = Math.sin(bossYaw * (Math.PI / 180.0));

        double impulseX = dodgeLeft ? -rightX : rightX;
        double impulseZ = dodgeLeft ? -rightZ : rightZ;
        double dodgeSpeed = 1.5;

        this.setVelocity(impulseX * dodgeSpeed, this.getVelocity().y, impulseZ * dodgeSpeed);
        this.velocityModified = true;

        this.triggerDash(dodgeLeft, 15);
        this.dodgeCooldown = 40;
    }

    @Override
    public boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean shouldDropXp() {
        return false;
    }

    @Override
    public boolean shouldFollowLeash() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public class StopMoveWhenUsingSkill extends Goal {
        private final Unknown mob;

        public StopMoveWhenUsingSkill(Unknown mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return mob.isUsingSkill() && mob.dataTracker.get(SKILL_ID) != SKILL_PREPARE_STEAL;
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }

        @Override
        public boolean shouldContinue() {
            return mob.isUsingSkill() && mob.dataTracker.get(SKILL_ID) != SKILL_PREPARE_STEAL;
        }

        @Override
        public void tick() {
            mob.getNavigation().stop();
        }
    }

    public class ChaseTargetGoal extends Goal {
        private final Unknown mob;
        private final double speed;

        public ChaseTargetGoal(Unknown mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill()
                    && mob.dataTracker.get(SKILL_ID) != SKILL_PREPARE_STEAL;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill()
                    && mob.dataTracker.get(SKILL_ID) != SKILL_PREPARE_STEAL;
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.getLookControl().lookAt(target, 30.0F, 30.0F);
                // Keep some distance to allow skills to trigger properly
                if (mob.squaredDistanceTo(target) > 2.0 * 2.0) {
                    mob.getNavigation().startMovingTo(target, speed);
                } else {
                    mob.getNavigation().stop();
                }
            }
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
        }
    }
}
