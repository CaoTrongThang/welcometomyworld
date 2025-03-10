package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.GlobalConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.GlobalConfig.*;

@Mixin(CreeperEntity.class)
public class CreeperAIMixin {
//    private int cooldown = 200;
//    private int counter = cooldown;
//    private boolean canCooldown = false;
//
//    PlayerEntity lastTargetPlayer;
//
//    @Inject(method = "tick", at = @At("HEAD"))
//    private void onTickMovement(CallbackInfo ci) {
//        if(!canCreeperAI) return;
//
//        if(canCooldown){
//            counter++;
//            if(counter >= cooldown){
//                canCooldown = false;
//            }
//
//            return;
//        }
//        CreeperEntity creeperEntity = (CreeperEntity) (Object) this;
//        PlayerEntity targetPlayer = null;
//
//        if (!creeperEntity.hasVehicle()) {
//            if (creeperEntity.getTarget() instanceof PlayerEntity) {
//                targetPlayer = (PlayerEntity) creeperEntity.getTarget();
//            }
//
//            if (targetPlayer == null) {
//                if (lastTargetPlayer != null) {
//                    if (creeperEntity.getServer().getPlayerManager().getPlayer(lastTargetPlayer.getUuid()) != null) {
//                        creeperEntity.setTarget(lastTargetPlayer);
//                        targetPlayer = lastTargetPlayer;
//                    }
//                }
//
//            } else {
//                if (lastTargetPlayer != targetPlayer) {
//                    lastTargetPlayer = targetPlayer;
//                }
//            }
//
//            if (lastTargetPlayer == null) {
//                return;
//            }
//
//            if (lastTargetPlayer.isDead() || lastTargetPlayer.isCreative() || lastTargetPlayer.isSpectator() || lastTargetPlayer.getWorld() != lastTargetPlayer.getWorld()) {
//                lastTargetPlayer = null;
//                creeperEntity.setTarget(null);
//                return;
//            }
//
//            if (creeperEntity.distanceTo(lastTargetPlayer) < 20) {
//                creeperEntity.setTarget(lastTargetPlayer);
//            } else {
//                creeperEntity.setTarget(null);
//                lastTargetPlayer = null;
//                return;
//            }
//
//            LOGGER.info("Ignite: " + creeperEntity.isIgnited());
//            if(creeperEntity.isIgnited()){
//                BlockPos downPos = creeperEntity.getBlockPos().down();
//
//                Vec3d directionToPlayer = targetPlayer.getPos()
//                        .subtract(new Vec3d(downPos.getX() + 0.5, creeperEntity.getY(), downPos.getZ() + 0.5))
//                        .normalize();
//
//                if (Math.abs(targetPlayer.getX() - creeperEntity.getX()) < 4 && Math.abs(targetPlayer.getY() - creeperEntity.getY()) < 2) {
//                    tryJumpForward(creeperEntity, directionToPlayer);
//                } else if (Math.abs(targetPlayer.getX() - creeperEntity.getX()) < 2 && Math.abs(targetPlayer.getY() - creeperEntity.getY()) < 4) {
//                    tryJumpUp(creeperEntity);
//                }
//            }
//        }
//    }
//
//    private void tryJumpForward(CreeperEntity creeperEntity, Vec3d playerDirection) {
//        if (!creeperEntity.isOnGround()) return; // Jump only if on the ground
//
//        //It's working but it only jumps with the same force, i want the force is enough to jump to the player's block
//
//        Vec3d jumpVelocity = creeperEntity.getRotationVec(0.0F).multiply(0, 0, 0).add((playerDirection.x), 0.55, (playerDirection.z));
//
//        creeperEntity.setVelocity(creeperEntity.getVelocity().add(jumpVelocity));
//        creeperEntity.velocityDirty = true;
//
//        resetCanCooldown();
//    }
//
//    private void tryJumpUp(CreeperEntity creeperEntity) {
//        if (!creeperEntity.isOnGround()) return; // Jump only if on the ground
//
//        // Adjust jump behavior
//        Vec3d jumpVelocity = creeperEntity.getRotationVec(0.0F).multiply(0, 0, 0).add(0, 1, 0);
//
//        creeperEntity.setVelocity(creeperEntity.getVelocity().add(jumpVelocity));
//        creeperEntity.velocityDirty = true;
//
//        resetCanCooldown();
//    }
//
//    private void resetCanCooldown(){
//        canCooldown = true;
//        counter = 0;
//    }
}
