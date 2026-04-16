package com.trongthang.welcometomyworld.entities.Voidan;

import com.trongthang.welcometomyworld.Utilities.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.animation.WardenAnimations;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

import net.minecraft.nbt.NbtCompound;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

import java.util.ArrayList;
import net.minecraft.entity.ai.pathing.EntityNavigation;

import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.entities.ai.CustomPathNavigateGround;
import net.minecraft.entity.ai.pathing.PathNodeType;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Voidan extends HostileEntity implements GeoEntity {

    // Skills
    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(Voidan.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(Voidan.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SKILL_TRIGGER = DataTracker.registerData(Voidan.class,
            TrackedDataHandlerRegistry.INTEGER);

    public static class Skill {
        public int id;
        public int length;
        public int cooldown;

        public Skill(int id, int length, int cooldown) {
            this.id = id;
            this.length = length;
            this.cooldown = cooldown;
        }
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int globalSkillCooldown = 0;
    private final int[] skillCooldowns = new int[50]; // Increased buffer just in case we add up to 50 skills

    public static final Skill SLAM_GROUND = new Skill(1, 23, 120);
    public static final Skill HAND_SWING_LEFT_FRONT = new Skill(2, 20, 120);
    public static final Skill HAND_SWING_RIGHT_FRONT = new Skill(3, 20, 120);
    public static final Skill HAND_SWING_180_FRONT_THEN_SLAM_GROUND = new Skill(4, 40, 300);
    public static final Skill SONIC_BOOM = new Skill(5, 20, 200);
    public static final Skill EMERGE = new Skill(6, 68, 0);
    public static final Skill ROAR = new Skill(7, 70, 200);

    private boolean hasEmerged = false;

    private static final int SLAM_GROUND_HIT_TICK = 15;
    private static final int HAND_SWING_LEFT_FRONT_HIT_TICK = 10;
    private static final int HAND_SWING_RIGHT_FRONT_HIT_TICK = 10;
    private static final int[] HAND_SWING_180_FRONT_THEN_SLAM_GROUND_HIT_TICK = { 12, 25 };
    private static final int SONIC_BOOM_HIT_TICK = 9;

    private static final float SLAM_GROUND_DAMAGE_MULTIPLIER = 2.5f;
    private static final float HAND_SWING_LEFT_FRONT_DAMAGE_MULTIPLIER = 1.5f;
    private static final float HAND_SWING_RIGHT_FRONT_DAMAGE_MULTIPLIER = 1.5f;
    private static final float HAND_SWING_180_FRONT_THEN_SLAM_GROUND_DAMAGE_MULTIPLIER = 3.0f;
    private static final float SONIC_BOOM_DAMAGE_MULTIPLIER = 2.0f;

    public Voidan(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setStepHeight(1.5f); // Climb over single blocks without getting stuck
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 75666.0D) // Stronger basic stats
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 50.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 35.0D)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 35.0D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(SKILL_TRIGGER, 0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new CustomPathNavigateGround(this, world);
    }

    @Override
    protected void initGoals() {
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);

        this.goalSelector.add(1, new StopMoveWhenUsingSkill(this));
        this.goalSelector.add(2, new ChaseTargetGoal(this, 1.5D));
        this.goalSelector.add(4, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int[] prevSkillId = { 0 };
        int[] prevSkillTrigger = { 0 };

        controllers.add(new AnimationController<>(this, "mainController", 5, state -> {
            boolean isUsingSkill = this.dataTracker.get(IS_USING_SKILL);
            int skillId = this.dataTracker.get(SKILL_ID);

            state.getController().setAnimationSpeed(1.0D);

            if (isUsingSkill) {
                int skillTrigger = this.dataTracker.get(SKILL_TRIGGER);

                state.getController().transitionLength(1);
                if (prevSkillId[0] != skillId || prevSkillTrigger[0] != skillTrigger) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;
                prevSkillTrigger[0] = skillTrigger;

                switch (skillId) {
                    case 1:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("slam_ground"));
                    case 2:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("hand_swing_left_front"));
                    case 3:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("hand_swing_right_front"));
                    case 4:
                        return state.setAndContinue(
                                RawAnimation.begin().thenPlay("hand_swing_180_front_then_slam_ground"));
                    case 5:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("shoot_sonic_boom_1"));
                    case 6:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("emerge"));
                    case 7:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("roar"));
                }
            } else {
                prevSkillId[0] = 0;
            }

            // Normal movement — smooth transition, blend delay
            state.getController().transitionLength(3);
            boolean isMoving = this.getVelocity().x != 0 || this.getVelocity().z != 0;
            if (isMoving) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient()) {
            skillsHandler();
        }
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

                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                this.setYaw(targetYaw);
                this.bodyYaw = targetYaw;
                this.headYaw = targetYaw;
            }

            // Handle skill effects...
            handleSkillEffects(skillId);

            // Complete skill
            if (skillTick >= skillTotalTicks) {
                handleSkillCompletionChain(skillId);
            }
            return;
        }

        if (globalSkillCooldown <= 0 && !isUsingSkill()) {
            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                double dist = this.distanceTo(target);
                Skill picked = null;

                if (dist <= 4.0) {
                    // Close range
                    picked = pickWeightedSkill(
                            new Object[] { HAND_SWING_LEFT_FRONT, 35f, true },
                            new Object[] { HAND_SWING_RIGHT_FRONT, 35f, true },
                            new Object[] { HAND_SWING_180_FRONT_THEN_SLAM_GROUND, 30f, true });
                } else if (dist <= 8.0) {
                    // Mid range
                    picked = pickWeightedSkill(
                            new Object[] { SLAM_GROUND, 100f, true },
                            new Object[] { HAND_SWING_180_FRONT_THEN_SLAM_GROUND, 100f, true });
                } else if (dist <= 15.0) {
                    // Far range
                    picked = pickWeightedSkill(
                            new Object[] { SONIC_BOOM, 100f, true });
                } else if (dist <= 30.0) {
                    // Very Far range
                    picked = pickWeightedSkill(
                            new Object[] { SONIC_BOOM, 100f, true });
                }

                if (picked != null) {
                    triggerSkill(picked);
                }
            }
        }
    }

    private Skill pickWeightedSkill(Object[]... candidates) {
        float totalWeight = 0;
        List<Object[]> available = new ArrayList<>();

        for (Object[] candidate : candidates) {
            Skill skill = (Skill) candidate[0];
            float weight = (float) candidate[1];
            boolean condition = (boolean) candidate[2];

            if (condition && canUseSkill(skill)) {
                totalWeight += weight;
                available.add(candidate);
            }
        }

        if (available.isEmpty())
            return null;

        float randomVal = this.random.nextFloat() * totalWeight;
        float current = 0;
        for (Object[] candidate : available) {
            current += (float) candidate[1];
            if (randomVal <= current) {
                return (Skill) candidate[0];
            }
        }
        return null;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
    }

    @Override
    @org.jetbrains.annotations.Nullable
    public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world,
            net.minecraft.world.LocalDifficulty difficulty, net.minecraft.entity.SpawnReason spawnReason,
            @org.jetbrains.annotations.Nullable net.minecraft.entity.EntityData entityData,
            @org.jetbrains.annotations.Nullable net.minecraft.nbt.NbtCompound entityNbt) {
        if (!this.hasEmerged) {
            this.hasEmerged = true;
            this.dataTracker.set(IS_USING_SKILL, true);
            this.dataTracker.set(SKILL_ID, EMERGE.id);
            this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
            this.skillTick = 0;
            this.skillTotalTicks = EMERGE.length;
            this.skillCooldowns[EMERGE.id] = EMERGE.cooldown;
            this.globalSkillCooldown = 40;
            this.skillHitFired = false;
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasEmerged", this.hasEmerged);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HasEmerged")) {
            this.hasEmerged = nbt.getBoolean("HasEmerged");
        }
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (this.isUsingSkill() && this.dataTracker.get(SKILL_ID) == 6) { // Immune to damage while emerging
            return false;
        }
        return super.damage(source, amount);
    }

    private void handleSkillEffects(int skillId) {
        float atk = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        switch (skillId) {
            case 1: // SLAM_GROUND — radius-10 AoE at tick 15
                if (!skillHitFired && skillTick >= SLAM_GROUND_HIT_TICK) {
                    skillHitFired = true;
                    dealAoeGroundDamage(10.0, atk * SLAM_GROUND_DAMAGE_MULTIPLIER);
                }
                break;

            case 2: // HAND_SWING_LEFT_FRONT — small radius-3 front cone at tick 10
                if (!skillHitFired && skillTick >= HAND_SWING_LEFT_FRONT_HIT_TICK) {
                    skillHitFired = true;
                    dealFrontConeDamage(6.0, atk * HAND_SWING_LEFT_FRONT_DAMAGE_MULTIPLIER);
                }
                break;

            case 3: // HAND_SWING_RIGHT_FRONT — same as left
                if (!skillHitFired && skillTick >= HAND_SWING_RIGHT_FRONT_HIT_TICK) {
                    skillHitFired = true;
                    dealFrontConeDamage(6.0, atk * HAND_SWING_RIGHT_FRONT_DAMAGE_MULTIPLIER);
                }
                break;

            case 4: // HAND_SWING_180_FRONT_THEN_SLAM_GROUND
                // Hit 1 (tick 12): 180-degree paper fan sweep at the front
                if (skillTick == HAND_SWING_180_FRONT_THEN_SLAM_GROUND_HIT_TICK[0]) {
                    dealPaperFanDamage(8.0, atk * HAND_SWING_180_FRONT_THEN_SLAM_GROUND_DAMAGE_MULTIPLIER);
                }
                // Hit 2 (tick 25): ground slam AoE radius 10
                if (skillTick == HAND_SWING_180_FRONT_THEN_SLAM_GROUND_HIT_TICK[1]) {
                    dealAoeGroundDamage(10.0, atk * HAND_SWING_180_FRONT_THEN_SLAM_GROUND_DAMAGE_MULTIPLIER);
                }
                break;
            case 5: // SONIC_BOOM
                if (!skillHitFired && skillTick >= SONIC_BOOM_HIT_TICK) {
                    skillHitFired = true;
                    dealSonicBoomDamage(atk * SONIC_BOOM_DAMAGE_MULTIPLIER);
                }
                break;
            case 6: // EMERGE
                if (skillTick == 5 || skillTick == 10 || skillTick == 20) {
                    float intensity = skillTick == 5 ? 1.0f : (skillTick == 10 ? 2.5f : 5.0f);
                    sendCameraShakeToNearbyPlayers(30.0, intensity, 20);
                    this.playSound(SoundEvents.ENTITY_WARDEN_HEARTBEAT, 3.0F, 1.0F);

                    if (this.getWorld() instanceof ServerWorld sw) {
                        List<net.minecraft.server.network.ServerPlayerEntity> players = sw
                                .getPlayers(p -> p.distanceTo(this) < 30.0);
                        for (net.minecraft.server.network.ServerPlayerEntity p : players) {
                            p.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                                    net.minecraft.entity.effect.StatusEffects.DARKNESS, 200, 0, false, false));
                        }

                        // Particle effects
                        int particleCount = skillTick * 10;
                        sw.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(),
                                particleCount, 1.5, 0.5, 1.5, 0.1);
                        sw.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(),
                                particleCount / 2, 1.5, 0.5, 1.5, 0.1);
                        sw.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(),
                                particleCount / 3, 1.5, 0.5, 1.5, 0.1);

                        // Ground slam rippling visual
                        for (int x = -2; x <= 2; x++) {
                            for (int z = -2; z <= 2; z++) {
                                if (this.random.nextFloat() < 0.5f) {
                                    BlockPos pos = this.getBlockPos().add(x, -1, z);
                                    BlockState ground = sw.getBlockState(pos);
                                    if (!ground.isAir()) {
                                        Utils.CreateBlockSlamGround(sw, ground, pos.up(), 0.05f);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case 7: // ROAR
                if (skillTick == 17) {
                    sendCameraShakeToNearbyPlayers(40.0, 3.0f, 40);
                    this.playSound(SoundEvents.ENTITY_WARDEN_ROAR, 4.0F, 1.0F);
                }
                if (skillTick >= 17 && skillTick <= 52 && skillTick % 10 == 7) {
                    dealAoeDamage(15.0, atk * 1.5f);

                    if (this.getWorld() instanceof ServerWorld sw) {
                        LivingEntity target = this.getTarget();

                        // Calculate progression 't' from 0.0 to 1.0 (between ticks 17 and 47)
                        double t = Math.min(1.0, (skillTick - 17.0) / 30.0);

                        // Start a bit mostly in front of Voidan
                        net.minecraft.util.math.Vec3d startPos = this.getPos()
                                .add(this.getRotationVector().multiply(2.5));
                        net.minecraft.util.math.Vec3d endPos;

                        if (target != null && target.isAlive()) {
                            endPos = target.getPos();
                        } else {
                            // If no target, spawn in a circle/spread around the boss itself
                            double angle = (skillTick - 17.0) * 0.5; // Rotate spawn points
                            endPos = this.getPos().add(Math.sin(angle) * 10, 0, Math.cos(angle) * 10);
                        }

                        // Interpolate slowly towards the target/destination
                        net.minecraft.util.math.Vec3d waveCenter = startPos.lerp(endPos, t);

                        // Determine left and right offsets (perpendicular direction)
                        double dx = endPos.x - startPos.x;
                        double dz = endPos.z - startPos.z;
                        double dist = Math.sqrt(dx * dx + dz * dz);

                        double perpX = 0, perpZ = 0;
                        if (dist > 0.01) {
                            perpX = -dz / dist;
                            perpZ = dx / dist;
                        }

                        // Random spread between 2 and 4 blocks apart
                        double spread = 2.0 + (this.random.nextDouble() * 2.0);

                        for (int i = -1; i <= 1; i += 2) {
                            double randX = (this.random.nextDouble() - 0.5) * 1.5;
                            double randZ = (this.random.nextDouble() - 0.5) * 1.5;

                            BlockPos spawnPos = BlockPos.ofFloored(
                                    waveCenter.x + (perpX * spread * i) + randX,
                                    waveCenter.y,
                                    waveCenter.z + (perpZ * spread * i) + randZ);

                            // Drop them safely on the surface
                            BlockPos surfacePos = sw.getTopPosition(
                                    net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos);

                            VoidanTentacle tentacle = new VoidanTentacle(EntitiesManager.VOIDAN_TENTACLE, sw);
                            tentacle.refreshPositionAndAngles(surfacePos,
                                    target != null ? target.getYaw() : this.getYaw(), 0.0f);
                            tentacle.setSummoner(this);
                            if (target != null) {
                                tentacle.setTarget(target);
                            }
                            sw.spawnEntity(tentacle);
                        }
                    }
                }
                break;
        }
    }

    private void handleSkillCompletionChain(int skillId) {
        switch (skillId) {
            case 2: // HAND_SWING_LEFT_FRONT
                if (this.random.nextDouble() < 0.6 && canUseSkill(HAND_SWING_RIGHT_FRONT)) {
                    triggerSkill(HAND_SWING_RIGHT_FRONT);
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case 3: // HAND_SWING_RIGHT_FRONT
                if (this.random.nextDouble() < 0.6 && canUseSkill(HAND_SWING_LEFT_FRONT)) {
                    triggerSkill(HAND_SWING_LEFT_FRONT);
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
                break;
            case 6: // EMERGE
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                triggerSkill(ROAR);
                break;
            default:
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
                break;
        }
    }

    public boolean canUseSkill(Skill skill) {
        return skillCooldowns[skill.id] <= 0;
    }

    // -------------------------------------------------------------------------
    // AoE helpers
    // -------------------------------------------------------------------------

    /**
     * Hits all living entities within {@code radius} blocks and spawns ripple
     * blocks.
     */
    private void dealAoeGroundDamage(double radius, float damage) {
        if (this.getWorld().isClient())
            return;

        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            Unknown.dealUnknownDamage(this, t, damage);
        }

        if (!(this.getWorld() instanceof ServerWorld sw))
            return;
        final double ox = this.getX(), oy = this.getY(), oz = this.getZ();
        int ringIndex = 0;
        Set<BlockPos> spawned = new HashSet<>();
        for (double r = 1.5; r <= radius + 0.5; r += 1.5) {
            final double cr = r;
            final int delay = ringIndex / 2;
            Utils.addRunAfter(() -> {
                int blockCount = (int) (cr * 8);
                for (int i = 0; i < blockCount; i++) {
                    double angle = 2 * Math.PI * i / blockCount;
                    double x = ox + cr * Math.cos(angle);
                    double z = oz + cr * Math.sin(angle);
                    BlockPos pos = BlockPos.ofFloored(x, oy, z);
                    if (spawned.contains(pos))
                        continue;
                    spawned.add(pos);
                    BlockState ground = sw.getBlockState(pos.down());
                    if (ground.isAir() || !ground.isOpaqueFullCube(sw, pos.down()))
                        continue;
                    Utils.CreateBlockSlamGround(sw, ground, pos.down());
                }
            }, delay);
            ringIndex++;
        }
    }

    /**
     * Hits entities within {@code radius} that are in a ±60° cone directly in
     * front.
     */
    private void dealFrontConeDamage(double radius, float damage) {
        if (this.getWorld().isClient())
            return;

        Vec3d forward = this.getRotationVec(1.0F).normalize();
        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            Vec3d toTarget = t.getPos().subtract(this.getPos()).normalize();
            // dot > 0.5 ≈ within ±60°
            if (forward.dotProduct(toTarget) > 0.5) {
                Unknown.dealUnknownDamage(this, t, damage);
            }
        }
    }

    /**
     * Hits entities within {@code radius} that are in a 180° paper-fan arc at the
     * front.
     */
    private void dealPaperFanDamage(double radius, float damage) {
        if (this.getWorld().isClient())
            return;

        Vec3d forward = this.getRotationVec(1.0F).normalize();
        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            Vec3d toTarget = t.getPos().subtract(this.getPos()).normalize();
            // dot > 0 ≈ within ±90° (full 180° fan)
            if (forward.dotProduct(toTarget) > 0.0) {
                Unknown.dealUnknownDamage(this, t, damage);
            }
        }
    }

    /**
     * Hits the target with a Sonic Boom, ignoring walls and armor.
     */
    private void dealSonicBoomDamage(float damage) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld))
            return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive())
            return;

        Vec3d sourceVec = this.getPos().add(0, 3.5, 0); // Voidan's chest height
        Vec3d targetVec = target.getEyePos().subtract(sourceVec);
        Vec3d direction = targetVec.normalize();

        // Increase particle density and range for Voidan (similar to Warden but +7
        // blocks past target)
        for (int i = 1; i < MathHelper.floor(targetVec.length()) + 7; ++i) {
            Vec3d particlePos = sourceVec.add(direction.multiply(i));
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0.0D,
                    0.0D, 0.0D, 0.0D);
        }

        this.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 4.0F, 0.8F); // Slightly deeper and louder
        Unknown.dealUnknownDamage(this, target, damage);

        // Massive knockback
        double d = 1.0 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
        double e = 4.0 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
        target.addVelocity(direction.getX() * e, direction.getY() * d, direction.getZ() * e);
    }

    // -------------------------------------------------------------------------
    // AoE helpers
    // -------------------------------------------------------------------------

    private void dealAoeDamage(double radius, float damage) {
        if (this.getWorld().isClient())
            return;
        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            Unknown.dealUnknownDamage(this, t, damage);
        }
    }

    private void sendCameraShakeToNearbyPlayers(double radius, float intensity, int ticks) {
        if (this.getWorld().isClient())
            return;
        net.minecraft.util.math.Box area = this.getBoundingBox().expand(radius);
        List<net.minecraft.server.network.ServerPlayerEntity> playersInRadius = this.getWorld()
                .getEntitiesByClass(net.minecraft.server.network.ServerPlayerEntity.class, area, p -> true);

        for (net.minecraft.server.network.ServerPlayerEntity p : playersInRadius) {
            net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            buf.writeFloat(intensity);
            buf.writeInt(ticks);
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.CAMERA_SHAKE_PACKET_ID, buf);
        }
    }

    public void triggerSkill(Skill skill) {
        if (this.getWorld().isClient())
            return;

        // Snap rotation towards target before starting the skill
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

            this.setYaw(targetYaw);
            this.bodyYaw = targetYaw;
            this.headYaw = targetYaw;
            this.getLookControl().lookAt(target, 360.0F, 360.0F);
        }

        this.dataTracker.set(IS_USING_SKILL, true);
        this.dataTracker.set(SKILL_ID, skill.id);
        this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
        this.skillTick = 0;
        this.skillTotalTicks = skill.length;
        this.skillCooldowns[skill.id] = skill.cooldown;
        this.globalSkillCooldown = 40; // 2 seconds between ANY skills
        this.skillHitFired = false;

        this.getNavigation().stop();
    }

    public class StopMoveWhenUsingSkill extends Goal {
        private final Voidan mob;

        public StopMoveWhenUsingSkill(Voidan mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return mob.isUsingSkill();
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }

        @Override
        public boolean shouldContinue() {
            return mob.isUsingSkill();
        }

        @Override
        public void tick() {
            mob.getNavigation().stop();
        }
    }

    public class ChaseTargetGoal extends Goal {
        private final Voidan mob;
        private final double speed;

        public ChaseTargetGoal(Voidan mob, double speed) {
            this.mob = mob;
            this.speed = speed;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill();
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill();
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

    @Override
    protected void pushAway(net.minecraft.entity.Entity entity) {
        super.pushAway(entity);
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }
}
