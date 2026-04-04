package com.trongthang.welcometomyworld.mixin.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.GlobalConfig.canPhantomAI;

// TODO: Make Phantom spawn like neutral
@Mixin(PhantomEntity.class)
public abstract class PhantomAIMixin extends Entity {

    @Unique
    private int maxHeight = 300;

    @Unique
    private int cooldown = 120;
    @Unique
    private int counter = 0;
    @Unique
    private boolean canLiftPlayer = true;

    @Unique
    private boolean canGrab = true;

    public PhantomAIMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onSizeChanged", at = @At("HEAD"), cancellable = true)
    private void onSizeChanged(CallbackInfo ci) {
        this.calculateDimensions();
        ci.cancel();
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (!canPhantomAI)
            return;
        if (!canGrab)
            return;

        PhantomEntity phantom = (PhantomEntity) (Object) this;
        if (phantom.getPhantomSize() < 9) {
            canGrab = false;
            return;
        }

        if (!canLiftPlayer) {
            counter++;
            if (counter > cooldown) {
                canLiftPlayer = true;
                counter = 0;
            }
            return;
        }

        // If currently carrying a passenger
        if (phantom.hasPassengers()) {
            Entity passenger = phantom.getFirstPassenger();
            if (passenger instanceof PlayerEntity) {
                PlayerEntity currentLiftPlayer = (PlayerEntity) passenger;

                // Drop conditions
                if (currentLiftPlayer.isCreative() || currentLiftPlayer.isSpectator() || currentLiftPlayer.isDead() ||
                        phantom.getHealth() < phantom.getMaxHealth() / 3 ||
                        !phantom.getWorld().isAir(phantom.getBlockPos().up(1)) ||
                        phantom.getY() > maxHeight) {
                    phantom.removeAllPassengers();
                    canLiftPlayer = false; // Start cooldown
                }
            } else {
                phantom.removeAllPassengers();
            }
        } else {
            // Check to grab a new player
            LivingEntity target = phantom.getTarget();
            if (target == null) {
                target = phantom.getAttacking(); // fallback to whatever was being attacked
            }

            if (target instanceof PlayerEntity && phantom.isAlive()) {
                PlayerEntity player = (PlayerEntity) target;
                if (!player.isCreative() && !player.isSpectator() && !player.isDead()) {
                    if (phantom.distanceTo(player) <= 5.0 && phantom.getHealth() >= phantom.getMaxHealth() / 3) {
                        player.startRiding(phantom, true);
                    }
                }
            }
        }
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        if (passenger instanceof PlayerEntity) {
            // Position the player below the phantom as if being carried by its claws
            double yOffset = this.getY() - passenger.getHeight() * 0.8;
            positionUpdater.accept(passenger, this.getX(), yOffset, this.getZ());
        }
    }
}
