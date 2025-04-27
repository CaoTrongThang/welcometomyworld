package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.classes.tameablePacket.StrongTameableEntityDefault;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//PORTALER: This mob is a portal that can move and can switch portal randomly, players can go to the portal to go to the end or the nether
public class Wanderer extends StrongTameableEntityDefault {

    ConcurrentHashMap<AnimationName, AnimationState> animationHashMap = new ConcurrentHashMap<>();

    private static final int WALK_CYCLE_DURATION_MS = 2670;
    private static final int[] FOOTSTEP_TIMINGS_MS = {1250, 2250};

    private int previousWalkPosition = -1;

    private static final int SWORD_SLASH_DURATION_MS = 2130;
    private static final int[] SWORD_SLASH_SOUND_TIMINGS_MS = {0, 700};
    private final Set<Integer> swordSlashPlayedFrames = new HashSet<>();

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    public final AnimationState blockAnimationState = new AnimationState();

    //SHOOT BOW
    public final AnimationState bowSkillAnimationState = new AnimationState();

    //SHOOT BOW
    public final AnimationState bowSkill2AnimationState = new AnimationState();

    // SWORD ATTACK
    public final AnimationState swordSlashAnimationState = new AnimationState();

    // PART
    public final AnimationState attack3AnimationState = new AnimationState();
    public final AnimationState backflipAnimationState = new AnimationState();

    public final AnimationState tameableAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    public final AnimationState healAnimationState = new AnimationState();

    private int useSkillCooldownCounter = 0;

    private int maxScale = 8;

    private int patrolRadius = 7;
    public BlockPos patrolCenterPos = null;

    public int animationTimeout = 0;
    public static final int DEFAULT_ANIMATION_TIMEOUT = 15;

    private double healthDecreaseWhenTameablePercent = 0.02f;
    private float percentHealthToBeTamed = 0.15f;

    private int flipCooldown = 100;
    private int flipCooldownCounter = 0;

    private Vec3d lookAtPos = null;
    private boolean shootingArrow = false;

    private int drinkHealthCooldown = 24000;
    private int drinkHealthCounter = 24000;
    private boolean isDrinkingHeal = false;


    public Wanderer(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);

        animationHashMap.put(AnimationName.IDLE, idleAnimationState);
        animationHashMap.put(AnimationName.WALK, walkAnimationState);
        animationHashMap.put(AnimationName.ATTACK, bowSkillAnimationState);
        animationHashMap.put(AnimationName.ATTACK4, bowSkill2AnimationState);
        animationHashMap.put(AnimationName.ATTACK2, swordSlashAnimationState);
        animationHashMap.put(AnimationName.ATTACK3, attack3AnimationState);
        animationHashMap.put(AnimationName.HEAL, healAnimationState);

        animationHashMap.put(AnimationName.TAMEABLE, tameableAnimationState);
        animationHashMap.put(AnimationName.SIT, sitAnimationState);
        animationHashMap.put(AnimationName.MOVEMENT, backflipAnimationState);
        animationHashMap.put(AnimationName.BLOCK, blockAnimationState);


        if (!this.getIsRandomFirstTime() && !this.getWorld().isClient) {
            double scale = WelcomeToMyWorld.random.nextDouble(1, maxScale);

            this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH) * scale);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (double) (scale / 2));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR) * (double) (scale / 3F));
            this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(this.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS) * (double) (scale / 2.5F));

            if (scale / 3 > 1) {
                this.setAllSkillCooldown((float) (this.getAllSkillCooldown() / (scale / 3)));
            }

            if (this.getAllSkillCooldown() <= 30) {
                this.setAllSkillCooldown(30);
            }

            this.setHealth(this.getMaxHealth());

            this.setIsRandomFirstTime(true);
        }
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.14f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 25f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40f)
                .add(EntityAttributes.GENERIC_ARMOR, 5)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10f)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1f);
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
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new StopMoveAndLookWhenCanBeTamed(this));
        this.goalSelector.add(2, new SwimGoal(this));
        this.goalSelector.add(3, new SitGoal(this));
        this.goalSelector.add(4, new PatrollingGoal(this));
        this.goalSelector.add(5, new CustomFollowOwnerGoal(this, 0.8, 15, 25, false));
        this.goalSelector.add(6, new StopWhenUsingSkill(this));
        this.goalSelector.add(8, new LargeEntityWanderGoal(this, 1.0, 1));
        this.goalSelector.add(9, new LookAtEntityGoal(this, LivingEntity.class, 15.0F)); // Increased range to 40
        this.goalSelector.add(11, new LookAroundGoal(this));

        this.targetSelector.add(1, new CustomTrackOwnerAttackGoal(this));
        this.targetSelector.add(2, new CustomAttackWithOwnerGoal(this));
        this.targetSelector.add(3, new CustomRevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public void setAnimationStates() {

        if (this.getWorld().isClient) {
            if (!this.getCanBeTamed()) {
                if (this.isInSittingPose()) {

                    if (!sitAnimationState.isRunning()) {
                        startAnimation(AnimationName.SIT);
                    }
                    return;
                }

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
            } else if (this.getCanBeTamed() && this.getOwner() == null) {
                if (!this.tameableAnimationState.isRunning()) {
                    this.startAnimation(AnimationName.TAMEABLE);
                }
            }


        } else {
            if (!this.getCanBeTamedSet()) {
                if (this.getHealth() <= this.getMaxHealth() * percentHealthToBeTamed && (this.getOwner() == null || !this.isTamed())) {
                    if (!this.getCanBeTamed()) {
                        if (WelcomeToMyWorld.random.nextInt(0, 100) < this.getTameChance()) {
                            this.setCanBeTamed(true);
                        } else {
                            this.setCanBeTamed(false);
                        }

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

    @Override
    public void tick() {
        super.tick();
        setAnimationStates();
        usingSkillsHandler();

        if (!this.getWorld().isClient) {
            if (this.getTarget() != null) {
                if(!this.getCanBeTamed()){
                    if(!this.shootingArrow){
                        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.getTarget().getPos());
                    }

                    if (this.shootingArrow && this.lookAtPos != null) {
                        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.lookAtPos);
                    }
                }

                if (this.getTarget().isDead() || this.getTarget().getHealth() <= 0) {
                    this.setTarget(null);
                }
            }

            if(this.getTarget() == null && this.getIsUsingSkill()){
                this.setIsUsingSkill(false);
            }
        }

        handleAnimationSoundsAndEffect();
    }

    private void usingSkillsHandler() {
        if (!this.getWorld().isClient && !this.getCanBeTamed() && !this.isInSittingPose()) {
            if (this.flipCooldownCounter < this.flipCooldown) {
                this.flipCooldownCounter++;
            }

            if (this.drinkHealthCounter < this.drinkHealthCooldown) {
                this.drinkHealthCounter++;
            }


            if (this.useSkillCooldownCounter < this.getAllSkillCooldown()) {
                this.useSkillCooldownCounter++;
                return;
            }

            if (this.getHealth() <= this.getMaxHealth() * 0.4f) {
                int timeout = 0;
                if (this.drinkHealthCounter >= this.drinkHealthCooldown) {
                    if (!this.getCanBeTamed()) {
                        this.useSkillCooldownCounter = 0;
                        this.isDrinkingHeal = true;
                        this.drinkHealthCounter = 0;

                        timeout = 55;

                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.HEAL, timeout);

                        Utils.addRunAfter(() -> {
                            Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_POTION_DRINKING, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.4f));
                            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                            this.setHealth(this.getHealth() + this.getMaxHealth() * 0.33f);
                        }, 19);

                        Utils.addRunAfter(() -> {
                            Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_POTION_DRINKING, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.4f));
                            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                            this.setHealth(this.getHealth() + this.getMaxHealth() * 0.33f);
                        }, 30);

                        Utils.addRunAfter(() -> {
                            Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_POTION_DRINKING, 0.8f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.4f));
                            this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                            this.setHealth(this.getHealth() + this.getMaxHealth() * 0.33f);
                        }, 40);

                        Utils.addRunAfter(() -> {
                            this.isDrinkingHeal = false;
                        }, timeout);

                        resetSkill(timeout);
                        return;
                    }
                }
            }

            if (this.getTarget() != null) {
                double distance = this.distanceTo(this.getTarget());
                int timeout = 0;

                if (this.isDrinkingHeal) return;

                if(distance > 35){
                    this.useSkillCooldownCounter = 0;
                    timeout = 40;
                    Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK2, timeout);
                    Utils.addRunAfter(() -> {
                        if(this.getTarget() == null) return;

                        // Calculate the direction vector from the mob to the target
                        Vec3d mobToTargetDirection = new Vec3d(
                                this.getTarget().getX() - this.getX(),
                                this.getTarget().getY() - this.getY(),
                                this.getTarget().getZ() - this.getZ()
                        ).normalize();

                        // Scale the direction vector to determine the offset
                        Vec3d offset = mobToTargetDirection.multiply(2, 1, 2); // Scale X, Y, and Z appropriately

                        // Calculate the position in front of the this.getTarget(
                        Vec3d blockFrontOfTarget = this.getTarget().getPos().add(offset);

                        // Teleport the mob to the calculated position
                        this.setPos(blockFrontOfTarget.x, blockFrontOfTarget.y, blockFrontOfTarget.z);

                        Utils.playSound(this.getWorld(), this.getTarget().getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT);

                    }, 6);
                    Utils.addRunAfter(this::createSwordSlashShockwavePolar, 17);
                }
                if (distance > 12 && distance <= 35) {

                    int rand = WelcomeToMyWorld.random.nextInt(0, 100);
                    timeout = 20;

                    if(rand <= 7){
                        this.useSkillCooldownCounter = 0;
                        {
                            Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK4, timeout);
                            rainOfArrowsSkill(this.getTarget().getPos());
                        }
                    } else {
                        this.useSkillCooldownCounter = (int) (this.getAllSkillCooldown() - 30f);
                        Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_BOW_ATTACK, 0.4f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.1f));
                        this.shootingArrow = true;
                        this.lookAtPos = this.getTarget().getPos();

                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK, timeout);
                        Utils.addRunAfter(() -> {
                            this.shootingArrow = false;
                        }, 22);

                        Utils.addRunAfter(() -> {
                            shootArrow();
                        }, 18);
                    }
                    resetSkill(timeout);
                }

                if (distance <= 12 && distance >= 8 || (this.getTarget().getY() > this.getY() && (Math.abs(this.getTarget().getY() - this.getY()) > 4))) {
                    int rand = WelcomeToMyWorld.random.nextInt(0, 100);
                    if (rand < 10) {

                    } else {
                        int swordSlashTimeout = (int) Math.ceil(SWORD_SLASH_DURATION_MS / 51.0); // Calculate once
                        timeout = swordSlashTimeout;
                        Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK2, timeout);
                        Utils.addRunAfter(this::createSwordSlashShockwavePolar, 17);
                    }

                    this.useSkillCooldownCounter = 0;
                    resetSkill(timeout);
                }

                if (distance <= 8) {
                    this.useSkillCooldownCounter = 0;
                    int rand1 = WelcomeToMyWorld.random.nextInt(0, 100);
                    timeout = 25;

                    boolean canFrontFlip = false;
                    boolean canBackFlip = false;

                    int flipStrength = 5;
                    if (this.flipCooldownCounter >= this.flipCooldown) {
                        this.flipCooldownCounter = 0;
                        if (rand1 <= 60) {
                            Vec3d backFlipDirection = new Vec3d(
                                    this.getX() - this.getTarget().getX(),
                                    2,
                                    this.getZ() - this.getTarget().getZ()
                            ).normalize();
                            canBackFlip = isFlipAreaClear(backFlipDirection);
                            if (canBackFlip) {
                                Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.MOVEMENT, timeout);
                                this.addVelocity(backFlipDirection.x * flipStrength, 0.8f, backFlipDirection.z * flipStrength);

                            }
                        } else {
                            Vec3d frontFlipDirection = new Vec3d(
                                    this.getTarget().getX() - this.getX(),
                                    2, // Ignore Y component
                                    this.getTarget().getZ() - this.getZ()
                            ).normalize();
                            canFrontFlip = isFlipAreaClear(frontFlipDirection);
                            if (canFrontFlip) {
                                Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.MOVEMENT, timeout);
                                this.addVelocity(frontFlipDirection.x * flipStrength, 0.8f, frontFlipDirection.z * flipStrength);
                            }
                        }
                    }

                    if (!canBackFlip && !canFrontFlip) {
                        if (rand1 <= 50) {
                            timeout = 40;
                            Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK2, timeout);
                            Utils.addRunAfter(this::createSwordSlashShockwavePolar, 17);
                        } else if(rand1 > 50 && rand1 < 90){
                            this.useSkillCooldownCounter = (int) (this.getAllSkillCooldown() - 30f);

                            timeout = 20;
                            Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_BOW_ATTACK, 0.4f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.1f));
                            this.shootingArrow = true;
                            this.lookAtPos = this.getTarget().getPos();

                            Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK, timeout);
                            Utils.addRunAfter(() -> {
                                this.shootingArrow = false;
                            }, 22);

                            Utils.addRunAfter(() -> {
                                shootArrow();
                            }, 18);
                        }
                        else if(rand1 >= 90) {
                            timeout = 20;
                            Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.ATTACK4, timeout);
                            rainOfArrowsSkill(this.getTarget().getPos());
                        }
                    } else {
                        Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.WANDERER_BACKFLIP, 0.4f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.1f));
                    }
                    resetSkill(timeout);
                }
            }
        }
    }

    private boolean isFlipAreaClear(Vec3d direction) {
        Box mobBB = this.getBoundingBox(); // Get the mob's bounding box
        // Offset the bounding box in the given direction by 4 blocks
        Box checkBox = mobBB.offset(direction.multiply(2));

        int minX = MathHelper.floor(checkBox.minX);
        int minY = MathHelper.floor(checkBox.minY);
        int minZ = MathHelper.floor(checkBox.minZ);
        int maxX = MathHelper.ceil(checkBox.maxX);
        int maxY = MathHelper.ceil(checkBox.maxY);
        int maxZ = MathHelper.ceil(checkBox.maxZ);

        for (int x = minX; x < maxX; x++) { // Loop through X axis
            for (int y = minY; y < maxY; y++) { // Loop through Y axis
                for (int z = minZ; z < maxZ; z++) { // Loop through Z axis
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = this.getWorld().getBlockState(pos);
                    VoxelShape collisionShape = state.getCollisionShape(this.getWorld(), pos);
                    if (!collisionShape.isEmpty()) { // Check if the block has a collision shape
                        return false; // Obstacle found
                    }
                }
            }
        }
        return true; // No obstacles found
    }


    private void resetSkill(int timeout) {
        this.setIsUsingSkill(true);
        Utils.addRunAfter(() -> {
            this.setIsUsingSkill(false);
        }, timeout);
    }

    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        Vec3d vec3d = anchorPoint.positionAt(this);
        double d = target.x - vec3d.x;
        double e = target.y - vec3d.y;
        double f = target.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        this.setPitch(MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * (double) (180F / (float) Math.PI)))));
        this.setYaw(MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * (double) (180F / (float) Math.PI)) - 90.0F));
        this.setHeadYaw(this.getYaw());
        this.prevPitch = this.getPitch();
        this.prevYaw = this.getYaw();
    }

    private void shootArrow() {
        LivingEntity target = this.getTarget(); // Get the mob's current target
        if (target != null && this.lookAtPos != null) {

            // Create the custom arrow entity
            WandererArrow arrow = new WandererArrow(this.getWorld(), this);

            // Set the initial position of the arrow
            arrow.setPosition(this.getX(), this.getEyeY() - 0.1, this.getZ());

            // Calculate the direction vector from the mob to the target
            double deltaX = this.lookAtPos.getX() - this.getX();
            double deltaZ = this.lookAtPos.getZ() - this.getZ();

            double minVelocity = 3;
            double maxVelocity = 7;

            double velocity = Math.min(minVelocity + (this.distanceTo(this.getTarget()) / 24), maxVelocity);

            double deltaY = target.getBodyY(0.3 / velocity) - this.getBodyY(0.5);

            Vec3d direction = new Vec3d(deltaX, deltaY, deltaZ).normalize();
            arrow.setVelocity(direction.x * velocity, direction.y * velocity, direction.z * velocity);
            arrow.setOwner(this);

            // Spawn the arrow in the world (ensure this runs only on the server side)
            if (!this.getWorld().isClient) { // Server-side check
                this.getWorld().spawnEntity(arrow); // Spawn the arrow
            }

            // Spawn a particle effect for debugging (optional)
            this.getWorld().addParticle(ParticleTypes.END_ROD, this.getX(), this.getEyeY() - 0.1, this.getZ(), 0, 0, 0);
        }
    }

    private void rainOfArrowsSkill(Vec3d pos) {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        // Define the skill parameters
        double radius = 10; // Radius of the circular effect
        int durationTicks = 150; // Duration of the skill in ticks (5 seconds = 100 ticks)
        int intervalTicks = 2; // Interval between arrow spawns (0.2 seconds = 4 ticks)

        // Schedule periodic arrow spawns
        for (int tick = 0; tick < durationTicks; tick += intervalTicks) {
            Utils.addRunAfter(() -> {
                // Generate a random position within the circle
                double angle = this.random.nextDouble() * Math.PI * 2; // Random angle in radians
                double randomRadius = this.random.nextDouble() * radius; // Random distance from the center
                double offsetX = randomRadius * Math.cos(angle); // X offset
                double offsetZ = randomRadius * Math.sin(angle); // Z offset

                // Calculate the spawn position above the target
                Vec3d spawnPos = pos.add(offsetX, 40, offsetZ); // Spawn 20 blocks above the ground

                // Create the custom arrow entity
                WandererArrow arrow = new WandererArrow(this.getWorld(), this);
                arrow.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                arrow.canExplode = false;

                arrow.setVelocity(0, -7, 0); // Downward velocity

                // Set the owner of the arrow
                arrow.setOwner(this);

                // Spawn the arrow in the world (server-side check)
                if (!this.getWorld().isClient) {
                    this.getWorld().spawnEntity(arrow);
                }

                // Spawn particles for debugging (optional)
                this.getWorld().addParticle(ParticleTypes.END_ROD, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0, 0);

            }, tick); // Run after `tick` ticks
        }
    }

    private void handleAnimationSoundsAndEffect() {
        if (!this.getWorld().isClient()) return;

        if (walkAnimationState.isRunning()) {
            handleWalkSounds();
        }

        if (swordSlashAnimationState.isRunning()) {
            handleSwordSlashSounds();
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
                    Utils.playClientSound(this.getBlockPos(), SoundsManager.WANDERER_WALK, 20, 0.4f, 1f);
                }
            }
        }
        previousWalkPosition = currentPos;
    }

    private void handleSwordSlashSounds() {
        if (!this.getWorld().isClient()) return;

        long animTime = swordSlashAnimationState.getTimeRunning();
        int currentPos = (int) (animTime % SWORD_SLASH_DURATION_MS);

        for (int timing : SWORD_SLASH_SOUND_TIMINGS_MS) {
            int windowStart = timing - 50; // Widen window to -50ms
            int windowEnd = timing + 50;   // and +50ms

            boolean inWindow = currentPos >= windowStart && currentPos <= windowEnd;
            boolean cycleWrap = timing > SWORD_SLASH_DURATION_MS - 50 &&
                    currentPos < timing - SWORD_SLASH_DURATION_MS + 50;

            if ((inWindow || cycleWrap) && !swordSlashPlayedFrames.contains(timing)) {
                triggerSwordSlashSoundSound(timing);
                swordSlashPlayedFrames.add(timing);
            }
        }

        // Clear after animation ends
        if (currentPos > SWORD_SLASH_DURATION_MS - 100) {
            swordSlashPlayedFrames.clear();
        }
    }

    private void triggerSwordSlashSoundSound(int timing) {
        SoundEvent soundId = null;

        if (timing >= 0 && timing <= 100) {
            soundId = SoundsManager.WANDERER_SWORD_CHARGE;
        } else {
            soundId = SoundsManager.WANDERER_SWORD_SLASH;
        }

        if (soundId == null) return;

        Utils.playClientSound(this.getBlockPos(), soundId, 16, 0.6f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.2f));
    }

    private void createSwordSlashShockwavePolar() {
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            BlockPos center = this.getBlockPos();

            // Get the mob's facing angle (in radians) from its XZ look vector.
            Vec3d lookVec = this.getRotationVector();
            double facingAngle = Math.atan2(lookVec.z, lookVec.x);

            // Configure the arc:
            double arcAngle = Math.toRadians(90);  // 90° arc; adjust as needed.
            double halfArc = arcAngle / 2.0;
            int slashLength = 25;                // Maximum distance of the slash.

            // Set resolution: how finely we sample the arc.
            double radiusStep = 1.0;             // Start at 1 block out.
            double angleStep = Math.toRadians(5);  // 5° increments.

            // Use a set to prevent processing duplicate block positions.
            Set<BlockPos> processedPositions = new HashSet<>();

            // Pre-compute nearby entities for damage.
            Box checkArea = new Box(center).expand(slashLength);
            List<LivingEntity> damageTargets = this.getWorld().getEntitiesByClass(LivingEntity.class, checkArea, entity -> true);

            // Iterate using polar coordinates, starting at radiusStep.
            for (double r = radiusStep; r <= slashLength; r += radiusStep) {
                for (double offset = -halfArc; offset <= halfArc; offset += angleStep) {
                    double angle = facingAngle + offset;
                    int xOffset = (int) Math.round(r * Math.cos(angle));
                    int zOffset = (int) Math.round(r * Math.sin(angle));
                    BlockPos targetPos = center.add(xOffset, 0, zOffset);

                    // Skip if we've already processed this block position.
                    if (processedPositions.contains(targetPos)) {
                        continue;
                    }
                    processedPositions.add(targetPos);

                    // Process for two vertical layers (y = 0 and y = -1)
                    for (int yOffset = 0; yOffset >= -1; yOffset--) {
                        BlockPos posWithY = targetPos.up(yOffset);
                        BlockState state = serverWorld.getBlockState(posWithY);
                        if (!state.isAir()) {
                            spawnBlockParticles(serverWorld, posWithY, state);
                        }
                    }
                }
            }

            // Damage handling (similar to your existing logic)
            for (LivingEntity target : damageTargets) {
                if (target == this) continue;
                double dx = target.getX() - center.getX();
                double dz = target.getZ() - center.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > slashLength) continue;
                double targetAngle = Math.atan2(dz, dx);
                double angleDiff = Math.abs(MathHelper.wrapDegrees(Math.toDegrees(targetAngle - facingAngle)));
                if (angleDiff > Math.toDegrees(halfArc)) continue;
                // Additional checks for friendly fire etc.
                if (this.getOwner() != null && target == this.getOwner()) continue;
                if (target instanceof TameableEntity tameable) {
                    if (tameable.isTamed() && tameable.getOwner() != null &&
                            tameable.getOwner() == this.getOwner()) continue;
                }

                if(target instanceof PlayerEntity){
                    if(this.isTamed() || this.getOwner() != null){
                        continue;
                    }
                }

                if (target instanceof Wanderer knight) {
                    if (!this.isTamed() && !knight.isTamed()) continue;
                    if (this.isTamed() && this.getOwner() != null &&
                            knight.isTamed() && knight.getOwner() != null &&
                            this.getOwner().equals(knight.getOwner())) {
                        continue;
                    }
                }
                float damage = (float) this.getAttributes().getBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                damageBlockingShield(target, damage);

                if (this.getTarget() != null) {
                    Vec3d pushBackDirection = new Vec3d(
                            this.getTarget().getX() - this.getX(),
                            0, // Ignore Y component
                            this.getTarget().getZ() - this.getZ()
                    );

                    if(target instanceof PlayerEntity){
                        target.addVelocity(pushBackDirection.x * 1.5f, 0.1f, pushBackDirection.z * 1.5f);
                    } else {
                        target.addVelocity(pushBackDirection.x * 0.2f, 0.1f, pushBackDirection.z * 0.2f);
                    }
                    target.disablesShield();
                    target.damage(this.getWorld().getDamageSources().mobAttack(this), damage);
                }
            }
        }
    }

    private void damageBlockingShield(LivingEntity target, float damage) {

        if (this.getTarget() == null) return;
        if (target.isBlocking() && target.getActiveItem().isDamageable()) {

            float reducedDamage = damage;

            target.getActiveItem().damage((int) reducedDamage, target,
                    entity -> entity.sendToolBreakStatus(target.getActiveHand()));

        }
    }

    private void spawnBlockParticles(ServerWorld world, BlockPos pos, BlockState state) {
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

                this.setHealth(this.getMaxHealth() / 2);

                Utils.grantAdvancement((ServerPlayerEntity) player, "tameable/a_long_way");

                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
            } else {
                this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private Item getTameFood() {
        return Items.GOLDEN_APPLE;
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
                animationHashMap.get(n).stop();
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
                animationHashMap.get(n).stop();
            } else {
                animationHashMap.get(n).stop();
            }
        }
        if (na != null) {
            // Clear the played frames when starting the sword slash animation
            if (na == AnimationName.ATTACK2) {
                swordSlashPlayedFrames.clear();
            }
            animationHashMap.get(na).start(this.age);
            animationTimeout = timeout;
        }
    }

    // Store the goal instance
    private ActiveTargetGoal<HostileEntity> hostileTargetGoal;

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isDead()) return false;

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

            if (!this.getCanBeTamed() && source.getAttacker() != this.getOwner()) {
                if (source.getAttacker() instanceof PlayerEntity player) {
                    if (player.isCreative() || player.isSpectator()) return super.damage(source, amount);
                }
            }

            int rand = WelcomeToMyWorld.random.nextInt(0, 100);
            if (rand <= 7 && (!this.getCanBeTamed() || this.isTamed() || this.getOwner() != null)) {
                Utils.sendAnimationPacket(this.getWorld(), this, AnimationName.BLOCK, 5);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundsManager.WANDERER_BLOCK, this.getSoundCategory(), 0.8F, 1.0F);
                return super.damage(source, 0);
            }
        }

        return super.damage(source, amount);
    }


    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if(!this.getWorld().isClient){
            if(!this.isTamed() || this.getOwner() == null){
                EnderPest enderPest = new EnderPest(EntitiesManager.ENDER_PEST, this.getWorld());
                enderPest.setPos(this.getX(), this.getY() + 1, this.getZ());
                this.getWorld().spawnEntity(enderPest);
                enderPest.openEnderPest(WelcomeToMyWorld.random.nextInt(3, 6));
            }
        }
    }

    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("canBeTamed", this.getCanBeTamed());
        nbt.putBoolean("canBeTamedSet", this.getCanBeTamedSet());
        nbt.putFloat("allSkillCooldown", this.getAllSkillCooldown());
        nbt.putBoolean("isRandomFirstTime", this.getIsRandomFirstTime());
        nbt.putInt("drinkHealthCounter", this.drinkHealthCounter);

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


        int counter = nbt.getInt("drinkHealthCounter");

        if(counter > 0){
            this.drinkHealthCounter = nbt.getInt("drinkHealthCounter");
        }


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
        private final Wanderer mob;

        public StopWhenUsingSkill(Wanderer mob) {
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

    public class StopMoveAndLookWhenCanBeTamed extends Goal {
        private final Wanderer mob;

        public StopMoveAndLookWhenCanBeTamed(Wanderer mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
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

    public class PatrollingGoal extends Goal {
        private final Wanderer mob;
        private final double maxTeleportDistance = 20.0; // Teleport if beyond this
        private int cooldown = 0;
        // Add a max chase distance field
        private final double maxChaseDistance = 14.0; // 2 * patrolRadius (7 * 2)

        public PatrollingGoal(Wanderer mob) {
            this.mob = mob;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
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

    class CustomTrackOwnerAttackGoal extends TrackTargetGoal {
        private final Wanderer mob;
        private LivingEntity attacker;
        private int lastAttackedTime;

        public CustomTrackOwnerAttackGoal(Wanderer mob) {
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
        private final Wanderer tameable;
        private LivingEntity attacking;
        private int lastAttackTime;

        public CustomAttackWithOwnerGoal(Wanderer tameable) {
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
}