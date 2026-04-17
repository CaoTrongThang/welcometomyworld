package com.trongthang.welcometomyworld.items;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.entities.Voidan.VoidanTentacle;
import com.trongthang.welcometomyworld.entities.client.items.VoidanHornItemRenderer;
import com.trongthang.welcometomyworld.managers.EntitiesManager;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VoidanHornItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public VoidanHornItem(Settings settings) {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient()) {
            triggerAnim(user, GeoItem.getOrAssignId(itemStack, (ServerWorld) world), "mainController", "use");
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundsManager.VOIDAN_HORN,
                    user.getSoundCategory(), 1.0f, 1.0f);

            // Cooldown: 200 seconds = 4000 ticks
            user.getItemCooldownManager().set(this, 4000);

            Utils.spawnCircleParticles((ServerPlayerEntity) user);

            // Delays for tentacle summons
            scheduleTentacleSummon(world, user, 60, 0); // 3s delay
            scheduleTentacleSummon(world, user, 80, 120); // 4s delay
            scheduleTentacleSummon(world, user, 100, 240); // 5s delay
        }

        return TypedActionResult.success(itemStack);
    }

    private void scheduleTentacleSummon(World world, PlayerEntity user, int ticks, float angleOffset) {
        Utils.addRunAfter(() -> {
            if (world instanceof ServerWorld sw && user.isAlive()) {
                double radius = 3.5;
                double yawRad = Math.toRadians(user.getYaw() + angleOffset);
                double x = user.getX() + radius * Math.sin(-yawRad);
                double z = user.getZ() + radius * Math.cos(yawRad);

                BlockPos spawnPos = BlockPos.ofFloored(x, user.getY(), z);
                BlockPos surfacePos = sw.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawnPos);

                VoidanTentacle tentacle = new VoidanTentacle(EntitiesManager.VOIDAN_TENTACLE, sw);
                tentacle.refreshPositionAndAngles(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5,
                        user.getYaw(), 0.0f);
                tentacle.setSummoner(user);
                sw.spawnEntity(tentacle);
            }
        }, ticks);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private VoidanHornItemRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new VoidanHornItemRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<VoidanHornItem> controller = new AnimationController<>(this, "mainController", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenPlayAndHold("use")));
        controller.triggerableAnim("use", RawAnimation.begin().thenPlayAndHold("use"));
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
