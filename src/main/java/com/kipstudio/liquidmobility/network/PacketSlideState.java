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

public class PacketSlideState {
    private final boolean sliding;

    public PacketSlideState(boolean sliding) {
        this.sliding = sliding;
    }

    public PacketSlideState(PacketBuffer buf) {
        this.sliding = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(sliding);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.isAlive() || CommonMovementHandler.isMobilityBlocked(player)) {
                return;
            }

            IMobilityData cap = MobilityCapability.get(player);
            if (cap == null) {
                return;
            }

            if (sliding && MobilityConfig.COMMON.enableSlide.get() && player.isOnGround()) {
                cap.setSlideTicks(MobilityConfig.COMMON.slideDurationTicks.get());
            } else {
                cap.setSlideTicks(0);
            }

            MobilityNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> player),
                    new PacketSyncMobility(player.getId(), cap)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}