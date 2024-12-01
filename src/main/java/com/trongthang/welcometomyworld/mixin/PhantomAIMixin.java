package com.trongthang.welcometomyworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

    private int maxHeight = 300;

    private int cooldown = 120;
    private int counter = 0;
    private boolean canLiftPlayer = true;

    public PhantomAIMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private PlayerEntity currentLiftPlayer = null;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if(!canPhantomAI) return;
        
        PhantomEntity phantom = (PhantomEntity) (Object) this;
        if (phantom.getPhantomSize() < 9) return;


        if (!canLiftPlayer) {
            counter++;
            if (counter > cooldown) {
                canLiftPlayer = true;
                cooldown++;
            }
            return;
        }



        // Lift the player and make the Phantom ascend
        if (currentLiftPlayer != null) {
            if (phantom.getServer().getPlayerManager().getPlayer(currentLiftPlayer.getUuid()) != null) {
                liftAndTeleportPlayer(phantom);
            }
        }

        LivingEntity attackEntity = phantom.getAttacking();

        if (currentLiftPlayer != null) return;

        if (attackEntity instanceof PlayerEntity && currentLiftPlayer == null) {
            currentLiftPlayer = (PlayerEntity) attackEntity;
        }
    }

    private void liftAndTeleportPlayer(PhantomEntity phantom) {
        if (currentLiftPlayer == null || currentLiftPlayer.isCreative() || currentLiftPlayer.isSpectator()) {
            dropPlayer(phantom);
            return;
        }

        if (phantom.distanceTo(currentLiftPlayer) <= 10.0 && phantom.getHealth() >= phantom.getMaxHealth() / 3) {
            Vec3d phantomPos = phantom.getPos();
            World world = phantom.getWorld();

            if (!world.isAir(phantom.getBlockPos().up(1))) {
                dropPlayer(phantom);
                return;
            }

            if (phantom.getY() > maxHeight) {
                dropPlayer(phantom);
                return;
            }

            currentLiftPlayer.teleport(phantomPos.getX(), phantomPos.getY() - 1.2, phantomPos.getZ());

        } else {
            currentLiftPlayer = null;
        }
    }

    private BlockPos scanAroundToFindAFlyUpPos(PhantomEntity phantom) {
        BlockState up = phantom.getWorld().getBlockState(phantom.getBlockPos().up());
        if (up.isAir()) {
            return phantom.getBlockPos().up();
        } else {

            BlockPos[] poses = {
                    phantom.getBlockPos().north(),
                    phantom.getBlockPos().south(),
                    phantom.getBlockPos().east(),
                    phantom.getBlockPos().west(),
                    phantom.getBlockPos().down()
            };

            for (BlockPos pos : poses) {
                if (phantom.getWorld().getBlockState(pos).isAir()) {
                    return pos;
                }
            }
        }
        return null;
    }


    private void dropPlayer(PhantomEntity phantom) {
        canLiftPlayer = false;
        currentLiftPlayer = null; // Reset after dropping
    }
}
