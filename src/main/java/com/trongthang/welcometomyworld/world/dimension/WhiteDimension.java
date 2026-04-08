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

public class WhiteDimension {
    public static final RegistryKey<DimensionOptions> WHITE_DIM_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
            new Identifier(WelcomeToMyWorld.MOD_ID, "white_dim"));
    public static final RegistryKey<World> WHITE_DIM_LEVEL_KEY = RegistryKey.of(RegistryKeys.WORLD,
            new Identifier(WelcomeToMyWorld.MOD_ID, "white_dim"));
    public static final RegistryKey<DimensionType> WHITE_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE,
            new Identifier(WelcomeToMyWorld.MOD_ID, "white_dim_type"));

    public static final Identifier WHITE_DIM_EFFECTS_ID = new Identifier(WelcomeToMyWorld.MOD_ID,
            "white_dim_effects");

    public static void bootstrapType(Registerable<DimensionType> context) {
        context.register(WHITE_DIM_TYPE, new DimensionType(
                OptionalLong.of(6000L), // Always Day
                true, // hasSkyLight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                false, // bedWorks
                false, // respawnAnchorWorks
                -64, // minY
                384, // height
                384, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD,
                WHITE_DIM_EFFECTS_ID,
                1.0f, // ambientLight (Full Bright)
                new DimensionType.MonsterSettings(false, false, UniformIntProvider.create(0, 0), 0))); // No monsters
                                                                                                       // for now, or
                                                                                                       // minimal
    }
}
