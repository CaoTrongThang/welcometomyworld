package com.trongthang.welcometomyworld.managers;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;

public class ParticlesManager {
    public static final DefaultParticleType VOID_DUST_PARTICLE = FabricParticleTypes.simple(true);

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(WelcomeToMyWorld.MOD_ID, "void_dust"),
                VOID_DUST_PARTICLE);
        WelcomeToMyWorld.LOGGER.info("Registering particles for " + WelcomeToMyWorld.MOD_ID);
    }
}
