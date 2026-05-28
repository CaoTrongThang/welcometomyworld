package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.classes.AnimationName;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.A_LIVING_CHEST_EATING_SOUND;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.A_LIVING_CHEST_EAT_ANIMATION;

public class Chester extends Enderchester implements InventoryOwner {

    private final SimpleInventory inventory = new SimpleInventory(54);
    private Item tameFood = Items.BONE;

    public Chester(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public ParticleEffect getParticleEffect() {
        return null;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.welcometomyworld.chesterstomach");
    }

    // Enderchester.openMobChest uses getChest(player) which we override below.

    public SimpleInventory getChest(PlayerEntity player) {
        return inventory;
    }

    @Override
    public void eatItemsOnGround() {

        if (this.getWorld().isClient)
            return;
        if (this.isDead())
            return;
        if (this.getIsSleepingData())
            return;

        if (this.getServer() == null) {
            return;
        }

        Box checkArea = new Box(this.getBlockPos()).expand(EAT_AREA);
        List<ItemEntity> itemEntities = this.getWorld().getEntitiesByClass(ItemEntity.class, checkArea, entity -> true);

        boolean isAdded = false;
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getStack();
            if (!hateItems.contains(stack.getItem()) && isAllowedByFilter(stack)) {
                boolean added = addItemToChest(this.inventory, stack);
                if (added) {
                    isAdded = true;
                    itemEntity.discard();
                }
            }
        }
        if (isAdded) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeInt(this.getId());

            startAnimation(AnimationName.EAT_ITEMS, 30);
            this.eatingTimeout = 30;

            // Sync animation and sound to all nearby players
            for (ServerPlayerEntity p : ((ServerWorld) this.getWorld()).getPlayers()) {
                if (p.canSee(this) || p.squaredDistanceTo(this) < 1024) {
                    ServerPlayNetworking.send(p, A_LIVING_CHEST_EAT_ANIMATION, buf);
                    ServerPlayNetworking.send(p, A_LIVING_CHEST_EATING_SOUND, PacketByteBufs.empty());
                }
            }
        }
    }

    @Override
    public void initGoals() {
        super.initGoals();
        this.targetSelector.add(5, new UntamedActiveTargetGoal<>(this, LivingEntity.class, false,
                (t) -> !(t instanceof PlayerEntity) && !(t instanceof Enderchester)));
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);

        this.targetSelector.remove(new UntamedActiveTargetGoal<>(this, LivingEntity.class, false,
                (t) -> !(t instanceof PlayerEntity) && !(t instanceof Enderchester)));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList items = new NbtList();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound itemTag = new NbtCompound();
                itemTag.putByte("Slot", (byte) i);
                stack.writeNbt(itemTag);
                items.add(itemTag);
            }
        }
        nbt.put("Inventory", items);
    }

    // Load inventory from NBT
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        inventory.clear();
        NbtList items = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < items.size(); i++) {
            NbtCompound itemTag = items.getCompound(i);
            int slot = itemTag.getByte("Slot") & 0xFF;
            if (slot < inventory.size()) {
                inventory.setStack(slot, ItemStack.fromNbt(itemTag));
            }
        }
    }

    // Drop inventory items on death

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                ItemEntity item = new ItemEntity(this.getWorld(), getX(), getY() + 1, getZ(), stack);
                this.getWorld().spawnEntity(item);
            }
        }
    }

    @Override
    public SimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public EntityView method_48926() {
        return this.getWorld();
    }

    @Override
    public Item getTameFood() {
        return this.tameFood;
    }

}
