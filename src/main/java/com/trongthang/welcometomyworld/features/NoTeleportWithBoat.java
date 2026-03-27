package com.trongthang.welcometomyworld.features;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity; // Import thêm thư viện này
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class NoTeleportWithBoat {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (player.isSpectator()) return ActionResult.PASS;

            // BƯỚC 1: Kiểm tra xem người chơi có đang ngồi trên phương tiện không?
            if (player.hasVehicle()) {

                // Lấy đối tượng chiếc thuyền/xe mỏ mà người chơi đang ngồi
                Entity vehicle = player.getVehicle();

                // BƯỚC 2: Kiểm tra xem chiếc thuyền này có chở NHIỀU HƠN 1 đối tượng không?
                // (Bản thân người chơi tính là 1. Nếu lớn hơn 1 tức là có chở thêm Dân làng/Mob/Người khác)
                if (vehicle != null && vehicle.getPassengerList().size() > 1) {

                    // Lấy thông tin block đang bị click
                    BlockState state = world.getBlockState(hitResult.getBlockPos());
                    Identifier blockId = Registries.BLOCK.getId(state.getBlock());

                    // Nếu click vào Waystone
                    if (blockId.getNamespace().equals("waystones")) {


                        // Hủy thao tác
                        return ActionResult.FAIL;
                    }
                }
            }

            // Nếu người chơi ngồi thuyền 1 mình (size == 1) hoặc không ngồi thuyền, mọi thứ diễn ra bình thường
            return ActionResult.PASS;
        });
    }
}