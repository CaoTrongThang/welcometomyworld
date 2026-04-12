package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import com.trongthang.welcometomyworld.VoidBossState;

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

    private static final double MAX_DISTANCE_XZ = 300.0;
    private static final double MAX_DISTANCE_Y = 200.0;

    // Rolling position history for body-segment trail following
    private static final int MAX_HISTORY = 1000;
    public int ticksSinceDeath = 0; // The timer for the death animation
    private final Deque<Vec3d> posHistoryDeque = new ArrayDeque<>();
    // Cached list view — rebuilt only when the deque changes, used by segments
    private List<Vec3d> posHistorySnapshot = new ArrayList<>();
    private boolean historyDirty = false;

    private int autoRegenHealth = 50;
    private int autoRegenCooldown = 100;

    public List<Vec3d> getPosHistory() {
        if (historyDirty || posHistorySnapshot == null) {
            posHistorySnapshot = List.copyOf(posHistoryDeque);
            historyDirty = false;
        }
        return posHistorySnapshot;
    }

    public void registerPart(VoidWormPartEntity part) {
        if (!parts.contains(part)) {
            parts.add(part);
        }
    }

    public List<VoidWormPartEntity> getParts() {
        return this.parts;
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

    public static final Skill ROAR = new Skill(1, 80, 800);
    public static final Skill CHARGE_ATTACK = new Skill(2, 60, 250);
    public static final Skill CRYSTAL_BARRAGE = new Skill(3, 300, 2000);
    public static final Skill GRAB_ATTACK = new Skill(4, 200, 3000);
    public static final Skill SUMMON_MINIONS = new Skill(5, 300, 1200);

    private static final int ROAR_HIT_TICK = 22;
    private double chargeDestX, chargeDestY, chargeDestZ;
    private double grabY;

    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int prepareTicks = 0;
    private int globalSkillCooldown = 0;
    public int combatTicks = 0;
    private int skillRemainingCharges = 0;

    private final int[] skillCooldowns = new int[10];

    public int hungerCooldownTicks = 0;
    private LivingEntity prevTarget = null;

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
        triggerSkill(skill, 1);
    }

    public void triggerSkill(Skill skill, int charges) {
        if (this.getWorld().isClient())
            return;
        this.dataTracker.set(IS_USING_SKILL, true);
        this.dataTracker.set(SKILL_PREPARING, true);
        this.dataTracker.set(SKILL_ID, skill.id);
        this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
        this.skillTick = 0;
        this.prepareTicks = 0;
        this.skillTotalTicks = skill.length;
        this.skillCooldowns[skill.id] = skill.cooldown;
        this.globalSkillCooldown = 40;
        this.skillHitFired = false;
        this.skillRemainingCharges = charges - 1;
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
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 142500.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.5D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 50.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 10.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 150.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 30.0D)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 20.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new BossFlightGoal(this));

        // --- Target goals ---
        this.targetSelector.add(1, new RevengeGoal(this));

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, SkeletonEntity.class, true) {
            @Override
            public boolean canStart() {
                return VoidWormEntity.this.hungerCooldownTicks <= 0 && super.canStart();
            }
        });

        this.targetSelector.add(2, new ActiveTargetGoal<>(this, WitherSkeletonEntity.class, true) {
            @Override
            public boolean canStart() {
                return VoidWormEntity.this.hungerCooldownTicks <= 0 && super.canStart();
            }
        });

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
                } else if (skillId == 4) { // GRAB_ATTACK
                    if (!this.dataTracker.get(SKILL_PREPARING)) {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("attack_openning_mouth"));
                    } else {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("moving"));
                    }
                } else if (skillId == 2) { // CHARGE_ATTACK
                    if (!this.dataTracker.get(SKILL_PREPARING)) {
                        return state.setAndContinue(RawAnimation.begin().thenPlay("attack_openning_mouth"));
                    } else {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("moving"));
                    }
                } else if (skillId == 3 || skillId == 5) { // CRYSTAL_BARRAGE or SUMMON_MINIONS
                    return state.setAndContinue(RawAnimation.begin().thenLoop("moving"));
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
        nbt.putInt("HungerCooldownTicks", this.hungerCooldownTicks);

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
        if (nbt.contains("HungerCooldownTicks")) {
            this.hungerCooldownTicks = nbt.getInt("HungerCooldownTicks");
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

        if (this.age % autoRegenCooldown == 0) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(autoRegenHealth);
            }
        }

        if (this.getWorld().isClient) {
            this.prevVisualPitch = this.visualPitch;
            this.prevVisualYaw = this.visualYaw;
        }

        super.tick();

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // Record head position for body-trail history (newest first)
            Vec3d currentPos = this.getPos();
            if (posHistoryDeque.isEmpty() || posHistoryDeque.getFirst().squaredDistanceTo(currentPos) > 0.05) {
                posHistoryDeque.addFirst(currentPos);
                if (posHistoryDeque.size() > MAX_HISTORY)
                    posHistoryDeque.removeLast();
                historyDirty = true;
            }

            if (!partsSpawned) {
                spawnParts(serverWorld);
                partsSpawned = true;
            }
            updateParts();

            // Head dictates part positioning directly to avoid sync issues when chunks
            // unload
            for (VoidWormPartEntity part : this.parts) {
                if (part != null && !part.isRemoved()) {
                    part.followByHistory();
                }
            }

            if (this.getHealth() > 0) {
                skillsHandler();
            }

            // Trigger rumble when diving underground and moving fast
            if (this.age % 10 == 0) {
                int surfaceY = serverWorld.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                        (int) this.getX(), (int) this.getZ());
                if (this.getY() < surfaceY && this.getVelocity().lengthSquared() > 0.05) {
                    sendCameraShakeToNearbyPlayers(40.0, 0.5f, 20);
                }
            }

            // Persistently track this boss
            if (this.age % 20 == 0) {
                VoidBossState state = VoidBossState.getServerState(serverWorld);
                state.bossUuid = this.getUuid();
                state.lastBossPos = this.getBlockPos();
                state.markDirty();
            }

            LivingEntity currentTarget = this.getTarget();

            if (this.hungerCooldownTicks > 0 && currentTarget == null) {
                this.hungerCooldownTicks--;
            }

            if (currentTarget == null && this.hasPassengers()) {
                this.removeAllPassengers();
            }

            if (currentTarget != null) {
                this.prevTarget = currentTarget;
                combatTicks = 400; // 20 seconds of combat state retention when target is lost
            } else {
                if (this.prevTarget != null) {
                    if (!this.prevTarget.isAlive() && (this.prevTarget instanceof SkeletonEntity
                            || this.prevTarget instanceof WitherSkeletonEntity)) {
                        this.hungerCooldownTicks = 10000;
                    }
                    this.prevTarget = null;
                }
                if (combatTicks > 0) {
                    combatTicks--;
                }
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

            if (isPreparing) {
                this.prepareTicks++;
                if (this.prepareTicks > 120) {
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER
                            .debug("SKILL_PREPARING timed out standing still, canceling", skillId);
                    this.setVelocity(this.getVelocity().multiply(0.5D));
                    this.velocityModified = true;
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                    return;
                }
            }

            if (skillId == 2) { // CHARGE_ATTACK
                if (target != null && target.isAlive()) {
                    if (isPreparing) {
                        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.debug("CHARGE_ATTACK PREPARING",
                                isPreparing);
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
                            Utils.playFarSound((ServerWorld) this.getWorld(), this,
                                    SoundsManager.VOID_WORM_CHARGE_ATTACK, net.minecraft.sound.SoundCategory.HOSTILE,
                                    1.0F, 1.0F, 64.0);
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
                                        (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 2.0f,
                                        true);
                                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                        SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY,
                                        net.minecraft.sound.SoundCategory.HOSTILE, 2.0F, 1.0F);

                                this.getWorld().sendEntityStatus(this, (byte) 62);

                                if (skillRemainingCharges > 0) {
                                    skillRemainingCharges--;
                                    this.dataTracker.set(SKILL_PREPARING, true);
                                    this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
                                    this.skillTick = 0;
                                    this.skillHitFired = false;
                                } else {
                                    this.dataTracker.set(IS_USING_SKILL, false);
                                    this.dataTracker.set(SKILL_PREPARING, false);
                                    this.dataTracker.set(SKILL_ID, 0);
                                }

                            }
                        }
                    }
                } else {
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER
                            .debug("CHARGE_ATTACK target null or dead, cancelling", skillId);
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
                        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.debug("ROAR PREPARING", isPreparing);
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
                            if (skillTick == ROAR_HIT_TICK) {
                                sendCameraShakeToNearbyPlayers(64.0, 4.0f, 60);
                            }
                            if (skillTick % 11 == 0) {
                                dealAoeDamage(64.0,
                                        (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 2.0f);
                            }
                        }
                    }
                } else {
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER
                            .debug("ROAR target null or dead, cancelling", skillId);
                    this.setVelocity(this.getVelocity().multiply(0.9D));
                    this.velocityModified = true;
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
            } else if (skillId == 3) { // CRYSTAL_BARRAGE

                if (target != null && target.isAlive()) {
                    skillTick++;

                    double targetX = target.getX();
                    double targetY = target.getY() + 40.0D; // Pick destination higher than target
                    double targetZ = target.getZ();

                    if (isPreparing) {
                        // Fly to the high point
                        Vec3d dir = new Vec3d(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
                        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        if (dir.lengthSquared() > 0.1)
                            dir = dir.normalize();

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.0;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        // Rotations
                        double dx = targetX - this.getX();
                        double dy = targetY - this.getY();
                        double dz = targetZ - this.getZ();
                        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                        float targetPitch = (float) (MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz))
                                * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        if (this.getY() >= targetY - 4.0D && distXZ <= 8.0D) {
                            this.dataTracker.set(SKILL_PREPARING, false);
                        }
                    } else {
                        // Phase 2: Circle and Shoot
                        double radius = 10.0;
                        Vec3d wormPos = this.getPos();
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
                        double radialError = distH - radius;
                        double verticalError = targetY - wormPos.y;

                        double orbitSpeed = 1.0;
                        Vec3d desiredDir = tangential.multiply(1.0)
                                .add(radial.multiply(MathHelper.clamp(radialError * 0.05, -1.0, 1.0)))
                                .add(new Vec3d(0, MathHelper.clamp(verticalError * 0.1, -0.6, 0.6), 0));

                        if (desiredDir.lengthSquared() > 0.001)
                            desiredDir = desiredDir.normalize();

                        this.setVelocity(desiredDir.multiply(orbitSpeed));
                        this.velocityModified = true;

                        // Face the movement direction
                        Vec3d vel = this.getVelocity();
                        if (vel.lengthSquared() > 0.001) {
                            float targetYaw = (float) (MathHelper.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90.0F;
                            this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                            this.setYaw(this.serverSideYaw);

                            double horizDist = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                            float targetPitch = (float) (MathHelper.atan2(vel.y, horizDist) * (180.0 / Math.PI));
                            this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                            this.setPitch(this.serverSidePitch);
                        }

                        // Every 5s (100 ticks), create 5 Purple Crystals
                        if (skillTick % 10 == 0) {
                            PurpleCrystalEntity crystal = new PurpleCrystalEntity(EntitiesManager.PURPLE_CRYSTAL,
                                    this.getWorld());

                            // Random position within 10 block radius
                            double spawnX = this.getX() + (this.random.nextDouble() - 0.5) * 20.0;
                            double spawnY = this.getY() + (this.random.nextDouble() - 0.5) * 20.0;
                            double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5) * 20.0;

                            crystal.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0, 0);
                            crystal.setOwner(this);
                            crystal.setDamage(
                                    (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 0.8f);
                            this.getWorld().spawnEntity(crystal);
                            SoundEvent sound = this.random.nextBoolean() ? SoundsManager.CRYSTAL_SPAWNED_1
                                    : SoundsManager.CRYSTAL_SPAWNED_2;

                            Utils.playFarSound((ServerWorld) this.getWorld(), this, sound,
                                    SoundCategory.HOSTILE, 3.0F, 1.0F, 64.0);
                        }
                    }
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
            } else if (skillId == 4) { // GRAB_ATTACK
                if (target != null && target.isAlive()) {
                    skillTick++;

                    if (isPreparing) {
                        double targetYHover = target.getY() + 40.0D;
                        Vec3d dir = new Vec3d(target.getX() - this.getX(), targetYHover - this.getY(),
                                target.getZ() - this.getZ());
                        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        if (dir.lengthSquared() > 0.1)
                            dir = dir.normalize();

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.5;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        // Face destination
                        float targetYaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
                        float targetPitch = (float) (MathHelper.atan2(dir.y, distXZ) * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        if (this.getY() >= targetYHover - 4.0D && distXZ <= 8.0D) {
                            this.dataTracker.set(SKILL_PREPARING, false);
                            this.chargeDestX = target.getX();
                            this.chargeDestY = target.getY();
                            this.chargeDestZ = target.getZ();
                            this.skillHitFired = false;
                            this.setVelocity(0, 0, 0);
                        }
                    } else if (!skillHitFired) {
                        // Phase 2: Dive to grab
                        double dx = this.chargeDestX - this.getX();
                        double dy = this.chargeDestY - this.getY();
                        double dz = this.chargeDestZ - this.getZ();
                        Vec3d dir = new Vec3d(dx, dy, dz);
                        double distSq = dir.lengthSquared();
                        if (dir.lengthSquared() > 0.01)
                            dir = dir.normalize();

                        double diveSpeed = 3.5;
                        this.setVelocity(dir.multiply(diveSpeed));
                        this.velocityModified = true;

                        // Face destination
                        double distXZ = Math.sqrt(dx * dx + dz * dz);
                        this.serverSideYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                        this.serverSidePitch = (float) (MathHelper.atan2(dy, distXZ) * (180.0 / Math.PI));
                        this.setYaw(this.serverSideYaw);
                        this.setPitch(this.serverSidePitch);

                        // Grab detection
                        if (this.squaredDistanceTo(target) <= 6.0 * 6.0) {
                            target.startRiding(this, true);
                            this.skillHitFired = true;
                            this.grabY = this.getY();
                            this.setVelocity(0, 0, 0);
                            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                    SoundsManager.MONSTER_ROAR,
                                    net.minecraft.sound.SoundCategory.HOSTILE, 0.4F, 1.5F);
                        } else if (distSq < 4.0 || this.horizontalCollision || this.verticalCollision
                                || skillTick > 150) {
                            // Missed
                            this.dataTracker.set(IS_USING_SKILL, false);
                            this.dataTracker.set(SKILL_ID, 0);
                        }
                    } else {
                        // Phase 3: Fly around randomly 15 blocks higher
                        if (skillTick == 1 || skillTick % 60 == 0 || (Math.abs(this.getX() - this.chargeDestX) < 3.0
                                && Math.abs(this.getZ() - this.chargeDestZ) < 3.0)) {
                            this.chargeDestX = this.getX() + (this.random.nextDouble() - 0.5) * 40.0;
                            this.chargeDestY = grabY + 15.0D + (this.random.nextDouble() - 0.5) * 5.0;
                            this.chargeDestZ = this.getZ() + (this.random.nextDouble() - 0.5) * 40.0;
                        }

                        double dx = this.chargeDestX - this.getX();
                        double dy = this.chargeDestY - this.getY();
                        double dz = this.chargeDestZ - this.getZ();
                        Vec3d dir = new Vec3d(dx, dy, dz);
                        double distXZ = Math.sqrt(dx * dx + dz * dz);

                        if (dir.lengthSquared() > 0.01) {
                            dir = dir.normalize();
                        }

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 1.5;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        float targetYaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
                        float targetPitch = (float) (MathHelper.atan2(dir.y, distXZ) * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        // Attack every 30 ticks
                        if (skillTick % 30 == 0) {
                            if (this.hasPassengers()) {
                                for (net.minecraft.entity.Entity passenger : this.getPassengerList()) {
                                    if (passenger instanceof LivingEntity le) {
                                        Unknown.dealUnknownDamage(this, target,
                                                (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                                        * 1.2f);
                                        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                                                net.minecraft.sound.SoundEvents.ENTITY_PLAYER_BURP,
                                                net.minecraft.sound.SoundCategory.HOSTILE, 1.5F, 0.5F);
                                        le.heal(500.0f);
                                    }
                                }
                            }
                        }

                        // Stop if player somehow gets off or died
                        if (!this.hasPassengers()) {
                            this.dataTracker.set(IS_USING_SKILL, false);
                            this.dataTracker.set(SKILL_ID, 0);
                        }
                    }
                }
            } else if (skillId == 5) { // SUMMON_MINIONS
                if (target != null && target.isAlive()) {
                    skillTick++;

                    // Disable Elytra flight for nearby players
                    for (PlayerEntity player : this.getWorld().getPlayers()) {
                        if (player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                            if (this.squaredDistanceTo(player) <= 60.0 * 60.0) {
                                if (player.isFallFlying()) {
                                    player.stopFallFlying();
                                }
                            }
                        }
                    }

                    double targetX = target.getX();
                    // Altitude oscillating between -5 and 5 blocks relative to target
                    double altitudeOffset = MathHelper.sin(this.age * 0.05f) * 5.0;
                    double targetY = target.getY() + altitudeOffset;
                    double targetZ = target.getZ();

                    double radius = 10.0;

                    if (isPreparing) {
                        // Phase 1: Fly to a point on the orbit circle
                        // Calculate a point that is 'radius' away from the target
                        double dx = this.getX() - targetX;
                        double dz = this.getZ() - targetZ;
                        double distH = Math.sqrt(dx * dx + dz * dz);

                        double orbitTargetX, orbitTargetZ;
                        if (distH > 0.1) {
                            orbitTargetX = targetX + (dx / distH) * radius;
                            orbitTargetZ = targetZ + (dz / distH) * radius;
                        } else {
                            orbitTargetX = targetX + radius;
                            orbitTargetZ = targetZ;
                        }

                        Vec3d targetPos = new Vec3d(orbitTargetX, targetY, orbitTargetZ);
                        Vec3d dir = targetPos.subtract(this.getPos());

                        if (dir.lengthSquared() > 0.1)
                            dir = dir.normalize();

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 3.0;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        float targetYaw = (float) (MathHelper.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
                        float targetPitch = (float) (MathHelper.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))
                                * (180.0 / Math.PI));

                        this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                        this.setYaw(this.serverSideYaw);
                        this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                        this.setPitch(this.serverSidePitch);

                        if (this.getPos().distanceTo(targetPos) <= 16.0D) {
                            this.dataTracker.set(SKILL_PREPARING, false);
                            sendCameraShakeToNearbyPlayers(80.0, 0.5f, 300); // Trigger low shake for 300 ticks
                            Utils.playFarSound((ServerWorld) this.getWorld(), this, SoundsManager.EARTH_RUMBLE,
                                    net.minecraft.sound.SoundCategory.HOSTILE, 5.0F, 1.0F, 64.0);
                        }
                    } else {
                        // Phase 2: Circle and Summon
                        Vec3d wormPos = this.getPos();
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
                        double radialError = distH - radius;
                        double verticalError = targetY - wormPos.y;

                        double orbitSpeed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 1.2;
                        Vec3d desiredDir = tangential.multiply(1.0)
                                .add(radial.multiply(MathHelper.clamp(radialError * 0.05, -1.0, 1.0)))
                                .add(new Vec3d(0, MathHelper.clamp(verticalError * 0.1, -0.6, 0.6), 0));

                        if (desiredDir.lengthSquared() > 0.001)
                            desiredDir = desiredDir.normalize();

                        this.setVelocity(desiredDir.multiply(orbitSpeed));
                        this.velocityModified = true;

                        // Face movement direction
                        Vec3d vel = this.getVelocity();
                        if (vel.lengthSquared() > 0.001) {
                            float targetYaw = (float) (MathHelper.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90.0F;
                            this.serverSideYaw = smoothAngle(this.serverSideYaw, targetYaw, 15.0f);
                            this.setYaw(this.serverSideYaw);

                            double horizDist = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                            float targetPitch = (float) (MathHelper.atan2(vel.y, horizDist) * (180.0 / Math.PI));
                            this.serverSidePitch = smoothAngle(this.serverSidePitch, targetPitch, 15.0f);
                            this.setPitch(this.serverSidePitch);
                        }

                        // Summon every 20 ticks
                        if (skillTick % 20 == 0) {
                            EntityType<?> typeToSummon = this.random.nextBoolean() ? EntityType.SKELETON
                                    : EntityType.WITHER_SKELETON;

                            BlockPos spawnPos = com.trongthang.welcometomyworld.features.SpawnMonstersPackEveryMins
                                    .findSafeSpawnPositionByPack(
                                            (ServerWorld) this.getWorld(), this.getBlockPos(), typeToSummon, 3, 10);

                            if (spawnPos != null) {
                                Entity summoned = typeToSummon.create(this.getWorld());
                                if (summoned instanceof MobEntity mob) {
                                    mob.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(),
                                            spawnPos.getZ() + 0.5, this.random.nextFloat() * 360f, 0);
                                    mob.initialize((ServerWorld) this.getWorld(),
                                            this.getWorld().getLocalDifficulty(spawnPos), SpawnReason.MOB_SUMMONED,
                                            null, null);
                                    mob.setTarget(target);
                                    this.getWorld().spawnEntity(mob);

                                    // Visual effect
                                    ((ServerWorld) this.getWorld()).spawnParticles(
                                            net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                                            spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, 20,
                                            0.5, 0.5, 0.5, 0.1);
                                }
                            }
                        }
                    }
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_PREPARING, false);
                    this.dataTracker.set(SKILL_ID, 0);
                }
            }

            if (!this.dataTracker.get(SKILL_PREPARING) && skillTick >= skillTotalTicks) {
                if (skillRemainingCharges > 0) {
                    skillRemainingCharges--;
                    this.dataTracker.set(SKILL_PREPARING, true);
                    this.dataTracker.set(SKILL_TRIGGER, this.dataTracker.get(SKILL_TRIGGER) + 1);
                    this.skillTick = 0;
                    this.skillHitFired = false;
                } else {
                    this.dataTracker.set(IS_USING_SKILL, false);
                    this.dataTracker.set(SKILL_ID, 0);
                    this.removeAllPassengers();
                }
            }

            return;
        }

        // Trigger logic
        if (globalSkillCooldown <= 0 && !isUsingSkill()) {

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                // Ignore target if too far from center (0, 50, 0)
                double dx = target.getX() - 0;
                double dz = target.getZ() - 0;
                double dy = target.getY() - 50;
                if (Math.sqrt(dx * dx + dz * dz) > MAX_DISTANCE_XZ || Math.abs(dy) > MAX_DISTANCE_Y) {
                    this.setTarget(null);
                    return;
                }

                double distX = Math.abs(target.getX() - this.getX());
                double distZ = Math.abs(target.getZ() - this.getZ());
                double distY = Math.abs(target.getY() - this.getY());

                boolean canCharge = canUseSkill(CHARGE_ATTACK) && target.getY() - this.getY() >= 15.0 && distX < 64.0
                        && distZ < 64.0;
                if (canCharge) {
                    // if health < 50% use grab attack instead of charge attack
                    if (this.getHealth() < this.getMaxHealth() * 0.8) {
                        triggerSkill(CHARGE_ATTACK, 2);
                    } else if (this.getHealth() < this.getMaxHealth() * 0.5) {
                        triggerSkill(CHARGE_ATTACK, 3);
                    } else {
                        triggerSkill(GRAB_ATTACK);
                    }

                    // Open mouth sound maybe handled inside or automatically by the animation if
                    // registered in GeckoLib
                } else if (canUseSkill(ROAR) && distX < 64.0 && distZ < 64.0 && distY < 60.0) {
                    triggerSkill(ROAR);
                    Utils.playFarSound((ServerWorld) this.getWorld(), this, SoundsManager.MONSTER_ROAR,
                            net.minecraft.sound.SoundCategory.HOSTILE, 0.8F, 1.0F, 84.0);
                } else if (canUseSkill(CRYSTAL_BARRAGE) && distX < 100 && distZ < 100) {
                    triggerSkill(CRYSTAL_BARRAGE);
                } else if (canUseSkill(GRAB_ATTACK) && distX < 64.0 && distZ < 64.0) {
                    triggerSkill(GRAB_ATTACK);
                } else if (canUseSkill(SUMMON_MINIONS) && distX < 200 && distZ < 200) {
                    triggerSkill(SUMMON_MINIONS);
                }
            }
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("welcometomyworld", "entities/void_worm");
    }

    public void dealDiveAoeDamage(double radius, float damage, boolean createBlock) {
        if (this.getWorld().isClient())
            return;
        net.minecraft.util.math.Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(
                LivingEntity.class, area, e -> e.isAlive() && e != this && !(e instanceof VoidWormPartEntity));

        for (LivingEntity target : nearby) {
            Unknown.dealUnknownDamage(this, target, damage);
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
                    int blockCount = (int) (currentRadius * 4); // optimized density
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
                Unknown.dealUnknownDamage(this, entity, amount);
                // give blind effect
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20 * 10, 1));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 3, 1));
            }
        }
    }

    @Override
    public boolean collidesWith(Entity other) {

        super.collidesWith(other);
        // if in the grab skill, return false
        if (this.isUsingSkill() && this.getSkillId() == 4) { // Changed from 5 to 4 (GRAB_ATTACK)
            Unknown.dealUnknownDamage(this, (LivingEntity) other,
                    (float) (this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.25f));
            return false;
        }

        return true;
    }

    public void handleSkillCollision(Entity other, Entity source) {
        if (this.getWorld().isClient)
            return;

        if (this.isUsingSkill() && this.getSkillId() == 5) { // SUMMON_MINIONS
            if (other instanceof LivingEntity living && living != this && !this.parts.contains(living)
                    && living.getType() != EntityType.SKELETON && living.getType() != EntityType.WITHER_SKELETON) {

                // Deal damage
                Unknown.dealUnknownDamage(this, living,
                        (float) (this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.0f));

                // Push back
                Vec3d pushDir = living.getPos().subtract(source.getPos()).normalize();
                if (pushDir.lengthSquared() < 0.01) {
                    pushDir = new Vec3d(0, 0.5, 0);
                }
                living.addVelocity(pushDir.x * 1.2, 0.2, pushDir.z * 1.2);
                living.velocityModified = true;
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
            Vec3d currentPos = this.getPos();
            if (posHistoryDeque.isEmpty() || posHistoryDeque.getFirst().squaredDistanceTo(currentPos) > 0.05) {
                posHistoryDeque.addFirst(currentPos);
                if (posHistoryDeque.size() > MAX_HISTORY)
                    posHistoryDeque.removeLast();
                historyDirty = true;
            }

            updateParts();

            // Head dictates part positioning directly to avoid sync issues when chunks
            // unload
            for (VoidWormPartEntity part : this.parts) {
                if (part != null && !part.isRemoved()) {
                    part.followByHistory();
                }
            }
        }

        // After 200 ticks (10 seconds), actually remove the entity
        if (this.ticksSinceDeath >= 200 && !this.getWorld().isClient()) {
            this.getWorld().sendEntityStatus(this, (byte) 60); // Death smoke particles
            this.remove(RemovalReason.KILLED);
        }
    }

    private void updateParts() {
        if (this.getWorld().isClient)
            return;

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

        // Clean up invalid parts from our list
        parts.removeIf(p -> p == null || p.isRemoved());

        // We only actively recreate missing parts if the head has been alive for a bit,
        // giving existing parts time to load from NBT and register themselves.
        if (partsSpawned && this.age % 40 == 0) {
            java.util.Set<Integer> seenSegments = new java.util.HashSet<>();
            java.util.List<VoidWormPartEntity> toRemove = new java.util.ArrayList<>();

            for (VoidWormPartEntity part : parts) {
                int idx = part.getSegmentIndex();
                if (idx < 1 || idx > BODY_SEGMENTS + 1 || seenSegments.contains(idx)) {
                    toRemove.add(part);
                    part.discard();
                } else {
                    seenSegments.add(idx);
                }
            }
            parts.removeAll(toRemove);

            // Create missing parts
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                for (int i = 1; i <= BODY_SEGMENTS + 1; i++) {
                    if (!seenSegments.contains(i)) {
                        EntityType<? extends HostileEntity> type = (i == BODY_SEGMENTS + 1)
                                ? EntitiesManager.VOID_WORM_TAIL
                                : EntitiesManager.VOID_WORM_BODY;
                        VoidWormPartEntity newPart = new VoidWormPartEntity(type, serverWorld, this, i, PART_DISTANCE);
                        newPart.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(),
                                this.getPitch());
                        serverWorld.spawnEntity(newPart);
                        parts.add(newPart);
                    }
                }
            }
        }
    }

    @Override
    protected void updatePassengerPosition(net.minecraft.entity.Entity passenger,
            net.minecraft.entity.Entity.PositionUpdater positionUpdater) {
        if (!this.hasPassenger(passenger))
            return;

        if (this.isUsingSkill() && this.getSkillId() == 4) {
            // Position passenger in "mouth" area
            // Worm head is at getPos()
            // We want it slightly in front and down?
            float yaw = this.getYaw() * ((float) Math.PI / 180F);
            float pitch = this.getPitch() * ((float) Math.PI / 180F);

            double forward = 0.0;
            double up = 2;

            double ox = -Math.sin(yaw) * Math.cos(pitch) * forward;
            double oy = -Math.sin(pitch) * forward + up;
            double oz = Math.cos(yaw) * Math.cos(pitch) * forward;

            positionUpdater.accept(passenger, this.getX() + ox, this.getY() + oy, this.getZ() + oz);
        } else {
            super.updatePassengerPosition(passenger, positionUpdater);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        for (VoidWormPartEntity part : parts) {
            part.remove(reason);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        int r = this.random.nextInt(3);
        if (r == 0)
            return SoundsManager.VOID_WORM_AMBIENT_1;
        if (r == 1)
            return SoundsManager.VOID_WORM_AMBIENT_2;
        return SoundsManager.VOID_WORM_AMBIENT_3;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsManager.VOID_WORM_DEATH_ROAR;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (this.hasPassengers()) {
            this.removeAllPassengers();
        }

        // Reset skill state upon death
        this.dataTracker.set(IS_USING_SKILL, false);
        this.dataTracker.set(SKILL_PREPARING, false);
        this.dataTracker.set(SKILL_ID, 0);
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
                // Stay within movement limits of (0, 50, 0)
                double dx = target.getX() - 0;
                double dz = target.getZ() - 0;
                double dy = target.getY() - 50;
                if (Math.sqrt(dx * dx + dz * dz) > MAX_DISTANCE_XZ || Math.abs(dy) > MAX_DISTANCE_Y) {
                    com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER
                            .info("BossFlightGoal target out of bounds, resetting target. Target: "
                                    + target.getName().getString());
                    worm.setTarget(null);
                    target = null;
                }
            }

            if (target != null && target.isAlive()) {
                // Clear any wander target immediately when entering combat
                wanderTarget = null;

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

                    // Clamp wander target within limits of (0, 50, 0)
                    double dx_h = x - 0;
                    double dz_h = z - 0;
                    double dy_h = y - 50;
                    double distXZ = Math.sqrt(dx_h * dx_h + dz_h * dz_h);

                    if (distXZ > MAX_DISTANCE_XZ - 50 || Math.abs(dy_h) > MAX_DISTANCE_Y - 20) {
                        if (distXZ > MAX_DISTANCE_XZ - 50) {
                            double scale = (MAX_DISTANCE_XZ - 50) / distXZ;
                            x = dx_h * scale;
                            z = dz_h * scale;
                        }
                        if (Math.abs(dy_h) > MAX_DISTANCE_Y - 20) {
                            y = 50 + Math.signum(dy_h) * (MAX_DISTANCE_Y - 20);
                        }
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
        if (entity instanceof VoidWormPartEntity || entity == this)
            return;

        handleSkillCollision(entity, this);
    }

    @Override
    public boolean shouldRender(double distance) {
        return true; // Always render if we are tracked by the client!
    }

    private static String wanderTargetLog(VoidWormEntity worm) {
        BossFlightGoal goal = (BossFlightGoal) worm.goalSelector.getGoals().stream()
                .filter(g -> g.getGoal() instanceof BossFlightGoal)
                .map(net.minecraft.entity.ai.goal.PrioritizedGoal::getGoal)
                .findFirst().orElse(null);
        if (goal != null && goal.wanderTarget != null) {
            return goal.wanderTarget.toString();
        }
        return "null";
    }
}
