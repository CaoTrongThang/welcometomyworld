package com.trongthang.welcometomyworld.entities.VoidWorm;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
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

import java.util.ArrayList;
import java.util.List;

public class VoidWormEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Configurable size
    private static final int BODY_SEGMENTS = 12;
    private final List<VoidWormPartEntity> parts = new ArrayList<>();
    private boolean partsSpawned = false;

    public void registerPart(VoidWormPartEntity part) {
        if (!parts.contains(part)) {
            parts.add(part);
        }
    }

    private static final float PART_DISTANCE = 5f;

    // Custom visual pitch to bypass vanilla LookControl pitch resetting
    public float visualPitch = 0.0f;
    public float prevVisualPitch = 0.0f;
    public float visualYaw = 0.0f;
    public float prevVisualYaw = 0.0f;

    // Server-side persistent rotation to avoid vanilla resetting pitch/yaw to 0
    private float serverSidePitch = 0.0f;
    private float serverSideYaw = 0.0f;

    private static final TrackedData<Float> TARGET_PITCH = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.FLOAT);

    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SKILL_PREPARING = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(VoidWormEntity.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SKILL_TRIGGER = DataTracker.registerData(VoidWormEntity.class,
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

    public static final Skill ROAR = new Skill(1, 120, 200);

    private static final int ROAR_HIT_TICK = 25;

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
        this.dataTracker.startTracking(TARGET_PITCH, 0.0f);
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_PREPARING, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(SKILL_TRIGGER, 0);
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
        this.goalSelector.add(2, new FlyTowardsTargetGoal(this));
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

                state.getController().transitionLength(5);
                if (prevSkillId[0] != skillId || prevSkillTrigger[0] != skillTrigger) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;
                prevSkillTrigger[0] = skillTrigger;

                if (skillId == 1) { // ROAR
                    return state.setAndContinue(RawAnimation.begin().thenPlay("void_worm_head_roar"));
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
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("PartsSpawned")) {
            this.partsSpawned = nbt.getBoolean("PartsSpawned");
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

            if (!partsSpawned) {
                spawnParts(serverWorld);
                partsSpawned = true;
            }
            updateParts();
            skillsHandler();

            if (this.getTarget() != null) {
                combatTicks = 400; // 20 seconds of combat state retention when target is lost
            } else if (combatTicks > 0) {
                combatTicks--;
            }
        } else {
            // Client side visual interpolation
            float targetP = this.dataTracker.get(TARGET_PITCH);
            this.visualPitch += MathHelper.wrapDegrees(targetP - this.visualPitch) * 0.3f;
            this.visualYaw += MathHelper.wrapDegrees(this.getYaw() - this.visualYaw) * 0.3f;
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

            if (skillId == 1) { // ROAR
                if (target != null && target.isAlive()) {
                    double targetX = target.getX();
                    double targetY = target.getY() + 30.0D;
                    double targetZ = target.getZ();

                    if (isPreparing) {
                        // Phase 1: Move to destination (40 blocks above target)
                        Vec3d dir = new Vec3d(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
                        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
                        if (dir.lengthSquared() > 0.1) {
                            dir = dir.normalize();
                        }

                        double speed = this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 2.0;
                        this.setVelocity(dir.multiply(speed));
                        this.velocityModified = true;

                        // Visual Look towards target while flying up
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
                        this.dataTracker.set(TARGET_PITCH, this.serverSidePitch);

                        // Check if reached destination (Y >= targetY - 4 and horizontal dist <= 8)
                        if (this.getY() >= targetY - 4.0D && distXZ <= 8.0D) {
                            // Reached destination! Stop preparing
                            this.dataTracker.set(SKILL_PREPARING, false);
                            this.setVelocity(0, 0, 0); // Stop
                        }
                    } else {
                        skillTick++;

                        // Hover
                        double currentY = this.getY();
                        double upVel = 0;
                        if (currentY < targetY) {
                            upVel = Math.min(2.0, targetY - currentY) * 0.2; // glide up smoothly
                        } else if (currentY > targetY + 2.0D) {
                            upVel = -0.1; // slowly drift down if overshooting
                        }

                        // Decelerate horizontal velocity gracefully so it stops
                        this.setVelocity(this.getVelocity().x * 0.8, upVel, this.getVelocity().z * 0.8);
                        this.velocityModified = true;

                        // Look straight down, do NOT adjust yaw to avoid breaking neck
                        this.serverSidePitch = -60.0F; // -90.0F looks straight down natively in many cases
                        this.setPitch(this.serverSidePitch);
                        this.dataTracker.set(TARGET_PITCH, this.serverSidePitch);

                        if (skillTick == 1) {
                            Utils.playFarSound((ServerWorld) this.getWorld(), this, SoundsManager.MONSTER_ROAR,
                                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0F, 1.0F, 84.0);
                        }

                        if (skillTick >= ROAR_HIT_TICK && skillTick <= 95) {
                            if (skillTick % 10 == 0) { // Deal damage every 5 ticks
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
                double distX = Math.abs(target.getX() - this.getX());
                double distZ = Math.abs(target.getZ() - this.getZ());
                double distY = Math.abs(target.getY() - this.getY());

                // Only trigger if reasonably close horizontally and not astronomically far
                // vertically
                if (canUseSkill(ROAR) && distX < 64.0 && distZ < 64.0 && distY < 60.0) {
                    triggerSkill(ROAR);
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
        LivingEntity previous = this;

        for (int i = 0; i < BODY_SEGMENTS; i++) {
            VoidWormPartEntity body = new VoidWormPartEntity(EntitiesManager.VOID_WORM_BODY, world, this, previous,
                    PART_DISTANCE);
            body.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            world.spawnEntity(body);
            parts.add(body);
            previous = body;
        }

        VoidWormPartEntity tail = new VoidWormPartEntity(EntitiesManager.VOID_WORM_TAIL, world, this, previous,
                PART_DISTANCE);
        tail.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        world.spawnEntity(tail);
        parts.add(tail);
    }

    private void updateParts() {
        // We only want to discard parts if the head is actually dead/killed, not just
        // unloaded.
        if (this.getHealth() <= 0.0f
                || (this.isRemoved() && this.getRemovalReason() != null && this.getRemovalReason().shouldDestroy())) {
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
    public void onRemoved() {
        com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER.info("head_on_removed", "onRemoved called", "uuid",
                this.getUuid());
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

    // A flight AI goal to chase targets or roam organically
    static class FlyTowardsTargetGoal extends Goal {
        private final VoidWormEntity worm;
        private Vec3d wanderTarget = null;

        public FlyTowardsTargetGoal(VoidWormEntity worm) {
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
        public void tick() {
            LivingEntity target = worm.getTarget();
            double speed = worm.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            Vec3d currentVel = worm.getVelocity();

            if (target != null && target.isAlive()) {
                Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
                Vec3d dir = targetPos.subtract(worm.getPos()).normalize();

                Vec3d newVel = currentVel.add(dir.multiply(0.06)).normalize().multiply(speed * 1.2D);

                // Add weaving even when chasing so it looks like a snake
                double lenTarget = Math.sqrt(newVel.x * newVel.x + newVel.z * newVel.z);
                Vec3d rightVec = lenTarget > 0.01 ? new Vec3d(-newVel.z, 0, newVel.x).normalize() : Vec3d.ZERO;
                double horizontalWave = MathHelper.sin(worm.age * 0.15f) * 0.15;
                double verticalWave = MathHelper.cos(worm.age * 0.08f) * 0.08;

                newVel = newVel.add(rightVec.multiply(horizontalWave))
                        .add(0, verticalWave, 0)
                        .normalize()
                        .multiply(speed * 1.2D);

                worm.setVelocity(newVel);
            } else {
                // Organic Wandering AI
                // Increase acceptance radius (400 sq dist) and interval (200 ticks) to avoid
                // tight looping
                if (wanderTarget == null || worm.squaredDistanceTo(wanderTarget) < 400.0 || worm.age % 200 == 0) {
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

                    wanderTarget = new Vec3d(x, y, z);
                }

                // Smoothly steer towards wanderTarget
                Vec3d dir = wanderTarget.subtract(worm.getPos()).normalize();

                // Keep wide curve radius by taking small amounts of direction
                Vec3d newVel = currentVel.add(dir.multiply(0.04)).normalize().multiply(speed * 0.8D);

                // Add horizontal and vertical weaving for organic snake-like slithering
                double lenTarget = Math.sqrt(newVel.x * newVel.x + newVel.z * newVel.z);
                Vec3d rightVec = lenTarget > 0.01 ? new Vec3d(-newVel.z, 0, newVel.x).normalize() : Vec3d.ZERO;

                double horizontalWave = MathHelper.sin(worm.age * 0.1f) * 0.12;
                double verticalWave = MathHelper.cos(worm.age * 0.05f) * 0.06;

                newVel = newVel.add(rightVec.multiply(horizontalWave))
                        .add(0, verticalWave, 0)
                        .normalize()
                        .multiply(speed * 0.8D);

                worm.setVelocity(newVel);
            }

            // Visual Angle Integration
            Vec3d vel = worm.getVelocity();
            float targetYaw = (float) (MathHelper.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90.0F;
            worm.serverSideYaw = wrapDegrees(worm.serverSideYaw, targetYaw, 5.0f);
            worm.setYaw(worm.serverSideYaw);
            worm.bodyYaw = worm.getYaw();
            worm.headYaw = worm.getYaw();

            double horizontalDist = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            float targetPitch = (float) (MathHelper.atan2(vel.y, horizontalDist) * (180 / Math.PI));
            targetPitch = MathHelper.clamp(targetPitch, -65.0f, 65.0f); // Limit so it never looks perfectly straight
                                                                        // up/down
            worm.serverSidePitch = wrapDegrees(worm.serverSidePitch, targetPitch, 15.0f);
            worm.setPitch(worm.serverSidePitch);
            worm.dataTracker.set(TARGET_PITCH, worm.serverSidePitch);

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
}
