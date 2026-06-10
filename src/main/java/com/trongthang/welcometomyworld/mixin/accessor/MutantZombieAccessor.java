package com.trongthang.welcometomyworld.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "fuzs.mutantmonsters.world.entity.mutant.MutantZombie")
public interface MutantZombieAccessor {
    @Invoker("setLives")
    void callSetLives(int lives);
}
