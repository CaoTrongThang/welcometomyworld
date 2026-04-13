package com.trongthang.welcometomyworld.items;

import com.trongthang.welcometomyworld.classes.CustomTameableEntity;
import com.trongthang.welcometomyworld.entities.client.items.CaptureCageRenderer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CaptureCageItem extends Item implements GeoItem {

    private static final String ENTITY_DATA_KEY = "CapturedEntityData";
    private static final String ENTITY_TYPE_KEY = "CapturedEntityType";
    private static final String ENTITY_NAME_KEY = "CapturedEntityName";

    private final int COOLDOWN = 100;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public CaptureCageItem(Settings settings) {
        super(settings.maxCount(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private CaptureCageRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new CaptureCageRenderer();

                return this.renderer;
            }
        });
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

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.PASS;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains(ENTITY_DATA_KEY)) {
            return ActionResult.PASS;
        }

        boolean isCombat = false;
        if (entity instanceof net.minecraft.entity.mob.MobEntity mob) {
            if (mob.getTarget() != null || mob.getAttacker() != null) {
                isCombat = true;
            }
        } else if (entity.getAttacker() != null) {
            isCombat = true;
        }

        if (isCombat) {
            if (!user.getWorld().isClient) {
                user.sendMessage(Text.literal("Cannot capture mob while it is in combat!").formatted(Formatting.RED),
                        true);
            }
            return ActionResult.FAIL;
        }

        if (entity instanceof TameableEntity tameable) {
            if (tameable.isTamed() && user.getUuid().equals(tameable.getOwnerUuid())) {
                if (!user.getWorld().isClient) {
                    captureEntity(stack, tameable, user);
                    user.getItemCooldownManager().set(this, COOLDOWN);
                }
                return ActionResult.success(user.getWorld().isClient);
            }
        }

        if (entity instanceof CustomTameableEntity customTameable) {
            if (customTameable.isTamed() && user.getUuid().equals(customTameable.getOwnerUuid())) {
                if (!user.getWorld().isClient) {
                    captureEntity(stack, customTameable, user);
                    user.getItemCooldownManager().set(this, COOLDOWN);
                }
                return ActionResult.success(user.getWorld().isClient);
            }
        }

        return ActionResult.PASS;
    }

    private void captureEntity(ItemStack stack, LivingEntity entity, PlayerEntity player) {
        NbtCompound nbt = stack.getOrCreateNbt();

        NbtCompound entityData = new NbtCompound();
        entity.saveSelfNbt(entityData);

        nbt.put(ENTITY_DATA_KEY, entityData);
        nbt.putString(ENTITY_TYPE_KEY, EntityType.getId(entity.getType()).toString());
        nbt.putString(ENTITY_NAME_KEY, entity.getName().getString());

        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 20; i++) {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                        entity.getX() + (serverWorld.random.nextDouble() - 0.5) * entity.getWidth(),
                        entity.getY() + serverWorld.random.nextDouble() * entity.getHeight(),
                        entity.getZ() + (serverWorld.random.nextDouble() - 0.5) * entity.getWidth(),
                        1, 0, 0.1, 0, 0.05);
            }
        }

        entity.discard();
        player.sendMessage(Text.literal("Captured " + entity.getName().getString()).formatted(Formatting.GREEN), true);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player != null && player.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.PASS;
        }

        if (world.isClient)
            return ActionResult.SUCCESS;

        ItemStack stack = context.getStack();
        NbtCompound nbt = stack.getNbt();

        if (nbt != null && nbt.contains(ENTITY_DATA_KEY)) {
            String entityTypeStr = nbt.getString(ENTITY_TYPE_KEY);
            NbtCompound entityData = nbt.getCompound(ENTITY_DATA_KEY);

            Optional<EntityType<?>> typeOpt = EntityType.get(entityTypeStr);
            if (typeOpt.isPresent()) {
                Entity entity = typeOpt.get().create(world);
                if (entity != null) {
                    entity.readNbt(entityData);
                    double spawnX = context.getBlockPos().getX() + 0.5;
                    double spawnY = context.getBlockPos().offset(context.getSide()).getY();
                    double spawnZ = context.getBlockPos().getZ() + 0.5;

                    entity.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0, 0);

                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnEntityAndPassengers(entity);
                        for (int i = 0; i < 20; i++) {
                            serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.POOF,
                                    spawnX + (serverWorld.random.nextDouble() - 0.5) * entity.getWidth(),
                                    spawnY + serverWorld.random.nextDouble() * entity.getHeight(),
                                    spawnZ + (serverWorld.random.nextDouble() - 0.5) * entity.getWidth(),
                                    1, 0, 0.1, 0, 0.05);
                        }
                    }

                    nbt.remove(ENTITY_DATA_KEY);
                    nbt.remove(ENTITY_TYPE_KEY);
                    nbt.remove(ENTITY_NAME_KEY);

                    if (player != null) {
                        player.getItemCooldownManager().set(this, 40); // 2 seconds cooldown
                        player.sendMessage(
                                Text.literal("Released " + entity.getName().getString()).formatted(Formatting.YELLOW),
                                true);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(ENTITY_DATA_KEY)) {
            String name = nbt.getString(ENTITY_NAME_KEY);
            tooltip.add(Text.literal("Contains: ").formatted(Formatting.GRAY)
                    .append(Text.literal(name).formatted(Formatting.YELLOW)));
        } else {
            tooltip.add(Text.literal("Empty").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text
                    .literal("Use on your tamed companions to capture them.")
                    .formatted(Formatting.BLUE));
            tooltip.add(Text
                    .literal("Sometimes it won't work on specific mobs...")
                    .setStyle(Style.EMPTY.withItalic(true))
                    .formatted(Formatting.GRAY));
        }
    }
}
