package com.trongthang.welcometomyworld.entities.Unknown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
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
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.managers.SoundsManager;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

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

    private static final int SLAM_HIT_TICK = 15; // 0.75s × 20 TPS
    private static final int PUNCH_HIT_TICK = 20; // 1s × 20 TPS
    private static final int LEG_TRIP_HIT_TICK = 10;
    private static final int GROUND_SLAM_KICK_HIT_TICK = 20;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int dashTimer = 0;
    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int skillCooldown = 0;
    private int tauntCooldown = 0;
    private int dodgeCooldown = 0;

    public Unknown(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40f);
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
        this.goalSelector.add(4, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(5, new LookAroundGoal(this));

        // --- Target goals ---
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, true));
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
                return state.setAndContinue(RawAnimation.begin().thenLoop("unknown_taunt"));
            }
            return state.setAndContinue(RawAnimation.begin()); // Empty animation or stop
        }));
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

        // Stop movement during skill
        this.getNavigation().stop();
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    /**
     * Deals damage to all LivingEntities (except self) within a radius.
     * Reusable for any ground-slam or AoE attack.
     */
    public void dealAoeGroundDamage(double radius, float damage, boolean createBlock) {
        if (this.getWorld().isClient())
            return;
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

                delay += 2;
            }
        }
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

                // Case 1: Close range (Slam)
                if (distSq <= 3.5 * 3.5) {
                    // i want this to be random 50% slam 50% punch
                    if (Math.random() < 0.5) {
                        triggerSkill(SKILL_SLAM, 40);
                        skillCooldown = 80;
                    } else {

                        triggerSkill(SKILL_PUNCH, 40);
                        skillCooldown = 120;
                    }
                }
                // Case 2: Mid range (Chain Skill)
                else if (distSq <= 12.0 * 12.0) {
                    // Try to trigger chain skill: Dash -> Trip -> Kick
                    triggerSkill(SKILL_DASH_FORWARD, 15);
                    skillCooldown = 150; // Long cooldown for chain
                }
                // Case 3: Far range (Punch)
                else if (distSq <= 20.0 * 20.0) {
                    triggerSkill(SKILL_PUNCH, 40);
                    skillCooldown = 120;
                }
            }
        }

        // --- Taunt logic ---
        if (skillCooldown > 0 && tauntCooldown <= 0 && !isUsingSkill()) {
            tauntCooldown = 200;
            boolean tauntChance = Math.random() < 0.5;
            LivingEntity tauntTarget = this.getTarget();
            if (tauntTarget != null && tauntTarget.isAlive()) {
                double distSq = this.squaredDistanceTo(tauntTarget);
                if (distSq > 10.0 * 10.0 && tauntChance) {
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
                    dealLinearShockwaveDamage(3.0, 20.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f);
                }
                break;
            case SKILL_DASH_FORWARD:
                if (skillTick == 5) {
                    LivingEntity target = this.getTarget();
                    if (target != null) {
                        // Teleport to target
                        double tx = target.getX();
                        double ty = target.getY();
                        double tz = target.getZ();

                        // Effects at old position
                        if (this.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1, this.getZ(),
                                    10, 0.5, 1.0, 0.5, 0.1);
                        }

                        this.teleport(tx, ty, tz, true);

                        // Effects at new position
                        if (this.getWorld() instanceof ServerWorld serverWorld) {
                            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, tx, ty + 1, tz, 15, 0.5, 1.0, 0.5,
                                    0.05);
                        }
                    }
                }
                break;
            case SKILL_LEG_TRIP:
                if (!skillHitFired && skillTick >= LEG_TRIP_HIT_TICK) {
                    skillHitFired = true;
                    dealAoeGroundDamage(4.0,
                            (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.7f, false);
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
        }

        if (!this.getWorld().isClient()) {
            skillsHandler();
            if (dodgeCooldown > 0)
                dodgeCooldown--;
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hurt = super.damage(source, amount);
        if (hurt && !this.getWorld().isClient() && dodgeCooldown <= 0 && Math.random() < 0.45) {
            tryDodge();
        }
        return hurt;
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
        this.dodgeCooldown = 60;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
