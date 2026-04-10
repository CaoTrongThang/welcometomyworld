package com.trongthang.welcometomyworld.entities.Unknown;

import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import com.trongthang.welcometomyworld.entities.UnknownBeamEntity;
import com.trongthang.welcometomyworld.entities.GroundSlashAttackEntity;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.classes.CustomTameableEntity;
import com.trongthang.welcometomyworld.managers.SoundsManager;

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
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;

public class Unknown extends HostileEntity implements GeoEntity {

    // 0 = NONE, 1 = LEFT, 2 = RIGHT — synced to client every tick via DataTracker
    private static final TrackedData<Integer> DASH_DIR = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.INTEGER);

    // Skills
    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Integer> SKILL_TRIGGER = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> IS_TAUNTING = DataTracker.registerData(Unknown.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    public static class Skill {
        public int id;
        public int length;
        public int cooldown;

        public Skill(int id, int length, int cooldown) {
            this.id = id;
            this.length = length;
            this.cooldown = cooldown;
        }
    };

    public static final Skill PUNCH = new Skill(1, 40, 120);
    public static final Skill GROUND_SLAM_KICK = new Skill(15, 40, 120);
    public static final Skill DASH_FORWARD = new Skill(3, 12, 150);
    public static final Skill LEG_TRIP = new Skill(4, 15, 80);
    public static final Skill JUMP_HIGH = new Skill(6, 20, 150);
    public static final Skill SLAM_GROUND_AFTER_JUMP = new Skill(7, 30, 100);
    public static final Skill STEAL_ITEM = new Skill(8, 60, 200);
    public static final Skill USE_ITEM = new Skill(9, 70, 100);
    public static final Skill DESTROY_ITEM = new Skill(10, 80, 100);
    public static final Skill PREPARE_STEAL = new Skill(11, 100, 600);
    public static final Skill POINT_FINGER = new Skill(12, 20, 400);
    public static final Skill GRAB_JUMP_SLAM = new Skill(13, 80, 200);
    public static final Skill KAMEHAMEHA = new Skill(20, 80, 200);
    public static final Skill UNKNOWN_JUMP_BACK = new Skill(5, 20, 150);
    public static final Skill UNKNOWN_SUMMONING_CIRCLE = new Skill(22, 240, 400);
    public static final Skill UNKNOWN_SPEAR_STAB = new Skill(23, 35, 120);
    public static final Skill UNKNOWN_SPEAR_3_HITS = new Skill(24, 70, 160);

    private static final int PUNCH_HIT_TICK = 20;
    private static final int LEG_TRIP_HIT_TICK = 8;
    private static final int GROUND_SLAM_KICK_HIT_TICK = 20;
    private static final int SLAM_AFTER_JUMP_HIT_TICK = 3;
    private static final int STEAL_HIT_TICK = 8;
    private static final int USE_HIT_TICK = 43;
    private static final int POINT_FINGER_HIT_TICK = 5;
    private static final int GRAB_SLAM_HIT_TICK = 41;
    private static final int[] DESTROY_HIT_TICKS = { 15, 38, 58 };
    private static final int SPEAR_STAB_HIT_TICK = 20;
    private static final int SPEAR_3_HIT_TICK_1 = 21;
    private static final int SPEAR_3_HIT_TICK_2 = 38;
    private static final int SPEAR_3_HIT_TICK_3 = 54;

    private static final float GROUND_SLAM_KICK_DAMAGE_MULTIPLIER = 2.5f;
    private static final float LEG_TRIP_DAMAGE_MULTIPLIER = 1.5f;
    private static final float SLAM_AFTER_JUMP_DAMAGE_MULTIPLIER = 2.5f;;
    private static final float GRAB_SLAM_DAMAGE_MULTIPLIER = 2.5f;
    private static final float KAMEHAMEHA_DAMAGE_MULTIPLIER = 1.5f;
    private static final float SPEAR_STAB_DAMAGE_MULTIPLIER = 1.5f;
    private static final float SPEAR_3_HIT_DAMAGE_MULTIPLIER = 1.5f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int dashTimer = 0;
    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int globalSkillCooldown = 0;
    private final int[] skillCooldowns = new int[100];

    public boolean canUseSkill(Skill skill) {
        return skillCooldowns[skill.id] <= 0;
    }

    /** Returns health as a fraction [0,1]. Use for phase-gating skills. */
    private float healthFraction() {
        return this.getHealth() / this.getMaxHealth();
    }

    /**
     * Picks one skill by weighted random from a candidate list.
     * Each entry: { skill, weight, condition }.
     * Skills whose cooldown is active OR whose condition is false are skipped.
     * Returns null if nothing is eligible.
     */
    private Skill pickWeightedSkill(Object[]... candidates) {
        float totalWeight = 0;
        for (Object[] c : candidates) {
            Skill skill = (Skill) c[0];
            float weight = ((Number) c[1]).floatValue();
            boolean condition = (Boolean) c[2];
            if (condition && canUseSkill(skill))
                totalWeight += weight;
        }
        if (totalWeight <= 0)
            return null;

        float roll = (float) (Math.random() * totalWeight);
        float acc = 0;
        for (Object[] c : candidates) {
            Skill skill = (Skill) c[0];
            float weight = ((Number) c[1]).floatValue();
            boolean condition = (Boolean) c[2];
            if (!condition || !canUseSkill(skill))
                continue;
            acc += weight;
            if (roll < acc)
                return skill;
        }
        return null;
    }

    private int tauntCooldown = 0;
    private int dodgeCooldown = 0;
    private ItemStack stolenItemCandidate = ItemStack.EMPTY;

    public Unknown(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 99999.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 50.0D) // 0 for testing
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 50f)
                .add(EntityAttributes.GENERIC_ARMOR, 30f)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 20f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DASH_DIR, 0);
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(SKILL_TRIGGER, 0);
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
        this.targetSelector.add(1, new RevengeGoal(this));

        // equip offhand with an item called "mobs_of_mythology:kobold_spear"
        // how to get the item from the mod
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
        int[] prevSkillTrigger = { 0 };
        int[] prevDir = { 0 };

        // --- All animations in one controller for smooth cross-fading ---
        controllers.add(new AnimationController<>(this, "mainController", 5, state -> {
            int dir = this.dataTracker.get(DASH_DIR);
            boolean isUsingSkill = this.dataTracker.get(IS_USING_SKILL);
            int skillId = this.dataTracker.get(SKILL_ID);

            state.getController().setAnimationSpeed(1.0D);

            if (isUsingSkill) {
                int skillTrigger = this.dataTracker.get(SKILL_TRIGGER);

                state.getController().transitionLength(0);
                if (prevSkillId[0] != skillId || prevSkillTrigger[0] != skillTrigger) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;
                prevSkillTrigger[0] = skillTrigger;

                switch (skillId) {
                    case 1: // PUNCH
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_saitama_punch"));
                    case 15: // GROUND_SLAM_KICK
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_slam_ground_kick"));
                    case 3: // DASH_FORWARD
                        // Placeholder if not exists, user said "unknown_dash_forward"
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_dash_forward"));
                    case 4: // LEG_TRIP
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_leg_trip"));
                    case 6: // JUMP
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_jump"));
                    case 7: // SLAM_GROUND_AFTER_JUMP
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_slam_ground_after_jump"));
                    case 8: // STEAL_ITEM
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_steal_item"));
                    case 9: // USE_ITEM
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_use_the_item"));
                    case 10: // DESTROY_ITEM
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_destroy_the_item"));
                    case 11: // PREPARE_STEAL
                        state.getController().setAnimationSpeed(2.0D);
                        return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_walking"));
                    case 12: // POINT_FINGER
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_point_finger"));
                    case 13: // GRAB_JUMP_SLAM
                        return state
                                .setAndContinue(RawAnimation.begin().thenPlay("unknown_grab_player_jump_slam_ground"));
                    case 20: // KAMEHAMEHA
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_kamehameha"));
                    case 21: // UNKNOWN_JUMP_BACK
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_jump_back"));
                    case 22: // UNKNOWN_SUMMONING_CIRCLE
                        // Use taunt or idle if unknown_summoning_circle is missing in json
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_summoning_circle"));
                    case 23: // UNKNOWN_SPEAR_STAB
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_spear_stab"));
                    case 24: // UNKNOWN_SPEAR_3_HITS
                        return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_spear_3_hits"));
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
            state.getController().transitionLength(12);
            prevDir[0] = 0;
            boolean isMoving = this.getVelocity().x != 0 || this.getVelocity().z != 0; // best moving check for even
                                                                                       // slow movement
            if (isMoving) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_walking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_idle_ground"));
        }));

        controllers.add(new AnimationController<>(this, "tauntController", 10, state -> {
            if (this.dataTracker.get(IS_TAUNTING) && !this.dataTracker.get(IS_USING_SKILL)) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("unknown_taunt"));
            }
            return state.setAndContinue(RawAnimation.begin()); // Empty animation or stop
        }));
    }

    private void skillsHandler() {
        if (!isUsingSkill() && globalSkillCooldown > 0) {
            globalSkillCooldown--;
        }
        for (int i = 0; i < skillCooldowns.length; i++) {
            if (skillCooldowns[i] > 0) {
                skillCooldowns[i]--;
            }
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
                handleSkillCompletionChain(skillId);
            }
            return;
        }

        // --- Trigger logic ---
        if (globalSkillCooldown <= 0 && !isUsingSkill()) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                double dist = this.distanceTo(target);

                // Case 1: Melee — player is actively blocking/using an item
                if (dist <= 10.0 && target instanceof PlayerEntity player && target.isUsingItem()
                        && canUseSkill(POINT_FINGER)) {
                    stolenItemCandidate = player.getActiveItem().copy();
                    triggerSkill(POINT_FINGER);

                    // Case 2: Close range
                } else if (dist <= 8.0) {
                    // To add a phase-2 skill: new Object[]{MY_SKILL, 30f, healthFraction() <= 0.5f}
                    Skill picked = pickWeightedSkill(
                            new Object[] { PUNCH, 30, true },
                            new Object[] { GROUND_SLAM_KICK, 50, true },
                            new Object[] { LEG_TRIP, 50, true },
                            new Object[] { UNKNOWN_SPEAR_3_HITS, 50, healthFraction() <= 0.8f },
                            new Object[] { UNKNOWN_JUMP_BACK, 50, healthFraction() <= 0.8f },
                            new Object[] { UNKNOWN_SUMMONING_CIRCLE, 50f, healthFraction() <= 0.4f },
                            new Object[] { UNKNOWN_SPEAR_STAB, 50f, healthFraction() <= 0.8f },
                            new Object[] { KAMEHAMEHA, 30f, healthFraction() <= 0.6f });
                    if (picked != null)
                        triggerSkill(picked);

                    // Case 3: Mid range
                } else if (dist <= 20.0) {
                    Skill picked = pickWeightedSkill(
                            new Object[] { DASH_FORWARD, 40f, true },
                            new Object[] { UNKNOWN_SPEAR_STAB, 50f, healthFraction() <= 0.8f },
                            new Object[] { UNKNOWN_SUMMONING_CIRCLE, 50f, healthFraction() <= 0.4f },
                            new Object[] { KAMEHAMEHA, 30f, healthFraction() <= 0.6f });
                    if (picked != null)
                        triggerSkill(picked);

                    // Case 4: Far range (≤40)
                } else if (dist <= 40.0) {
                    Skill picked = pickWeightedSkill(
                            new Object[] { JUMP_HIGH, 30f, true },
                            new Object[] { PUNCH, 20f, true },
                            new Object[] { KAMEHAMEHA, 30f, healthFraction() <= 0.6f });
                    if (picked != null)
                        triggerSkill(picked);

                    // Case 5: Very far range (>40)
                } else {
                    Skill picked = pickWeightedSkill(
                            new Object[] { UNKNOWN_SUMMONING_CIRCLE, 50f, healthFraction() <= 0.4f },
                            new Object[] { JUMP_HIGH, 50f, true });
                    if (picked != null)
                        triggerSkill(picked);
                }
            }
        }

        // // --- Taunt logic ---
        // if (globalSkillCooldown > 0 && tauntCooldown <= 0 && !isUsingSkill()) {
        // tauntCooldown = 200;
        // LivingEntity tauntTarget = this.getTarget();
        // if (tauntTarget != null && tauntTarget.isAlive()) {
        // double distSq = this.squaredDistanceTo(tauntTarget);
        // if (distSq > 10.0 * 10.0) {
        // this.dataTracker.set(IS_TAUNTING, true);
        // this.getLookControl().lookAt(tauntTarget, 30.0F, 30.0F);
        // } else {
        // this.dataTracker.set(IS_TAUNTING, false);
        // }
        // } else {
        // this.dataTracker.set(IS_TAUNTING, false);
        // }
        // }
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
    public void triggerSkill(Skill skill) {
        if (this.getWorld().isClient())
            return;
        this.dataTracker.set(IS_USING_SKILL, true);
        this.dataTracker.set(SKILL_ID, skill.id);
        this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
        this.skillTick = 0;
        this.skillTotalTicks = skill.length;
        this.skillCooldowns[skill.id] = skill.cooldown;
        this.globalSkillCooldown = 40; // 2 seconds between ANY skills
        this.skillHitFired = false;

        // Stop movement during skill, except for PREPARE_STEAL
        if (skill.id != PREPARE_STEAL.id) {
            this.getNavigation().stop();
        }
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    public int getSkillId() {
        return this.dataTracker.get(SKILL_ID);
    }

    // 0 = NONE, 1 = LEFT, 2 = RIGHT — used by renderer for side-dash afterimage
    public int getDashDir() {
        return this.dataTracker.get(DASH_DIR);
    }

    private void handleSkillEffects(int skillId) {
        LivingEntity target = this.getTarget();

        switch (skillId) {
            case 15: // GROUND_SLAM_KICK
                if (skillTick == GROUND_SLAM_KICK_HIT_TICK - 5) { // Play slightly before impact for better sync
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT, this.getSoundCategory(), 1.0F, 1.0F);
                }
                if (!skillHitFired && skillTick >= GROUND_SLAM_KICK_HIT_TICK) {

                    skillHitFired = true;
                    this.removeAllPassengers(); // Release the player when the slam hits
                    dealAoeGroundDamage(10.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * GROUND_SLAM_KICK_DAMAGE_MULTIPLIER,
                            true);
                }

                break;
            case 1: // PUNCH
                if (skillTick == PUNCH_HIT_TICK - 18) {
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.EXPLODE_PUNCH, this.getSoundCategory(), 1.0F,
                            MathHelper.nextBetween(this.random, 0.8F, 1.2F));
                }
                if (!skillHitFired && skillTick >= PUNCH_HIT_TICK) {
                    skillHitFired = true;
                    dealLinearShockwaveDamage(3.0, 40.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f);
                }
                break;
            case 3: // DASH_FORWARD
                if (skillTick == 5) {
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

                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundsManager.WHOOSH_1, this.getSoundCategory(), 0.1F,
                                (float) (0.8 + Math.random() * 0.4));
                    }
                }
                break;
            case 6: // JUMP
                if (skillTick == 1) {
                    double vx = 0, vz = 0;
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
            case 13: // GRAB_JUMP_SLAM
                if (skillTick == 5) {
                    if (target != null) {
                        target.startRiding(this, true);
                    }
                } else if (skillTick >= 10 && skillTick < 16) { // Blast into the sky for 6 ticks
                    this.setVelocity(0, 5.0, 0); // 5 blocks up per tick = 30 blocks total height roughly
                    this.velocityModified = true;
                } else if (skillTick >= 16 && skillTick < 38) {
                    // Hover in sky
                    this.setVelocity(0, 0.05, 0);
                    this.velocityModified = true;
                } else if (skillTick == 38) {
                    // Plunge to the ground incredibly fast for 3 ticks right before the slam
                    this.setVelocity(0, -15.0, 0);
                    this.velocityModified = true;
                } else if (!skillHitFired && skillTick >= GRAB_SLAM_HIT_TICK) {
                    skillHitFired = true;

                    double currentY = this.getY();
                    // Create smokes around the slam area
                    if (this.getWorld() instanceof ServerWorld serverWorld) {
                        BlockPos pos = BlockPos.ofFloored(this.getX(), this.getY(), this.getZ());
                        while (pos.getY() > serverWorld.getBottomY()
                                && serverWorld.getBlockState(pos.down()).isReplaceable()) {
                            pos = pos.down();
                        }
                        currentY = pos.getY();

                        // Create smokes around the slam area
                        serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), currentY + 1.0, this.getZ(),
                                80, 2.0, 0.5, 2.0, 0.05);
                        serverWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), currentY + 0.5,
                                this.getZ(), 60, 3.0, 0.5, 3.0, 0.1);
                    }
                    this.refreshPositionAndAngles(this.getX(), currentY, this.getZ(), this.getYaw(), this.getPitch());
                    this.setPosition(this.getX(), currentY, this.getZ());
                    this.velocityModified = true;

                    dealAoeGroundDamage(9.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * GRAB_SLAM_DAMAGE_MULTIPLIER,
                            true);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY, this.getSoundCategory(), 1.0F, 1.0F);

                    // give the player slowness
                    for (net.minecraft.entity.Entity passenger : this.getPassengerList()) {
                        if (passenger instanceof PlayerEntity player) {
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 255));
                        }
                    }

                } else if (skillTick == GRAB_SLAM_HIT_TICK + 4) {
                    // Delaying dismount by a few ticks to let the client smoothly interpolate the
                    // teleport downward together
                    for (net.minecraft.entity.Entity passenger : this.getPassengerList()) {
                        passenger.fallDistance = 0;
                    }
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                    this.removeAllPassengers();
                }
                break;
            case 22: // UNKNOWN_SUMMONING_CIRCLE
                if (!skillHitFired && skillTick >= 22 && skillTick <= 180) {
                    if (skillTick % 6 == 0 && skillTick <= 180) { // Summon one every 12 ticks
                        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                            SummoningCircleEntity circle = new SummoningCircleEntity(
                                    EntitiesManager.SUMMONING_CIRCLE,
                                    serverWorld);
                            circle.setOwner(this);
                            circle.setMaxAge(100 + this.random.nextInt(60));

                            // Calculate position around the target (pivot)
                            if (target != null) {
                                double dist = 12.0 + Math.random() * 12.0; // Increased distance
                                double yaw = Math.random() * 360;
                                double rad = Math.toRadians(yaw);
                                double offsetX = -Math.sin(rad) * dist;
                                double offsetZ = Math.cos(rad) * dist;
                                double offsetY = 1.0 + Math.random() * 6; // Slightly higher than player

                                circle.setPosition(target.getX() + offsetX, target.getY() + offsetY,
                                        target.getZ() + offsetZ);
                                serverWorld.spawnEntity(circle);
                                this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                                        SoundsManager.SUMMON_CIRCLE, this.getSoundCategory(), 0.2F,
                                        (float) (0.8 + Math.random() * 0.4));
                            }
                        }
                    }
                }
                break;
            case 4: // LEG_TRIP
                if (!skillHitFired && skillTick >= LEG_TRIP_HIT_TICK) {
                    skillHitFired = true;
                    List<LivingEntity> entities = dealAoeGroundDamage(10.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * LEG_TRIP_DAMAGE_MULTIPLIER,
                            false);
                    for (LivingEntity entity : entities) {
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 255));
                    }

                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.DASH_CAPE, this.getSoundCategory(), 1.0F, 1.0F);

                }
                break;
            case 7: // SLAM_GROUND_AFTER_JUMP
                if (!skillHitFired && skillTick >= SLAM_AFTER_JUMP_HIT_TICK) {

                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY, this.getSoundCategory(), 1.0F, 1.0F);

                    skillHitFired = true;
                    dealAoeGroundDamage(9.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * SLAM_AFTER_JUMP_DAMAGE_MULTIPLIER,
                            true);
                }

                break;
            case 11: // PREPARE_STEAL
                LivingEntity stealTarget = this.getTarget();
                if (stealTarget != null) {
                    if (stealTarget instanceof PlayerEntity player) {
                        player.stopUsingItem(); // Forced stop
                    }

                    this.getNavigation().startMovingTo(stealTarget, 2);
                    double distSq = this.squaredDistanceTo(stealTarget);

                    // i think if in this phase, keep checking if that "stolenItemCandidate" is
                    // still in the user's inventory, else skip
                    if (stealTarget instanceof PlayerEntity player) {
                        if (!player.getInventory().contains(stolenItemCandidate)) {
                            this.dataTracker.set(IS_USING_SKILL, false);
                            this.dataTracker.set(SKILL_ID, 0);
                            this.getNavigation().stop();
                            return;
                        }
                    }

                    if (distSq <= 2.0 * 2.0) {
                        this.getNavigation().stop();
                        triggerSkill(STEAL_ITEM);
                    }
                }

                break;
            case 8: // STEAL_ITEM
                if (skillTick < STEAL_HIT_TICK) {
                    LivingEntity currentTarget = this.getTarget();
                    if (currentTarget instanceof PlayerEntity player) {
                        player.stopUsingItem();
                    }
                }
                if (!skillHitFired && skillTick >= STEAL_HIT_TICK) {
                    skillHitFired = true;
                    if (target instanceof PlayerEntity player) {
                        if (!stolenItemCandidate.isEmpty()) {
                            ItemStack foundStack = ItemStack.EMPTY;
                            int foundSlot = -1;

                            // 1. Check Main Hand
                            if (ItemStack.canCombine(player.getMainHandStack(), stolenItemCandidate)) {
                                foundStack = player.getMainHandStack();
                                foundSlot = player.getInventory().selectedSlot;
                            }
                            // 2. Check Offhand
                            else if (ItemStack.canCombine(player.getOffHandStack(), stolenItemCandidate)) {
                                foundStack = player.getOffHandStack();
                                foundSlot = 40; // Offhand slot index is 40 in 1.20.1
                            }
                            // 3. Fallback: Search the rest of inventory
                            else {
                                for (int i = 0; i < player.getInventory().size(); i++) {
                                    ItemStack stack = player.getInventory().getStack(i);
                                    if (ItemStack.canCombine(stack, stolenItemCandidate)) {
                                        foundStack = stack;
                                        foundSlot = i;
                                        break;
                                    }
                                }
                            }

                            if (!foundStack.isEmpty()) {
                                this.setStackInHand(Hand.MAIN_HAND, foundStack.copy());
                                player.getInventory().removeStack(foundSlot);
                            }
                        }
                    }

                    ItemStack currentMainHand = this.getMainHandStack();
                    if (currentMainHand.isEmpty()
                            || (!currentMainHand.isFood() && !(currentMainHand.getItem() instanceof PotionItem)
                                    && !(currentMainHand.getItem() instanceof ShieldItem))) {
                        // Failed to steal, or stole something useless, stop playing animation early
                        this.dataTracker.set(IS_USING_SKILL, false);
                        this.dataTracker.set(SKILL_ID, 0);
                        if (!currentMainHand.isEmpty()) {
                            this.dropStack(currentMainHand);
                            this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }
                break;
            case 9: // USE_ITEM
            {
                ItemStack currentStack = this.getMainHandStack();
                boolean isPotion = currentStack.getItem() instanceof PotionItem;
                boolean isFood = currentStack.isFood();

                // Play effects periodically while eating/drinking (ticks 10 to 43)
                if (skillTick >= 10 && skillTick <= USE_HIT_TICK) {
                    if (skillTick % 8 == 0) {
                        if ((isFood || isPotion) && this.getWorld() instanceof ServerWorld serverWorld) {
                            Vec3d lookVec = this.getRotationVec(1.0F);
                            double px = this.getX() + lookVec.x * 0.5;
                            double py = this.getY() + this.getStandingEyeHeight();
                            double pz = this.getZ() + lookVec.z * 0.5;

                            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, currentStack),
                                    px, py, pz, 5, 0.05, 0.05, 0.05, 0.03);

                            this.playSound(isPotion ? SoundEvents.ENTITY_GENERIC_DRINK : SoundEvents.ENTITY_GENERIC_EAT,
                                    0.5F, 1.0F);
                        }
                    }
                }

                if (!skillHitFired && skillTick >= USE_HIT_TICK) {
                    skillHitFired = true;
                    if (isFood) {
                        // "Use" the item (remove it)
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        this.heal(10.0F * currentStack.getCount() * 2);

                        if (currentStack.getItem().isFood()) {
                            currentStack.getItem().getFoodComponent().getStatusEffects().forEach(effect -> {
                                this.addStatusEffect(new StatusEffectInstance(effect.getFirst()));
                            });
                        }

                        this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
                    } else if (isPotion) {
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        PotionUtil.getPotionEffects(currentStack).forEach(effect -> {
                            this.addStatusEffect(new StatusEffectInstance(effect));
                        });
                        this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
                    }
                }
                break;
            }
            case 10: // DESTROY_ITEM
            {
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
                            if (bossStack.isDamageable()) {
                                int remainingDurability = bossStack.getMaxDamage() - bossStack.getDamage();
                                int damageAmount;

                                if (skillTick == DESTROY_HIT_TICKS[2]) {
                                    damageAmount = 500;
                                } else {
                                    damageAmount = Math.max(1, remainingDurability / 3);
                                }

                                int newDamage = bossStack.getDamage() + damageAmount;
                                if (newDamage >= bossStack.getMaxDamage()) {
                                    this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                    this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                                } else {
                                    bossStack.setDamage(newDamage);
                                }

                                // Final hit: if the shield is still not broken, drop it to the ground
                                if (skillTick == DESTROY_HIT_TICKS[2]
                                        && bossStack.getDamage() < bossStack.getMaxDamage()) {
                                    this.dropStack(bossStack);
                                    this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                                    this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0F, 0.5F);
                                }
                            }
                        }
                    }
                }
                break;
            }
            case 12: // POINT_FINGER
            {
                if (!skillHitFired && skillTick >= POINT_FINGER_HIT_TICK) {
                    skillHitFired = true;
                    if (target instanceof PlayerEntity player) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 254));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 1));
                    }
                }
                break;
            }
            case 20: // KAMEHAMEHA
            {
                if (skillTick == 21) {
                    UnknownBeamEntity beam = new UnknownBeamEntity(EntitiesManager.UNKNOWN_BEAM, this.getWorld());
                    beam.setPosOwner(this);
                    beam.setDamageOwner(this);
                    beam.setLength(50.0f); // Longer
                    beam.setRadius(3.5f); // Thicker
                    beam.setDamage((float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                            * KAMEHAMEHA_DAMAGE_MULTIPLIER);
                    beam.refreshPositionAndAngles(this.getX(), this.getEyeY(), this.getZ(), this.getYaw(),
                            this.getPitch());
                    this.getWorld().spawnEntity(beam);

                    this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                            net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, this.getSoundCategory(), 2.0F, 1.0F);
                }
                break;
            }
            case 21: // UNKNOWN_JUMP_BACK
                if (skillTick == 5) {
                    Vec3d look = this.getRotationVec(1.0F);
                    // Force 1.5 blocks high (~0.6 VY) and 20 blocks back (~1.5 horizontal impulse)
                    double jumpHeightForce = 1;
                    double jumpBackForce = 20;

                    this.setVelocity(-look.x * jumpBackForce, jumpHeightForce, -look.z * jumpBackForce);
                    this.velocityModified = true;

                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.DASH_CAPE, this.getSoundCategory(), 1.0F, 1.0F);

                }

                // Chance to PUNCH during jump back (tick 12 approx mid-air/descending)
                if (skillTick == 12 && this.random.nextDouble() < 0.4) {
                    if (target != null && this.squaredDistanceTo(target) <= 10.0 * 10.0) {
                        triggerSkill(PUNCH);
                    }
                }
                break;

            case 23: // UNKNOWN_SPEAR_STAB
                if (skillTick == 19) {
                    if (target != null) {
                        this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                                SoundsManager.DASH_CAPE, this.getSoundCategory(), 1.0F, 1.0F);
                        this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                                SoundsManager.SPEAR_ATTACK_4, this.getSoundCategory(), 1.0F, 1.0F);
                        // Teleport slightly behind or near the target
                        Vec3d dirToBoss = this.getPos().subtract(target.getPos()).normalize();
                        Vec3d tpPos = target.getPos().add(dirToBoss.multiply(3.0));
                        this.requestTeleport(tpPos.x, tpPos.y, tpPos.z);

                        // Look at target
                        double dx = target.getX() - this.getX();
                        double dz = target.getZ() - this.getZ();
                        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                        this.setYaw(yaw);
                        this.setHeadYaw(yaw);

                        if (this.getWorld() instanceof ServerWorld sw) {
                            sw.spawnParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1, this.getZ(), 10, 0.5,
                                    0.5, 0.5, 0.1);
                        }
                    }
                }
                if (!skillHitFired && skillTick >= SPEAR_STAB_HIT_TICK) {
                    skillHitFired = true;

                    dealLinearShockwaveDamage(3.0, 15.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * SPEAR_STAB_DAMAGE_MULTIPLIER);

                    spawnGroundSlash(this.getYaw(), 1, SPEAR_STAB_DAMAGE_MULTIPLIER);
                    if (target != null) {
                        Vec3d dashDir = target.getPos().subtract(this.getPos()).normalize();
                        this.setVelocity(dashDir.multiply(5));
                        this.velocityModified = true;
                    }

                    // it has 50 chance to trigger the second spear stab
                    if (this.random.nextDouble() < 0.5) {
                        triggerSkill(UNKNOWN_SPEAR_STAB);
                    }
                }
                break;

            case 24: // UNKNOWN_SPEAR_3_HITS
                if (skillTick == SPEAR_3_HIT_TICK_1) {
                    // Phase 1: clockwise arc — hit in a ±45° cone in front
                    dealLinearShockwaveDamage(5.0, 12.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * SPEAR_3_HIT_DAMAGE_MULTIPLIER);

                    this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundsManager.SPEAR_ATTACK_1, this.getSoundCategory(), 1.0F, 1.0F);
                } else if (skillTick == SPEAR_3_HIT_TICK_2) {
                    // Phase 2: second arc sweep
                    dealLinearShockwaveDamage(5.0, 12.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * SPEAR_3_HIT_DAMAGE_MULTIPLIER);
                    this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundsManager.SPEAR_ATTACK_2, this.getSoundCategory(), 1.0F, 1.0F);
                } else if (!skillHitFired && skillTick >= SPEAR_3_HIT_TICK_3) {
                    skillHitFired = true;
                    // Phase 3: slam AOE + 8-direction radial slash burst
                    dealAoeGroundDamage(8.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                    * SPEAR_3_HIT_DAMAGE_MULTIPLIER,
                            true);
                    this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY, this.getSoundCategory(), 1.2F, 1.0F);
                    // Spawn slashes in 8 cardinal + diagonal directions
                    spawnGroundSlash(this.getYaw(), 8, SPEAR_3_HIT_DAMAGE_MULTIPLIER);
                }

                if (!skillHitFired && skillTick >= SPEAR_3_HIT_TICK_3 - 5) {
                    this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundsManager.SPEAR_ATTACK_3, this.getSoundCategory(), 0.5F, 1.0F);
                }
                break;
        }
    }

    private void handleSkillCompletionChain(int skillId) {
        switch (skillId) {
            case 3: // DASH_FORWARD
                boolean canGrab = canUseSkill(GRAB_JUMP_SLAM);
                boolean canTrip = canUseSkill(LEG_TRIP);

                if (canGrab && canTrip) {
                    if (this.random.nextDouble() < 0.5)
                        triggerSkill(GRAB_JUMP_SLAM);
                    else
                        triggerSkill(LEG_TRIP);
                } else if (canGrab) {
                    // check if play is active shield
                    LivingEntity target = this.getTarget();
                    if (target instanceof PlayerEntity player) {
                        if (player.isBlocking() && player.getActiveItem().getItem() instanceof ShieldItem) {
                            triggerSkill(STEAL_ITEM);
                        } else {
                            triggerSkill(GRAB_JUMP_SLAM);
                        }
                    }
                } else if (canTrip) {
                    triggerSkill(LEG_TRIP);
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case 4: // LEG_TRIP

                triggerSkill(GROUND_SLAM_KICK);

                break;
            case 6: // JUMP
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
                }
                triggerSkill(SLAM_GROUND_AFTER_JUMP); // Chain to Slam After Jump
                break;
            case 7: // SLAM_GROUND_AFTER_JUMP
            case 9: // USE_ITEM
            case 10: // DESTROY_ITEM
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                break;
            case 11: // PREPARE_STEAL
                LivingEntity completionTarget = this.getTarget();
                double endDistSq = completionTarget != null ? this.squaredDistanceTo(completionTarget) : -1;

                if (completionTarget != null && endDistSq <= 3.0 * 3.0) {
                    triggerSkill(STEAL_ITEM);
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case 8: // STEAL_ITEM
                ItemStack stolenItem = this.getMainHandStack();
                if (!stolenItem.isEmpty()) {
                    if (stolenItem.isFood() || stolenItem.getItem() instanceof PotionItem) {
                        triggerSkill(USE_ITEM);
                    } else if (stolenItem.getItem() instanceof ShieldItem) {
                        triggerSkill(DESTROY_ITEM);
                    } else {
                        this.dataTracker.set(IS_USING_SKILL, false);
                        this.dataTracker.set(SKILL_ID, 0);
                        this.dropStack(stolenItem);
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    }
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case 12: // POINT_FINGER
            {
                LivingEntity pointTarget = this.getTarget();
                if (pointTarget != null) {
                    double distSq = this.squaredDistanceTo(pointTarget);
                    if (distSq <= 3.0 * 3.0) {
                        triggerSkill(STEAL_ITEM);
                    } else {
                        triggerSkill(PREPARE_STEAL);
                    }
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            }
            default:
                // End skill
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                this.removeAllPassengers();
                break;
        }
    }

    private Vec3d getLocalOffsetTarget(double forwardOffset, double rightOffset, double heightOffset) {
        float yaw = this.bodyYaw * ((float) Math.PI / 180F);

        double offsetX = -Math.sin(yaw) * forwardOffset - Math.cos(yaw) * rightOffset;
        double offsetZ = Math.cos(yaw) * forwardOffset - Math.sin(yaw) * rightOffset;

        return new Vec3d(this.getX() + offsetX, this.getY() + heightOffset, this.getZ() + offsetZ);
    }

    @Override
    protected void updatePassengerPosition(net.minecraft.entity.Entity passenger,
            net.minecraft.entity.Entity.PositionUpdater positionUpdater) {
        if (!this.hasPassenger(passenger))
            return;

        if (this.isUsingSkill() && this.getSkillId() == 13) {
            double right = 0.0; // Middle front
            double forward = 1.0;
            double up = 0.5;
            // Animate the passenger location smoothly to follow the grabbing arm!
            if (skillTick <= 10) {
                // Tick 0-10: Player grabbed and lifted slightly
                up = 1;
                forward = 1.0;
            } else if (skillTick < 38) {
                // Tick 10-38: Boss holds player high above head while rising/hovering in the
                // sky
                up = 2;
                forward = 0.5;
            } else if (skillTick <= 41) {
                // Tick 38-41: The exact moment of the plunge, the arm visibly thrusts the
                // player directly into the floor!
                float progress = Math.min(1.0f, (skillTick - 38) / 3.0f);
                up = net.minecraft.util.math.MathHelper.lerp(progress, 2.0, -1.5);
                forward = net.minecraft.util.math.MathHelper.lerp(progress, 0.5, 1.5);
            } else {
                // Pinned to the floor after the slam
                up = -1.5;
                forward = 1.5;
            }

            Vec3d targetPos = getLocalOffsetTarget(forward, right, up);
            positionUpdater.accept(passenger, targetPos.x, targetPos.y, targetPos.z);
        } else {
            super.updatePassengerPosition(passenger, positionUpdater);
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
            if (tauntCooldown == 100) { // 100 ticks (5 seconds total elapsed since start) to allow it to fully finish
                                        // and blend
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
        LivingEntity currentTarget = this.getTarget();

        if (!this.getWorld().isClient() && dodgeCooldown <= 0 && Math.random() < 0.45 && !isUsingSkill()) {
            tryDodge();
            return false;
        }

        boolean result = super.damage(source, amount);

        // Smarter targeting: if we are hit by a new attacker, check if we should switch
        if (result && source.getAttacker() instanceof LivingEntity attacker) {
            if (currentTarget != null && currentTarget.isAlive() && currentTarget != attacker) {
                double distToCurrent = this.distanceTo(currentTarget);
                double distToNew = this.distanceTo(attacker);

                // If currently fighting someone close (< 8 blocks) and hit by someone far away,
                // stay focused on the close target.
                // RevengeGoal naturally swaps the target to the last attacker, so we swap back
                // if the distance gap is significant.
                if (distToCurrent < 8.0 && distToNew > distToCurrent + 2.0) {
                    this.setTarget(currentTarget);
                }
            }
        }

        return result;
    }

    /**
     * Spawns ground slash projectiles.
     * 
     * @param baseYaw The central direction (usually this.getYaw())
     * @param count   Number of slashes. 1 = forward, 8 = radial burst.
     */
    private void spawnGroundSlash(float baseYaw, int count, float multiplier) {
        if (this.getWorld().isClient())
            return;

        float angleStep = 360f / count;
        for (int i = 0; i < count; i++) {
            float yaw = baseYaw + (i * angleStep);
            GroundSlashAttackEntity slash = new GroundSlashAttackEntity(EntitiesManager.GROUND_SLASH_ATTACK,
                    this.getWorld());
            slash.setOwner(this);
            slash.setDamage((float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * multiplier);

            // Spawn slightly in front of boss
            double rad = Math.toRadians(yaw);
            double ox = -Math.sin(rad) * 1.5;
            double oz = Math.cos(rad) * 1.5;

            slash.setPosition(this.getX() + ox, this.getY(), this.getZ() + oz);
            slash.setDirection(yaw);
            this.getWorld().spawnEntity(slash);
        }
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

        for (LivingEntity target : nearby) {
            dealUnknownDamage(this, target, damage);
        }

        // Spawn visual blocks representing the ground slam impact (ripple effect)
        if (createBlock && this.getWorld() instanceof ServerWorld serverWorld) {
            // The boss position when the slam hits
            final double originX = this.getX();
            final double originY = this.getY();
            final double originZ = this.getZ();

            int ringIndex = 0;
            Set<BlockPos> spawnedPositions = new HashSet<>();

            for (double r = 1.5; r <= radius + 0.5; r += 1.5) {
                final double currentRadius = r;
                final int finalDelay = ringIndex / 2; // 2 rings per tick (0.5 tick per ring-step)

                Utils.addRunAfter(() -> {
                    int blockCount = (int) (currentRadius * 8); // denser rings
                    for (int i = 0; i < blockCount; i++) {
                        double angle = 2 * Math.PI * i / blockCount;
                        double x = originX + currentRadius * Math.cos(angle);
                        double z = originZ + currentRadius * Math.sin(angle);
                        BlockPos spawnPos = BlockPos.ofFloored(x, originY, z);

                        if (spawnedPositions.contains(spawnPos)) {
                            continue;
                        }
                        spawnedPositions.add(spawnPos);

                        // Try to use the block state from the ground for the effect
                        BlockState groundState = serverWorld.getBlockState(spawnPos.down());
                        if (groundState.isAir() || !groundState.isOpaqueFullCube(serverWorld, spawnPos.down())) {
                            continue;
                        }

                        Utils.CreateBlockSlamGround(serverWorld, groundState, spawnPos.down());
                    }
                }, finalDelay);

                ringIndex++;
            }
        }
        return nearby;
    }

    public static void dealUnknownDamage(LivingEntity attacker, LivingEntity target, float amount) {
        if (target.isBlocking() && target.getActiveItem().getItem() instanceof ShieldItem) {
            int shieldDamage = (int) (amount * 0.5f);
            if (shieldDamage > 0) {
                target.getActiveItem().damage(shieldDamage, target,
                        (e) -> e.sendToolBreakStatus(target.getActiveHand()));
            }
        }
        // this also deal additional damage if the target health is more than 100, deal
        // more than 1% of the target health
        if (!(target instanceof TameableEntity || target instanceof CustomTameableEntity) && target.getHealth() > 100) {
            target.damage(attacker.getDamageSources().mobAttack(attacker),
                    amount + Math.min(target.getMaxHealth() * 0.1f, 1000));
        } else {
            target.damage(attacker.getDamageSources().mobAttack(attacker), amount);
        }

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
                    dealUnknownDamage(this, target, damage);
                }
            }
        }

        // 2) Visuals propagating outwards
        int segments = (int) length;
        int blocksPerTick = 4; // 4 blocks per tick = much faster shockwave
        Set<BlockPos> spawnedPositions = new HashSet<>();

        for (int i = 1; i <= segments; i++) {
            final double dist = i;
            final int delay = (i - 1) / blocksPerTick;

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

                    if (spawnedPositions.contains(spawnPos)) {
                        continue;
                    }
                    spawnedPositions.add(spawnPos);

                    BlockState groundState = serverWorld.getBlockState(spawnPos.down());
                    if (groundState.isAir() || !groundState.isOpaqueFullCube(serverWorld, spawnPos.down())) {
                        continue;
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
        double dodgeSpeed = 2;

        this.setVelocity(impulseX * dodgeSpeed, this.getVelocity().y, impulseZ * dodgeSpeed);
        this.velocityModified = true;

        this.triggerDash(dodgeLeft, 10);
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundsManager.WHOOSH_1, this.getSoundCategory(), 1.0F, MathHelper.nextBetween(this.random, 0.8F, 1.2F));

        this.dodgeCooldown = 40;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isDisallowedInPeaceful() {
        return false;
    }

    @Override
    public boolean shouldDropXp() {
        return true;
    }

    @Override
    public boolean shouldFollowLeash() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("welcometomyworld", "entities/unknown");
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

        public boolean canStart() {
            return mob.isUsingSkill() && mob.dataTracker.get(SKILL_ID) != PREPARE_STEAL.id;
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }

        public boolean shouldContinue() {
            return mob.isUsingSkill() && mob.dataTracker.get(SKILL_ID) != PREPARE_STEAL.id;
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

        public boolean canStart() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill()
                    && mob.dataTracker.get(SKILL_ID) != PREPARE_STEAL.id;
        }

        public boolean shouldContinue() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill()
                    && mob.dataTracker.get(SKILL_ID) != PREPARE_STEAL.id;
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
