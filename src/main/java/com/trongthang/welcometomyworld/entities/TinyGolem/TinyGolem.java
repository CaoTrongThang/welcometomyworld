package com.trongthang.welcometomyworld.entities.TinyGolem;

import com.trongthang.welcometomyworld.classes.CustomTameableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.EntityData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TinyGolem extends CustomTameableEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final TrackedData<Integer> STATE = DataTracker.registerData(TinyGolem.class,
            TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STATE_TICK = DataTracker.registerData(TinyGolem.class,
            TrackedDataHandlerRegistry.INTEGER);

    // States
    public static final int STATE_IDLE = 0;
    public static final int STATE_SIT = 1;
    public static final int STATE_SIT_IDLE = 2; // already sitting
    public static final int STATE_ATTACK = 3;
    public static final int STATE_PREPARE_SPIN = 4;
    public static final int STATE_SPINNING = 5;
    public static final int STATE_STOP_SPIN = 6;
    public static final int STATE_EMERGE = 7;

    private int spinCount = 0;

    private int stateTickLocal = 0;
    private boolean hitFired = false;
    private boolean hasEmerged = false;
    private int globalCooldown = 0;

    public TinyGolem(EntityType<? extends CustomTameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.16D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5f)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32f);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(STATE, STATE_IDLE);
        this.dataTracker.startTracking(STATE_TICK, 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        // Add a goal to stop typical movement while performing skills
        // Only chase and attack when not on cooldown
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canStart() {
                return TinyGolem.this.globalCooldown <= 0 && getState() == STATE_IDLE && super.canStart();
            }

            @Override
            public boolean shouldContinue() {
                return TinyGolem.this.globalCooldown <= 0 && getState() == STATE_IDLE && super.shouldContinue();
            }

            @Override
            protected void attack(LivingEntity target, double squaredDistance) {
                double d = this.getSquaredMaxAttackDistance(target);
                if (squaredDistance <= d && this.getCooldown() <= 0) {
                    this.resetCooldown();
                    if (TinyGolem.this.random.nextFloat() < 0.2f) {
                        TinyGolem.this.setState(STATE_PREPARE_SPIN);
                    } else {
                        TinyGolem.this.setState(STATE_ATTACK);
                    }
                }
            }
        });
        // While on cooldown, back away from the target to ~2 blocks
        this.goalSelector.add(3, new Goal() {
            private static final double SAFE_DIST = 2.0;

            @Override
            public boolean canStart() {
                if (TinyGolem.this.globalCooldown <= 0 || TinyGolem.this.getState() != STATE_IDLE)
                    return false;
                LivingEntity target = TinyGolem.this.getTarget();
                return target != null && TinyGolem.this.squaredDistanceTo(target) < SAFE_DIST * SAFE_DIST;
            }

            @Override
            public boolean shouldContinue() {
                if (TinyGolem.this.globalCooldown <= 0 || TinyGolem.this.getState() != STATE_IDLE)
                    return false;
                LivingEntity target = TinyGolem.this.getTarget();
                return target != null && TinyGolem.this.squaredDistanceTo(target) < SAFE_DIST * SAFE_DIST;
            }

            @Override
            public void tick() {
                LivingEntity target = TinyGolem.this.getTarget();
                if (target == null)
                    return;
                Vec3d away = TinyGolem.this.getPos().subtract(target.getPos()).normalize();
                Vec3d flee = TinyGolem.this.getPos().add(away.multiply(SAFE_DIST + 0.5));
                TinyGolem.this.getNavigation().startMovingTo(flee.x, flee.y, flee.z, 1.2);
            }
        });
        this.goalSelector.add(4, new CustomFollowOwnerGoal(this, 1.0D, 5.0F, 10.0F, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F) {
            @Override
            public boolean canStart() {
                return !TinyGolem.this.isSitting() && super.canStart();
            }
        });
        this.goalSelector.add(6, new LookAroundGoal(this) {
            @Override
            public boolean canStart() {
                return !TinyGolem.this.isSitting() && super.canStart();
            }
        });

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
        this.targetSelector.add(3, new CustomRevengeGoal(this));
        this.targetSelector.add(4,
                new ActiveTargetGoal<>(this, HostileEntity.class, 5, true, false, (entity) -> !this.isSitting()));
    }

    public void onSummon() {
        this.setState(STATE_EMERGE);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (!this.hasEmerged) {
            this.hasEmerged = true;
            this.setState(STATE_EMERGE);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (!this.isTamed() && itemStack.isOf(Items.COPPER_INGOT)) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }

            if (!this.getWorld().isClient) {
                if (this.random.nextInt(3) == 0) { // 33% chance
                    this.setTamed(true);
                    this.setOwner(player);
                    this.getWorld().sendEntityStatus(this, (byte) 7); // Heart particles
                } else {
                    this.getWorld().sendEntityStatus(this, (byte) 6); // Smoke particles (fail)
                }
            }
            return ActionResult.success(this.getWorld().isClient);
        }

        if (this.isTamed() && player.getUuid().equals(this.getOwnerUuid())) {
            // Check for sneak + interact to equip items
            if (player.isSneaking()) {
                if (!itemStack.isEmpty()) {
                    // Equip to mainhand
                    ItemStack currentHand = this.getEquippedStack(EquipmentSlot.MAINHAND);

                    ItemStack stackToEquip = player.getAbilities().creativeMode ? itemStack.copy() : itemStack.split(1);
                    stackToEquip.setCount(1);
                    this.equipStack(EquipmentSlot.MAINHAND, stackToEquip);

                    if (!currentHand.isEmpty()) {
                        this.dropStack(currentHand);
                    }
                    this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }

                // If hand is empty, unequip mainhand
                if (itemStack.isEmpty()) {
                    if (!this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                        this.dropStack(this.getEquippedStack(EquipmentSlot.MAINHAND));
                        this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.0F, 1.0F);
                        return ActionResult.SUCCESS;
                    }
                }
            } else {
                // If not sneaking, toggle sitting if it's empty hand or not interacting with
                // something else
                if (itemStack.isEmpty() || itemStack.isOf(healingFood())) {
                    if (!itemStack.isOf(healingFood())) {
                        this.setSitting(!this.isSitting());
                        this.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1.0F, this.isSitting() ? 0.8F : 1.2F);
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget(null);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return super.interactMob(player, hand);

    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        if (this.isSitting()) {
            return EntityDimensions.changing(1.3f, 0.6f);
        }
        return super.getDimensions(pose);
    }

    @Override
    public void setSitting(boolean sitting) {
        super.setSitting(sitting);
        this.calculateDimensions();
    }

    public int getState() {
        return this.dataTracker.get(STATE);
    }

    public void setState(int state) {
        this.dataTracker.set(STATE, state);
        this.dataTracker.set(STATE_TICK, 0);
        this.stateTickLocal = 0;
        this.hitFired = false;

        if (state == STATE_EMERGE && !this.getWorld().isClient) {
            if (this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5, this.getZ(), 20, 0.5,
                        0.5, 0.5, 0.1);
            }
        }

        if (state == STATE_PREPARE_SPIN) {
            this.spinCount = 0;
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("State", this.getState());
        nbt.putBoolean("HasEmerged", this.hasEmerged);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("State")) {
            this.setState(nbt.getInt("State"));
        }
        if (nbt.contains("HasEmerged")) {
            this.hasEmerged = nbt.getBoolean("HasEmerged");
        }
    }

    @Override
    public Item healingFood() {
        return Items.COPPER_INGOT;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            // Client side footstep sound & sit smoke effects
            int state = getState();
            if (state == STATE_IDLE && (this.getVelocity().x != 0 || this.getVelocity().z != 0)) {
                int stTick = this.dataTracker.get(STATE_TICK);
                this.dataTracker.set(STATE_TICK, stTick + 1);
                // "walking": hit tick is at 10 and 22 if you want to do the footstep sound
                if (stTick % 22 == 10 || stTick % 22 == 0) { // rough approximation
                    this.getWorld().playSound(this.getX(), this.getY(), this.getZ(),
                            SoundEvents.BLOCK_STONE_STEP, this.getSoundCategory(), 0.5f, 1.5f, false);
                }
            }

            // Chimney smoke
            if (this.random.nextFloat() < 0.1f && !this.isSitting()) {
                double x = this.getX();
                double y = this.getY() + this.getHeight() + 0.3;
                double z = this.getZ();
                this.getWorld().addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.05, 0);
            }
            return;
        }

        if (globalCooldown > 0)
            globalCooldown--;

        int state = getState();

        // Single Source of Truth for sitting state to prevent any desync
        if (this.isSitting() && state == STATE_IDLE) {
            setState(STATE_SIT); // Use STATE_SIT to trigger transition effects
            state = STATE_SIT;
        } else if (!this.isSitting() && (state == STATE_SIT || state == STATE_SIT_IDLE)) {
            setState(STATE_IDLE); // Fallback: put into idle if it broke out of sitting
            state = STATE_IDLE;
        }

        if (state != STATE_IDLE && state != STATE_SIT_IDLE) {
            this.stateTickLocal++;
            this.dataTracker.set(STATE_TICK, this.stateTickLocal);
            // Stop pathing while doing skills/sit anim
            this.getNavigation().stop();
        }

        switch (state) {
            case STATE_EMERGE:
                if (stateTickLocal == 10) {
                    if (this.getWorld() instanceof ServerWorld sw) {
                        sw.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), 15,
                                0.4, 0.2, 0.4, 0.05);
                        sw.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_IRON_GOLEM_REPAIR,
                                this.getSoundCategory(), 1.0F, 1.2F);
                    }
                }
                if (stateTickLocal >= 20) {
                    setState(STATE_IDLE);
                }
                break;
            case STATE_SIT:
                if (stateTickLocal == 24) {
                    if (this.getWorld() instanceof ServerWorld sw) {
                        Vec3d look = this.getRotationVec(1.0F);
                        sw.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                this.getX() + look.x * 0.5, this.getY(), this.getZ() + look.z * 0.5,
                                10, 0.2, 0.1, 0.2, 0.05);
                        sw.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, this.getSoundCategory(), 1.0f, 1.5f);
                    }
                }
                if (stateTickLocal >= 30) {
                    setState(STATE_SIT_IDLE);
                }
                break;
            case STATE_ATTACK:
                if (stateTickLocal >= 8 && !hitFired) {
                    hitFired = true;
                    // do a 3 blocks front attack
                    doFrontAttack(3.0);
                }
                if (stateTickLocal >= 10) { // total length 10
                    setState(STATE_IDLE);
                    globalCooldown = 20;
                }
                break;
            case STATE_PREPARE_SPIN:
                if (stateTickLocal >= 20) {
                    setState(STATE_SPINNING);
                }
                break;
            case STATE_SPINNING:
                if (stateTickLocal % 5 == 2) {
                    doAoEAttack(3.0); // 5 blocks around AoE
                    this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
                }
                if (stateTickLocal >= 5) {
                    spinCount++;
                    if (spinCount >= 10) { // spin 10 times
                        setState(STATE_STOP_SPIN);
                    } else {
                        stateTickLocal = 0; // loop
                    }
                }
                break;
            case STATE_STOP_SPIN:
                if (stateTickLocal >= 20) {
                    setState(STATE_IDLE);
                    globalCooldown = 40;
                }
                break;
        }

    }

    private void doAoEAttack(double radius) {
        Box hitBox = this.getBoundingBox().expand(radius, 1.5, radius);

        List<LivingEntity> hitEntities = this.getWorld().getEntitiesByClass(LivingEntity.class, hitBox,
                e -> e != this && e.isAlive() && !e.isTeammate(this)
                        && (this.getOwner() == null || !e.equals(this.getOwner()))
                        && (!this.isTamed() || !(e instanceof PlayerEntity)));

        for (LivingEntity target : hitEntities) {
            float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            damage += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), target.getGroup());

            if (target.damage(this.getWorld().getDamageSources().mobAttack(this), damage)) {
                EnchantmentHelper.onTargetDamaged(this, target);
            }
        }
    }

    private void doFrontAttack(double range) {
        Vec3d look = this.getRotationVec(1.0F);
        Vec3d collisionCenter = this.getPos().add(look.multiply(range / 2.0));
        Box hitBox = new Box(collisionCenter, collisionCenter).expand(range / 2.0, 1.5, range / 2.0);

        List<LivingEntity> hitEntities = this.getWorld().getEntitiesByClass(LivingEntity.class, hitBox,
                e -> e != this && e.isAlive() && !e.isTeammate(this)
                        && (this.getOwner() == null || !e.equals(this.getOwner()))
                        && (!this.isTamed() || !(e instanceof PlayerEntity)));

        for (LivingEntity target : hitEntities) {
            float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            damage += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), target.getGroup());

            if (target.damage(this.getWorld().getDamageSources().mobAttack(this), damage)) {
                EnchantmentHelper.onTargetDamaged(this, target);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            int entityState = this.dataTracker.get(STATE);

            if (entityState == STATE_EMERGE) {
                state.getController().transitionLength(0);
                return state.setAndContinue(RawAnimation.begin().thenPlay("emerge"));
            } else if (entityState == STATE_SIT) {
                state.getController().transitionLength(0);
                return state.setAndContinue(RawAnimation.begin().thenPlay("sit"));
            } else if (entityState == STATE_SIT_IDLE) {
                state.getController().transitionLength(0);
                return state.setAndContinue(RawAnimation.begin().thenPlay("sit")); // wait at end
            } else if (entityState == STATE_ATTACK) {
                state.getController().transitionLength(2);
                return state.setAndContinue(RawAnimation.begin().thenPlay("attack"));
            } else if (entityState == STATE_PREPARE_SPIN) {
                state.getController().transitionLength(3);
                return state.setAndContinue(RawAnimation.begin().thenPlay("prepare_spin_attack"));
            } else if (entityState == STATE_SPINNING) {
                state.getController().transitionLength(0);
                return state.setAndContinue(RawAnimation.begin().thenPlay("spinning"));
            } else if (entityState == STATE_STOP_SPIN) {
                state.getController().transitionLength(3);
                return state.setAndContinue(RawAnimation.begin().thenPlay("stop_spinning"));
            }

            if (this.getVelocity().x != 0 || this.getVelocity().z != 0) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walking"));
            }

            state.getController().setAnimationSpeed(1.0);
            state.getController().transitionLength(3);
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

}
