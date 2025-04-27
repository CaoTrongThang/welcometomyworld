package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.tameablePacket.StrongTameableEntityDefault;
import com.trongthang.welcometomyworld.client.ClientScheduler;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.trongthang.welcometomyworld.Utilities.SpawnParticiles.spawnParticlesAroundEntity;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.PLAY_BLOCK_LEVER_CLICK;
import static net.minecraft.entity.effect.StatusEffects.REGENERATION;
import static net.minecraft.entity.effect.StatusEffects.RESISTANCE;

public class Blossom extends StrongTameableEntityDefault {

    private static final List<String> TAMED_MESSAGES = List.of(
            "You're the worthy challenger to protect this world...",
            "I'll help you in your journey",
            "Our fates are now intertwined...",
            "The blossoms whisper of your destiny...",
            "Together we shall nurture this world's balance"
    );

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    private static final TrackedData<Boolean> IS_GREETING = DataTracker.registerData(Blossom.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final int ATTACK_2_DURATION_MS = 10000;
    private static final int[] ATTACK_2_SOUND_TIMINGS_MS = {100};
    private final Set<Integer> attack2PlayedFrames = new HashSet<>();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState greetingAnimationState = new AnimationState();

    public final AnimationState healAnimationState = new AnimationState();
    public final AnimationState selfHealAnimationState = new AnimationState();

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState attack2AnimationState = new AnimationState();

    public final AnimationState sitAnimationState = new AnimationState();

    public LivingEntity greetingTarget = null;

    private double skillCooldownDecreasedBasedOnMobScale = 1;
    private int useSkillCooldownCounter = 0;

    private static final int BUFF_DURATION = 600;

    private int attackSkillRange = 6;

    private int maxStatsScale = 8;

    public BlockPos patrolCenterPos = null;

    public int animationTimeout = 0;
    public static final int DEFAULT_ANIMATION_TIMEOUT = 15;

    private int ultimateCooldown = 400;
    private int defaultUltimateCooldown = 4800;

    private int healCooldownCounter = 200;
    private float selfHealPercent = 0.03f;
    private float groupHealPercent = 0.03f;

    private int defaultHealCooldown = 1200;

    private int particleCounter = 0;
    private int particleCooldown = 10;


    public Blossom(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.navigation = new BlossomNavigation(this, world);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.WALK, walkAnimationState);
        animationHashMap.put(AnimationName.GRETTING, greetingAnimationState);
        animationHashMap.put(AnimationName.HEAL, healAnimationState);
        animationHashMap.put(AnimationName.SELF_HEAL, selfHealAnimationState);

        animationHashMap.put(AnimationName.SIT, sitAnimationState);
        animationHashMap.put(AnimationName.ATTACK, attackAnimationState);
        animationHashMap.put(AnimationName.ATTACK2, attack2AnimationState);

        if (!this.getWorld().isClient && !this.getIsRandomFirstTime()) {
            double scale = WelcomeToMyWorld.random.nextDouble(1, maxStatsScale);
            skillCooldownDecreasedBasedOnMobScale = scale;

            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) * scale);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (double) (scale / 2));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR) * (double) (scale / 4F));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * (double) (scale / 2.5F));

            if (skillCooldownDecreasedBasedOnMobScale / 3 > 1) {
                this.setAllSkillCooldown((float) (this.getAllSkillCooldown() / (skillCooldownDecreasedBasedOnMobScale / 3)));
            }

            if (this.getAllSkillCooldown() <= 60) {
                this.setAllSkillCooldown(60);
            }

            this.setHealth(this.getMaxHealth());

            this.setIsRandomFirstTime(true);
        }
    }

    @Override
    public MoveControl getMoveControl() {
        Entity var2 = this.getControllingVehicle();
        if (var2 instanceof MobEntity mobEntity) {
            return mobEntity.getMoveControl();
        } else {
            return this.moveControl;
        }
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 23f)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 1f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64f)
                .add(EntityAttributes.GENERIC_ARMOR, 4f)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10f)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5f);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }


    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_GREETING, false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new CustomSitGoal(this));
        this.goalSelector.add(3, new PatrollingGoal(this));
        this.goalSelector.add(5, new CustomFollowOwnerGoal(this, 0.8, 20, 25, false));
        this.goalSelector.add(6, new FollowTargetGoal(this, 8.0D, 20.0F, 48.0F));
        this.goalSelector.add(7, new StopWhenUsingSkill(this));
        this.goalSelector.add(8, new CustomFlyingWanderingAroundGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new CustomTrackOwnerAttackGoal(this));
        this.targetSelector.add(2, new CustomAttackWithOwnerGoal(this));
        this.targetSelector.add(3, new CustomRevengeGoal(this).setGroupRevenge());

        this.targetSelector.add(4, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        setAnimationStates();

//        if (this.getWorld().isClient) {
//            WelcomeToMyWorld.LOGGER.info("[CLIENT] DEATH: " + this.isDead());
////            WelcomeToMyWorld.LOGGER.info("[CLIENT] SITTING: " + this.isInSittingPose());
//        } else {
//            WelcomeToMyWorld.LOGGER.info("[SERVER] DEATH: " + this.isDead());
////            WelcomeToMyWorld.LOGGER.info("[SERVER] SITTING: " + this.isInSittingPose());
//        }

        if (!this.getWorld().isClient) {
            if (this.getIsGreeting()) {
                if (this.greetingTarget != null) {
                    ServerPlayerEntity player = this.getWorld().getServer().getPlayerManager().getPlayer(this.greetingTarget.getUuid());
                    if (player != null) {
                        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, player.getPos());
                    }
                }

                Utils.addRunAfter(() -> {
                    if (!this.getIsGreeting()) return;
                    this.setIsGreeting(false);
                    this.discard();
                }, 70);
                return;
            }

            if (this.getTarget() != null) {
                if (this.getTarget().isDead()) {
                    this.setTarget(null);
                }
            }
        } else {
            particleCounter++;
            if (particleCounter > particleCooldown) {
                particleCounter = 0;
                spawnParticlesAroundEntity(this, ParticleTypes.HAPPY_VILLAGER, 1, 2);
            }
        }

        usingSkillsHandler();
        handleAnimationSoundsAndEffect();
    }


    public void setAnimationStates() {
        if (this.getWorld().isClient) {
            if (this.getIsGreeting()) {
                if (!greetingAnimationState.isRunning()) {
                    ClientScheduler.schedule(() -> {
                        spawnTeleportParticles(this.getX(), this.getY(), this.getZ());
                        Utils.playClientSound(this.getBlockPos(), SoundsManager.BLOSSOM_LAUGH, 16, 0.3f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.2f));
                        Utils.playClientSound(this.getBlockPos(), SoundsManager.BLOSSOM_WALK, 16, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.2f));
                    }, 10);

                    spawnTeleportParticles(this.getX(), this.getY(), this.getZ());

                    startAnimation(AnimationName.GRETTING);

                    ClientScheduler.schedule(() -> {
                        spawnTeleportParticles(this.getX(), this.getY(), this.getZ());
                        Utils.playClientSound(this.getBlockPos(), SoundsManager.BLOSSOM_WALK, 16, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.2f));
                    }, 70);

                }
                return;
            }
            if (!this.getIsUsingSkill()) {
                if (this.isInSittingPose()) {
                    if (!sitAnimationState.isRunning()) {
                        startAnimation(AnimationName.SIT);
                    }
                } else {
                    if (animationTimeout <= 0 && !this.getIsUsingSkill()) {
                        if (!walkAnimationState.isRunning()) {
                            startAnimation(AnimationName.WALK);
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
        if (!this.getWorld().isClient && !this.getIsGreeting()) {
            if (this.ultimateCooldown > 0) {
                this.ultimateCooldown--;
            }

            if (this.healCooldownCounter > 0) {
                this.healCooldownCounter--;
            }

            if (!this.isInSittingPose()) {
                if (this.getIsUsingSkill()) {
                    if (this.getTarget() != null) {
                        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.getTarget().getPos());
                    }
                    return;
                }

                if (this.useSkillCooldownCounter < this.getAllSkillCooldown() && !this.getIsUsingSkill()) {
                    this.useSkillCooldownCounter++;
                    return;
                }

                ServerWorld world = (ServerWorld) this.getWorld();
                int timeout = 0;

                if (this.isTamed() || this.getOwner() != null) {
                    if (this.healCooldownCounter <= 0) {
                        this.healCooldownCounter = this.defaultHealCooldown;
                        this.useSkillCooldownCounter = 0;

                        timeout = 41;

                        Box checkArea = new Box(this.getBlockPos()).expand(15);
                        List<LivingEntity> allies = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea,
                                entity -> {
                                    if (entity instanceof PlayerEntity) {
                                        return true;
                                    }
                                    if (entity instanceof TameableEntity tameable) {
                                        if (tameable.getOwner() == this.getOwner()) {
                                            return true;
                                        }
                                    }
                                    return false;
                                });

                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.HEAL, timeout);

                        Utils.addRunAfter(() -> {
                            // Calculate buff parameters based on stats
                            int regenAmplifier = Math.min((int) (this.getMaxHealth() / 200), 10);
                            int resistanceAmplifier = Math.min((int) (this.getArmor() / 2.5f), 10);

                            // Randomly choose between Regeneration or Resistance
                            boolean useRegen = this.random.nextFloat() < 0.5f;
                            int totalBuff = 0;
                            for (LivingEntity ally : allies) {
                                StatusEffect effect = useRegen ? REGENERATION : RESISTANCE;

                                if (ally.hasStatusEffect(effect)) {
                                    continue;
                                }

                                int amplifier = useRegen ? regenAmplifier : resistanceAmplifier;

                                if (amplifier > 0) {
                                    totalBuff++;
                                    ally.addStatusEffect(new StatusEffectInstance(
                                            effect,
                                            BUFF_DURATION,
                                            amplifier,
                                            false,
                                            true,
                                            true
                                    ));

                                    // Spawn matching particles
                                    spawnBuffParticles(ally.getWorld(), ally.getPos(), effect);
                                }
                                if (ally instanceof PlayerEntity) {
                                    Utils.sendSoundPacketToClient(SoundsManager.BLOSSOM_BUFF, ally.getBlockPos());
                                }

                                this.healCooldownCounter = Math.min(totalBuff * 200, this.defaultHealCooldown);

                                ally.setHealth(ally.getHealth() + (ally.getMaxHealth() * this.groupHealPercent));
                            }

                            if (!allies.isEmpty()) {
                                spawnBuffCircle(this.getWorld(), this.getBlockPos(),
                                        useRegen ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.TOTEM_OF_UNDYING);
                            }
                        }, 20);

                        resetSkill(timeout);
                        if (!allies.isEmpty()) return;
                    }
                }

                if (this.getTarget() != null) {
                    if (this.healCooldownCounter <= 0) {
                        if (this.getHealth() < this.getMaxHealth() * 0.8f) {
                            this.useSkillCooldownCounter = 0;
                            this.healCooldownCounter = this.defaultHealCooldown;

                            timeout = 37;
                            Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.SELF_HEAL, timeout);

                            Utils.addRunAfter(() -> {
                                this.setHealth(this.getHealth() + (this.getMaxHealth() * this.selfHealPercent));
                            }, 10);

                            resetSkill(timeout);

                            spawnBuffCircle(this.getWorld(), this.getBlockPos(), ParticleTypes.HEART);
                            return;
                        }
                    }

                    if (this.ultimateCooldown <= 0) {
                        this.ultimateCooldown = this.defaultUltimateCooldown;
                        this.useSkillCooldownCounter = 0;

                        timeout = 200;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK2, timeout);

                        if (!world.isRaining() && !world.isThundering()) {
                            world.setWeather(0, 200, true, true);

                            Utils.addRunAfter(() -> {
                                world.setWeather(0, 0, false, false);
                            }, 220);
                        }

                        for (int x = 0; x < 20; x++) {
                            Utils.addRunAfter(() -> {
                                if (this.isDead()) return;

                                createShockwave();
                                if (this.getTarget() == null) return;
                            }, 100 + (x * 5));
                        }

                        resetSkill(timeout);
                    } else {
                        timeout = 40;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK, timeout);

                        Utils.addRunAfter(() -> {
                            createShockwave();
                            if (this.getTarget() == null) return;
                        }, 20);

                        this.useSkillCooldownCounter = 0;
                        resetSkill(timeout);
                    }
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

    // Add these helper methods
    private void spawnBuffParticles(World world, Vec3d position, StatusEffect effect) {
        if (world instanceof ServerWorld serverWorld) {
            ParticleEffect particle = effect == REGENERATION ?
                    ParticleTypes.HAPPY_VILLAGER :
                    ParticleTypes.TOTEM_OF_UNDYING;

            serverWorld.spawnParticles(particle,
                    position.x, position.y + 1, position.z,
                    5, // Count
                    0.5, 0.5, 0.5, // Delta
                    0.1 // Speed
            );
        }
    }

    private void spawnBuffCircle(World world, BlockPos center, ParticleEffect particle) {
        if (world instanceof ServerWorld serverWorld) {
            int points = 30;
            double radius = 3.0;

            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double x = center.getX() + 0.5 + radius * Math.cos(angle);
                double z = center.getZ() + 0.5 + radius * Math.sin(angle);

                serverWorld.spawnParticles(particle,
                        x, center.getY() + 0.1, z,
                        1, // Count
                        0, 0.1, 0, // Delta
                        0.05 // Speed
                );
            }
        }
    }

    private void handleAnimationSoundsAndEffect() {
        if (!this.getWorld().isClient) return;

        if (attack2AnimationState.isRunning()) {
            handleAttack2Sounds();
        }
    }

    private void spawnTeleportParticles(double x, double y, double z) {
        World world = this.getWorld();

        // Spawn particles in a small explosion
        for (int i = 0; i < 20; i++) {
            double offsetX = world.getRandom().nextGaussian() * 0.2;
            double offsetY = world.getRandom().nextGaussian() * 0.2;
            double offsetZ = world.getRandom().nextGaussian() * 0.2;

            world.addParticle(
                    ParticleTypes.END_ROD,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0.0, 0.0, 0.0 // Velocity (set to 0)
            );
        }
    }

    private void spawnTeleportParticles() {
        ServerWorld world = (ServerWorld) this.getWorld();

        // Spawn particles in a small explosion
        for (int i = 0; i < 5; i++) {
            double offsetX = world.getRandom().nextGaussian() * 0.2;
            double offsetY = world.getRandom().nextGaussian() * 0.2;
            double offsetZ = world.getRandom().nextGaussian() * 0.2;

            world.spawnParticles(
                    ParticleTypes.END_ROD,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    15,
                    0.3,
                    0.1,
                    0.3,
                    0.05
            );
        }
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
                triggerAttack3SoundSound(timing);
                attack2PlayedFrames.add(timing);
            }
        }

        if (currentPos > ATTACK_2_DURATION_MS - 150) {
            attack2PlayedFrames.clear();
        }
    }

    private void triggerAttack3SoundSound(int timing) {
        SoundEvent soundId = null;

        if (timing >= 50 && timing <= 500) {
            soundId = SoundsManager.BLOSSOM_ULTIMATE;
        }

        if (soundId == null) return;

//        Utils.sendSoundPacket(soundId, getBlockPos());
        Utils.playClientSound(this.getBlockPos(), soundId, 30);
    }

    private void createShockwave() {
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            if (this.getTarget() == null) {
                return;
            }


            BlockPos targetBlockPos = this.getTarget().getBlockPos().add(0, 1, 0);
            BlockPos center = this.getTarget().getBlockPos();

            for(int y = 0; y < 8; y++){
                BlockPos currentPos = targetBlockPos.add(0, y, 0);
                BlockState state = serverWorld.getBlockState(currentPos);

                if (!state.getCollisionShape(serverWorld, currentPos).isEmpty()) {
                    center = currentPos;
                    break;
                }
            }

            Utils.summonLightning(center, (ServerWorld) this.getWorld(), true);

            if (center == null) {
                center = this.getBlockPos();
            }

            Box checkArea = new Box(center).expand(attackSkillRange);
            List<LivingEntity> damageTarget = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

            // Set to track blocks where particles have been spawned
            for (int x = -attackSkillRange; x <= attackSkillRange; x++) {
                for (int z = -attackSkillRange; z <= attackSkillRange; z++) {
                    for (int y = center.getY() - 1; y >= center.getY() - 3; y--) {

                        BlockPos pos = new BlockPos(center.getX() + x, y, center.getZ() + z);
                        BlockState state = serverWorld.getBlockState(pos);
                        if (!state.isAir()) {
                            float ra = random.nextFloat();
                            if (ra < 0.4f) {
                                spawnBlockParticles(serverWorld, pos, state);
                            }

                            if (ra < 0.04f) {
                                Utils.CreateBlockSlamGround(serverWorld, state, pos);
                            }

                            break;
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

                if (target instanceof PlayerEntity) {
                    if (this.isTamed() || this.getOwner() != null) {
                        continue;
                    }
                }

                if (target instanceof Blossom mob) {
                    // Skip untamed vs. untamed damage
                    if (!this.isTamed() && !mob.isTamed()) {
                        continue;
                    }

                    // Skip tamed vs. tamed damage if they have the same owner
                    if (this.isTamed() && this.getOwner() != null && mob.isTamed() && mob.getOwner() != null) {
                        if (this.getOwner().equals(mob.getOwner())) {
                            continue;
                        }
                    }
                }

                boolean invisToSky = true;

                for(int y = 0; y < 8; y++){
                    BlockPos currentPos = targetBlockPos.add(0, y, 0);
                    BlockState state = serverWorld.getBlockState(currentPos);

                    if (!state.getCollisionShape(serverWorld, currentPos).isEmpty()) {
                        invisToSky = false;
                        break;
                    }
                }

                if(invisToSky){
                    float damage = (float) this.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * 1.5f;

                    if(!damageBlockingShield(target, damage)){
                        target.damage(this.getWorld().getDamageSources().mobAttack(this), damage);
                    };
                }
            }
        }
    }

    protected boolean teleportRandomly() {
        if (!this.getWorld().isClient() && this.isAlive()) {
            double d = this.getX() + (this.random.nextDouble() - (double) 0.5F) * (double) 32.0F;
            double e = this.getY() + (double) (this.random.nextInt(32) - 16);
            double f = this.getZ() + (this.random.nextDouble() - (double) 0.5F) * (double) 32.0F;
            return this.teleportTo(d, e, f);
        } else {
            return false;
        }
    }

    private boolean teleportTo(double x, double y, double z) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        while (mutable.getY() > this.getWorld().getBottomY() && !this.getWorld().getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = this.getWorld().getBlockState(mutable);
        boolean bl = blockState.blocksMovement();
        boolean bl2 = blockState.getFluidState().isIn(FluidTags.WATER);
        if (bl && !bl2) {
            boolean bl3 = this.teleport(x, y, z, false);

            return bl3;
        } else {
            return false;
        }
    }


    private boolean damageBlockingShield(LivingEntity target, float damage) {
        if (this.getTarget() == null) return false;
        if (target.isBlocking() && target.getActiveItem().isDamageable()) {
            target.getActiveItem().damage((int) damage, target,
                    entity -> entity.sendToolBreakStatus(target.getActiveHand()));

            return true;
        }

        return false;
    }

    public static void spawnBlockParticles(ServerWorld world, BlockPos pos, BlockState state) {
        ParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        world.spawnParticles(particle, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.5, 0.2, 0.1);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        World world = this.getWorld();

        if (world.isClient) {
            if (this.isTamed() && this.getOwner() == player) {
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }

        if (this.isTamed() && this.getOwner() == player) {
            this.setSitting(!this.isSitting());
            this.setInSittingPose(this.isSitting());
            this.navigation.stop();
            this.setIsPatrolling(false);
            this.targetSelector.remove(new ActiveTargetGoal<>(this, HostileEntity.class, 10, false, false, (hostile) -> {
                LivingEntity owner = this.getOwner();
                if (owner == null) {
                    return true;
                }
                return (owner.getAttacker() == null && owner.getAttacking() == null);
            }));

            if (this.isSitting()) {
                this.setNoGravity(false);
            } else {
                this.setNoGravity(true);
            }

            this.setTarget(null);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
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
        }
    }

    // Store the goal instance
    private ActiveTargetGoal<HostileEntity> hostileTargetGoal;

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getIsGreeting()) {
            return super.damage(source, 0);
        }

        if (!this.getWorld().isClient) {
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

            if (source.getAttacker() instanceof LivingEntity && WelcomeToMyWorld.random.nextInt(0, 100) <= 40 && !this.isInSittingPose()) {
                spawnTeleportParticles();
                if (this.teleportRandomly()) {
                    return super.damage(source, 0);
                } else {
                    return super.damage(source, amount);
                }
            }
        }

        return super.damage(source, amount);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundsManager.BLOSSOM_HURT;
    }

    @Override
    public void onDeath(DamageSource source) {

        super.onDeath(source);
        if (this.isTamed() || this.getOwner() != null) return;

        if (WelcomeToMyWorld.random.nextInt(0, 100) > this.getTameChance()) return;

        Entity attacker = source.getAttacker();

        if(attacker == null) return;

        if(attacker.getWorld().isClient()) return;

        ServerPlayerEntity player = null;
        if(attacker instanceof TameableEntity tameable){
            if(tameable.isTamed() && tameable.getOwner() != null){
                if(tameable.getOwner() instanceof ServerPlayerEntity playerEntity){
                    player = playerEntity;
                }
            }
        }
        if(player != null){
            summonNewBlossom(player);
            return;
        }

        if (attacker instanceof ServerPlayerEntity p) {
            summonNewBlossom(p);
        }
    }

    private void summonNewBlossom(ServerPlayerEntity p){
        ServerWorld world = (ServerWorld) this.getWorld();
        BlockPos deathPos = this.getBlockPos();

        Utils.grantAdvancement((ServerPlayerEntity) p, "tameable/blooming");

        // Immediate explosion effect
        world.spawnParticles(ParticleTypes.POOF,
                deathPos.getX() + 0.5, deathPos.getY() + 1.0, deathPos.getZ() + 0.5,
                30, 0.5, 0.5, 0.5, 0.2
        );

        // Delayed flower effects
        world.getServer().execute(() -> {
            // Create flower circle
            createFlowerRing(world, deathPos);

            // Spiral particle effect
            for (int i = 0; i < 360; i += 15) {
                double angle = Math.toRadians(i);
                double x = deathPos.getX() + 0.5 + Math.cos(angle) * 3;
                double z = deathPos.getZ() + 0.5 + Math.sin(angle) * 3;

                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        x, deathPos.getY() + 1, z,
                        3, 0, 0.1, 0, 0.05
                );
            }

            // Beam effect
            for (int y = 0; y < 10; y++) {
                world.spawnParticles(ParticleTypes.END_ROD,
                        deathPos.getX() + 0.5, deathPos.getY() + y, deathPos.getZ() + 0.5,
                        2, 0.1, 0.1, 0.1, 0.02
                );
            }
        });

        world.getServer().execute(() -> {
            Utils.addRunAfter(() -> {
                Text message = Text.literal("").styled(style -> style.withColor(Formatting.WHITE))
                        .append(Text.literal("Blossom:").styled(style -> style.withColor(Formatting.GREEN)))
                        .append(Text.literal(" " + TAMED_MESSAGES.get(WelcomeToMyWorld.random.nextInt(TAMED_MESSAGES.size()))).styled(style -> style.withColor(Formatting.WHITE)));

                p.sendMessage(message);

                ServerPlayNetworking.send((ServerPlayerEntity) p, PLAY_BLOCK_LEVER_CLICK, PacketByteBufs.empty());
            }, 80);

            Blossom blossom = EntitiesManager.BLOSSOM.create(world);

            blossom.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH));
            blossom.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
            blossom.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR));
            blossom.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

            blossom.setAllSkillCooldown(this.getAllSkillCooldown());

            blossom.goalSelector.remove(new ActiveTargetGoal<>(this, HostileEntity.class, true));
            blossom.targetSelector.remove(new ActiveTargetGoal<>(this, HostileEntity.class, true));

            if (blossom != null) {
                // Teleportation effect
                world.spawnParticles(ParticleTypes.CHERRY_LEAVES,
                        p.getX(), p.getY() + 1, p.getZ(),
                        30, 0.5, 0.5, 0.5, 0.1
                );

                blossom.refreshPositionAndAngles(deathPos, 0, 0);
                world.spawnEntity(blossom);

                // Set owner and properties
                blossom.setOwner(p);
                blossom.setTamed(true);
                blossom.setHealth(blossom.getMaxHealth() * 0.5f);

                // Healing effect
                world.playSound(null, deathPos, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                        SoundCategory.NEUTRAL, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);

                // Sustained particles
                for (int i = 0; i < 6; i++) {
                    world.getServer().execute(() -> {
                        world.spawnParticles(ParticleTypes.GLOW,
                                blossom.getX(), blossom.getY() + 1, blossom.getZ(),
                                10, 0.3, 0.5, 0.3, 0.05
                        );
                    });
                }
            }
        });
    }

    private void createFlowerRing(ServerWorld world, BlockPos center) {
        BlockState flower = Blocks.POPPY.getDefaultState();
        int radius = 3;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    BlockPos pos = center.add(x, 0, z);
                    if (world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, flower);
                        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                3, 0.1, 0.1, 0.1, 0.05
                        );
                    }
                }
            }
        }

        world.playSound(null, center, SoundsManager.BLOSSOM_RISE,
                SoundCategory.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);
        world.playSound(null, center, SoundsManager.BLOSSOM_AMBIENT,
                SoundCategory.BLOCKS, 1.0f, 0.8f + world.random.nextFloat() * 0.4f);
    }

    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    public boolean getIsGreeting() {
        return this.dataTracker.get(IS_GREETING);
    }

    public void setIsGreeting(boolean variant) {
        this.dataTracker.set(IS_GREETING, variant);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("allSkillCooldown", this.getAllSkillCooldown());
        nbt.putInt("ultimateCooldown", this.ultimateCooldown);
        nbt.putBoolean("isRandomFirstTime", this.getIsRandomFirstTime());
        nbt.putBoolean("isGreeting", this.getIsGreeting());

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
        this.setIsRandomFirstTime(nbt.getBoolean("isRandomFirstTime"));
        this.setIsGreeting(nbt.getBoolean("isGreeting"));

        if (nbt.getFloat("allSkillCooldown") > 0) {
            this.setAllSkillCooldown(nbt.getFloat("allSkillCooldown"));
        }

        if (nbt.contains("ultimateCooldown")) {
            this.ultimateCooldown = nbt.getInt("ultimateCooldown");
        }

        if (nbt.contains("patrolPos", NbtElement.LIST_TYPE)) {
            NbtList homePos = nbt.getList("patrolPos", NbtElement.DOUBLE_TYPE);
            this.patrolCenterPos = new BlockPos((int) homePos.getDouble(0), (int) homePos.getDouble(1), (int) homePos.getDouble(2));
        } else {
            this.patrolCenterPos = null;
        }
    }

    class CustomFlyingWanderingAroundGoal extends Goal {
        private static final int HORIZONTAL_RANGE = 24;
        private static final int VERTICAL_RANGE = 12;
        private static final float SPEED_MODIFIER = 2f;

        private static final int GROUND_SEARCH_DEPTH = 10;
        private static final int MIN_GROUND_DISTANCE = 2;
        private static final int MAX_GROUND_DISTANCE = 8;

        private final Blossom blossom;
        private int failedAttempts;

        CustomFlyingWanderingAroundGoal(Blossom blossom) {
            this.blossom = blossom;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            return blossom.navigation.isIdle() && blossom.getRandom().nextFloat() < 0.05f && !blossom.isInSittingPose() && !blossom.getIsPatrolling();
        }

        public void start() {
            Vec3d target = findValidAirPosition();
            if (target != null) {
                blossom.getNavigation().startMovingTo(target.x, target.y, target.z, SPEED_MODIFIER);
                failedAttempts = 0;
            } else {
                failedAttempts++;
            }
        }

        public boolean shouldContinue() {
            return blossom.navigation.isFollowingPath() && failedAttempts < 3 && !blossom.isInSittingPose() && !blossom.getIsPatrolling() && !blossom.isDead();
        }

        private Vec3d findValidAirPosition() {
            Random random = blossom.getRandom();
            Vec3d currentPos = blossom.getPos();

            for (int i = 0; i < 30; i++) {
                Vec3d candidate = currentPos.add(
                        random.nextGaussian() * HORIZONTAL_RANGE,
                        (random.nextFloat() - 0.5f) * VERTICAL_RANGE,
                        random.nextGaussian() * HORIZONTAL_RANGE
                );

                BlockPos candidatePos = BlockPos.ofFloored(candidate);
                World world = blossom.getWorld();

                if (!world.isChunkLoaded(candidatePos)) continue;

                // Find nearest solid block below candidate
                int groundY = findGroundY(world, candidatePos);
                if (groundY != -1) {
                    // Adjust to fly 5-10 blocks above ground
                    candidate = new Vec3d(
                            candidate.x,
                            groundY + MIN_GROUND_DISTANCE + random.nextInt(MAX_GROUND_DISTANCE - MIN_GROUND_DISTANCE + 1),
                            candidate.z
                    );
                } else {
                    // No ground found, lower flight altitude
                    candidate = new Vec3d(
                            candidate.x,
                            Math.max(candidate.y - (MIN_GROUND_DISTANCE + random.nextInt(6)), world.getBottomY()),
                            candidate.z
                    );
                }

                if (isValidFlightPosition(BlockPos.ofFloored(candidate))) {
                    return candidate;
                }
            }
            return null;
        }

        private int findGroundY(World world, BlockPos pos) {
            // Search downward for solid blocks within GROUND_SEARCH_DEPTH
            int startY = pos.getY();
            int minY = Math.max(startY - GROUND_SEARCH_DEPTH, world.getBottomY());

            for (int y = startY; y >= minY; y--) {
                BlockPos currentPos = new BlockPos(pos.getX(), y, pos.getZ());
                if (world.getBlockState(currentPos).isSolid()) {
                    return y;
                }
            }
            return -1; // No ground found
        }

        private boolean isValidFlightPosition(BlockPos pos) {
            World world = blossom.getWorld();
            return world.isAir(pos) && world.isAir(pos.up());
        }
    }

    public class PatrollingGoal extends Goal {
        private final Blossom mob;
        private final double patrolRadius = 24.0;
        private final double teleportDistance = 32.0;
        private int cooldown = 0;
        private int failedNavigationAttempts = 0;

        // Timer to change waypoint for patrol behavior
        private int waypointCooldown = 0;
        private Vec3d currentWaypoint = null;

        public PatrollingGoal(Blossom mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getIsPatrolling() && mob.patrolCenterPos != null;
        }

        @Override
        public boolean shouldContinue() {
            if (!canStart()) {
                return false;
            }
            if (mob.patrolCenterPos == null) {
                return false;
            }

            if (!mob.getIsPatrolling()) {
                return false;
            }

            double distance = getDistanceFromCenter();
            // Continue goal if we are out of bounds or if cooldown is still active
            return distance > patrolRadius || cooldown > 0 || waypointCooldown > 0;
        }

        @Override
        public void tick() {
            if (cooldown > 0) {
                cooldown--;
                return;
            }

            if(mob.patrolCenterPos == null) return;

            Vec3d center = mob.patrolCenterPos.toCenterPos();
            double distanceFromCenter = getDistanceFromCenter();

            // If too far from the patrol center, try teleport or navigate back
            if (distanceFromCenter > teleportDistance) {
                attemptTeleport(center);
                return;
            } else if (distanceFromCenter > patrolRadius) {
                attemptReturnToCenter(center);
                return;
            }

            // Patrolling behavior: choose a new random waypoint if necessary.
            if (currentWaypoint == null || waypointCooldown <= 0) {
                currentWaypoint = selectNewPatrolWaypoint();
                waypointCooldown = 80 + random.nextInt(70);
            } else {
                waypointCooldown--;
            }

            // Command the mob's navigation to move to the current waypoint.
            if (mob.getNavigation().isIdle()) {
                boolean pathFound = mob.getNavigation().startMovingTo(currentWaypoint.x, currentWaypoint.y, currentWaypoint.z, 1.0f);
                if (!pathFound) {
                    // If path not found after several attempts, try teleporting to the waypoint
                    if (++failedNavigationAttempts >= 3) {
                        attemptTeleport(currentWaypoint);
                    }
                } else {
                    failedNavigationAttempts = 0;
                }
            }
        }

        private double getDistanceFromCenter() {
            return mob.getPos().distanceTo(mob.patrolCenterPos.toCenterPos());
        }

        private Vec3d selectNewPatrolWaypoint() {
            // Generate a random waypoint within a sphere around the patrol center
            Vec3d center = mob.patrolCenterPos.toCenterPos();
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = patrolRadius * random.nextDouble();
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            // Allow some vertical variation (e.g., ± patrolRadius/2)
            double offsetY = (random.nextDouble() - 0.5) * patrolRadius;
            return center.add(offsetX, offsetY, offsetZ);
        }

        private void attemptTeleport(Vec3d target) {
            if (mob.teleportTo(target.x, target.y, target.z)) {

                cooldown = 100;
                mob.setTarget(null);
                mob.getNavigation().stop();
                failedNavigationAttempts = 0;
            }
        }

        private void attemptReturnToCenter(Vec3d target) {
            if (mob.getNavigation().isIdle()) {
                boolean pathFound = mob.getNavigation().startMovingTo(target.x, target.y, target.z, 2.0f);
                if (!pathFound) {
                    if (++failedNavigationAttempts >= 3) {
                        attemptTeleport(target);
                    }
                } else {
                    failedNavigationAttempts = 0;
                }
            }
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            cooldown = 0;
            failedNavigationAttempts = 0;
            waypointCooldown = 0;
            currentWaypoint = null;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }
    }

    public class BlossomNavigation extends BirdNavigation {
        public BlossomNavigation(MobEntity mob, World world) {
            super(mob, world);
            setCanPathThroughDoors(true);
            setCanSwim(true);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            // Allow navigation through any air block
            return world.isAir(pos);
        }
    }

    public class StopWhenUsingSkill extends Goal {
        private final Blossom mob;

        public StopWhenUsingSkill(Blossom mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
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

    class CustomTrackOwnerAttackGoal extends TrackTargetGoal {
        private final Blossom mob;
        private LivingEntity attacker;
        private int lastAttackedTime;

        public CustomTrackOwnerAttackGoal(Blossom mob) {
            super(mob, false);
            this.mob = mob;
            this.setControls(EnumSet.of(Control.TARGET));
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
        private final Blossom tameable;
        private LivingEntity attacking;
        private int lastAttackTime;

        public CustomAttackWithOwnerGoal(Blossom tameable) {
            super(tameable, false);
            this.tameable = tameable;
            this.setControls(EnumSet.of(Control.TARGET));
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
                    if (this.attacking instanceof TameableEntity target) {
                        if (target.getOwner() != null) {
                            if (target.getOwner() == this.tameable.getOwner()) {
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

    public class CustomSitGoal extends Goal {
        private final Blossom tameable;

        public CustomSitGoal(Blossom tameable) {
            this.tameable = tameable;
            this.setControls(EnumSet.of(Control.JUMP, Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean shouldContinue() {
            return this.tameable.isSitting();
        }

        @Override
        public boolean canStart() {
            if (!this.tameable.isTamed()) {
                return false;
            } else {
                LivingEntity livingEntity = this.tameable.getOwner();
                if (livingEntity == null) {
                    return true;
                } else {
                    return this.tameable.squaredDistanceTo(livingEntity) < (double) 144.0F && livingEntity.getAttacker() != null ? false : this.tameable.isSitting();
                }
            }
        }

        @Override
        public void start() {
            this.tameable.getNavigation().stop();
            this.tameable.setInSittingPose(true);
        }

        @Override
        public void stop() {
            this.tameable.setInSittingPose(false);
        }
    }

    public class FollowTargetGoal extends Goal {
        // Add these new constants for vertical control
        private static final double VERTICAL_DEADZONE = 0; // Blocks before reacting
        private static final double MAX_VERTICAL_SPEED = 0.25;
        private static final double VERTICAL_ACCELERATION = 0.04;
        private static final double HOVER_HEIGHT_OFFSET = 0; // Preferred height above target

        // Existing fields
        private final Blossom mob;
        private final double speed;
        private final float minDistance;
        private final float maxDistance;
        private int updateCooldown;

        public FollowTargetGoal(Blossom mob, double speed, float minDistance, float maxDistance) {
            this.mob = mob;
            this.speed = speed;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return mob.getTarget() != null && mob.getTarget().isAlive();
        }

        @Override
        public boolean shouldContinue() {
            return canStart() && !mob.getNavigation().isIdle() && mob.distanceTo(mob.getTarget()) > 10;
        }

        @Override
        public void start() {
            updateCooldown = 0;
            mob.getNavigation().setSpeed(speed);
        }

        @Override
        public void tick() {
            LivingEntity target = mob.getTarget();
            if (target == null) return;

            mob.getLookControl().lookAt(target, 30.0F, 30.0F);

            // Calculate target position with vertical buffer
            Vec3d targetPos = calculateFollowPosition(target);

            // Update path with vertical damping
            if (updateCooldown-- <= 0) {
                updateCooldown = 10;
                mob.getNavigation().startMovingTo(
                        targetPos.x,
                        mob.getY(),
                        targetPos.z,
                        speed
                );
            }

            applySmoothVerticalMovement(target);
        }

        private Vec3d calculateFollowPosition(LivingEntity target) {
            Vec3d targetVec = target.getPos().subtract(mob.getPos()).normalize();
            double horizontalDistance = mob.getRandom().nextFloat() * (maxDistance - minDistance) + minDistance;

            return target.getPos().add(
                    targetVec.multiply(-horizontalDistance).multiply(1, 0, 1)
            ).add(0, HOVER_HEIGHT_OFFSET, 0); // Add hover offset
        }

        private void applySmoothVerticalMovement(LivingEntity target) {
            double yDiff = target.getY() + HOVER_HEIGHT_OFFSET - mob.getY();
            double verticalVelocity = mob.getVelocity().y;

            // Apply acceleration only when outside deadzone
            if (Math.abs(yDiff) > VERTICAL_DEADZONE) {
                double desiredSpeed = Math.copySign(
                        Math.min(Math.abs(yDiff) * VERTICAL_ACCELERATION, MAX_VERTICAL_SPEED),
                        yDiff
                );

                // Smooth velocity transition
                double velocityChange = (desiredSpeed - verticalVelocity) * 0.1;
                mob.setVelocity(mob.getVelocity().add(0, velocityChange, 0));
            }

            // Add natural floating motion
            mob.setVelocity(mob.getVelocity().add(
                    0,
                    (mob.getRandom().nextFloat() - 0.5) * 0.02,
                    0
            ));
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
        }
    }

}