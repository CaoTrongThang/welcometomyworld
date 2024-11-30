package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.Utils;
import com.trongthang.welcometomyworld.saveData.BlockProgress;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
//
//    BlockState currentBlockToBreak;
//    BlockPos currentBlockToBreakPos;
//
//    BlockState nextBreakingBlockTarget;
//    BlockPos nextBreakingBlockPos;
//
//    private int progressEachBreak = 25;
//    private int lastProgress = 100;
//
//    private int breakingBlockCooldown = 20;
//    private int coolDownCounter = 0;
//
//    //This will make sure the mob won't stop targeting the player
//    private PlayerEntity lastTargetPlayer;
//
//    SoundEvent breakingSound = SoundEvents.BLOCK_ANVIL_BREAK;
//
//    @Inject(method = "tick", at = @At("head"), cancellable = true)
//    private void tick(CallbackInfo callbackInfo) {
//        HostileEntity entity;
//
//
//        if(currentBlockToBreakPos != null){
//            coolDownCounter++;
//        }
//
//        if ((LivingEntity) (Object) this instanceof HostileEntity) {
//            entity = (HostileEntity) (Object) this;
//        } else {
//            entity = null;
//        }
//
//        if (lastTargetPlayer == null) {
//            if (entity == null) return;
//            Entity target = entity.getTarget();
//            if (target != null) {
//                if (entity.getTarget() instanceof PlayerEntity) {
//                    lastTargetPlayer = (PlayerEntity) target;
//                }
//            }
//        }
//
//        if (entity == null) return;
//        if (lastTargetPlayer == null) return;
//
//
//        if (currentBlockToBreakPos == null) {
//            // Determine block position and break it
//            BlockHitResult blockHit = entity.getWorld().raycast(new RaycastContext(
//                    entity.getEyePos(),
//                    lastTargetPlayer.getPos(),
//                    RaycastContext.ShapeType.COLLIDER,
//                    RaycastContext.FluidHandling.NONE,
//                    entity
//            ));
//
//            BlockPos blockBlockingWay = null;
//            if (blockHit != null) {
//                blockBlockingWay = blockHit.getBlockPos();
//                System.out.println("BlockHit result: " + blockBlockingWay); // Debug: Log the result of the block hit
//            } else {
//                System.out.println("BlockHit is null"); // Debug: Log when the BlockHit is null
//            }
//
//            //Trying to make the block slowly broken
//            if (blockBlockingWay != null) {
//                currentBlockToBreakPos = blockBlockingWay;
//                currentBlockToBreak = entity.getWorld().getBlockState(currentBlockToBreakPos);
//                System.out.println("currentBlockToBreakPos set to: " + currentBlockToBreakPos); // Debug: Log when block position is set
//            } else {
//                System.out.println("blockBlockingWay is null, not setting currentBlockToBreakPos"); // Debug: Log when block position is not set
//            }
//        } else {
//            if (coolDownCounter > breakingBlockCooldown) {
//                LOGGER.info("Here");
//                lastProgress -= progressEachBreak;
//                if (lastProgress <= 0) {
//                    if (currentBlockToBreakPos != null) {
//                        entity.getWorld().setBlockState(currentBlockToBreakPos, Blocks.AIR.getDefaultState());
//                        dataHandler.blocksBrokenByMobWillRestore.put(currentBlockToBreakPos, currentBlockToBreak);
//                        Utils.playSound(entity.getServer().getOverworld(), currentBlockToBreakPos, currentBlockToBreak.getSoundGroup().getBreakSound());
//                        currentBlockToBreakPos = null;
//                        coolDownCounter = 0;
//                        lastProgress = 100;
//                    }
//                } else {
//                    Utils.saveBlockProgress.put(currentBlockToBreakPos, new BlockProgress(entity, lastProgress));
//                }
//            }
//        }
//
//
//        // TODO: i want to check if the mob see the player but there is no way to get to the player, it'll start breaking blocks
//        // TODO: if the player is below the mob, i'll dig straight down, but if above it'll dig like a stair, kinda hard, if same y, it'll break through blocks
//    }
//
//
//    //This is in working, no need to worry
//    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
//    private void onDamage(DamageSource source, float damageAmount, CallbackInfoReturnable<Boolean> cir) {
//
//        LivingEntity entity = (LivingEntity) (Object) this;
//
//        if (source.getSource() instanceof ArrowEntity) {
//            //Increase damage dealt by Arrows
//            entity.damage(entity.getWorld().getDamageSources().generic(), damageAmount);
//        }
//
//        if ("explosion.player".equals(source.getName())) {
//            //Increase damage dealt by Explosions
//            entity.damage(entity.getWorld().getDamageSources().generic(), damageAmount);
//        }
//    }
}
