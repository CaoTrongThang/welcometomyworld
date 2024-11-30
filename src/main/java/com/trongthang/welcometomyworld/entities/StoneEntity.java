package com.trongthang.welcometomyworld.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class StoneEntity extends LivingEntity {
    protected StoneEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        // No armor for the log
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
//            PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 5);
//            if (nearestPlayer != null) {
//                Vec3d direction = this.getPos().subtract(nearestPlayer.getPos()).normalize();
//                this.setVelocity(direction.multiply(0.5));
//                this.velocityModified = true; // Force velocity update
//            }
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }
}
