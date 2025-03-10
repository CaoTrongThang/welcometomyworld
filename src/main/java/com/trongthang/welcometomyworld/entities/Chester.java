package com.trongthang.welcometomyworld.entities;

import com.trongthang.welcometomyworld.Utilities.Utils;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.AnimationName;
import com.trongthang.welcometomyworld.managers.SoundsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.UntamedActiveTargetGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.A_LIVING_CHEST_EATING_SOUND;
import static com.trongthang.welcometomyworld.WelcomeToMyWorld.A_LIVING_CHEST_EAT_ANIMATION;

public class Chester extends Enderchester implements InventoryOwner, NamedScreenHandlerFactory {

    private final SimpleInventory inventory = new SimpleInventory(54);
    public ParticleEffect particleEffect = ParticleTypes.END_ROD;
    private Item tameFood = Items.BONE;

    public Chester(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    @Override
    public void spawnParticlesAround() {

    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.welcometomyworld.chesterstomach");
    }

    @Override
    public void openMobChest(PlayerEntity player) {
        player.openHandledScreen(this);
    }

    public SimpleInventory getChest(PlayerEntity player) {
        return inventory;
    }

    @Override
    public void eatItemsOnGround() {

        if (this.getWorld().isClient) return;
        if (this.isDead()) return;
        if (this.getIsSleepingData()) return;

        if (this.getServer() == null) {
            return;
        }

        Box checkArea = new Box(this.getBlockPos()).expand(EAT_AREA);
        List<ItemEntity> itemEntities = this.getWorld().getEntitiesByClass(ItemEntity.class, checkArea, entity -> true);

        // Iterate through the found items
        boolean isAdded = false;
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getStack();
            // If the item is not something the mob hates, try to add it to the inventory
            if (!hateItems.contains(stack.getItem())) {
                boolean added = addItemToChest(this.inventory, stack);

                // If the item was successfully added to the chest, remove it from the world
                if (added) {
                    isAdded = true;
                    itemEntity.discard();  // Remove the item from the world
                }
            }
        }
        if (isAdded) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeInt(this.getId());

            startAnimation(AnimationName.EAT_ITEMS, 30);
            if (this.getOwner() == null) {
                for (ServerPlayerEntity p : this.getServer().getPlayerManager().getPlayerList()) {
                    if (p.canSee(this)) {
                        ServerPlayNetworking.send(p, A_LIVING_CHEST_EAT_ANIMATION, buf);
                    }
                }
                Utils.playSound((ServerWorld) this.getWorld(), this.getBlockPos(), SoundsManager.ENDERCHESTER_MUNCH, 0.2f, WelcomeToMyWorld.random.nextFloat(0.8f, 1.2f));
            } else {
                ServerPlayNetworking.send((ServerPlayerEntity) this.getOwner(), A_LIVING_CHEST_EATING_SOUND, PacketByteBufs.empty());
                ServerPlayNetworking.send((ServerPlayerEntity) this.getOwner(), A_LIVING_CHEST_EAT_ANIMATION, buf);
            }

        }
    }

    @Override
    public void initGoals() {
        super.initGoals();
        this.targetSelector.add(5, new UntamedActiveTargetGoal<>(this, LivingEntity.class, false, (t) -> !(t instanceof PlayerEntity) && !(t instanceof Enderchester)));
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);

        this.targetSelector.remove(new UntamedActiveTargetGoal<>(this, LivingEntity.class, false, (t) -> !(t instanceof PlayerEntity) && !(t instanceof Enderchester)));
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

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
        return GenericContainerScreenHandler.createGeneric9x6(syncId, playerInv, inventory);
    }


}
