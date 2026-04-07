package com.trongthang.welcometomyworld.worldgen.structure;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.util.Identifier;

public class ModStructures {
    public static StructureType<FallenKnightCastleStructure> FALLEN_KNIGHT_CASTLE;
    public static StructurePieceType FALLEN_KNIGHT_CASTLE_PIECE;
    public static StructureProcessorType<FallenKnightChestProcessor> FALLEN_KNIGHT_CHEST_PROCESSOR;

    public static void register() {
        FALLEN_KNIGHT_CASTLE = Registry.register(Registries.STRUCTURE_TYPE,
                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle"),
                () -> FallenKnightCastleStructure.CODEC);

        FALLEN_KNIGHT_CASTLE_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle_piece"), FallenKnightCastlePiece::new);

        FALLEN_KNIGHT_CHEST_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR,
                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_chest_processor"),
                () -> FallenKnightChestProcessor.CODEC);
    }
}
