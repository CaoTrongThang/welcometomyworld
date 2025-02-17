package com.trongthang.welcometomyworld.entities;

import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AncientWhale extends FlyingEntity
{

    public final AnimationState idleAnimationState = new AnimationState();

    private int idleAnimationTimout = 0;


    public AncientWhale(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.1f)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 2f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f);
    }

    private void setupAnimationStates(){
        if(this.idleAnimationTimout <= 0){
            this.idleAnimationTimout = 80;
            this.idleAnimationState.start(this.age);
        }else {
            this.idleAnimationTimout--;
        }
    }

    @Override
    public void tick(){
        super.tick();

        if(this.getWorld().isClient()){
            this.setupAnimationStates();
        }
    }

    class AncientWhaleMoveControl extends MoveControl {
        private float targetSpeed = 0.1F;

        public AncientWhaleMoveControl(AncientWhale whaleEntity, MobEntity owner) {
            super(owner);
        }

        @Override
        public void tick() {
            // Get the whale's current velocity
            Vec3d currentVelocity = AncientWhale.this.getVelocity();

            // Convert yaw (angle in degrees) to radians for movement calculations
            float radians = AncientWhale.this.getYaw() * ((float) Math.PI / 180F);

            // Input-based movement: move forward
            double moveForward = MathHelper.cos(radians) * targetSpeed;
            double moveStrafe = MathHelper.sin(radians) * targetSpeed;
            double moveUpward = 0.0; // For simplicity, no vertical movement control right now

            // Apply movement to the whale
            Vec3d newVelocity = new Vec3d(moveStrafe, moveUpward, moveForward);

            // Add the new movement to the current velocity
            AncientWhale.this.setVelocity(currentVelocity.add(newVelocity).multiply(0.5)); // Adjust this multiplier to change responsiveness

            // Optional: You can change the whale's speed based on specific conditions (like idle vs. moving).
            // For now, it's a constant speed, but you can modify `targetSpeed` based on other logic.
        }
    }
}
