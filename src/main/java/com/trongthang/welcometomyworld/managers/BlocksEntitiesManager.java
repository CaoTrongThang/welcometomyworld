package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.blockentities.BurningPlankBlockEntity;
import com.trongthang.welcometomyworld.blockentities.JustACounterBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.LOGGER;

public class BlocksEntitiesManager {

    // The BlockEntityType that will hold the custom block entity
    public static final BlockEntityType<BurningPlankBlockEntity> BURNING_PLANK_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(WelcomeToMyWorld.MOD_ID, "burning_plank_block_entity"),
                    BlockEntityType.Builder.create(BurningPlankBlockEntity::new, BlocksManager.BURNING_PLANK).build(null)
            );

    public static final BlockEntityType<JustACounterBlockEntity> RUSTED_IRON_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(WelcomeToMyWorld.MOD_ID, "rusted_iron_block_entity"),
                    BlockEntityType.Builder.create(JustACounterBlockEntity::new, BlocksManager.RUSTED_IRON_BLOCK).build(null)
            );
    
    public static void initialize() {
        LOGGER.info("Registering Block Entities...");
    }
}

