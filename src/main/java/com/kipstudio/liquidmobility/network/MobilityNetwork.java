// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.LiquidMobility;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class MobilityNetwork {
    private static final String PROTOCOL_VERSION = "2";
    public static SimpleChannel CHANNEL;
    private static int packetId = 0;

    @SuppressWarnings("null")
    public static void registerPackets() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(LiquidMobility.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(packetId++, PacketParkourJump.class,
                PacketParkourJump::toBytes, PacketParkourJump::new, PacketParkourJump::handle);
        CHANNEL.registerMessage(packetId++, PacketDash.class,
                PacketDash::toBytes, PacketDash::new, PacketDash::handle);
        CHANNEL.registerMessage(packetId++, PacketSlideState.class,
                PacketSlideState::toBytes, PacketSlideState::new, PacketSlideState::handle);
        CHANNEL.registerMessage(packetId++, PacketSlideKick.class,
                PacketSlideKick::toBytes, PacketSlideKick::new, PacketSlideKick::handle);
        CHANNEL.registerMessage(packetId++, PacketFastDrop.class,
                PacketFastDrop::toBytes, PacketFastDrop::new, PacketFastDrop::handle);
        CHANNEL.registerMessage(packetId++, PacketGroundPound.class,
                PacketGroundPound::toBytes, PacketGroundPound::new, PacketGroundPound::handle);
        CHANNEL.registerMessage(packetId++, PacketWallState.class,
                PacketWallState::toBytes, PacketWallState::new, PacketWallState::handle);
        CHANNEL.registerMessage(packetId++, PacketSyncMobility.class,
                PacketSyncMobility::toBytes, PacketSyncMobility::new, PacketSyncMobility::handle);
    }
}