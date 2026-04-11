package com.trongthang.welcometomyworld.items;

import com.trongthang.welcometomyworld.client.renderer.armor.VoidWormBoneArmorRenderer;
import com.trongthang.welcometomyworld.managers.EffectsManager;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VoidWormBoneArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public VoidWormBoneArmorItem(ArmorMaterial armorMaterial, Type type, Settings properties) {
        super(armorMaterial, type, properties);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private VoidWormBoneArmorRenderer renderer;

            @Override
            @SuppressWarnings("unchecked")
            public BipedEntityModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, BipedEntityModel<LivingEntity> original) {
                if (this.renderer == null)
                    this.renderer = new VoidWormBoneArmorRenderer();

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity player) {
            if (isBeingWorn(stack, player)) {
                if (isFullSetWorn(player)) {
                    if (!world.isClient()) {
                        // Apply effects every 20 ticks
                        if (player.age % 20 == 0) {
                            player.addStatusEffect(
                                    new StatusEffectInstance(StatusEffects.RESISTANCE, 240, 1, false, false, true));
                            player.addStatusEffect(
                                    new StatusEffectInstance(StatusEffects.STRENGTH, 240, 2, false, false, true));
                            player.addStatusEffect(
                                    new StatusEffectInstance(EffectsManager.VOID_SIGHT, 240, 0, false, false, true));
                        }
                    }
                }
            }
        }
    }

    private boolean isBeingWorn(ItemStack stack, PlayerEntity player) {
        for (ItemStack armorStack : player.getArmorItems()) {
            if (armorStack == stack) {
                return true;
            }
        }
        return false;
    }

    private boolean isFullSetWorn(PlayerEntity player) {
        for (ItemStack armorStack : player.getArmorItems()) {
            if (!(armorStack.getItem() instanceof VoidWormBoneArmorItem)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
