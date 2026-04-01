package com.trongthang.welcometomyworld.mixin;

import com.trongthang.welcometomyworld.features.MobsGearsUp;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static com.trongthang.welcometomyworld.WelcomeToMyWorld.random;
import static com.trongthang.welcometomyworld.features.MobsGearsUp.DEFAULT_EQUIP_CHANCE;

@Mixin(MobEntity.class)
public abstract class MobEntityGearsUp {

    @Inject(method = "initialize", at = @At("HEAD"))
    private void onInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            @Nullable EntityData entityData, @Nullable NbtCompound entityNbt,
            CallbackInfoReturnable<EntityData> cir) {
        MobEntity mob = (MobEntity) (Object) this;
        Identifier mobId = EntityType.getId(mob.getType());

        MobsGearsUp.MobSettings settings = MobsGearsUp.getSettings(mobId);
        if (settings != null) {
            equipArmor(mob, settings);
            equipMainHand(mob, settings);
            equipOffhand(mob, settings);
        }
    }

    private void equipArmor(LivingEntity mob, MobsGearsUp.MobSettings settings) {
        if (settings.helmet && !MobsGearsUp.HELMETS.isEmpty() && mob.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            equipItem(mob, EquipmentSlot.HEAD, MobsGearsUp.HELMETS, settings);
        }
        if (settings.chestplate && !MobsGearsUp.CHESTPLATES.isEmpty()
                && mob.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
            equipItem(mob, EquipmentSlot.CHEST, MobsGearsUp.CHESTPLATES, settings);
        }
        if (settings.leggings && !MobsGearsUp.LEGGINGS.isEmpty()
                && mob.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) {
            equipItem(mob, EquipmentSlot.LEGS, MobsGearsUp.LEGGINGS, settings);
        }
        if (settings.boots && !MobsGearsUp.BOOTS.isEmpty() && mob.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
            equipItem(mob, EquipmentSlot.FEET, MobsGearsUp.BOOTS, settings);
        }
    }

    private void equipMainHand(LivingEntity mob, MobsGearsUp.MobSettings settings) {
        if (mob.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) { // Check if empty
            if (!checkChance(mob, 550.0f, 120.0f, 0.90f))
                return;

            List<Item> mainHandOptions = new ArrayList<>();

            if (settings.melee)
                mainHandOptions.addAll(MobsGearsUp.MELEE_WEAPONS);
            if (settings.range)
                mainHandOptions.addAll(MobsGearsUp.RANGE_WEAPONS);

            if (!mainHandOptions.isEmpty()) {
                Item item = mainHandOptions.get(random.nextInt(mainHandOptions.size()));
                ItemStack stack = new ItemStack(item);
                applyEnchantments(mob, stack, settings);
                mob.equipStack(EquipmentSlot.MAINHAND, stack);
            }
        }
    }

    private void equipOffhand(LivingEntity mob, MobsGearsUp.MobSettings settings) {
        if (mob.getEquippedStack(EquipmentSlot.OFFHAND).isEmpty()) { // Check if empty
            if (!checkChance(mob, 1320.0f, 285.0f, 0.40f))
                return;
            if (settings.offhand && !MobsGearsUp.OFF_HANDS.isEmpty()) {
                Item item = MobsGearsUp.OFF_HANDS.get(random.nextInt(MobsGearsUp.OFF_HANDS.size()));
                ItemStack stack = new ItemStack(item);
                applyEnchantments(mob, stack, settings);
                mob.equipStack(EquipmentSlot.OFFHAND, stack);
            }
        }
    }

    private void equipItem(LivingEntity mob, EquipmentSlot slot, List<Item> items, MobsGearsUp.MobSettings settings) {
        if ((slot.isArmorSlot() && mob.getArmorItems() != null)) {
            if (!checkChance(mob, 550.0f, 120.0f, 0.90f))
                return;

            Item item = items.get(random.nextInt(items.size()));
            ItemStack stack = new ItemStack(item);

            applyEnchantments(mob, stack, settings);

            mob.equipStack(slot, stack);
        }
    }

    private void applyEnchantments(LivingEntity mob, ItemStack stack, MobsGearsUp.MobSettings settings) {
        if (!settings.enchantment)
            return;
        if (!checkChance(mob, 615.0f, 133.0f, 0.80f))
            return;

        MobsGearsUp.EnchantmentCategory category = getItemCategory(stack.getItem());
        if (category == null)
            return;

        List<Identifier> enchantmentIds = MobsGearsUp.CATEGORIZED_ENCHANTMENTS.getOrDefault(category,
                Collections.emptyList());
        List<Enchantment> applicable = new ArrayList<>();

        for (Identifier id : enchantmentIds) {
            Enchantment enchantment = Registries.ENCHANTMENT.get(id);
            if (enchantment != null && enchantment.isAcceptableItem(stack)) {
                applicable.add(enchantment);
            }
        }

        if (!applicable.isEmpty()) {
            int rand = random.nextInt(applicable.size());
            Enchantment chosen = applicable.get(rand);
            applicable.remove(rand);

            int level = random.nextInt(chosen.getMaxLevel()) + 1;
            EnchantmentHelper.set(Map.of(chosen, level), stack);
        }

        int counter = 0;
        while (checkChance(mob, 2000.0f, 450.0f, 0.20f) && counter <= 5) {
            if (applicable.isEmpty())
                break;

            int rand = random.nextInt(applicable.size());
            Enchantment chosen = applicable.get(rand);
            applicable.remove(rand);

            int level = random.nextInt(chosen.getMaxLevel()) + 1;
            EnchantmentHelper.set(Map.of(chosen, level), stack);

            counter++;
        }
    }

    private MobsGearsUp.EnchantmentCategory getItemCategory(Item item) {
        if (MobsGearsUp.HELMETS.contains(item) ||
                MobsGearsUp.CHESTPLATES.contains(item) ||
                MobsGearsUp.LEGGINGS.contains(item) ||
                MobsGearsUp.BOOTS.contains(item)) {
            return MobsGearsUp.EnchantmentCategory.ARMOR;
        } else if (MobsGearsUp.MELEE_WEAPONS.contains(item)) {
            return MobsGearsUp.EnchantmentCategory.WEAPON;
        } else if (MobsGearsUp.RANGE_WEAPONS.contains(item)) {
            if (item instanceof BowItem) {
                return MobsGearsUp.EnchantmentCategory.BOW;
            } else if (item instanceof CrossbowItem) {
                return MobsGearsUp.EnchantmentCategory.CROSSBOW;
            }
        } else if (MobsGearsUp.OFF_HANDS.contains(item) && item instanceof ShieldItem) {
            return MobsGearsUp.EnchantmentCategory.SHIELD;
        }
        return null;
    }

    private float getImprovedMobsDifficulty(LivingEntity mob) {
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("improvedmobs")) {
            return io.github.flemmli97.improvedmobs.difficulty.DifficultyData.getDifficulty(mob.getWorld(), mob);
        }
        return -1;
    }

    private boolean checkChance(LivingEntity mob, float diffDiv, float dayDiv, float maxChance) {
        float difficulty = getImprovedMobsDifficulty(mob);
        float chance;

        if (difficulty >= 0) {
            chance = Math.min(DEFAULT_EQUIP_CHANCE + (difficulty / diffDiv), maxChance);
        } else {
            long currentTime = mob.getWorld().getTimeOfDay();
            int currentDay = (int) (currentTime / 24000);
            chance = Math.min(DEFAULT_EQUIP_CHANCE + ((float) currentDay / dayDiv), maxChance);
        }

        return random.nextFloat() <= chance;
    }
}
