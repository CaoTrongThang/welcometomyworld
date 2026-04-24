package com.trongthang.welcometomyworld.client;

import com.trongthang.welcometomyworld.entities.client.Blossom.BlossomModel;
import com.trongthang.welcometomyworld.entities.client.Blossom.BlossomRenderer;
import com.trongthang.welcometomyworld.entities.client.Enderchester.EnderchesterModel;
import com.trongthang.welcometomyworld.entities.client.Enderchester.EnderchesterRenderer;
import com.trongthang.welcometomyworld.entities.client.FallingSkeleton.FallingSkeletonRenderer;

import com.trongthang.welcometomyworld.entities.client.BlockSlamGroudEntity.BlockSlamGroundRenderer;
import com.trongthang.welcometomyworld.entities.client.Chester.ChesterModel;
import com.trongthang.welcometomyworld.entities.client.Chester.ChesterRenderer;
import com.trongthang.welcometomyworld.entities.client.FallenKnight.FallenKnightModel;
import com.trongthang.welcometomyworld.entities.client.FallenKnight.FallenKnightRenderer;
import com.trongthang.welcometomyworld.entities.client.Portaler.PortalerModel;
import com.trongthang.welcometomyworld.entities.client.Portaler.PortalerRenderer;
import com.trongthang.welcometomyworld.entities.client.EnderPest.EnderPestModel;
import com.trongthang.welcometomyworld.entities.client.EnderPest.EnderPestRenderer;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererArrow.WandererArrowModel;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererArrow.WandererArrowRenderer;
import com.trongthang.welcometomyworld.entities.client.blocks.PurplePortalActivatorRenderer;
import com.trongthang.welcometomyworld.entities.client.blocks.VoidBlockRenderer;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererModel;
import com.trongthang.welcometomyworld.entities.client.Wanderer.WandererRenderer;
import com.trongthang.welcometomyworld.entities.client.Unknown.UnknownBeamRenderer;
import com.trongthang.welcometomyworld.entities.client.Unknown.UnknownRenderer;
import com.trongthang.welcometomyworld.entities.client.Unknown.SummoningCircleRenderer;
import com.trongthang.welcometomyworld.entities.client.Unknown.GroundSlashAttackRenderer;
import com.trongthang.welcometomyworld.entities.client.VoidWorm.VoidWormRenderer;
import com.trongthang.welcometomyworld.entities.client.VoidWorm.VoidWormBodyRenderer;
import com.trongthang.welcometomyworld.entities.client.VoidWorm.VoidWormTailRenderer;
import com.trongthang.welcometomyworld.entities.client.VoidWorm.PurpleCrystalRenderer;
import com.trongthang.welcometomyworld.entities.client.PurplePortal.PurplePortalRenderer;
import com.trongthang.welcometomyworld.entities.client.RiftPortal.RiftPortalRenderer;
import com.trongthang.welcometomyworld.entities.client.Voidan.VoidanRenderer;
import com.trongthang.welcometomyworld.entities.client.Voidan.VoidanTentacleRenderer;

import com.trongthang.welcometomyworld.managers.BlocksEntitiesManager;
import com.trongthang.welcometomyworld.managers.BlocksManager;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.world.dimension.VoidDimension;
import com.trongthang.welcometomyworld.world.dimension.VoidDimensionEffect;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimension;
import com.trongthang.welcometomyworld.world.dimension.WhiteDimensionEffect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class RendererClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {

                EntityRendererRegistry.register(EntitiesManager.BLOCK_SLAM_GROUND, BlockSlamGroundRenderer::new);

                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.CUSTOM_VINE, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.GLOWING_WHITE_GRASS, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.GLOWING_PURPLE_GRASS, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.RUSTED_IRON_BARS, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.TOUGHER_IRON_BARS, RenderLayer.getCutout());

                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.GAMING_DISC_TROPHY, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.EASYCRAFT_TROPHY, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.CHALLENGER_TROPHY, RenderLayer.getCutout());
                BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.MUSIC_TROPHY, RenderLayer.getCutout());

                DimensionRenderingRegistry.registerDimensionEffects(VoidDimension.VOID_DIM_EFFECTS_ID,
                                new VoidDimensionEffect());

                DimensionRenderingRegistry.registerDimensionEffects(WhiteDimension.WHITE_DIM_EFFECTS_ID,
                                new WhiteDimensionEffect());

                DimensionRenderingRegistry.registerSkyRenderer(VoidDimension.VOID_DIM_LEVEL_KEY, context -> {
                });

                DimensionRenderingRegistry.registerSkyRenderer(WhiteDimension.WHITE_DIM_LEVEL_KEY, context -> {
                });

                DimensionRenderingRegistry.registerCloudRenderer(VoidDimension.VOID_DIM_LEVEL_KEY, context -> {
                });

                DimensionRenderingRegistry.registerCloudRenderer(WhiteDimension.WHITE_DIM_LEVEL_KEY, context -> {
                });

                // BlockRenderLayerMap.INSTANCE.putBlock(BlocksManager.GAME_DISC_TROPHY,
                // RenderLayer.getCutout());

                EntityModelLayerRegistry.registerModelLayer(EnderchesterModel.A_LIVING_ENDER_CHEST,
                                EnderchesterModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.ENDERCHESTER, EnderchesterRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(ChesterModel.CHESTER, ChesterModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.CHESTER, ChesterRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(PortalerModel.PORTALER,
                                PortalerModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.PORTALER, PortalerRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(EnderPestModel.ENDER_PEST,
                                EnderPestModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.ENDER_PEST, EnderPestRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(FallenKnightModel.FALLEN_KNIGHT,
                                FallenKnightModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.FALLEN_KNIGHT, FallenKnightRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(WandererModel.WANDERER,
                                WandererModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.WANDERER, WandererRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(WandererArrowModel.WANDERER_ARROW,
                                WandererArrowModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.WANDERER_ARROW, WandererArrowRenderer::new);

                EntityModelLayerRegistry.registerModelLayer(BlossomModel.BLOSSOM, BlossomModel::getTexturedModelData);
                EntityRendererRegistry.register(EntitiesManager.BLOSSOM, BlossomRenderer::new);

                EntityRendererRegistry.register(EntitiesManager.UNKNOWN, UnknownRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.TINY_GOLEM,
                                com.trongthang.welcometomyworld.entities.TinyGolem.TinyGolemRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.VOIDAN, VoidanRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.VOIDAN_TENTACLE, VoidanTentacleRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.UNKNOWN_BEAM, UnknownBeamRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.SUMMONING_CIRCLE, SummoningCircleRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.GROUND_SLASH_ATTACK, GroundSlashAttackRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.FALLING_SKELETON, FallingSkeletonRenderer::new);

                EntityRendererRegistry.register(EntitiesManager.VOID_WORM, VoidWormRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.VOID_WORM_BODY, VoidWormBodyRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.VOID_WORM_TAIL, VoidWormTailRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.PURPLE_CRYSTAL, PurpleCrystalRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.PURPLE_PORTAL_ENTITY, PurplePortalRenderer::new);
                EntityRendererRegistry.register(EntitiesManager.RIFT_PORTAL_ENTITY, RiftPortalRenderer::new);

                BlockEntityRendererFactories.register(BlocksEntitiesManager.VOID_BLOCK_ENTITY, VoidBlockRenderer::new);
                BlockEntityRendererFactories.register(BlocksEntitiesManager.PURPLE_PORTAL_ACTIVATOR_BLOCK_ENTITY,
                                PurplePortalActivatorRenderer::new);
                BlockEntityRendererFactories.register(BlocksEntitiesManager.VOIDAN_SUMMONER_BLOCK_ENTITY,
                                com.trongthang.welcometomyworld.entities.client.blocks.VoidanSummonerRenderer::new);

        }
}
