// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.network.PacketSyncMobility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ClientMobilitySync {
    public static void apply(PacketSyncMobility packet) {
        ClientWorld level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Entity entity = level.getEntity(packet.getEntityId());
        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        IMobilityData cap = MobilityCapability.get((PlayerEntity) entity);
        if (cap != null) {
            cap.setDoubleJumped(packet.isDoubleJumped());
            cap.setSlideTicks(packet.getSlideTicks());
            cap.setDashCooldown(packet.getDashCooldown());
            cap.setDashTicks(packet.getDashTicks());
            cap.setWallRunTicks(packet.getWallRunTicks());
            cap.setWallDirection(packet.getWallDirection());
            cap.setFastDropping(packet.isFastDropping());
        }
    }
}