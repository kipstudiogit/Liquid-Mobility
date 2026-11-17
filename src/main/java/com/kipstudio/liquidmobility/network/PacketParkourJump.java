// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.JumpActionType;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketParkourJump {
    private final JumpActionType actionType;

    public PacketParkourJump(JumpActionType actionType) {
        this.actionType = actionType;
    }

    public PacketParkourJump(PacketBuffer buf) {
        this.actionType = JumpActionType.fromId(buf.readByte());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(actionType.getId());
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

            switch (actionType) {
                case DOUBLE_JUMP:
                    if (MobilityConfig.COMMON.enableDoubleJump.get() && !cap.hasDoubleJumped()) {
                        cap.setDoubleJumped(true);
                        cap.setFastDropping(false);
                        player.fallDistance = 0.0F;
                        MobilityNetwork.CHANNEL.send(
                                PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                new PacketSyncMobility(player.getId(), cap)
                        );
                    }
                    break;
                case COYOTE_JUMP:
                    if (!player.isOnGround() && player.fallDistance < 2.5F) {
                        cap.setFastDropping(false);
                        player.fallDistance = 0.0F;
                    }
                    break;
                case WALL_JUMP:
                    if (MobilityConfig.COMMON.enableWallRun.get() && !player.isOnGround()) {
                        cap.setWallRunTicks(0);
                        cap.setDoubleJumped(false);
                        cap.setFastDropping(false);
                        player.fallDistance = 0.0F;
                        MobilityNetwork.CHANNEL.send(
                                PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                new PacketSyncMobility(player.getId(), cap)
                        );
                    }
                    break;
                case SLIDE_JUMP:
                    if (MobilityConfig.COMMON.enableSlide.get() && cap.getSlideTicks() > 0) {
                        cap.setSlideTicks(0);
                        player.fallDistance = 0.0F;
                        MobilityNetwork.CHANNEL.send(
                                PacketDistributor.TRACKING_ENTITY.with(() -> player),
                                new PacketSyncMobility(player.getId(), cap)
                        );
                    }
                    break;
                case VAULT:
                    if (MobilityConfig.COMMON.enableVault.get()) {
                        cap.setWallRunTicks(0);
                        player.fallDistance = 0.0F;
                    }
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}