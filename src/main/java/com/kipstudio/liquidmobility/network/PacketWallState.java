// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketWallState {
    private final WallDirection direction;
    private final int wallRunTicks;
    private final boolean clinging;

    public PacketWallState(WallDirection direction, int wallRunTicks, boolean clinging) {
        this.direction = direction;
        this.wallRunTicks = wallRunTicks;
        this.clinging = clinging;
    }

    public PacketWallState(PacketBuffer buf) {
        this.direction = WallDirection.fromId(buf.readByte());
        this.wallRunTicks = buf.readVarInt();
        this.clinging = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(direction.getId());
        buf.writeVarInt(wallRunTicks);
        buf.writeBoolean(clinging);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.isAlive() || CommonMovementHandler.isMobilityBlocked(player)) {
                return;
            }
            if (!MobilityConfig.COMMON.enableWallRun.get()) {
                return;
            }

            IMobilityData cap = MobilityCapability.get(player);
            if (cap == null) {
                return;
            }

            int maxDuration = Math.max(
                    MobilityConfig.COMMON.wallRunHorizontalDurationTicks.get(),
                    MobilityConfig.COMMON.wallClimbDurationTicks.get()
            );
            int clampedTicks = Math.max(0, Math.min(wallRunTicks, maxDuration));

            cap.setWallDirection(direction);
            cap.setWallRunTicks(clampedTicks);

            if (direction != WallDirection.NONE && (clampedTicks > 0 || clinging) && !player.isOnGround()) {
                player.fallDistance = 0.0F;
                cap.setFastDropping(false);
            }

            MobilityNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> player),
                    new PacketSyncMobility(player.getId(), cap)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}