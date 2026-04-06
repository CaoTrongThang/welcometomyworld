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
import net.minecraft.world.dimension.DimensionTypes;

public class VoidDimension {
    public static final RegistryKey<DimensionOptions> VOID_DIM_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
            new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim"));
    public static final RegistryKey<World> VOID_DIM_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
            new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim"));
    public static final RegistryKey<DimensionType> VOID_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "void_dim_type"));

    public static void bootstrapType(Registerable<DimensionType> context) {
        context.register(VOID_DIM_TYPE, new DimensionType(
                OptionalLong.empty(), // no fixed time — cycles naturally (or we lock sky via effects)
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural — disables compass/star/sunrise
                1.0, // coordinateScale
                false, // bedWorks
                false, // respawnAnchorWorks
                -128, // minY — deep terrain bottom
                384, // height — full range
                384, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                DimensionTypes.THE_END_ID, // effectsLocation — End sky: no sun/moon, pure dark void
                0.1f, // ambientLight — very dim, almost pitch black
                new DimensionType.MonsterSettings(true, false, UniformIntProvider.create(0, 7), 0)));
    }
}
