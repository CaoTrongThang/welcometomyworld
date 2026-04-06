package com.trongthang.welcometomyworld.world.dimension;

import java.util.OptionalLong;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

public class VoidDimension {
        public static final RegistryKey<DimensionOptions> VOID_DIM_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim"));
        public static final RegistryKey<World> VOID_DIM_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim"));
        public static final RegistryKey<DimensionType> VOID_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
                        new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim_type"));

        public static final Identifier VOID_DIM_EFFECTS_ID = new Identifier(WelcomeToMyWorld.MOD_ID,
                        "void_dim_effects");

        public static void bootstrapType(Registerable<DimensionType> context) {
                // how do i change water color?
                context.register(VOID_DIM_TYPE, new DimensionType(
                                OptionalLong.of(18000L),
                                false, false, false, false, 1.0, false, false, -128, 384, 384,
                                BlockTags.INFINIBURN_OVERWORLD,
                                VOID_DIM_EFFECTS_ID, // Use the constant
                                0.0f,

                                new DimensionType.MonsterSettings(true, false, UniformIntProvider.create(0, 7), 0)));
        }
}