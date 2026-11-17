// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.ClientMobilitySync;
import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncMobility {
    private final int entityId;
    private final boolean doubleJumped;
    private final int slideTicks;
    private final int dashCooldown;
    private final int dashTicks;
    private final int wallRunTicks;
    private final WallDirection wallDirection;
    private final boolean fastDropping;

    public PacketSyncMobility(int entityId, IMobilityData data) {
        this.entityId = entityId;
        this.doubleJumped = data.hasDoubleJumped();
        this.slideTicks = data.getSlideTicks();
        this.dashCooldown = data.getDashCooldown();
        this.dashTicks = data.getDashTicks();
        this.wallRunTicks = data.getWallRunTicks();
        this.wallDirection = data.getWallDirection();
        this.fastDropping = data.isFastDropping();
    }

    public PacketSyncMobility(PacketBuffer buf) {
        this.entityId = buf.readVarInt();
        this.doubleJumped = buf.readBoolean();
        this.slideTicks = buf.readVarInt();
        this.dashCooldown = buf.readVarInt();
        this.dashTicks = buf.readVarInt();
        this.wallRunTicks = buf.readVarInt();
        this.wallDirection = WallDirection.fromId(buf.readByte());
        this.fastDropping = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(doubleJumped);
        buf.writeVarInt(slideTicks);
        buf.writeVarInt(dashCooldown);
        buf.writeVarInt(dashTicks);
        buf.writeVarInt(wallRunTicks);
        buf.writeByte(wallDirection.getId());
        buf.writeBoolean(fastDropping);
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isDoubleJumped() {
        return doubleJumped;
    }

    public int getSlideTicks() {
        return slideTicks;
    }

    public int getDashCooldown() {
        return dashCooldown;
    }

    public int getDashTicks() {
        return dashTicks;
    }

    public int getWallRunTicks() {
        return wallRunTicks;
    }

    public WallDirection getWallDirection() {
        return wallDirection;
    }

    public boolean isFastDropping() {
        return fastDropping;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMobilitySync.apply(this))
        );
        ctx.get().setPacketHandled(true);
    }
}