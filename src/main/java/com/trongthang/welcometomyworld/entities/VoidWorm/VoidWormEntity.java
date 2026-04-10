package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import com.trongthang.welcometomyworld.VoidBossState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class VoidWormEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Configurable size
    private static final int BODY_SEGMENTS = 10;
    private final List<VoidWormPartEntity> parts = new ArrayList<>();
    private boolean partsSpawned = false;

    // Rolling position history for body-segment trail following
    private static final int MAX_HISTORY = 400;
    public int ticksSinceDeath = 0; // The timer for the death animation
    private final Deque<Vec3d> posHistoryDeque = new ArrayDeque<>(MAX_HISTORY + 1);
    // Cached list view — rebuilt only when the deque changes, used by segments
    private List<Vec3d> posHistorySnapshot = new ArrayList<>(MAX_HISTORY);
    private boolean historyDirty = false;

    public List<Vec3d> getPosHistory() {
        if (historyDirty) {
            posHistorySnapshot = new ArrayList<>(posHistoryDeque);
            historyDirty = false;
        }
        return posHistorySnapshot;
    }

    public void registerPart(VoidWormPartEntity part) {
        if (!parts.contains(part)) {
            parts.add(part);
        }
    }

    private static final float PART_DISTANCE = 8f;

    // Custom visual pitch to bypass vanilla LookControl pitch resetting
    public float visualPitch = 0.0f;
    public float prevVisualPitch = 0.0f;
    public float visualYaw = 0.0f;
    public float prevVisualYaw = 0.0f;

    // Client-side low-pass filtered velocity — eliminates per-tick noise before
    // pitch is derived
    private Vec3d smoothedVelocity = Vec3d.ZERO;

    // Server-side persistent rotation to avoid vanilla resetting pitch/yaw to 0
    private float serverSidePitch = 0.0f;
    private float serverSideYaw = 0.0f;

    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SKILL_PREPARING = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SKILL_TRIGGER = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TICKS_SINCE_DEATH = DataTracker.registerData(VoidWormEntity.class,
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

    public static final Skill ROAR = new Skill(1, 80, 500);
    public static final Skill CHARGE_ATTACK = new Skill(2, 60, 300);

    private static final int ROAR_HIT_TICK = 22;
    private double chargeDestX, chargeDestY, chargeDestZ;

    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int globalSkillCooldown = 0;
    public int combatTicks = 0;

    private final int[] skillCooldowns = new int[10];

    public boolean canUseSkill(Skill skill) {
        return skillCooldowns[skill.id] <= 0;
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    public int getSkillId() {
        return this.dataTracker.get(SKILL_ID);
    }

    public void triggerSkill(Skill skill) {
        if (this.getWorld().isClient())
            return;
        this.dataTracker.set(IS_USING_SKILL, true);
        this.dataTracker.set(SKILL_PREPARING, true);
        this.dataTracker.set(SKILL_ID, skill.id);
        this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
        this.skillTick = 0;
        this.skillTotalTicks = skill.length;
        this.skillCooldowns[skill.id] = skill.cooldown;
        this.globalSkillCooldown = 40;
        this.skillHitFired = false;
    }

    public VoidWormEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true; // allow passing through blocks
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_PREPARING, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(SKILL_TRIGGER, 0);
        this.dataTracker.startTracking(TICKS_SINCE_DEATH, 0);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.5D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 150.0D);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(2, new BossFlightGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int[] prevSkillId = { 0 };
        int[] prevSkillTrigger = { 0 };

        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            boolean isUsingSkill = this.dataTracker.get(IS_USING_SKILL);
            int skillId = this.dataTracker.get(SKILL_ID);

            state.getController().setAnimationSpeed(1.0D);

            if (isUsingSkill) {
                int skillTrigger = this.dataTracker.get(SKILL_TRIGGER);

                // Roar must start immediately so animation and sound stay in sync
                state.getController().transitionLength(skillId == 1 ? 0 : 5);
                if (prevSkillId[0] != skillId || prevSkillTrigger[0] != skillTrigger) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;
                prevSkillTrigger[0] = skillTrigger;

                if (skillId == 1) { // ROAR
                    return state.setAndContinue(RawAnimation.begin().thenPlay("void_worm_head_roar"));
                } else if (skillId == 2) { // CHARGE_ATTACK
                    if (!this.dataTracker.get(SKILL_PREPARING)) {
                        return state.setAndContinue(RawAnimation.begin().thenPlay("attack_openning_mouth"));
                    } else {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("moving"));
                    }
                }
            }
            prevSkillId[0] = 0;

            state.getController().transitionLength(5);
            // Fallback to 'moving' if 'void_worm_head_idle' is missing in the latest JSON
            return state.setAndContinue(RawAnimation.begin().thenLoop("moving"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state,
            net.minecraft.util.math.BlockPos landedPosition) {
        // No fall damage
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL
                && this.isDisallowedInPeaceful()) {
            this.discard();
        } else {
            this.despawnCounter = 0;
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("PartsSpawned", this.partsSpawned);
        nbt.putInt("TicksSinceDeath", this.ticksSinceDeath);

        net.minecraft.nbt.NbtList historyList = new net.minecraft.nbt.NbtList();
        for (Vec3d pos : posHistoryDeque) {
            NbtCompound posTag = new NbtCompound();
            posTag.putDouble("x", pos.x);
            posTag.putDouble("y", pos.y);
            posTag.putDouble("z", pos.z);
            historyList.add(posTag);
        }
        nbt.put("PosHistory", historyList);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("PartsSpawned")) {
            this.partsSpawned = nbt.getBoolean("PartsSpawned");
        }
        if (nbt.contains("TicksSinceDeath")) {
            this.ticksSinceDeath = nbt.getInt("TicksSinceDeath");
            this.dataTracker.set(TICKS_SINCE_DEATH, this.ticksSinceDeath);
        }

        if (nbt.contains("PosHistory", 9)) {
            net.minecraft.nbt.NbtList historyList = nbt.getList("PosHistory", 10);
            posHistoryDeque.clear();
            for (int i = 0; i < historyList.size(); i++) {
                NbtCompound posTag = historyList.getCompound(i);
                posHistoryDeque.add(new Vec3d(posTag.getDouble("x"), posTag.getDouble("y"), posTag.getDouble("z")));
            }
            historyDirty = true;
        }
    }

    @Override
    public void tick() {
        this.noClip = true; // Ensure it stays true
        this.setNoGravity(true); // Ensure it stays set

        if (this.getWorld().isClient) {
            this.prevVisualPitch = this.visualPitch;
            this.prevVisualYaw = this.visualYaw;
        }

        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Keep the chunk loaded while the head is alive and ticking
            serverWorld.getChunkManager().addTicket(net.minecraft.server.world.ChunkTicketType.PORTAL,
                    new net.minecraft.util.math.ChunkPos(this.getBlockPos()), 3, this.getBlockPos());

            // Record head position for body-trail history (newest first)
            posHistoryDeque.addFirst(this.getPos());
            if (posHistoryDeque.size() > MAX_HISTORY)
                posHistoryDeque.removeLast();
            historyDirty = true;

            if (!partsSpawned) {
                spawnParts(serverWorld);
                partsSpawned = true;
            }
            updateParts();
            skillsHandler();

            // Persistently track this boss
            if (this.age % 20 == 0) {
                VoidBossState state = VoidBossState.getServerState(serverWorld);
                state.bossUuid = this.getUuid();
                state.lastBossPos = this.getBlockPos();
                state.markDirty();
            }

            if (this.getTarget() != null) {
                combatTicks = 400; // 20 seconds of combat state retention when target is lost
            } else if (combatTicks > 0) {
                combatTicks--;
            }
        } else {
            // Client side visual interpolation
            // Low-pass filter raw velocity to kill per-tick micro-variance before
            // deriving pitch — 0.1 blend keeps it responsive but jitter-free.
            boolean isRoaring = this.isUsingSkill() && this.getSkillId() == 1 && !this.dataTracker.get(SKILL_PREPARING);

            if (isRoaring) {
                this.visualPitch += MathHelper.wrapDegrees(-60.0f - this.visualPitch) * 0.15f;
            } else {
                Vec3d vel = this.getVelocity();
                this.smoothedVelocity = this.smoothedVelocity.add(vel.subtract(this.smoothedVelocity).multiply(0.1));
                if (this.smoothedVelocity.lengthSquared() > 0.001) {
                    double horizLen = Math.sqrt(this.smoothedVelocity.x * this.smoothedVelocity.x
                            + this.smoothedVelocity.z * this.smoothedVelocity.z);
                    float derivedPitch = (float) (Math.atan2(this.smoothedVelocity.y, horizLen) * (180.0 / Math.PI));
                    this.visualPitch += MathHelper.wrapDegrees(derivedPitch - this.visualPitch) * 0.3f;
                }
            }
            // else: hold last visualPitch while stationary (e.g. during ROAR)
            this.visualYaw += MathHelper.wrapDegrees(this.getYaw() - this.visualYaw) * 0.15f;
        }
    }

    public void dealDiveAoeDamage(double radius, float damage, boolean createBlock) {
        if (this.getWorld().isClient())
            return;
        net.minecraft.util.math.Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(
                LivingEntity.class, area, e -> e.isAlive() && e != this && !(e instanceof VoidWormPartEntity));

        for (LivingEntity target : nearby) {
            target.damage(this.getDamageSources().mobAttack(this), damage);
            // give effects
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
        }

        if (createBlock && this.getWorld() instanceof ServerWorld serverWorld) {
            final double originX = this.getX();
            final double originY = this.getY();
            final double originZ = this.getZ();
            int ringIndex = 0;
            java.util.Set<net.minecraft.util.math.BlockPos> spawnedPositions = new java.util.HashSet<>();

            for (double r = 1.5; r <= radius + 0.5; r += 1.5) {
                final double currentRadius = r;
                final int finalDelay = ringIndex / 2; // 2 rings per tick (0.5 tick per ring-step)

                Utils.addRunAfter(() -> {
                    int blockCount = (int) (currentRadius * 8); // denser rings
                    for (int i = 0; i < blockCount; i++) {
                        double angle = 2 * Math.PI * i / blockCount;
                        double x = originX + currentRadius * Math.cos(angle);
                        double z = originZ + currentRadius * Math.sin(angle);
                        net.minecraft.util.math.BlockPos spawnPos = net.minecraft.util.math.BlockPos.ofFloored(x,
                                originY, z);

                        if (spawnedPositions.contains(spawnPos)) {
                            continue;
                        }
                        spawnedPositions.add(spawnPos);

                        net.minecraft.block.BlockState groundState = serverWorld.getBlockState(spawnPos.down());
                        if (groundState.isAir() || !groundState.isOpaqueFullCube(serverWorld, spawnPos.down())) {
                            continue;
                        }

                        Utils.CreateBlockSlamGround(serverWorld, groundState, spawnPos.down());
                    }
                }, finalDelay);

                ringIndex++;
            }
        }
    }

    private void dealAoeDamage(double radius, float amount) {
        net.minecraft.util.math.Box box = this.getBoundingBox().expand(radius);
        List<LivingEntity> entities = this.getWorld().getEntitiesByClass(LivingEntity.class, box, entity -> {
            return entity.isAlive() && !(entity instanceof VoidWormEntity) && !(entity instanceof VoidWormPartEntity);
        });
        for (LivingEntity entity : entities) {
            double distSq = this.squaredDistanceTo(entity);
            if (distSq <= radius * radius) {
                entity.damage(this.getDamageSources().mobAttack(this), amount);
                // give blind effect
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 10, 1));
            }
        }
    }

    private float smoothAngle(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (delta > maxStep)
            delta = maxStep;
        if (delta < -maxStep)
            delta = -maxStep;
        return current + delta;
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
            boolean isPreparing = this.dataTracker.get(SKILL_PREPARING);
            int skillId = this.dataTracker.get(SKILL_ID);
            LivingEntity target = getTarget();

            if (skillId == 2) { // CHARGE_ATTACK
                if (target != null && target.isAlive()) {
                    if (isPreparing) {
                        double targetYHover = target.getY() + 40.0D;
                        Vec3d dir = new Vec3d(target.getX() - this.getX(), targetYHover - this.getY(),
                                target.getZ() - this.getZ());
                        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        if (dir.lengthSquared() > 0.1)
                            dir = dir.normalize();

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.0;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        double dx = target.getX() - this.getX();
                        double dy = targetYHover - this.getY();
                        double dz = target.getZ() - this.getZ();

                        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                        // Removed negative sign, positive pitch is UP, negative is DOWN
                        float targetPitch = (float) (MathHelper.atan2(dy, distXZ) * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.bodyYaw = this.getYaw();
                        this.headYaw = this.getYaw();
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        if (this.getY() >= targetYHover - 4.0D && distXZ <= 8.0D) {
                            this.dataTracker.set(SKILL_PREPARING, false);
                            this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1); // trigger
                                                                                                          // animation
                                                                                                          // reset
                            this.chargeDestX = target.getX();
                            this.chargeDestY = target.getY();
                            this.chargeDestZ = target.getZ();
                            this.setVelocity(0, 0, 0);
                        }
                    } else {
                        skillTick++;

                        if (skillTick < 5) {
                            this.setVelocity(0, 0, 0);
                            this.velocityModified = true;
                            // Face destination
                            double dx = this.chargeDestX - this.getX();
                            double dy = this.chargeDestY - this.getY();
                            double dz = this.chargeDestZ - this.getZ();
                            double distXZ = Math.sqrt(dx * dx + dz * dz);
                            this.serverSideYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                            // Fix pitch inversion here as well
                            this.serverSidePitch = (float) (MathHelper.atan2(dy, distXZ) * (180.0 / Math.PI));

                            this.setYaw(this.serverSideYaw);
                            this.setPitch(this.serverSidePitch);
                            this.bodyYaw = this.getYaw();
                            this.headYaw = this.getYaw();

                        } else if (!skillHitFired) {
                            double dx = this.chargeDestX - this.getX();
                            double dy = this.chargeDestY - this.getY();
                            double dz = this.chargeDestZ - this.getZ();
                            Vec3d dir = new Vec3d(dx, dy, dz);
                            double distSq = dir.lengthSquared();
                            if (dir.lengthSquared() > 0.01)
                                dir = dir.normalize();

                            double chargeSpeed = 4.0; // very fast dive
                            this.setVelocity(dir.multiply(chargeSpeed));
                            this.velocityModified = true;

                            boolean hitGround = this.horizontalCollision || this.verticalCollision;
                            if (distSq < 16.0 || hitGround || this.getY() <= this.chargeDestY + 2.0) {
                                dealDiveAoeDamage(20.0,
                                        (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 3.0f,
                                        true);
                                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                        SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY,
                                        net.minecraft.sound.SoundCategory.HOSTILE, 2.0F, 1.0F);

                                this.dataTracker.set(IS_USING_SKILL, false);
                                this.dataTracker.set(SKILL_PREPARING, false);
                                this.dataTracker.set(SKILL_ID, 0);
                            }
                        }
                    }
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.9D));
                    this.velocityModified = true;
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
            } else if (skillId == 1) { // ROAR
                if (target != null && target.isAlive()) {
                    double targetX = target.getX();
                    double targetY = target.getY() + 20.0D;
                    double targetZ = target.getZ();

                    if (isPreparing) {
                        // Fly to position (30 blocks above target)
                        Vec3d dir = new Vec3d(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
                        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        if (dir.lengthSquared() > 0.1) {
                            dir = dir.normalize();
                        }

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.0;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        double dx = targetX - this.getX();
                        double dy = targetY - this.getY();
                        double dz = targetZ - this.getZ();
                        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

                        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                        float targetPitch = (float) -(MathHelper.atan2(dy, horizontalDist) * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.bodyYaw = this.getYaw();
                        this.headYaw = this.getYaw();
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        if (this.getY() >= targetY - 4.0D && distXZ <= 8.0D) {
                            // Reached position: start animation AND sound at the same tick.
                            // Both have identical 0→1s buildups, so they sync perfectly.

                            this.dataTracker.set(SKILL_PREPARING, false);
                            this.setVelocity(0, 0, 0);
                        }
                    } else {
                        skillTick++;

                        // Hold position while roaring
                        double currentY = this.getY();
                        double upVel = 0;
                        if (currentY < targetY - 0.5) {
                            upVel = Math.min(1.0, targetY - currentY) * 0.15;
                        } else if (currentY > targetY + 1.5) {
                            upVel = -0.05;
                        }
                        this.setVelocity(this.getVelocity().x * 0.5, upVel, this.getVelocity().z * 0.5);
                        this.velocityModified = true;

                        // Look straight down
                        this.serverSidePitch = -60.0F;
                        this.setPitch(this.serverSidePitch);

                        if (skillTick >= ROAR_HIT_TICK && skillTick <= 95) {
                            if (skillTick % 11 == 0) {
                                dealAoeDamage(64.0,
                                        (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 2.0f);
                            }
                        }
                    }
                } else {
                    this.setVelocity(this.getVelocity().multiply(0.9D));
                    this.velocityModified = true;
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
            }

            if (!this.dataTracker.get(SKILL_PREPARING) && skillTick >= skillTotalTicks) {
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
            }
            return;
        }

        // Trigger logic
        if (globalSkillCooldown <= 0 && !isUsingSkill()) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                // Ignore target if too far from center (0, 50, 0)
                Vec3d center = new Vec3d(0, 50, 0);
                if (target.getPos().distanceTo(center) > 500.0) {
                    this.setTarget(null);
                    return;
                }

                double distX = Math.abs(target.getX() - this.getX());
                double distZ = Math.abs(target.getZ() - this.getZ());
                double distY = Math.abs(target.getY() - this.getY());

                boolean canCharge = canUseSkill(CHARGE_ATTACK) && target.getY() - this.getY() >= 15.0 && distX < 64.0
                        && distZ < 64.0;
                if (canCharge) {
                    triggerSkill(CHARGE_ATTACK);
                    // Open mouth sound maybe handled inside or automatically by the animation if
                    // registered in GeckoLib
                } else if (canUseSkill(ROAR) && distX < 64.0 && distZ < 64.0 && distY < 60.0) {
                    triggerSkill(ROAR);
                    Utils.playFarSound((ServerWorld) this.getWorld(), this, SoundsManager.MONSTER_ROAR,
                            net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 1.0F, 84.0);
                }
            }
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9D)); // Apply custom air friction
        }
    }

    private void spawnParts(ServerWorld world) {
        for (int i = 0; i < BODY_SEGMENTS; i++) {
            VoidWormPartEntity body = new VoidWormPartEntity(EntitiesManager.VOID_WORM_BODY, world, this, i + 1,
                    PART_DISTANCE);
            body.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            world.spawnEntity(body);
            parts.add(body);
        }

        VoidWormPartEntity tail = new VoidWormPartEntity(EntitiesManager.VOID_WORM_TAIL, world, this, BODY_SEGMENTS + 1,
                PART_DISTANCE);
        tail.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        world.spawnEntity(tail);
        parts.add(tail);
    }

    @Override
    protected void updatePostDeath() {
        this.ticksSinceDeath++;
        if (!this.getWorld().isClient()) {
            this.dataTracker.set(TICKS_SINCE_DEATH, this.ticksSinceDeath);
        }

        if (this.getWorld() instanceof ServerWorld) {
            // Continue recording position history so segments follow the head up
            posHistoryDeque.addFirst(this.getPos());
            if (posHistoryDeque.size() > MAX_HISTORY)
                posHistoryDeque.removeLast();
            historyDirty = true;

            updateParts();
        }

        // After 200 ticks (10 seconds), actually remove the entity
        if (this.ticksSinceDeath >= 200 && !this.getWorld().isClient()) {
            this.getWorld().sendEntityStatus(this, (byte) 60); // Death smoke particles
            this.remove(RemovalReason.KILLED);
        }
    }

    private void updateParts() {
        // Only discard parts if the head is actually removed OR if the death animation
        // is finished
        if (this.isRemoved() && (this.getRemovalReason() == null || this.getRemovalReason().shouldDestroy()
                || this.ticksSinceDeath >= 200)) {
            for (VoidWormPartEntity part : parts) {
                if (part != null)
                    part.discard();
            }
            return;
        }

        // Each part handles its own position following its leader
        // But we can also force updates if needed
    }

    @Override
    public void remove(RemovalReason reason) {
        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("head_remove", "removing head", "reason", reason,
                "uuid", this.getUuid());
        super.remove(reason);
        for (VoidWormPartEntity part : parts) {
            part.remove(reason);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsManager.VOID_WORM_AMBIENT_1;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        // Just discard locally, actual removal of parts handled separately if they were
        // killed
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.IN_WALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.FALL) ||
                source.isOf(net.minecraft.entity.damage.DamageTypes.DROWN)) {
            return false;
        }
        return super.damage(source, amount);
    }

    // Default flight AI: orbits the target at a safe radius, wanders when no
    // target.
    // Skills are the only way the worm closes in on the player.
    static class BossFlightGoal extends Goal {
        private static final double ORBIT_RADIUS = 50;
        private final VoidWormEntity worm;
        Vec3d wanderTarget = null;

        public BossFlightGoal(VoidWormEntity worm) {
            this.worm = worm;
        }

        @Override
        public boolean canStart() {
            return !worm.isUsingSkill();
        }

        @Override
        public boolean shouldContinue() {
            return !worm.isUsingSkill();
        }

        @Override
        public void start() {
            // After a skill, the worm may be directly above the target (distH ≈ 0).
            // Kick it in the true tangential direction (perpendicular to worm→target)
            // so it immediately starts circling instead of possibly bolting outward.
            LivingEntity target = worm.getTarget();
            if (target != null && target.isAlive()) {
                double toTargetX = target.getX() - worm.getX();
                double toTargetZ = target.getZ() - worm.getZ();
                double distH = Math.sqrt(toTargetX * toTargetX + toTargetZ * toTargetZ);
                if (distH < ORBIT_RADIUS * 0.6) {
                    double speed = worm.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    // Perpendicular to worm→target (true tangential = start circling)
                    double normX = distH > 0.01 ? toTargetX / distH : 1.0;
                    double normZ = distH > 0.01 ? toTargetZ / distH : 0.0;
                    worm.setVelocity(-normZ * speed, 0, normX * speed);
                    worm.velocityModified = true;
                }
            }
        }

        @Override
        public void stop() {
            // Force a fresh wander target after any skill finishes.
            wanderTarget = null;
        }

        @Override
        public void tick() {
            LivingEntity target = worm.getTarget();
            double speed = worm.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            Vec3d currentVel = worm.getVelocity();

            if (target != null && target.isAlive()) {
                // Clear any wander target immediately when entering combat
                wanderTarget = null;

                // Stay within 500 blocks of (0, 50, 0)
                Vec3d center = new Vec3d(0, 50, 0);
                if (target.getPos().distanceTo(center) > 500.0) {
                    worm.setTarget(null);
                    return;
                }

                // Orbit the target at ORBIT_RADIUS. Skills are the only way to close in.
                Vec3d wormPos = worm.getPos();

                // Vertical Roaming: Oscillate height between -16 and 16 blocks relative to
                // target
                double heightOffset = MathHelper.sin(worm.age * 0.04f) * 16.0;
                double orbitY = target.getY() + heightOffset;

                double toTargetX = target.getX() - wormPos.x;
                double toTargetZ = target.getZ() - wormPos.z;
                double distH = Math.sqrt(toTargetX * toTargetX + toTargetZ * toTargetZ);

                // Radial unit vector (worm → target, horizontal)
                Vec3d radial = distH > 0.01
                        ? new Vec3d(toTargetX / distH, 0, toTargetZ / distH)
                        : Vec3d.ZERO;
                // Tangential (clockwise orbit)
                Vec3d tangential = new Vec3d(-radial.z, 0, radial.x);

                // Positive radialError → too far, steer in; negative → too close, steer out
                double radialError = distH - ORBIT_RADIUS;
                double verticalError = orbitY - wormPos.y;

                // Build desired velocity: tangential orbit + radial correction + vertical
                double targetSpeed = speed * 1.5D;
                Vec3d desiredDir = tangential.multiply(1.0)
                        .add(radial.multiply(MathHelper.clamp(radialError * 0.05, -1.0, 1.0)))
                        .add(new Vec3d(0, MathHelper.clamp(verticalError * 0.08, -0.5, 0.5), 0));

                // Small weave baked into the desired direction (before normalizing)
                double lenH = Math.sqrt(desiredDir.x * desiredDir.x + desiredDir.z * desiredDir.z);
                Vec3d rightVec = lenH > 0.01 ? new Vec3d(-desiredDir.z, 0, desiredDir.x).normalize() : Vec3d.ZERO;
                desiredDir = desiredDir
                        .add(rightVec.multiply(MathHelper.sin(worm.age * 0.15f) * 0.12))
                        .add(0, MathHelper.cos(worm.age * 0.08f) * 0.06, 0);

                Vec3d desiredVel = desiredDir.lengthSquared() > 0.0001
                        ? desiredDir.normalize().multiply(targetSpeed)
                        : currentVel;

                // Blend current velocity toward desired. 0.25 reaches full speed in ~5 ticks
                // while still smoothing sharp direction changes.
                Vec3d newVel = currentVel.add(desiredVel.subtract(currentVel).multiply(0.50));

                worm.setVelocity(newVel);
            } else {
                // Organic Wandering AI
                // Accept radius = 40 blocks (1600 sq), interval = 400 ticks to ensure it
                // reaches far destinations.
                if (wanderTarget == null || worm.squaredDistanceTo(wanderTarget) < 1600.0 || worm.age % 400 == 0) {
                    // Forward-weighted U-turns (-75 to +75 degrees): prevents circling back
                    float randomYaw = worm.getYaw() + (worm.getRandom().nextFloat() * 150f - 75f);
                    // Gentle swoops (-40 to +40 degrees)
                    float randomPitch = (worm.getRandom().nextFloat() * 80f - 40f);

                    float radYaw = randomYaw * ((float) Math.PI / 180F);
                    float radPitch = randomPitch * ((float) Math.PI / 180F);

                    // Pick a far distant point
                    double distance;
                    if (worm.combatTicks > 0) {
                        distance = 20.0 + worm.getRandom().nextDouble() * 40.0; // Reduced distance while in combat
                    } else {
                        distance = 100.0 + worm.getRandom().nextDouble() * 100.0;
                    }
                    double x = worm.getX() - MathHelper.sin(radYaw) * MathHelper.cos(radPitch) * distance;
                    double y = worm.getY() - MathHelper.sin(radPitch) * distance;
                    double z = worm.getZ() + MathHelper.cos(radYaw) * MathHelper.cos(radPitch) * distance;

                    // Animal Instinct: Breaching behavior & terrain avoidance
                    int surfaceY = worm.getWorld().getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            (int) worm.getX(), (int) worm.getZ());

                    if (worm.getY() < surfaceY + 15) {
                        // Getting too low! Steer back up to the sky
                        y = surfaceY + (40.0 + worm.getRandom().nextDouble() * 40.0);
                    } else if (worm.getY() > Math.max(120, surfaceY + 80)) {
                        // Too high above the surface! Swoop back down gently
                        y = worm.getY() - (30.0 + worm.getRandom().nextDouble() * 40.0);
                    } else if (worm.getY() < 0) {
                        y = worm.getY() + (40.0 + worm.getRandom().nextDouble() * 40.0);
                    }

                    // Map vertical limits safety
                    double minY = worm.getWorld().getBottomY() + 40.0;
                    double maxY = worm.getWorld().getTopY() - 40.0;
                    if (y < minY)
                        y = minY + worm.getRandom().nextDouble() * 40.0;
                    if (y > maxY)
                        y = maxY - worm.getRandom().nextDouble() * 40.0;

                    // Clamp wander target within 500 blocks of (0, 50, 0)
                    Vec3d home = new Vec3d(0, 50, 0);
                    Vec3d targetPos = new Vec3d(x, y, z);
                    if (targetPos.distanceTo(home) > 450.0) { // Aim for slightly inside the 500 limit
                        Vec3d dirToHome = home.subtract(targetPos).normalize();
                        targetPos = home.add(dirToHome.multiply(-450.0));
                        x = targetPos.x;
                        y = targetPos.y;
                        z = targetPos.z;
                    }

                    wanderTarget = new Vec3d(x, y, z);
                }

                // Smoothly steer towards wanderTarget
                // Build desired velocity toward wanderTarget with weave baked in
                Vec3d toTarget = wanderTarget.subtract(worm.getPos());
                Vec3d dir = toTarget.lengthSquared() > 0.0001 ? toTarget.normalize() : Vec3d.ZERO;

                double targetSpeed = speed * 0.8D;
                double lenTarget = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                Vec3d rightVec = lenTarget > 0.01 ? new Vec3d(-dir.z, 0, dir.x).normalize() : Vec3d.ZERO;
                Vec3d desiredDir = dir
                        .add(rightVec.multiply(MathHelper.sin(worm.age * 0.1f) * 0.1))
                        .add(0, MathHelper.cos(worm.age * 0.05f) * 0.05, 0);

                Vec3d desiredVel = desiredDir.lengthSquared() > 0.0001
                        ? desiredDir.normalize().multiply(targetSpeed)
                        : currentVel;

                // Blend toward desired: 0.2 reaches full speed quickly, wide turns still feel
                // natural
                Vec3d newVel = currentVel.add(desiredVel.subtract(currentVel).multiply(0.2));

                worm.setVelocity(newVel);
            }

            // Visual Angle Integration - derive head rotation from actual velocity
            Vec3d vel = worm.getVelocity();
            double velLenSq = vel.lengthSquared();
            if (velLenSq > 0.0001) {
                float targetYaw = (float) (MathHelper.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90.0F;
                // 8°/tick for yaw: head turns faster so it tracks the body's arc well
                worm.serverSideYaw = wrapDegrees(worm.serverSideYaw, targetYaw, 8.0f);
                worm.setYaw(worm.serverSideYaw);
                worm.bodyYaw = worm.getYaw();
                worm.headYaw = worm.getYaw();

                double horizontalDist = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                float targetPitch = (float) (MathHelper.atan2(vel.y, horizontalDist) * (180 / Math.PI));
                targetPitch = MathHelper.clamp(targetPitch, -65.0f, 65.0f);
                // 5°/tick for pitch: keep this slower so vertical lurches don't snap the head
                worm.serverSidePitch = wrapDegrees(worm.serverSidePitch, targetPitch, 5.0f);
                worm.setPitch(worm.serverSidePitch);
            }

            worm.velocityModified = true;
        }

        private float wrapDegrees(float current, float target, float maxStep) {
            float delta = MathHelper.wrapDegrees(target - current);
            if (delta > maxStep)
                delta = maxStep;
            if (delta < -maxStep)
                delta = -maxStep;
            return current + delta;
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(net.minecraft.entity.Entity entity) {
        // Disabling collisions between parts significantly improves performance
    }
}
