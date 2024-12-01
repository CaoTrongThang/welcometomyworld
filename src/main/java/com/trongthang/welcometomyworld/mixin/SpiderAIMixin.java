package com.trongthang.welcometomyworld.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.trongthang.welcometomyworld.BlocksPlacedAndBrokenByMobsHandler.SPIDER_COBWEB_DESPAWN_TICK;
import static com.trongthang.welcometomyworld.GlobalConfig.canSpiderAI;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.dataHandler;

@Mixin(SpiderEntity.class)
public abstract class SpiderAIMixin extends Entity {

    public int attackSpiderWebCooldown = 60;
    public int counter = 60;

    public SpiderAIMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if(!canSpiderAI) return;

        SpiderEntity spider = (SpiderEntity) (Object) this;

        PlayerEntity targetPlayer = null;

        if (spider.getTarget() instanceof PlayerEntity) {
            targetPlayer = (PlayerEntity) spider.getTarget();
        } else {
            targetPlayer = null;
        }

        if (targetPlayer != null) {
            counter++;
            if (!targetPlayer.isOnGround()) {
                counter += 1;
                if (counter > attackSpiderWebCooldown) {
                    placeCobwebBlock(spider.getWorld(), targetPlayer.getBlockPos());
                    counter = 0;
                }
            } else if ((spider.distanceTo(targetPlayer) < 10 && spider.distanceTo(targetPlayer) > 4) && counter > attackSpiderWebCooldown) {
                placeCobwebBlock(spider.getWorld(), targetPlayer.getBlockPos());
                counter = 0;
            }
        }
    }

    private boolean placeCobwebBlock(World world, BlockPos pos) {
        if (!world.isAir(pos)) return false;

        dataHandler.blocksPlacedByMobWillRemove.put(pos, SPIDER_COBWEB_DESPAWN_TICK);

        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());


        // Play a block placement sound
        world.playSound(
                null,                           // Player to play sound for (null means all nearby players)
                pos,                            // Position of the sound
                SoundEvents.ENTITY_SPIDER_AMBIENT, // Sound to play (default place sound for the block)
                net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                1.0F,                           // Volume (1.0 = normal)
                1.0F                            // Pitch (1.0 = normal)
        );

        world.playSound(
                null,                           // Player to play sound for (null means all nearby players)
                pos,                            // Position of the sound
                Blocks.COBWEB.getDefaultState().getSoundGroup().getPlaceSound(), // Sound to play (default place sound for the block)
                net.minecraft.sound.SoundCategory.BLOCKS, // Category of the sound
                1.0F,                           // Volume (1.0 = normal)
                1.0F                            // Pitch (1.0 = normal)
        );
        return true;
    }
}
