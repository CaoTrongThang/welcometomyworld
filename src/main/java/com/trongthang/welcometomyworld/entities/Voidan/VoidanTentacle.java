package com.trongthang.welcometomyworld.entities.Voidan;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.Unknown.Unknown;
import com.trongthang.welcometomyworld.managers.SoundsManager;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.passive.TameableEntity;
import com.trongthang.welcometomyworld.classes.CustomTameableEntity;

public class VoidanTentacle extends HostileEntity implements GeoEntity {
    private static final TrackedData<Boolean> IS_USING_SKILL = DataTracker.registerData(VoidanTentacle.class,
            TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKILL_ID = DataTracker.registerData(VoidanTentacle.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SKILL_TRIGGER = DataTracker.registerData(VoidanTentacle.class,
            TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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

    private boolean skillHitFired = false;
    private int skillTotalTicks = 0;
    private int skillTick = 0;
    private int globalSkillCooldown = 0;
    private final int[] skillCooldowns = new int[10];
    private boolean hasEmerged = false;
    private LivingEntity summoner;
    private UUID summonerUuid;
    private UUID ownerUuid;

    private int ageTicks = 0;
    private static final int MAX_AGE = 2400; // 120 seconds

    public static final Skill EMERGE = new Skill(1, 11, 0); // hit tick 3
    public static final Skill SLAM_HEAD_TO_GROUND = new Skill(2, 23, 80); // hit tick 12
    public static final Skill SPIN_AROUND = new Skill(3, 120, 200); // hit from tick 16 to 110 every 10 ticks
    public static final Skill SHOOT = new Skill(4, 11, 200); // hit tick 11
    public static final Skill BACK_TO_GROUND = new Skill(5, 50, 60); // hit tick 7, total 10 + 40 delay

    public VoidanTentacle(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 2000.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 30.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D);
    }

    public void setSummoner(LivingEntity summoner) {
        this.summoner = summoner;
        if (summoner != null) {
            this.summonerUuid = summoner.getUuid();
            if (summoner instanceof TameableEntity tameable) {
                this.ownerUuid = tameable.getOwnerUuid();
            } else if (summoner instanceof CustomTameableEntity custom) {
                this.ownerUuid = custom.getOwnerUuid();
            } else if (summoner instanceof PlayerEntity) {
                this.ownerUuid = summoner.getUuid();
            }
        }
    }

    public LivingEntity getSummoner() {
        return summoner;
    }

    public boolean isFriendly(LivingEntity other) {
        if (other == this || other == summoner)
            return true;
        if (summonerUuid != null && other.getUuid().equals(summonerUuid))
            return true;

        if (other instanceof VoidanTentacle otherTentacle) {
            if (summonerUuid != null && summonerUuid.equals(otherTentacle.summonerUuid))
                return true;
            if (ownerUuid != null && ownerUuid.equals(otherTentacle.ownerUuid))
                return true;
        }

        if (ownerUuid != null) {
            if (other.getUuid().equals(ownerUuid))
                return true;
            if (other instanceof TameableEntity tameable && ownerUuid.equals(tameable.getOwnerUuid()))
                return true;
            if (other instanceof CustomTameableEntity custom && ownerUuid.equals(custom.getOwnerUuid()))
                return true;
        }
        return false;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_USING_SKILL, false);
        this.dataTracker.startTracking(SKILL_ID, 0);
        this.dataTracker.startTracking(SKILL_TRIGGER, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtTargetGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, (entity) -> {
            return entity instanceof LivingEntity && !isFriendly((LivingEntity) entity);
        }));
    }

    @Override
    public boolean collidesWith(net.minecraft.entity.Entity other) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(net.minecraft.entity.Entity entity) {
    }

    @Override
    public void pushAwayFrom(net.minecraft.entity.Entity entity) {
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (source.getAttacker() instanceof LivingEntity attacker && isFriendly(attacker)) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int[] prevSkillId = { 0 };
        int[] prevSkillTrigger = { 0 };

        controllers.add(new AnimationController<>(this, "mainController", 0, state -> {
            boolean isUsingSkill = this.dataTracker.get(IS_USING_SKILL);
            int skillId = this.dataTracker.get(SKILL_ID);

            if (isUsingSkill) {
                int skillTrigger = this.dataTracker.get(SKILL_TRIGGER);

                state.getController().transitionLength(0); // instant transition for snappy skills
                if (prevSkillId[0] != skillId || prevSkillTrigger[0] != skillTrigger) {
                    state.getController().forceAnimationReset();
                }
                prevSkillId[0] = skillId;
                prevSkillTrigger[0] = skillTrigger;

                switch (skillId) {
                    case 1:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("emerge"));
                    case 2:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("slam_head_to_ground"));
                    case 3:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("spin_around"));
                    case 4:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("shoot"));
                    case 5:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("back_to_ground"));
                }
            } else {
                prevSkillId[0] = 0;
            }

            state.getController().transitionLength(3);
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient()) {
            this.setVelocity(0, 0, 0); // Ensure it does not move or fall

            ageTicks++;
            if (ageTicks > MAX_AGE) {
                this.discard();
            }

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

            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                this.setYaw(targetYaw);
                this.bodyYaw = targetYaw;
                this.headYaw = targetYaw;
            }

            handleSkillEffects(skillId);

            if (skillTick >= skillTotalTicks) {
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
            }
            return;
        }

        if (globalSkillCooldown <= 0 && !isUsingSkill()) {
            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                double dist = this.distanceTo(target);
                Skill picked = null;

                if (dist <= 5.0) {
                    picked = pickWeightedSkill(
                            new Object[] { SLAM_HEAD_TO_GROUND, 50f, true },
                            new Object[] { SPIN_AROUND, 40f, true });
                } else if (dist <= 30.0) {
                    picked = pickWeightedSkill(
                            new Object[] { SHOOT, 100f, true });
                } else {
                    picked = BACK_TO_GROUND;
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

            if (condition && skillCooldowns[skill.id] <= 0) {
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

    public void triggerSkill(Skill skill) {
        if (this.getWorld().isClient())
            return;

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
        this.globalSkillCooldown = (skill.id == EMERGE.id) ? 80 : 20;
        this.skillHitFired = false;
    }

    public boolean isUsingSkill() {
        return this.dataTracker.get(IS_USING_SKILL);
    }

    @Override
    @org.jetbrains.annotations.Nullable
    public net.minecraft.entity.EntityData initialize(net.minecraft.world.ServerWorldAccess world,
            net.minecraft.world.LocalDifficulty difficulty, net.minecraft.entity.SpawnReason spawnReason,
            @org.jetbrains.annotations.Nullable net.minecraft.entity.EntityData entityData,
            @org.jetbrains.annotations.Nullable net.minecraft.nbt.NbtCompound entityNbt) {
        if (!this.hasEmerged) {
            this.hasEmerged = true;
            this.triggerSkill(EMERGE);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasEmerged", this.hasEmerged);
        nbt.putInt("AgeTicks", this.ageTicks);
        if (summonerUuid != null) {
            nbt.putUuid("SummonerUuid", summonerUuid);
        }
        if (ownerUuid != null) {
            nbt.putUuid("OwnerUuid", ownerUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HasEmerged")) {
            this.hasEmerged = nbt.getBoolean("HasEmerged");
            // If already emerged, we don't want to start with the EMERGE skill active by
            // default
            if (this.hasEmerged) {
                this.dataTracker.set(IS_USING_SKILL, false);
                this.dataTracker.set(SKILL_ID, 0);
            }
        }
        if (nbt.contains("AgeTicks")) {
            this.ageTicks = nbt.getInt("AgeTicks");
        }
        if (nbt.containsUuid("SummonerUuid")) {
            this.summonerUuid = nbt.getUuid("SummonerUuid");
        }
        if (nbt.containsUuid("OwnerUuid")) {
            this.ownerUuid = nbt.getUuid("OwnerUuid");
        }
    }

    private void handleSkillEffects(int skillId) {
        float atk = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        switch (skillId) {
            case 1: // EMERGE
                if (!skillHitFired && skillTick == 3) {
                    skillHitFired = true;
                    if (this.getWorld() instanceof ServerWorld sw) {
                        this.playSound(com.trongthang.welcometomyworld.managers.SoundsManager.EMERGE_VOIDAN_TENTACLE,
                                2.0F, 1.0F);
                        sw.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 50, 1.0,
                                0.5, 1.0, 0.1);
                        sw.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 20, 1.0, 0.5,
                                1.0, 0.1);

                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                if (this.random.nextFloat() < 0.7f) {
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
            case 2: // SLAM_HEAD_TO_GROUND
                if (!skillHitFired && skillTick >= 12) {
                    skillHitFired = true;
                    dealSlamHeadDamage(3.0, atk * 1.5f, 4.0);
                }
                break;
            case 3: // SPIN_AROUND
                if (skillTick >= 16 && skillTick <= 110 && skillTick % 10 == 6) {
                    dealAoeGroundDamage(8.0, atk * 0.8f, true);
                }
                break;
            case 4: // SHOOT
                if (!skillHitFired && skillTick >= 10) {
                    skillHitFired = true;
                    shootBeam();
                }
                break;
            case 5: // BACK_TO_GROUND
                if (!skillHitFired && skillTick == 7) {
                    skillHitFired = true;
                    if (this.getWorld() instanceof ServerWorld sw) {
                        this.playSound(SoundsManager.EMERGE_VOIDAN_TENTACLE, 2.0F, 0.8F);
                        sw.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 30, 0.5,
                                0.2, 0.5, 0.1);
                        sw.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.2,
                                0.5, 0.1);
                    }
                }
                // Teleport after the 'back_to_ground' animation finishes (~10 ticks)
                if (skillTick == 10) {
                    LivingEntity target = getTarget();
                    if (target != null && target.isAlive() && this.getWorld() instanceof ServerWorld sw) {
                        double angle = this.random.nextDouble() * 2 * Math.PI;
                        double dist = 4.0 + this.random.nextDouble() * 2.0;
                        double x = target.getX() + Math.cos(angle) * dist;
                        double z = target.getZ() + Math.sin(angle) * dist;
                        BlockPos targetPos = BlockPos.ofFloored(x, target.getY(), z);
                        BlockPos surfacePos = sw.getTopPosition(
                                net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetPos);

                        this.refreshPositionAndAngles(surfacePos.getX() + 0.5, surfacePos.getY(),
                                surfacePos.getZ() + 0.5, this.getYaw(), 0.0f);
                    }
                }
                // Finally emerge after the 2s delay (total 50 ticks)
                if (skillTick >= 50) {
                    this.triggerSkill(EMERGE);
                }
                break;
        }
    }

    private void dealSlamHeadDamage(double radius, float damage, double forwardDist) {
        if (this.getWorld().isClient())
            return;

        double impactX = this.getX() - Math.sin(Math.toRadians(this.bodyYaw)) * forwardDist;
        double impactY = this.getY();
        double impactZ = this.getZ() + Math.cos(Math.toRadians(this.bodyYaw)) * forwardDist;

        Box area = new Box(impactX - radius, impactY - 2.0, impactZ - radius, impactX + radius, impactY + 3.0,
                impactZ + radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            if (isFriendly(t))
                continue;
            double sqDist = t.squaredDistanceTo(impactX, t.getY(), impactZ);
            if (sqDist <= radius * radius) {
                Unknown.dealUnknownDamage(this, t, damage, 0.002f, 0.01f);
            }
        }

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundsManager.FALLEN_KNIGHT_GROUND_IMPACT_NO_DELAY, this.getSoundCategory(), 0.3F, 1.0F);
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION, impactX, impactY + 1.0, impactZ, 3, 1.0, 1.0, 1.0, 0);

            int ringIndex = 0;
            java.util.Set<BlockPos> spawnedPositions = new java.util.HashSet<>();

            for (double r = 1.5; r <= radius + 0.5; r += 1.5) {
                final double currentRadius = r;
                final int finalDelay = ringIndex / 2;

                Utils.addRunAfter(() -> {
                    int blockCount = (int) (currentRadius * 8); // denser rings
                    for (int i = 0; i < blockCount; i++) {
                        double angle = 2 * Math.PI * i / blockCount;
                        double x = impactX + currentRadius * Math.cos(angle);
                        double z = impactZ + currentRadius * Math.sin(angle);
                        BlockPos spawnPos = BlockPos.ofFloored(x, impactY, z);

                        if (spawnedPositions.contains(spawnPos)) {
                            continue;
                        }
                        spawnedPositions.add(spawnPos);

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
    }

    private void dealAoeGroundDamage(double radius, float damage, boolean applyEffects) {
        if (this.getWorld().isClient())
            return;

        Box area = this.getBoundingBox().expand(radius);
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(LivingEntity.class, area, e -> e != this);
        for (LivingEntity t : nearby) {
            if (isFriendly(t))
                continue;
            Unknown.dealUnknownDamage(this, t, damage, 0.002f, 0.01f);

            if (applyEffects) {
                t.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.SLOWNESS, 100, 1));
                t.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                        net.minecraft.entity.effect.StatusEffects.WEAKNESS, 100, 1));
            }

            if (this.getWorld() instanceof ServerWorld sw) {
                // Ensure the particle spawns precisely
                sw.spawnParticles(ParticleTypes.SONIC_BOOM, t.getX(), t.getY() + 1.0, t.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    private void shootBeam() {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !isFriendly(target) && this.getWorld() instanceof ServerWorld sw) {
            Unknown.dealUnknownDamage(this, target, 15.0f, 0.002f, 0.01f);

            // Spawn a mini sonic boom particle trail towards the target's location
            Vec3d startPos = this.getPos().add(0, 3.0, 0); // mouth height
            Vec3d targetPos = target.getEyePos();
            Vec3d dir = targetPos.subtract(startPos);
            Vec3d dirNormalized = dir.normalize();

            int distance = MathHelper.floor(dir.length()) + 2;
            for (int i = 1; i < distance; i++) {
                Vec3d pos = startPos.add(dirNormalized.multiply(i));
                sw.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }

            // Play a smaller sonic boom sound effect
            this.playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        }
    }

    public class LookAtTargetGoal extends Goal {
        private final VoidanTentacle mob;

        public LookAtTargetGoal(VoidanTentacle mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUsingSkill();
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.getLookControl().lookAt(target, 30.0F, 30.0F);
            }
        }
    }
}
