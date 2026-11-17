// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketFastDrop {
    private final boolean active;

    public PacketFastDrop(boolean active) {
        this.active = active;
    }

    public PacketFastDrop(PacketBuffer buf) {
        this.active = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.isAlive() || CommonMovementHandler.isMobilityBlocked(player)) {
                return;
            }
            if (!MobilityConfig.COMMON.enableFastDrop.get()) {
                return;
            }

            IMobilityData cap = MobilityCapability.get(player);
            if (cap != null) {
                boolean allow = active && !player.isOnGround();
                cap.setFastDropping(allow);

                MobilityNetwork.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> player),
                        new PacketSyncMobility(player.getId(), cap)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}