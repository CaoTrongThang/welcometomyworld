package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


//this code is belong to VariableSpawnerHardness by StrikerRocker, but I want to change the hardness
@Mixin({AbstractBlock.AbstractBlockState.class})
public abstract class HarderSpawner {
    private static final float[] hardnessByDifficulty = new float[]{10.0F, 20.0F, 35.5F, 40.0F};

    @Shadow
    public abstract Block getBlock();

    @Inject(
            method = {"getHardness"},
            at = {@At("TAIL")},
            cancellable = true
    )
    public void getHardness(BlockView worldIn, BlockPos pos, CallbackInfoReturnable cir) {
        if (this.getBlock() == Blocks.SPAWNER && worldIn instanceof WorldAccess) {
            cir.setReturnValue(onGetBlockHardness(worldIn));
        }

    }

    private static float onGetBlockHardness(BlockView worldIn) {
        WorldProperties worldInfo = ((WorldAccess)worldIn).getLevelProperties();
        return worldInfo.isHardcore() ? 55.0F : hardnessByDifficulty[worldInfo.getDifficulty().ordinal()];
    }
}
