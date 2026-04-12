package com.trongthang.welcometomyworld.entities.client.items;

import com.trongthang.welcometomyworld.items.CaptureCageItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Optional;

public class CaptureCageRenderer extends GeoItemRenderer<CaptureCageItem> {

    private static final String ENTITY_DATA_KEY = "CapturedEntityData";
    private static final String ENTITY_TYPE_KEY = "CapturedEntityType";
    private Entity cachedEntity;
    private String cachedEntityId;

    public CaptureCageRenderer() {
        super(new CaptureCageModel());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack,
            VertexConsumerProvider bufferSource, int combinedLight, int combinedOverlay) {
        super.render(stack, transformType, poseStack, bufferSource, combinedLight, combinedOverlay);

        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ENTITY_DATA_KEY)) {
            cachedEntity = null;
            cachedEntityId = null;
            return;
        }

        String entityTypeStr = nbt.getString(ENTITY_TYPE_KEY);
        NbtCompound entityData = nbt.getCompound(ENTITY_DATA_KEY);

        if (cachedEntity == null || !entityTypeStr.equals(cachedEntityId)) {
            Optional<EntityType<?>> typeOpt = EntityType.get(entityTypeStr);
            if (typeOpt.isPresent()) {
                cachedEntity = typeOpt.get().create(MinecraftClient.getInstance().world);
                if (cachedEntity != null) {
                    cachedEntity.readNbt(entityData);
                    cachedEntityId = entityTypeStr;
                }
            }
        }

        if (cachedEntity != null) {
            poseStack.push();

            poseStack.translate(0.5, 0.75, 0.5);

            float rotation = (System.currentTimeMillis() % 5000) / 5000f * 360f;
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

            float width = cachedEntity.getWidth();
            float height = cachedEntity.getHeight();
            float maxDim = Math.max(width, height);

            // Reduced scale from 0.35f to 0.25f to fit better inside the cage
            float scale = 0.25f / Math.max(maxDim, 0.5f);
            poseStack.scale(scale, scale, scale);

            // Center the entity vertically based on its height to ensure it stays within
            // the cage
            poseStack.translate(0, -height / 2.0, 0);

            renderEntity(cachedEntity, poseStack, bufferSource, combinedLight);

            poseStack.pop();
        }
    }

    private <T extends Entity> void renderEntity(T entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light) {
        // Use the dispatcher for correct entity rendering including all features and
        // layers
        MinecraftClient.getInstance().getEntityRenderDispatcher()
                .render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrices, vertexConsumers, light);
    }
}
