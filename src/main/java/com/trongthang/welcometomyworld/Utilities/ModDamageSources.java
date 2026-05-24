package com.trongthang.welcometomyworld.Utilities;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageSources {
    public static final RegistryKey<DamageType> PUNCH_BLOCK = RegistryKey.of(RegistryKeys.DAMAGE_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "punch_block"));

    public static DamageSource create(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
}
