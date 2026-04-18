package com.trongthang.welcometomyworld.managers;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.worldgen.structure.FallenKnightCastlePiece;
import com.trongthang.welcometomyworld.worldgen.structure.FallenKnightCastleStructure;
import com.trongthang.welcometomyworld.worldgen.structure.FallenKnightChestProcessor;
import com.trongthang.welcometomyworld.worldgen.structure.NetherMediumOutpostChestProcessor;
import com.trongthang.welcometomyworld.worldgen.structure.NetherMediumOutpostPiece;
import com.trongthang.welcometomyworld.worldgen.structure.NetherMediumOutpostStructure;
import com.trongthang.welcometomyworld.worldgen.structure.UnknownBasePiece;
import com.trongthang.welcometomyworld.worldgen.structure.UnknownBaseProcessor;
import com.trongthang.welcometomyworld.worldgen.structure.UnknownBaseStructure;
import com.trongthang.welcometomyworld.worldgen.structure.VoidanArenaPiece;
import com.trongthang.welcometomyworld.worldgen.structure.VoidanArenaStructure;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.util.Identifier;

public class StructuresManager {
        public static StructureType<FallenKnightCastleStructure> FALLEN_KNIGHT_CASTLE;
        public static StructurePieceType FALLEN_KNIGHT_CASTLE_PIECE;
        public static StructureProcessorType<FallenKnightChestProcessor> FALLEN_KNIGHT_CHEST_PROCESSOR;

        public static StructureType<NetherMediumOutpostStructure> NETHER_MEDIUM_OUTPOST;
        public static StructurePieceType NETHER_MEDIUM_OUTPOST_PIECE;
        public static StructureProcessorType<NetherMediumOutpostChestProcessor> NETHER_MEDIUM_OUTPOST_CHEST_PROCESSOR;

        public static StructureType<UnknownBaseStructure> UNKNOWN_BASE;
        public static StructurePieceType UNKNOWN_BASE_PIECE;
        public static StructureProcessorType<UnknownBaseProcessor> UNKNOWN_BASE_PROCESSOR;

        public static StructureType<VoidanArenaStructure> VOIDAN_ARENA;
        public static StructurePieceType VOIDAN_ARENA_PIECE;

        public static void register() {
                FALLEN_KNIGHT_CASTLE = Registry.register(Registries.STRUCTURE_TYPE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle"),
                                () -> FallenKnightCastleStructure.CODEC);

                FALLEN_KNIGHT_CASTLE_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_castle_piece"),
                                FallenKnightCastlePiece::new);

                FALLEN_KNIGHT_CHEST_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "fallen_knight_chest_processor"),
                                () -> FallenKnightChestProcessor.CODEC);

                NETHER_MEDIUM_OUTPOST = Registry.register(Registries.STRUCTURE_TYPE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "nether_medium_outpost"),
                                () -> NetherMediumOutpostStructure.CODEC);

                NETHER_MEDIUM_OUTPOST_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "nether_medium_outpost_piece"),
                                NetherMediumOutpostPiece::new);

                NETHER_MEDIUM_OUTPOST_CHEST_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "nether_medium_outpost_chest_processor"),
                                () -> NetherMediumOutpostChestProcessor.CODEC);

                UNKNOWN_BASE = Registry.register(Registries.STRUCTURE_TYPE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "unknown_base"),
                                () -> UnknownBaseStructure.CODEC);

                UNKNOWN_BASE_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "unknown_base_piece"),
                                UnknownBasePiece::new);

                UNKNOWN_BASE_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "unknown_base_processor"),
                                () -> UnknownBaseProcessor.CODEC);

                VOIDAN_ARENA = Registry.register(Registries.STRUCTURE_TYPE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "voidan_arena"),
                                () -> VoidanArenaStructure.CODEC);

                VOIDAN_ARENA_PIECE = Registry.register(Registries.STRUCTURE_PIECE,
                                new Identifier(WelcomeToMyWorld.MOD_ID, "voidan_arena_piece"),
                                VoidanArenaPiece::new);
        }
}
